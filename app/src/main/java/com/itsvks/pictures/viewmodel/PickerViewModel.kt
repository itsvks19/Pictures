package com.itsvks.pictures.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsvks.pictures.core.Resource
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.extensions.mapMedia
import com.itsvks.pictures.extensions.mediaFlowWithType
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.AllowedMedia
import com.itsvks.pictures.models.MediaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
open class PickerViewModel @Inject constructor(
  private val repository: MediaRepository
) : ViewModel() {
  var allowedMedia: AllowedMedia = AllowedMedia.BOTH
  var albumId: Long = -1L
    set(value) {
      field = value
      mediaState = lazy {
        repository.mediaFlowWithType(value, allowedMedia)
          .mapMedia(
            albumId = value,
            groupByMonth = false,
            withMonthHeader = false,
            updateDatabase = {})
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MediaState())
      }
    }

  var mediaState = lazy {
    repository.mediaFlowWithType(albumId, allowedMedia)
      .mapMedia(
        albumId = albumId,
        groupByMonth = false,
        withMonthHeader = false,
        updateDatabase = {})
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MediaState())
  }

  val albumsState by lazy {
    repository.getAlbumsWithType(allowedMedia)
      .map { result ->
        val data = result.data ?: emptyList()
        val error = if (result is Resource.Error) result.message
          ?: "An error occurred" else ""
        if (data.isEmpty()) {
          return@map AlbumState(albums = listOf(emptyAlbum), error = error)
        }
        val albums = mutableListOf<Album>().apply {
          add(emptyAlbum)
          addAll(data)
        }
        AlbumState(albums = albums, error = error)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumState())
  }


  private val emptyAlbum = Album(
    id = -1,
    label = "All",
    uri = Uri.EMPTY,
    pathToThumbnail = "",
    timestamp = 0,
    relativePath = ""
  )
}