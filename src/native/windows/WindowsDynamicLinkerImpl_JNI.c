/* !---- DO NOT EDIT: This file autogenerated by com\sun\gluegen\JavaEmitter.java on Tue May 27 02:37:55 PDT 2008 ----! */

#include <jni.h>
#include <stdlib.h>

#include <assert.h>

 #include <windows.h>
 /* This typedef is apparently needed for compilers before VC8,
    and for the embedded ARM compilers we're using */
 #if (_MSC_VER < 1400) || defined(UNDER_CE)
 typedef int intptr_t;
 #endif
 /* GetProcAddress doesn't exist in A/W variants under desktop Windows */
 #ifndef UNDER_CE
 #define GetProcAddressA GetProcAddress
 #endif

/*   Java->C glue code:
 *   Java package: com.jogamp.common.os.WindowsDynamicLinkerImpl
 *    Java method: int FreeLibrary(long hLibModule)
 *     C function: BOOL FreeLibrary(HANDLE hLibModule);
 */
JNIEXPORT jint JNICALL 
Java_com_jogamp_common_os_WindowsDynamicLinkerImpl_FreeLibrary__J(JNIEnv *env, jclass _unused, jlong hLibModule) {
  BOOL _res;
  _res = FreeLibrary((HANDLE) (intptr_t) hLibModule);
  return _res;
}


/*   Java->C glue code:
 *   Java package: com.jogamp.common.os.WindowsDynamicLinkerImpl
 *    Java method: int GetLastError()
 *     C function: DWORD GetLastError(void);
 */
JNIEXPORT jint JNICALL 
Java_com_jogamp_common_os_WindowsDynamicLinkerImpl_GetLastError__(JNIEnv *env, jclass _unused) {
  DWORD _res;
  _res = GetLastError();
  return _res;
}


/*   Java->C glue code:
 *   Java package: com.jogamp.common.os.WindowsDynamicLinkerImpl
 *    Java method: long GetProcAddressA(long hModule, java.lang.String lpProcName)
 *     C function: PROC GetProcAddressA(HANDLE hModule, LPCSTR lpProcName);
 */
JNIEXPORT jlong JNICALL 
Java_com_jogamp_common_os_WindowsDynamicLinkerImpl_GetProcAddressA__JLjava_lang_String_2(JNIEnv *env, jclass _unused, jlong hModule, jstring lpProcName) {
  const char* _strchars_lpProcName = NULL;
  PROC _res;
  if (lpProcName != NULL) {
    _strchars_lpProcName = (*env)->GetStringUTFChars(env, lpProcName, (jboolean*)NULL);
    if (_strchars_lpProcName == NULL) {
      (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"),
                       "Failed to get UTF-8 chars for argument \"lpProcName\" in native dispatcher for \"GetProcAddressA\"");
      return 0;
    }
  }
  _res = GetProcAddressA((HANDLE) (intptr_t) hModule, (LPCSTR) _strchars_lpProcName);
  if (lpProcName != NULL) {
    (*env)->ReleaseStringUTFChars(env, lpProcName, _strchars_lpProcName);
  }
  return (jlong) (intptr_t) _res;
}


/*   Java->C glue code:
 *   Java package: com.jogamp.common.os.WindowsDynamicLinkerImpl
 *    Java method: long LoadLibraryW(java.lang.String lpLibFileName)
 *     C function: HANDLE LoadLibraryW(LPCWSTR lpLibFileName);
 */
JNIEXPORT jlong JNICALL 
Java_com_jogamp_common_os_WindowsDynamicLinkerImpl_LoadLibraryW__Ljava_lang_String_2(JNIEnv *env, jclass _unused, jstring lpLibFileName) {
  jchar* _strchars_lpLibFileName = NULL;
  HANDLE _res;
  if (lpLibFileName != NULL) {
    _strchars_lpLibFileName = (jchar *) calloc((*env)->GetStringLength(env, lpLibFileName) + 1, sizeof(jchar));
    if (_strchars_lpLibFileName == NULL) {
      (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"),
                       "Could not allocate temporary buffer for copying string argument \"lpLibFileName\" in native dispatcher for \"LoadLibraryW\"");
      return 0;
    }
    (*env)->GetStringRegion(env, lpLibFileName, 0, (*env)->GetStringLength(env, lpLibFileName), _strchars_lpLibFileName);
  }
  _res = LoadLibraryW((LPCWSTR) _strchars_lpLibFileName);
  if (lpLibFileName != NULL) {
    free((void*) _strchars_lpLibFileName);
  }
  return (jlong) (intptr_t) _res;
}


