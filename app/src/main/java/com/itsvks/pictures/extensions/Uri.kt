package com.itsvks.pictures.extensions

import android.content.Context
import android.net.Uri
import com.itsvks.pictures.PicturesApp

fun Uri.getBytes(context: Context = PicturesApp.instance.applicationContext) =
  context.contentResolver.openInputStream(this)?.use {
    it.readBytes()
  }

fun Uri.isFromApps(): Boolean =
  scheme.toString() == "content" && toString().contains("Android/")