#include "com_montycall_android_lebanoncall_MontyNative.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

JNIEXPORT jstring JNICALL Java_com_montycall_android_lebanoncall_MontyNative_getPrivateKey (JNIEnv *env, jobject obj) {
	return (*env)->NewStringUTF(env, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvxNGbAilFmrOnPt3d3t+wuJaz33U5G26DZvaMIWHqAmqHpKRw4xP0hf6dpFsdKZR+zQDiwkp4iuYMCc+G6fy8QekFAbR7gBHgECUobsTQU9X8xrQus/bZzkDXCOShuhBDJXtCG7pw155ZML5Z3IBDmbjFSvg8Gp61VON+o32ByhqQMGvy4twxIV06U6t+n7mN/0d14rPrn3l7No5ELBsnAcUpw01EGyWMtVzL7t04u25lojjEuG9h6AtDtFHgpD7YKax7djkIeqrOeTcHjnAk8az6Xr9c2Lh0v/SYQ1487GRhbuA7k+j6RlBuBnsSD41Dt5lyJ1W0hZttksSvQaVzwIDAQAB");
}


JNIEXPORT jstring JNICALL Java_com_montycall_android_lebanoncall_MontyNative_getTokenString (JNIEnv *env, jobject obj, jstring str) {
	return (*env)->NewStringUTF(env, "09123elkjasdf09nladsf209@ki0546109asv09asdfklj0bji0ggi");
}
