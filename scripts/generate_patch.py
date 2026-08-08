import sys
import bsdiff4

old_file = sys.argv[1]
new_file = sys.argv[2]
patch_file = sys.argv[3]

with open(old_file, 'rb') as oldf, \
     open(new_file, 'rb') as newf, \
     open(patch_file, 'wb') as patchf:
    bsdiff4.file_diff(oldf, newf, patchf)

print(f'Patch created: {patch_file}')
