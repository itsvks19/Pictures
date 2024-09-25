package com.itsvks.pictures.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.core.Resource
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.models.AllowedMedia
import com.itsvks.pictures.models.EncryptedMedia
import com.itsvks.pictures.models.EncryptedMediaItem
import com.itsvks.pictures.models.EncryptedMediaState
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaItem
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.util.MediaOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RepeatOnResume(action: () -> Unit) {
  val owner = LocalLifecycleOwner.current
  LaunchedEffect(Unit) {
    owner.lifecycleScope.launch {
      owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        action()
      }
    }
  }
}

fun <T> MutableState<T>.update(newState: T) {
  value = newState
}

fun MediaRepository.mediaFlowWithType(
  albumId: Long,
  allowedMedia: AllowedMedia
): Flow<Resource<List<Media>>> =
  (if (albumId != -1L) {
    getMediaByAlbumIdWithType(albumId, allowedMedia)
  } else {
    getMediaByType(allowedMedia)
  }).flowOn(Dispatchers.IO).conflate()

fun MediaRepository.mediaFlow(albumId: Long, target: String?): Flow<Resource<List<Media>>> =
  (if (albumId != -1L) {
    getMediaByAlbumId(albumId)
  } else if (!target.isNullOrEmpty()) {
    when (target) {
      Constants.Target.TARGET_FAVORITES -> getFavorites(mediaOrder = MediaOrder.Default)
      Constants.Target.TARGET_TRASH -> getTrashed()
      else -> getMedia()
    }
  } else {
    getMedia()
  })

fun Flow<Resource<List<Media>>>.mapMedia(
  albumId: Long,
  groupByMonth: Boolean = false,
  withMonthHeader: Boolean = true,
  updateDatabase: () -> Unit
) = map {
  updateDatabase()
  mapMediaToItem(
    data = it.data ?: emptyList(),
    error = it.message ?: "",
    albumId = albumId,
    groupByMonth = groupByMonth,
    withMonthHeader = withMonthHeader
  )
}

suspend fun MutableStateFlow<MediaState>.collectMedia(
  data: List<Media>,
  error: String,
  albumId: Long,
  groupByMonth: Boolean = false,
  withMonthHeader: Boolean = true
) = withContext(Dispatchers.IO) {
  emit(
    mapMediaToItem(
      data = data,
      error = error,
      albumId = albumId,
      groupByMonth = groupByMonth,
      withMonthHeader = withMonthHeader
    )
  )
}

suspend fun mapMediaToItem(
  data: List<Media>,
  error: String,
  albumId: Long,
  groupByMonth: Boolean = false,
  withMonthHeader: Boolean = true
) = withContext(Dispatchers.IO) {
  val mappedData = mutableListOf<MediaItem>()
  val mappedDataWithMonthly = mutableListOf<MediaItem>()
  val monthHeaderList: MutableSet<String> = mutableSetOf()
  val headers = mutableListOf<MediaItem.Header>()
  val groupedData = data.groupBy {
    if (groupByMonth) {
      it.timestamp.getMonth()
    } else {
      it.timestamp.getDate(
        stringToday = "Today"
        /** Localized in composition */
        ,
        stringYesterday = "Yesterday"
        /** Localized in composition */
      )
    }
  }
  groupedData.forEach { (date, data) ->
    val dateHeader = MediaItem.Header("header_$date", date, data.map { it.id }.toSet())
    headers.add(dateHeader)
    val groupedMedia = data.map {
      MediaItem.MediaViewItem("media_${it.id}_${it.label}", it)
    }
    if (groupByMonth) {
      mappedData.add(dateHeader)
      mappedData.addAll(groupedMedia)
      mappedDataWithMonthly.add(dateHeader)
      mappedDataWithMonthly.addAll(groupedMedia)
    } else {
      val month = getMonth(date)
      if (month.isNotEmpty() && !monthHeaderList.contains(month)) {
        monthHeaderList.add(month)
        if (withMonthHeader && mappedDataWithMonthly.isNotEmpty()) {
          mappedDataWithMonthly.add(
            MediaItem.Header(
              "header_big_${month}_${data.size}",
              month,
              data.map { it.id }.toSet()
            )
          )
        }
      }
      mappedData.add(dateHeader)
      if (withMonthHeader) {
        mappedDataWithMonthly.add(dateHeader)
      }
      mappedData.addAll(groupedMedia)
      if (withMonthHeader) {
        mappedDataWithMonthly.addAll(groupedMedia)
      }
    }
  }
  MediaState(
    isLoading = false,
    error = error,
    media = data,
    headers = headers,
    mappedMedia = mappedData,
    mappedMediaWithMonthly = if (withMonthHeader) mappedDataWithMonthly else emptyList(),
    dateHeader = data.dateHeader(albumId)
  )
}

