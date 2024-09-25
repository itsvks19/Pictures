package com.itsvks.pictures.viewmodel.albums

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Resource
import com.itsvks.pictures.core.components.filter.FilterKind
import com.itsvks.pictures.core.components.filter.FilterOption
import com.itsvks.pictures.core.settings.Settings
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.IgnoredAlbum
import com.itsvks.pictures.models.PinnedAlbum
import com.itsvks.pictures.models.TimelineSettings
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.util.MediaOrder
import com.itsvks.pictures.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
  private val repository: MediaRepository,
  val handler: MediaHandleUseCase
) : ViewModel() {
  fun onAlbumClick(navigate: (String) -> Unit): (Album) -> Unit = { album ->
    navigate(Screen.AlbumViewScreen.route + "?albumId=${album.id}&albumName=${album.label}")
  }

  val onAlbumLongClick: (Album) -> Unit = { album ->
    toggleAlbumPin(album, !album.isPinned)
  }

  fun moveAlbumToTrash(result: ActivityResultLauncher<IntentSenderRequest>, album: Album) {
    viewModelScope.launch(Dispatchers.IO) {
      val response = repository.getMediaByAlbumId(album.id).firstOrNull()
      val data = response?.data ?: emptyList()
      repository.trashMedia(result, data, true)
    }
  }

  @Composable
  fun rememberFilters(): SnapshotStateList<FilterOption> {
    val lastValue by Settings.Album.rememberLastSort()
    return remember(lastValue) {
      mutableStateListOf(
        FilterOption(
          titleRes = R.string.filter_type_date,
          filterKind = FilterKind.DATE,
          onClick = { albumOrder = it }
        ),
        FilterOption(
          titleRes = R.string.filter_type_name,
          filterKind = FilterKind.NAME,
          onClick = { albumOrder = it }
        )
      )
    }
  }

  private fun toggleAlbumPin(album: Album, isPinned: Boolean = true) {
    viewModelScope.launch(Dispatchers.IO) {
      if (isPinned) {
        repository.insertPinnedAlbum(PinnedAlbum(album.id))
      } else {
        repository.removePinnedAlbum(PinnedAlbum(album.id))
      }
    }
  }

  private val settingsFlow = repository.getSettings()
    .stateIn(viewModelScope, started = SharingStarted.Eagerly, TimelineSettings())

  private val pinnedAlbums = repository.getPinnedAlbums()
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val blacklistedAlbums = repository.getBlacklistedAlbums()
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


  private var albumOrder: MediaOrder
    get() = settingsFlow.value?.albumMediaOrder ?: MediaOrder.Date(OrderType.Descending)
    set(value) {
      viewModelScope.launch(Dispatchers.IO) {
        settingsFlow.value?.copy(albumMediaOrder = value)?.let {
          repository.updateSettings(it)
        }
      }
    }

  val albumsFlow = combine(
    repository.getAlbums(mediaOrder = albumOrder),
    pinnedAlbums,
    blacklistedAlbums,
    settingsFlow
  ) { result, pinnedAlbums, blacklistedAlbums, settings ->
    val newOrder = settings?.albumMediaOrder ?: albumOrder
    val data = newOrder.sortAlbums(result.data ?: emptyList())
    val cleanData = data.removeBlacklisted(blacklistedAlbums).mapPinned(pinnedAlbums)

    AlbumState(
      albums = cleanData,
      albumsWithBlacklisted = data,
      albumsUnpinned = cleanData.filter { !it.isPinned },
      albumsPinned = cleanData.filter { it.isPinned }.sortedBy { it.label },
      isLoading = false,
      error = if (result is Resource.Error) result.message ?: "An error occurred" else ""
    )
  }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), AlbumState())

  private fun List<Album>.mapPinned(pinnedAlbums: List<PinnedAlbum>): List<Album> =
    map { album -> album.copy(isPinned = pinnedAlbums.any { it.id == album.id }) }

  private fun List<Album>.removeBlacklisted(blacklistedAlbums: List<IgnoredAlbum>): List<Album> =
    toMutableList().apply {
      removeAll { album -> blacklistedAlbums.any { it.matchesAlbum(album) } }
    }
}