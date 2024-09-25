package com.itsvks.pictures.util.room

import android.net.Uri
import androidx.room.TypeConverter
import com.itsvks.pictures.util.MediaOrder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Converters {
  @TypeConverter
  fun toString(value: String?): List<String> = Json.decodeFromString(value ?: "[]")

  @TypeConverter
  fun fromList(list: List<String>?) = Json.encodeToString(list ?: emptyList())

  @TypeConverter
  fun toUri(value: String): Uri = Uri.parse(value)

  @TypeConverter
  fun fromUri(uri: Uri) = uri.toString()

  @TypeConverter
  fun toMediaOrder(value: String): MediaOrder = Json.decodeFromString(value)

  @TypeConverter
  fun fromMediaOrder(mediaOrder: MediaOrder) = Json.encodeToString(mediaOrder)
}