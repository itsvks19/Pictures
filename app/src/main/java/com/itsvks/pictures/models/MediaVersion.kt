package com.itsvks.pictures.models

import androidx.room.Entity

@Entity(tableName = "media_version", primaryKeys = ["version"])
data class MediaVersion(
  val version: String
)
