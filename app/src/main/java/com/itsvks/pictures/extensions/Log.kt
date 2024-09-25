package com.itsvks.pictures.extensions

import android.util.Log
import com.itsvks.pictures.BuildConfig


fun printDebug(message: Any) {
  printDebug(message.toString())
}

fun printDebug(message: String) {
  if (BuildConfig.DEBUG) {
    Log.d("GalleryInfo", message)
  }
}

fun printError(message: String) {
  Log.e("GalleryInfo", message)
}

fun printWarning(message: String) {
  if (BuildConfig.DEBUG) {
    Log.w("GalleryInfo", message)
  }
}