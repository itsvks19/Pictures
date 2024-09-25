package com.itsvks.pictures.util

import android.content.Intent
import android.provider.MediaStore
import com.itsvks.pictures.models.MediaType

object PickerUtils {
  private const val MIME_TYPE_IMAGE_ANY = "image/*"
  private const val MIME_TYPE_VIDEO_ANY = "video/*"
  private const val MIME_TYPE_ANY = "*/*"

  fun translateMimeType(intent: Intent?) = when (intent?.action) {
    Intent.ACTION_SET_WALLPAPER -> MIME_TYPE_IMAGE_ANY
    else -> (intent?.type ?: MIME_TYPE_ANY).let {
      when (it) {
        MediaStore.Images.Media.CONTENT_TYPE -> MIME_TYPE_IMAGE_ANY
        MediaStore.Video.Media.CONTENT_TYPE -> MIME_TYPE_VIDEO_ANY
        else -> when {
          it == MIME_TYPE_ANY
              || it.startsWith("image/")
              || it.startsWith("video/") -> it

          else -> null
        }
      }
    }
  }

  fun mediaTypeFromGenericMimeType(mimeType: String?) = when (mimeType) {
    MIME_TYPE_IMAGE_ANY -> MediaType.IMAGE
    MIME_TYPE_VIDEO_ANY -> MediaType.VIDEO
    else -> null
  }

  fun isMimeTypeNotGeneric(mimeType: String?) = mimeType?.let {
    it !in listOf(
      MIME_TYPE_IMAGE_ANY,
      MIME_TYPE_VIDEO_ANY,
      MIME_TYPE_ANY,
    )
  } ?: false
}