private fun List<Media>.dateHeader(albumId: Long): String =
  if (albumId != -1L) {
    val startDate = last().timestamp.getDate()
    val endDate = first().timestamp.getDate()
    getDateHeader(startDate, endDate)
  } else ""

suspend fun MutableStateFlow<EncryptedMediaState>.collectEncryptedMedia(
  data: List<EncryptedMedia>,
  groupByMonth: Boolean = false,
  withMonthHeader: Boolean = true
) = withContext(Dispatchers.IO) {
  emit(
    mapEncryptedMediaToItem(
      data = data,
      groupByMonth = groupByMonth,
      withMonthHeader = withMonthHeader
    )
  )
}

suspend fun mapEncryptedMediaToItem(
  data: List<EncryptedMedia>,
  groupByMonth: Boolean = false,
  withMonthHeader: Boolean = true
) = withContext(Dispatchers.IO) {
  val mappedData = mutableListOf<EncryptedMediaItem>()
  val mappedDataWithMonthly = mutableListOf<EncryptedMediaItem>()
  val monthHeaderList: MutableSet<String> = mutableSetOf()
  val headers = mutableListOf<EncryptedMediaItem.Header>()
  val groupedData = data.groupBy {
    if (groupByMonth) {
      it.timestamp.getMonth()
    } else {
      it.timestamp.getDate(
        stringToday = "Today"
        /** Localized in composition */
        ,
        stringYesterday = "Yesterday"
        /** Localized in composition */
      )
    }
  }
  groupedData.forEach { (date, data) ->
    val dateHeader = EncryptedMediaItem.Header("header_$date", date, data.map { it.id }.toSet())
    headers.add(dateHeader)
    val groupedMedia = data.map {
      EncryptedMediaItem.MediaViewItem("media_${it.id}_${it.label}", it)
    }
    if (groupByMonth) {
      mappedData.add(dateHeader)
      mappedData.addAll(groupedMedia)
      mappedDataWithMonthly.add(dateHeader)
      mappedDataWithMonthly.addAll(groupedMedia)
    } else {
      val month = getMonth(date)
      if (month.isNotEmpty() && !monthHeaderList.contains(month)) {
        monthHeaderList.add(month)
        if (withMonthHeader && mappedDataWithMonthly.isNotEmpty()) {
          mappedDataWithMonthly.add(
            EncryptedMediaItem.Header(
              "header_big_${month}_${data.size}",
              month,
              data.map { it.id }.toSet()
            )
          )
        }
      }
      mappedData.add(dateHeader)
      if (withMonthHeader) {
        mappedDataWithMonthly.add(dateHeader)
      }
      mappedData.addAll(groupedMedia)
      if (withMonthHeader) {
        mappedDataWithMonthly.addAll(groupedMedia)
      }
    }
  }
  EncryptedMediaState(
    isLoading = false,
    media = data,
    headers = headers,
    mappedMedia = mappedData,
    mappedMediaWithMonthly = if (withMonthHeader) mappedDataWithMonthly else emptyList(),
  )
}