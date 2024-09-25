package com.itsvks.pictures.database.mediastore

import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import com.itsvks.pictures.extensions.eq
import com.itsvks.pictures.extensions.or

object MediaQuery {
  val MediaStoreFileUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

  val MediaProjection = arrayOf(
    FileColumns._ID,
    FileColumns.DATA,
    FileColumns.RELATIVE_PATH,
    FileColumns.DISPLAY_NAME,
    FileColumns.BUCKET_ID,
    FileColumns.BUCKET_DISPLAY_NAME,
    FileColumns.DATE_TAKEN,
    FileColumns.DATE_MODIFIED,
    FileColumns.DURATION,
    FileColumns.SIZE,
    FileColumns.MIME_TYPE,
    FileColumns.IS_FAVORITE,
    FileColumns.IS_TRASHED,
    FileColumns.DATE_EXPIRES,
  )

  val AlbumsProjection = arrayOf(
    FileColumns._ID,
    FileColumns.DATA,
    FileColumns.RELATIVE_PATH,
    FileColumns.DISPLAY_NAME,
    FileColumns.BUCKET_ID,
    FileColumns.BUCKET_DISPLAY_NAME,
    FileColumns.DATE_TAKEN,
    FileColumns.DATE_MODIFIED,
    FileColumns.SIZE,
    FileColumns.MIME_TYPE,
  )

  object Selection {
    val image = FileColumns.MEDIA_TYPE eq FileColumns.MEDIA_TYPE_IMAGE
    val video = FileColumns.MEDIA_TYPE eq FileColumns.MEDIA_TYPE_VIDEO
    val imageOrVideo = image or video
  }
}