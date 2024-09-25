package com.itsvks.pictures.extensions

import android.content.Context
import com.itsvks.pictures.database.InternalDatabase

suspend fun InternalDatabase.isMediaUpToDate(context: Context): Boolean {
  return getMediaDao().isMediaVersionUpToDate(context.mediaStoreVersion)
}