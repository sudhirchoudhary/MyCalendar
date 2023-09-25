package com.example.mycalendar.utils

import android.util.Log

fun Any.logd(msg: String) {
    Log.d("RequestX", "${this.javaClass.simpleName}: $msg")
}