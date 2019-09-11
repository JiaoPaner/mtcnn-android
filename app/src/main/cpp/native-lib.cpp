#include <jni.h>
#include <string>
using namespace std;
extern "C" JNIEXPORT jstring JNICALL
Java_com_wisesoft_mtcnn_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
