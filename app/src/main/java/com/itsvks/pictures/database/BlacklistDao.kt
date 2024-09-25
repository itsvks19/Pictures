package com.itsvks.pictures.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.itsvks.pictures.models.IgnoredAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistDao {
  @Query("SELECT * FROM blacklist")
  fun getBlacklistedAlbums(): Flow<List<IgnoredAlbum>>

  @Upsert
  suspend fun addBlacklistedAlbum(ignoredAlbum: IgnoredAlbum)

  @Delete
  suspend fun removeBlacklistedAlbum(ignoredAlbum: IgnoredAlbum)
}