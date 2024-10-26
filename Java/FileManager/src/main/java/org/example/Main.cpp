#include "org_example_Main.h"
#include <windows.h>

JNIEXPORT jstring JNICALL Java_org_example_Main_getVolumeTypeByName(JNIEnv* env, jclass jc, jstring js) {
    //https://learn.microsoft.com/ru-ru/windows/win32/api/fileapi/nf-fileapi-getvolumeinformationa?redirectedfrom=MSDN
    char fileSystemNameBuffer[MAX_PATH];
    DWORD maxComponentLength, fileSystemFlags;

    if (GetVolumeInformationA(
        env->GetStringUTFChars(js, 0),
        nullptr,
        0,
        nullptr,
        &maxComponentLength,
        &fileSystemFlags,
        fileSystemNameBuffer,
        sizeof(fileSystemNameBuffer))
    ) {
        return env->NewStringUTF(fileSystemNameBuffer);
    } else {
        return env->NewStringUTF("Не удалось определить");
    }
}