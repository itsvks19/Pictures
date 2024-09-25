package com.itsvks.pictures.database.mediastore.queries

import android.database.Cursor
import kotlinx.coroutines.flow.Flow

abstract class QueryFlow<T> {
  abstract fun flowData(): Flow<List<T>>

  abstract fun flowCursor(): Flow<Cursor?>
}