package com.itsvks.pictures.models

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcelable
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.room.Entity
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.extensions.getDate
import kotlinx.parcelize.Parcelize
import java.io.File
import kotlin.random.Random

@Immutable
@Parcelize
@Entity(tableName = "media", primaryKeys = ["id"])
data class Media(
  val id: Long = 0,
  val label: String,
  val uri: Uri,
  val path: String,
  val relativePath: String,
  val albumId: Long,
  val albumLabel: String,
  val timestamp: Long,
  val expiryTimestamp: Long? = null,
  val takenTimestamp: Long? = null,
  val fullDate: String,
  val mimeType: String,
  val favorite: Int,
  val trashed: Int,
  val size: Long,
  val duration: String? = null
) : Parcelable {

  @Stable
  override fun toString(): String {
    return "$id, $path, $fullDate, $mimeType, favorite=$favorite"
  }

  companion object {
    fun createFromUri(context: Context, uri: Uri): Media? {
      if (uri.path == null) return null

      val extension = uri.toString().substringAfterLast(".")
      var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
      var duration: String? = null

      try {
        val retriever = MediaMetadataRetriever().apply { setDataSource(context, uri) }
        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
        val isVideo = hasVideo == "yes"

        if (isVideo) {
          duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        }

        if (mimeType.isEmpty()) {
          mimeType = if (isVideo) "video/*" else "image/*"
        }
      } catch (err: Exception) {
        println(err)
      }

      var timestamp = 0L
      uri.path?.let { File(it) }?.let {
        timestamp = try {
          it.lastModified()
        } catch (err: Exception) {
          err.printStackTrace()
          0L
        }
      }

      var formattedDate = ""
      if (timestamp != 0L) {
        formattedDate = timestamp.getDate(Constants.EXTENDED_DATE_FORMAT)
      }

      return Media(
        id = Random(System.currentTimeMillis()).nextLong(-1000, 25600000),
        label = uri.toString().substringAfterLast("/"),
        uri = uri,
        path = uri.path.toString(),
        relativePath = uri.path.toString().substringBeforeLast("/"),
        albumId = -69L,
        albumLabel = "",
        timestamp = timestamp,
        fullDate = formattedDate,
        mimeType = mimeType,
        favorite = 0,
        trashed = 0,
        size = 0,
        duration = duration
      )
    }
  }
}

enum class AllowedMedia {
  PHOTOS, VIDEOS, BOTH;

  override fun toString(): String {
    return when (this) {
      PHOTOS -> "image%"
      VIDEOS -> "video%"
      BOTH -> "%/%"
    }
  }

  fun toStringAny() = toString()
}
