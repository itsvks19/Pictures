package com.itsvks.pictures.extensions

import android.database.Cursor
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

fun <T> Cursor?.mapEachRow(
  projection: Array<String>,
  mapping: (Cursor, Array<Int>) -> T,
) = this?.use { cursor ->
  if (!cursor.moveToFirst()) {
    return@use emptyList<T>()
  }

  val indexCache = projection.map { column ->
    cursor.getColumnIndexOrThrow(column)
  }.toTypedArray()

  val data = mutableListOf<T>()
  do {
    data.add(mapping(cursor, indexCache))
  } while (cursor.moveToNext())

  data.toList()
} ?: emptyList()

fun Cursor?.tryGetString(columnIndex: Int, fallback: String? = null): String? {
  return this?.getStringOrNull(columnIndex) ?: fallback
}

fun Cursor?.tryGetLong(columnIndex: Int, fallback: Long? = null): Long? {
  return this?.getLongOrNull(columnIndex) ?: fallback
}