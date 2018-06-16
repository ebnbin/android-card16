package com.ebnbin.card16.util

import android.util.Log

/**
 * 日志 tag.
 */
private const val LOG_TAG = "ebnbin"

/**
 * 输出日志.
 */
fun log(any: Any?) {
    if (any is Throwable) {
        Log.e(LOG_TAG, "", any)
    } else {
        Log.d(LOG_TAG, any.toString())
    }
}
