package com.itsvks.pictures.models

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class Album(
  val id: Long = 0,
  val label: String,
  val uri: Uri,
  val pathToThumbnail: String,
  val relativePath: String,
  val timestamp: Long,
  var count: Long = 0,
  var size: Long = 0,
  val isPinned: Boolean = false
) : Parcelable {

  @IgnoredOnParcel
  @Stable
  val volume = pathToThumbnail.substringBeforeLast("/").removeSuffix(relativePath.removeSuffix("/"))

  @IgnoredOnParcel
  @Stable
  val isOnSdcard = volume.toLowerCase(Locale.current).matches(".*[0-9a-f]{4}-[0-9a-f]{4}".toRegex())

  companion object {
    val NewAlbum = Album(
      id = -200,
      label = "New Album",
      uri = Uri.EMPTY,
      pathToThumbnail = "",
      relativePath = "",
      timestamp = 0
    )
  }
}