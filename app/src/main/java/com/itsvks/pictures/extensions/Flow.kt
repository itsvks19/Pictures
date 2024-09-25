package com.itsvks.pictures.extensions

import android.database.Cursor
import com.itsvks.pictures.core.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T> Flow<Cursor?>.mapEachRow(
  projection: Array<String>,
  mapping: (Cursor, Array<Int>) -> T,
) = map { it.mapEachRow(projection, mapping) }

fun <T> Flow<List<T>>.mapAsResource(
  errorOnEmpty: Boolean = false,
  errorMessage: String = "No data found"
) = map {
  if (errorOnEmpty && it.isEmpty()) {
    Resource.Error(errorMessage)
  } else {
    Resource.Success(it)
  }
}