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
        old_parts = old_v.split('.')
        if len(old_parts) >= 3:
            manifest['files'][0]['patch_for_version_code'] = int(old_parts[-1])
        else:
            old_numeric = ''.join(filter(str.isdigit, old_v))
            manifest['files'][0]['patch_for_version_code'] = int(old_numeric) if old_numeric else 1
    except:
        pass

import base64

json_string = json.dumps(manifest)
base64_content = base64.b64encode(json_string.encode('utf-8')).decode('utf-8')

# The broken v1.0.5 client expects a GithubContentApi response when downloading release.json directly
github_api_mock = {
    "_links": {
        "git": "",
        "html": "",
        "self": ""
    },
    "content": base64_content,
    "download_url": "",
    "encoding": "base64",
    "git_url": "",
    "html_url": "",
    "name": "release.json",
    "path": "release.json",
    "sha": "",
    "size": len(base64_content),
    "type": "file",
    "url": ""
}

with open('release.json', 'w') as f:
    json.dump(github_api_mock, f, indent=2)

print('release.json generated')
