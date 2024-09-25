package com.itsvks.pictures.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Immutable
@Parcelize
data class EncryptedMedia(
  val id: Long = 0,
  val label: String,
  val bytes: ByteArray,
  val path: String,
  val timestamp: Long,
  val mimeType: String,
  val duration: String? = null
) : Parcelable, Serializable {

  @IgnoredOnParcel
  @Stable
  val isVideo = mimeType.startsWith("video/") && duration != null

  @IgnoredOnParcel
  @Stable
  val isImage = mimeType.startsWith("image/")

  @IgnoredOnParcel
  @Stable
  val isRaw =
    mimeType.isNotBlank() && (mimeType.startsWith("image/x-") || mimeType.startsWith("image/vnd."))

  @IgnoredOnParcel
  @Stable
  val fileExtension = label.substringAfterLast(".").removePrefix(".")

  @Stable
  override fun toString(): String {
    return "$id, $path, $timestamp, $mimeType"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EncryptedMedia

    if (id != other.id) return false
    if (label != other.label) return false
    if (!bytes.contentEquals(other.bytes)) return false
    if (path != other.path) return false
    if (timestamp != other.timestamp) return false
    if (mimeType != other.mimeType) return false
    if (duration != other.duration) return false
    if (isVideo != other.isVideo) return false
    if (isImage != other.isImage) return false
    if (isRaw != other.isRaw) return false
    if (fileExtension != other.fileExtension) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + label.hashCode()
    result = 31 * result + bytes.contentHashCode()
    result = 31 * result + path.hashCode()
    result = 31 * result + timestamp.hashCode()
    result = 31 * result + mimeType.hashCode()
    result = 31 * result + (duration?.hashCode() ?: 0)
    result = 31 * result + isVideo.hashCode()
    result = 31 * result + isImage.hashCode()
    result = 31 * result + isRaw.hashCode()
    result = 31 * result + fileExtension.hashCode()
    return result
  }
}
