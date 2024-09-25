package com.itsvks.pictures.database.mediastore.queries

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.core.os.bundleOf
import com.itsvks.pictures.database.mediastore.MediaQuery
import com.itsvks.pictures.extensions.Query
import com.itsvks.pictures.extensions.and
import com.itsvks.pictures.extensions.eq
import com.itsvks.pictures.extensions.join
import com.itsvks.pictures.extensions.queryFlow
import com.itsvks.pictures.extensions.tryGetString
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.MediaType
import com.itsvks.pictures.util.PickerUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlbumsFlow(
  private val context: Context,
  private val mimeType: String? = null,
) : QueryFlow<Album>() {

  override fun flowCursor(): Flow<Cursor?> {
    val uri = MediaQuery.MediaStoreFileUri
    val projection = MediaQuery.AlbumsProjection

    val imageOrVideo = PickerUtils.mediaTypeFromGenericMimeType(mimeType)?.let {
      when (it) {
        MediaType.IMAGE -> MediaQuery.Selection.image
        MediaType.VIDEO -> MediaQuery.Selection.video
      }
    } ?: MediaQuery.Selection.imageOrVideo

    val rawMimeType = mimeType?.takeIf { PickerUtils.isMimeTypeNotGeneric(it) }
    val mimeTypeQuery = rawMimeType?.let {
      FileColumns.MIME_TYPE eq Query.ARG
    }

    // Join all the non-null queries
    val selection = listOfNotNull(
      mimeTypeQuery,
      imageOrVideo,
    ).join(Query::and)

    val selectionArgs = listOfNotNull(
      rawMimeType,
    ).toTypedArray()

    val sortOrder = FileColumns.DATE_MODIFIED + " DESC"

    val queryArgs = Bundle().apply {
      putAll(
        bundleOf(
          ContentResolver.QUERY_ARG_SQL_SELECTION to selection?.build(),
          ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to selectionArgs,
          ContentResolver.QUERY_ARG_SQL_SORT_ORDER to sortOrder,
        )
      )
    }

    return context.contentResolver.queryFlow(
      uri,
      projection,
      queryArgs,
    )
  }

  override fun flowData() = flowCursor().map {
    mutableMapOf<Int, Album>().apply {
      it?.use {
        val idIndex = it.getColumnIndex(FileColumns._ID)
        val albumIdIndex = it.getColumnIndex(FileColumns.BUCKET_ID)
        val labelIndex = it.getColumnIndex(FileColumns.BUCKET_DISPLAY_NAME)
        val thumbnailPathIndex = it.getColumnIndex(FileColumns.DATA)
        val thumbnailRelativePathIndex = it.getColumnIndex(FileColumns.RELATIVE_PATH)
        val thumbnailDateIndex = it.getColumnIndex(FileColumns.DATE_MODIFIED)
        val sizeIndex = it.getColumnIndex(FileColumns.SIZE)
        val mimeTypeIndex = it.getColumnIndex(FileColumns.MIME_TYPE)

        if (!it.moveToFirst()) {
          return@use
        }

        while (!it.isAfterLast) {
          val bucketId = it.getInt(albumIdIndex)

          this[bucketId]?.also { album ->
            album.count += 1
            album.size += it.getLong(sizeIndex)
          } ?: run {
            val albumId = it.getLong(albumIdIndex)
            val id = it.getLong(idIndex)
            val label = it.tryGetString(labelIndex, "Root Folder")
            val thumbnailPath = it.getString(thumbnailPathIndex)
            val thumbnailRelativePath = it.getString(thumbnailRelativePathIndex)
            val thumbnailDate = it.getLong(thumbnailDateIndex)
            val size = it.getLong(sizeIndex)
            val mimeType = it.getString(mimeTypeIndex)

            val contentUri = if (mimeType.contains("image")) {
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
              MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            this[bucketId] = Album(
              id = albumId,
              label = label ?: "Root Folder",
              uri = ContentUris.withAppendedId(contentUri, id),
              pathToThumbnail = thumbnailPath,
              relativePath = thumbnailRelativePath,
              timestamp = thumbnailDate
            ).apply {
              this.count += 1
              this.size += size
            }
          }

          it.moveToNext()
        }
      }
    }.values.toList()
  }
}