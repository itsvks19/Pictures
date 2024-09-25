package com.itsvks.pictures.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.itsvks.pictures.models.AllowedMedia
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaVersion
import com.itsvks.pictures.models.TimelineSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

  @Query("SELECT * FROM media ORDER BY timestamp DESC")
  suspend fun getMedia(): List<Media>

  @Query("SELECT * FROM media WHERE mimeType LIKE :allowedMedia ORDER BY timestamp DESC")
  suspend fun getMediaByType(allowedMedia: AllowedMedia): List<Media>

  @Query("SELECT * FROM media WHERE favorite = 1 ORDER BY timestamp DESC")
  suspend fun getFavorites(): List<Media>

  @Query("SELECT * FROM media WHERE id = :id LIMIT 1")
  suspend fun getMediaById(id: Long): Media

  @Query("SELECT * FROM media WHERE albumId = :albumId ORDER BY timestamp DESC")
  suspend fun getMediaByAlbumId(albumId: Long): List<Media>

  @Query("SELECT * FROM media WHERE albumId = :albumId AND mimeType LIKE :allowedMedia ORDER BY timestamp DESC")
  suspend fun getMediaByAlbumIdAndType(albumId: Long, allowedMedia: AllowedMedia): List<Media>

  @Upsert(entity = Media::class)
  suspend fun addMediaList(mediaList: List<Media>)

  @Transaction
  suspend fun updateMedia(mediaList: List<Media>) {
    addMediaList(mediaList = mediaList)
    deleteMediaNotInList(mediaIds = mediaList.map { it.id })
  }

  @Query("DELETE FROM media WHERE id NOT IN (:mediaIds)")
  suspend fun deleteMediaNotInList(mediaIds: List<Long>)

  @Upsert(entity = MediaVersion::class)
  suspend fun setMediaVersion(version: MediaVersion)

  @Query("SELECT EXISTS(SELECT * FROM media_version WHERE version = :version) LIMIT 1")
  suspend fun isMediaVersionUpToDate(version: String): Boolean

  @Query("SELECT * FROM timeline_settings LIMIT 1")
  fun getTimelineSettings(): Flow<TimelineSettings?>

  @Upsert(entity = TimelineSettings::class)
  suspend fun setTimelineSettings(settings: TimelineSettings)
}