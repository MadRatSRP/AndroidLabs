package com.androidlabs.util

import android.util.Log
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showLogMessage(textMessage: String) {
    Log.d(this.javaClass.simpleName, textMessage)
}
fun Fragment.showLogMessage(@StringRes textMessageId: Int) {
    Log.d(this.javaClass.simpleName, getString(textMessageId))
}
fun Fragment.showSnackMessage(@StringRes messageId: Int) {
    this.view?.let {
        Snackbar.make(it, messageId, Snackbar.LENGTH_LONG).show()
    }
}
