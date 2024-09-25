package com.itsvks.pictures.extensions

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun WindowInsetsControllerCompat.toggleSystemBars(show: Boolean) {
  if (show) show(WindowInsetsCompat.Type.systemBars())
  else hide(WindowInsetsCompat.Type.systemBars())
}