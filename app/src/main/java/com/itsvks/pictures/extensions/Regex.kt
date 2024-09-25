package com.itsvks.pictures.extensions

import com.itsvks.pictures.models.Album

fun Regex.matchesAlbum(album: Album): Boolean {
  return album.pathToThumbnail.matches(this) ||
      album.relativePath.matches(this) ||
      album.volume.matches(this)
}