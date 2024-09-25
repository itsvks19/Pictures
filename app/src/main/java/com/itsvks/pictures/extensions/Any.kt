package com.itsvks.pictures.extensions


val Any.isHeaderKey
  get() = this is String && startsWith("header_")

val Any.isBigHeaderKey
  get() = this is String && startsWith("header_big_")

val Any.isIgnoredKey
  get() = this is String && this == "aboveGrid"