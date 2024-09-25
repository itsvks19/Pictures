package com.itsvks.pictures.database.mediastore.queries

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.core.os.bundleOf
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.core.MediaStoreBuckets
import com.itsvks.pictures.database.mediastore.MediaQuery
import com.itsvks.pictures.extensions.Query
import com.itsvks.pictures.extensions.and
import com.itsvks.pictures.extensions.eq
import com.itsvks.pictures.extensions.getDate
import com.itsvks.pictures.extensions.isTrashed
import com.itsvks.pictures.extensions.join
import com.itsvks.pictures.extensions.mapEachRow
import com.itsvks.pictures.extensions.queryFlow
import com.itsvks.pictures.extensions.tryGetLong
import com.itsvks.pictures.extensions.tryGetString
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaType
import com.itsvks.pictures.util.PickerUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaUriFlow(
  private val contentResolver: ContentResolver,
  private val mimeType: String? = null,
  private val uris: List<Uri>,
  private val reviewMode: Boolean = false
) : QueryFlow<Media>() {

  private var buckedId: Long = MediaStoreBuckets.MEDIA_STORE_BUCKET_TIMELINE.id

  override fun flowCursor(): Flow<Cursor?> {
    val uri = MediaQuery.MediaStoreFileUri
    val projection = MediaQuery.MediaProjection
    val imageOrVideo = PickerUtils.mediaTypeFromGenericMimeType(mimeType)?.let {
      when (it) {
        MediaType.IMAGE -> MediaQuery.Selection.image
        MediaType.VIDEO -> MediaQuery.Selection.video
      }
    } ?: MediaQuery.Selection.imageOrVideo
    val albumFilter = when (buckedId) {
      MediaStoreBuckets.MEDIA_STORE_BUCKET_FAVORITES.id -> FileColumns.IS_FAVORITE eq 1

      MediaStoreBuckets.MEDIA_STORE_BUCKET_TRASH.id -> FileColumns.IS_TRASHED eq 1

      MediaStoreBuckets.MEDIA_STORE_BUCKET_TIMELINE.id,
      MediaStoreBuckets.MEDIA_STORE_BUCKET_PHOTOS.id,
      MediaStoreBuckets.MEDIA_STORE_BUCKET_VIDEOS.id -> null

      else -> FileColumns.BUCKET_ID eq Query.ARG
    }

    val rawMimeType = mimeType?.takeIf { PickerUtils.isMimeTypeNotGeneric(it) }
    val mimeTypeQuery = rawMimeType?.let {
      FileColumns.MIME_TYPE eq Query.ARG
    }

    val selection = listOfNotNull(
      imageOrVideo,
      albumFilter,
      mimeTypeQuery,
    ).join(Query::and)

    val selectionArgs = listOfNotNull(
      buckedId.takeIf {
        MediaStoreBuckets.entries.toTypedArray().none { bucket -> it == bucket.id }
      }?.toString(),
      rawMimeType,
    ).toTypedArray()

    val sortOrder = when (buckedId) {
      MediaStoreBuckets.MEDIA_STORE_BUCKET_TRASH.id -> "${FileColumns.DATE_EXPIRES} DESC"
      else -> "${FileColumns.DATE_MODIFIED} DESC"
    }

    val queryArgs = Bundle().apply {
      putAll(
        bundleOf(
          ContentResolver.QUERY_ARG_SQL_SELECTION to selection?.build(),
          ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to selectionArgs,
          ContentResolver.QUERY_ARG_SQL_SORT_ORDER to sortOrder,
        )
      )

      putInt(
        MediaStore.QUERY_ARG_MATCH_TRASHED, when (buckedId) {
          MediaStoreBuckets.MEDIA_STORE_BUCKET_TRASH.id -> MediaStore.MATCH_ONLY
          else -> MediaStore.MATCH_EXCLUDE
        }
      )
    }

    return contentResolver.queryFlow(
      uri,
      projection,
      queryArgs,
    )
  }

  override fun flowData() =
    flowCursor().mapEachRow(MediaQuery.MediaProjection) { it, indexCache ->
      var i = 0

      val id = it.getLong(indexCache[i++])
      val path = it.getString(indexCache[i++])
      val relativePath = it.getString(indexCache[i++])
      val title = it.getString(indexCache[i++])
      val albumId = it.getLong(indexCache[i++])
      val albumLabel = it.tryGetString(indexCache[i++], Build.MODEL)
      val takenTimestamp = it.tryGetLong(indexCache[i++])
      val modifiedTimestamp = it.getLong(indexCache[i++])
      val duration = it.tryGetString(indexCache[i++])
      val size = it.getLong(indexCache[i++])
      val mimeType = it.getString(indexCache[i++])
      val isFavorite = it.getInt(indexCache[i++])
      val isTrashed = it.getInt(indexCache[i++])
      val expiryTimestamp = it.tryGetLong(indexCache[i])

      val contentUri = if (mimeType.contains("image")) {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
      } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
      }
      val uri = ContentUris.withAppendedId(contentUri, id)

      val formattedDate = modifiedTimestamp.getDate(Constants.FULL_DATE_FORMAT)

      Media(
        id = id,
        label = title,
        uri = uri,
        path = path,
        relativePath = relativePath,
        albumId = albumId,
        albumLabel = albumLabel ?: Build.MODEL,
        timestamp = modifiedTimestamp,
        takenTimestamp = takenTimestamp,
        expiryTimestamp = expiryTimestamp,
        fullDate = formattedDate,
        duration = duration,
        favorite = isFavorite,
        trashed = isTrashed,
        size = size,
        mimeType = mimeType
      )
    }.let { flow ->
      val ids = uris.map { ContentUris.parseId(it) }
      if (reviewMode) {
        flow.map { mediaList ->
          mediaList.filter { media ->
            ids.contains(media.id) && !media.isTrashed
          }
        }
      } else {
        flow.map { mediaList ->
          mediaList.filter { media ->
            ids.contains(media.id)
          }
        }
      }
    }
}