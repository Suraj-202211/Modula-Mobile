import sys
import bsdiff4

old_file = sys.argv[1]
new_file = sys.argv[2]
patch_file = sys.argv[3]

bsdiff4.file_diff(old_file, new_file, patch_file)

print(f'Patch created: {patch_file}')
