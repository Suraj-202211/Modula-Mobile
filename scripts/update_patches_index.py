import argparse
import hashlib
import json
import os
import sys

def sha256(path):
    h = hashlib.sha256()
    if not path or not os.path.exists(path):
        return None
    with open(path, 'rb') as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def main():
    parser = argparse.ArgumentParser(description="Update authoritative patches.json index")
    parser.add_argument('--index', required=True, help='Path to patches.json')
    parser.add_argument('--from-code', required=True, type=int, help='Source version code')
    parser.add_argument('--to-code', required=True, type=int, help='Target version code')
    parser.add_argument('--url', required=True, help='Stable URL to the patch asset')
    parser.add_argument('--patch-file', required=True, help='Path to the patch file')
    parser.add_argument('--source-apk', required=True, help='Path to the original source APK')
    parser.add_argument('--target-apk', required=True, help='Path to the new target APK')
    args = parser.parse_args()

    # 1. Validate files exist
    for f in [args.patch_file, args.source_apk, args.target_apk]:
        if not os.path.exists(f):
            print(f"ERROR: File not found: {f}", file=sys.stderr)
            sys.exit(1)
            
    # 2. Calculate SHAs and sizes
    patch_size = os.path.getsize(args.patch_file)
    patch_sha = sha256(args.patch_file)
    source_sha = sha256(args.source_apk)
    target_sha = sha256(args.target_apk)
    
    if not patch_sha or not source_sha or not target_sha:
        print("ERROR: Failed to calculate SHA256 checksums", file=sys.stderr)
        sys.exit(1)
        
    print(f"Calculated Patch SHA256: {patch_sha}")
    print(f"Calculated Source APK SHA256: {source_sha}")
    print(f"Calculated Target APK SHA256: {target_sha}")
    
    # 3. Load or init index
    index_path = args.index
    data = {"schemaVersion": 1, "patches": []}
    if os.path.exists(index_path):
        try:
            with open(index_path, 'r', encoding='utf-8') as f:
                loaded = json.load(f)
                if "patches" in loaded:
                    data = loaded
        except Exception as e:
            print(f"WARNING: Failed to parse existing {index_path} ({e}), initializing new index.", file=sys.stderr)
            
    # 4. Construct edge
    new_edge = {
        "fromVersionCode": args.from_code,
        "toVersionCode": args.to_code,
        "patchUrl": args.url,
        "patchSizeBytes": patch_size,
        "patchSha256": patch_sha,
        "sourceSha256": source_sha,
        "targetSha256": target_sha
    }
    
    # 5. Add or Update
    patches = data.get("patches", [])
    updated = False
    for i, patch in enumerate(patches):
        if patch.get("fromVersionCode") == args.from_code and patch.get("toVersionCode") == args.to_code:
            patches[i] = new_edge
            updated = True
            print(f"Updated existing edge {args.from_code} -> {args.to_code}")
            break
            
    if not updated:
        patches.append(new_edge)
        print(f"Added new edge {args.from_code} -> {args.to_code}")
        
    # 6. Sort deterministically (by target ascending, then source ascending)
    # This helps order the file nicely but Dijkstra handles it anyway.
    data["patches"] = sorted(patches, key=lambda x: (x.get("toVersionCode", 0), x.get("fromVersionCode", 0)))
    
    # 7. Write atomically
    tmp_path = index_path + ".tmp"
    with open(tmp_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        
    os.replace(tmp_path, index_path)
    print(f"Successfully wrote {len(data['patches'])} patches to {index_path}")

if __name__ == '__main__':
    main()
