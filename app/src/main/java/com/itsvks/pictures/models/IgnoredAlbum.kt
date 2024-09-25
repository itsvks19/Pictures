package com.itsvks.pictures.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.itsvks.pictures.extensions.volume
import kotlinx.parcelize.Parcelize

@Entity(tableName = "blacklist")
@Parcelize
@Immutable
data class IgnoredAlbum(
  @PrimaryKey(autoGenerate = false)
  val id: Long,
  val label: String,
  val wildcard: String? = null,
  @ColumnInfo(defaultValue = ALBUMS_ONLY.toString())
  val location: Int = ALBUMS_ONLY,
  @ColumnInfo(defaultValue = "[]")
  val matchedAlbums: List<String> = emptyList()
) : Parcelable {
  companion object {
    const val ALBUMS_ONLY = 0
    const val TIMELINE_ONLY = 1
    const val ALBUMS_AND_TIMELINE = 2
  }

  private val hiddenInBoth get() = location == ALBUMS_AND_TIMELINE
  private val hiddenInAlbums get() = location == ALBUMS_ONLY || hiddenInBoth
  private val hiddenInTimeline get() = location == TIMELINE_ONLY || hiddenInBoth

  fun matchesMedia(media: Media) = matches(
    id = media.albumId,
    path = media.path,
    relativePath = media.relativePath,
    volume = media.volume,
    shouldRemove = hiddenInTimeline
  )

  fun matchesAlbum(album: Album) = matches(
    id = album.id,
    path = album.pathToThumbnail,
    relativePath = album.relativePath,
    volume = album.volume,
    shouldRemove = hiddenInAlbums
  )

  private fun matches(
    id: Long,
    path: String,
    relativePath: String,
    volume: String,
    shouldRemove: Boolean
  ): Boolean {
    val matchesId = this.id == id
    if (matchesId) return shouldRemove

    val regex = wildcard?.toRegex()
    return regex?.let {
      path.matches(it) || relativePath.matches(it) || volume.matches(it)
    } ?: false
  }
}
