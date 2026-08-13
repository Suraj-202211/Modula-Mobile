
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
