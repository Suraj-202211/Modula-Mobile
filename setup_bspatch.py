import urllib.request
import os
import zipfile
import shutil

jni_dir = "D:/modula-mobile/native-launcher/ZalithLauncher/src/main/jni/bspatch"
os.makedirs(jni_dir, exist_ok=True)

print("Downloading bzip2 source...")
url = "https://sourceware.org/pub/bzip2/bzip2-1.0.8.tar.gz"
urllib.request.urlretrieve(url, "bzip2.tar.gz")
os.system("tar -xzf bzip2.tar.gz")

print("Copying bzip2 files to jni...")
bzip2_files = ["blocksort.c", "huffman.c", "crctable.c", "randtable.c", "compress.c", "decompress.c", "bzlib.c", "bzlib.h", "bzlib_private.h"]
for f in bzip2_files:
    shutil.copy(os.path.join("bzip2-1.0.8", f), jni_dir)

print("Downloading bspatch.c...")
urllib.request.urlretrieve("https://raw.githubusercontent.com/mendsley/bsdiff/master/bspatch.c", os.path.join(jni_dir, "bspatch.c"))

print("Generating BsPatch JNI bridge...")
jni_bridge = """
#include <jni.h>
#include <stdlib.h>
#include <string.h>

// Forward declaration of bspatch main
int bspatch_main(int argc, char * argv[]);

JNIEXPORT jint JNICALL
Java_com_movtery_zalithlauncher_upgrade_BsPatch_applyPatch(JNIEnv *env, jclass clazz, jstring old_apk, jstring new_apk, jstring patch) {
    const char *oldApkPath = (*env)->GetStringUTFChars(env, old_apk, 0);
    const char *newApkPath = (*env)->GetStringUTFChars(env, new_apk, 0);
    const char *patchPath = (*env)->GetStringUTFChars(env, patch, 0);

    char *argv[4];
    argv[0] = "bspatch";
    argv[1] = (char*) oldApkPath;
    argv[2] = (char*) newApkPath;
    argv[3] = (char*) patchPath;

    int res = bspatch_main(4, argv);

    (*env)->ReleaseStringUTFChars(env, old_apk, oldApkPath);
    (*env)->ReleaseStringUTFChars(env, new_apk, newApkPath);
    (*env)->ReleaseStringUTFChars(env, patch, patchPath);

    return res;
}
"""
with open(os.path.join(jni_dir, "bspatch_jni.c"), "w") as f:
    f.write(jni_bridge)

print("Modifying bspatch.c to rename main...")
with open(os.path.join(jni_dir, "bspatch.c"), "r") as f:
    content = f.read()
content = content.replace("int main(", "int bspatch_main(")
# Remove errx and err which are not in android ndk properly
content = content.replace("errx(1", "return 1; //errx(1")
content = content.replace("err(1", "return 1; //err(1")
with open(os.path.join(jni_dir, "bspatch.c"), "w") as f:
    f.write(content)

print("Done!")
