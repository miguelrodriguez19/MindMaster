package com.miguelrodriguez19.mindmaster.model.structures.exceptions

import android.util.Log

object ExceptionHolder {

    private const val TAG = "ExceptionHolder"

    fun illegalArgument(message: String, cause: Throwable? = null): Nothing {
        Log.e(TAG, "illegalArgument: $message")
        throw IllegalArgumentException(message, cause)
    }

    fun illegalState(message: String, cause: Throwable? = null): Nothing {
        Log.e(TAG, "illegalArgument: $message")
        throw IllegalStateException(message, cause)
    }
}