package com.itsvks.pictures.extensions

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.VideoFile
import com.itsvks.pictures.R
import com.itsvks.pictures.models.EncryptedMedia
import com.itsvks.pictures.models.InfoRow
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.util.ExifMetadata

val sdcardRegex = "^/storage/[A-Z0-9]+-[A-Z0-9]+/.*$".toRegex()

val Media.isRaw: Boolean
  get() {
    return mimeType.isNotBlank() && (mimeType.startsWith("image/x-") || mimeType.startsWith("image/vnd."))
  }

val Media.fileExtension get() = label.substringAfterLast(".").removePrefix(".")

val Media.volume get() = path.substringBeforeLast("/").removeSuffix(relativePath.removeSuffix("/"))

val Media.readUriOnly get() = albumId == -69L && albumLabel == ""

val Media.isVideo get() = mimeType.startsWith("video/") && duration != null

val Media.isImage get() = mimeType.startsWith("image/")

val Media.isTrashed get() = trashed == 1

val Media.isFavorite get() = favorite == 1

fun Media.toEncryptedMedia(bytes: ByteArray): EncryptedMedia {
  return EncryptedMedia(
    id, label, bytes, path, timestamp, mimeType, duration
  )
}

fun List<Media>.mediaPair(): Pair<List<Media>, List<Media>> {
  val trashableMedia = ArrayList<Media>()
  val nonTrashableMedia = ArrayList<Media>()
  forEach {
    if (it.path.matches(sdcardRegex)) {
      nonTrashableMedia.add(it)
    } else {
      trashableMedia.add(it)
    }
  }
  return trashableMedia to nonTrashableMedia
}

fun Media.canBeTrashed(): Boolean {
  return !path.matches(sdcardRegex)
}

fun Media.retrieveMetadata(
  context: Context,
  exifMetadata: ExifMetadata?,
  onLabelClick: () -> Unit
): List<InfoRow> {
  val infoList = arrayListOf<InfoRow>()

  if (isTrashed) {
    infoList.apply {
      add(
        InfoRow(
          icon = Icons.Outlined.Photo,
          trailingIcon = Icons.Outlined.Edit,
          label = context.getString(R.string.label),
          onClick = onLabelClick,
          content = label
        )
      )

      add(
        InfoRow(
          icon = Icons.Outlined.Info,
          label = context.getString(R.string.path),
          content = path
        )
      )
    }

    return infoList
  }

  try {
    infoList.apply {
      if (exifMetadata != null && !exifMetadata.modelName.isNullOrEmpty()) {
        val aperture = exifMetadata.apertureValue
        val focalLength = exifMetadata.focalLength
        val iso = exifMetadata.isoValue
        val content = buildString {
          if (aperture != 0.0) append("f/$aperture")
          if (focalLength != 0.0) append(" • ${focalLength}mm")
          if (iso != 0) append("${context.getString(R.string.iso)}$iso")
        }

        add(
          InfoRow(
            icon = Icons.Outlined.Camera,
            label = "${exifMetadata.manufacturerName} ${exifMetadata.modelName}",
            content = content
          )
        )
      }

      add(
        InfoRow(
          icon = Icons.Outlined.Photo,
          trailingIcon = Icons.Outlined.Edit,
          label = context.getString(R.string.label),
          onClick = onLabelClick,
          content = label
        )
      )

      val contentString = buildString {
        append(path.toFile().formattedFileSize(context))

        if (mimeType.contains("video")) append(" • ${duration.formatMinSec()}")
        else if (exifMetadata != null && exifMetadata.imageWidth != 0 && exifMetadata.imageHeight != 0) {
          val width = exifMetadata.imageWidth
          val height = exifMetadata.imageHeight
          val imageMp = exifMetadata.imageMp
          if (imageMp > "0") append(" • $imageMp MP")
          if (width > 0 && height > 0) append(" • $width x $height")
        }
      }

      val icon = if (mimeType.contains("video")) {
        Icons.Outlined.VideoFile
      } else Icons.Outlined.ImageSearch

      add(
        InfoRow(
          icon = icon,
          label = context.getString(R.string.metadata),
          content = contentString
        )
      )

      add(
        InfoRow(
          icon = Icons.Outlined.Info,
          label = context.getString(R.string.path),
          content = path.substringBeforeLast("/")
        )
      )
    }
  } catch (err: Exception) {
    err.printStackTrace()
  }

  return infoList
}