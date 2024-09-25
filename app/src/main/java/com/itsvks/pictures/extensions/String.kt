package com.itsvks.pictures.extensions

fun String?.formatMinSec(): String {
  return when (val value = this?.toLong()) {
    null -> ""
    else -> value.formatMinSec()
  }
}