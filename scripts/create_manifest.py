import hashlib
import json
import os
import sys

version = sys.argv[1]
apk_name = sys.argv[2]
patch_name = sys.argv[3] if len(sys.argv) > 3 else None
repo = sys.argv[4] if len(sys.argv) > 4 else "unknown/repo"

def sha256(path):
    h = hashlib.sha256()
    if not os.path.exists(path):
        return None
    with open(path, 'rb') as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

patch_exists = patch_name is not None and patch_name != "none" and os.path.exists(patch_name)

# Strip out any non-numeric characters for the version code (e.g. 0.0.1-test -> 001 -> 1)
numeric_version = ''.join(filter(str.isdigit, version))
version_code = int(numeric_version) if numeric_version else 1

# Our RemoteData structure
manifest = {
    'code': version_code,
    'version': version,
    'created_at': "2026-08-08T00:00:00Z", # Placeholder timestamp
    'files': [
        {
            'file_name': f"Modula Mobile v{version}",
            'uri': f'https://github.com/{repo}/releases/download/v{version}/{apk_name}',
            'arch': "all",
            'size': os.path.getsize(apk_name) if os.path.exists(apk_name) else 0,
            'apk_sha256': sha256(apk_name)
        }
    ],
    'default_body': {
        'language': 'en',
        'markdown': f'# Modula Mobile v{version}\n\n* Reduced launcher startup time\n* Improved Mesa renderer compatibility\n* Added dynamic RAM allocation\n* Enabled bsdiff delta updates'
    },
    'bodies': []
}

if patch_exists:
    manifest['files'][0]['patch_uri'] = f'https://github.com/{repo}/releases/download/v{version}/{patch_name}'
    manifest['files'][0]['patch_size'] = os.path.getsize(patch_name)
    manifest['files'][0]['patch_sha256'] = sha256(patch_name)

    # Assuming we patched from the previous version, we need a way to pass the previous version code
    # For now we'll just parse the old version from the patch name (launcher-vOLD_to_vNEW.patch)
    try:
        old_v = patch_name.split('_to_')[0].replace('launcher-v', '')
        old_numeric = ''.join(filter(str.isdigit, old_v))
        manifest['files'][0]['patch_for_version_code'] = int(old_numeric) if old_numeric else 1
    except:
        pass

with open('release.json', 'w') as f:
    json.dump(manifest, f, indent=2)

print('release.json generated')
