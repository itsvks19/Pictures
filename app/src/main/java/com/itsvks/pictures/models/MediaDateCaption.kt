package com.itsvks.pictures.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.extensions.getDate
import com.itsvks.pictures.util.ExifMetadata

@Stable
data class MediaDateCaption(
  val date: String,
  val deviceInfo: String? = null,
  val description: String
)

@Composable
fun rememberMediaDateCaption(
  exifMetadata: ExifMetadata?,
  media: Any
): MediaDateCaption {
  val deviceInfo = remember(exifMetadata) { exifMetadata?.lensDescription }
  val defaultDesc = stringResource(R.string.image_add_description)
  val description = remember(exifMetadata) { exifMetadata?.imageDescription ?: defaultDesc }

  val timestamp = when (media) {
    is Media -> media.timestamp
    is EncryptedMedia -> media.timestamp
    else -> throw IllegalArgumentException("Unsupported media type")
  }

  return remember(media) {
    MediaDateCaption(
      date = timestamp.getDate(Constants.EXIF_DATE_FORMAT),
      deviceInfo = deviceInfo,
      description = description
    )
  }
}
