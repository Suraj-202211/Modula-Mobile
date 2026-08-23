import json

try:
    with open('release.json', 'r') as f:
        data = json.load(f)
    print("Parsed JSON successfully.")
    
    import base64
    content = base64.b64decode(data['content']).decode('utf-8')
    print("Decoded content:")
    print(content[:100])
    
    remote = json.loads(content)
    print("Parsed RemoteData successfully!")
    print(f"Code: {remote.get('code')}")
    
except Exception as e:
    print(f"Error: {e}")
