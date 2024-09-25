package com.itsvks.pictures.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.itsvks.pictures.models.IgnoredAlbum
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaVersion
import com.itsvks.pictures.models.PinnedAlbum
import com.itsvks.pictures.models.TimelineSettings
import com.itsvks.pictures.util.room.Converters

@Database(
  entities = [PinnedAlbum::class, IgnoredAlbum::class, Media::class, MediaVersion::class, TimelineSettings::class],
  version = 5,
  exportSchema = true,
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
    AutoMigration(from = 2, to = 3),
    AutoMigration(from = 3, to = 4),
    AutoMigration(from = 4, to = 5)
  ]
)
@TypeConverters(Converters::class)
abstract class InternalDatabase : RoomDatabase() {
  abstract fun getPinnedDao(): PinnedDao
  abstract fun getBlacklistDao(): BlacklistDao
  abstract fun getMediaDao(): MediaDao

  companion object {
    const val NAME = "internal_db"
  }
}