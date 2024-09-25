package com.itsvks.pictures.screens.ignored.setup

import com.itsvks.pictures.models.Album

sealed class IgnoredType {
  data class SELECTION(val selectedAlbum: Album?) : IgnoredType()
  data class REGEX(val regex: String) : IgnoredType()
}