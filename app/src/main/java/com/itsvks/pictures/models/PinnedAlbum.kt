package com.itsvks.pictures.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_table")
@Immutable
data class PinnedAlbum(
  @PrimaryKey(autoGenerate = true)
  val id: Long
)
