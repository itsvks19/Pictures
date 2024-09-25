package com.itsvks.pictures.viewmodel

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsvks.pictures.core.Resource
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.extensions.collectMedia
import com.itsvks.pictures.extensions.mapMediaToItem
import com.itsvks.pictures.extensions.mediaFlow
import com.itsvks.pictures.extensions.update
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.models.TimelineSettings
import com.itsvks.pictures.models.Vault
import com.itsvks.pictures.models.VaultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
  private val repository: MediaRepository,
  val handler: MediaHandleUseCase
) : ViewModel() {

  var lastQuery = mutableStateOf("")
  val multiSelectState = mutableStateOf(false)
  private val _searchMediaState = MutableStateFlow(MediaState())
  val searchMediaState = _searchMediaState.asStateFlow()
  val selectedPhotoState = mutableStateListOf<Media>()

  var albumId: Long = -1L
  var target: String? = null

  var groupByMonth: Boolean
    get() = settingsFlow.value?.groupTimelineByMonth ?: false
    set(value) {
      viewModelScope.launch(Dispatchers.IO) {
        settingsFlow.value?.copy(groupTimelineByMonth = value)?.let {
          repository.updateSettings(it)
        }
      }
    }

  private val settingsFlow = repository.getSettings().stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = TimelineSettings()
  )

  private val blacklistedAlbums = repository.getBlacklistedAlbums().stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = emptyList()
  )

  val mediaFlow by lazy {
    combine(
      repository.mediaFlow(albumId, target),
      settingsFlow,
      blacklistedAlbums,
    ) { result, settings, blacklistedAlbums ->
      if (result is Resource.Error) return@combine MediaState(
        error = result.message ?: "",
        isLoading = false
      )
      updateDatabase()
      mapMediaToItem(
        data = (result.data ?: emptyList()).toMutableList().apply {
          removeAll { media -> blacklistedAlbums.any { it.matchesMedia(media) } }
        },
        error = result.message ?: "",
        albumId = albumId,
        groupByMonth = settings?.groupTimelineByMonth ?: false,
      )
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), MediaState())
  }

  val vaultsFlow = repository.getVaults()
    .map { it.data ?: emptyList() }
    .map { VaultState(it, isLoading = false) }
    .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), VaultState())

  private sealed class Event {
    data object UpdateDatabase : Event()
  }

  private val updater = Channel<Event>()

  @Composable
  fun CollectDatabaseUpdates() {
    LaunchedEffect(Unit) {
      viewModelScope.launch(Dispatchers.IO) {
        updater.receiveAsFlow().collectLatest {
          when (it) {
            is Event.UpdateDatabase -> {
              repository.updateInternalDatabase()
            }
          }
        }
      }
    }
  }

  private fun updateDatabase() {
    viewModelScope.launch(Dispatchers.IO) {
      updater.send(Event.UpdateDatabase)
    }
  }

  fun addMedia(vault: Vault, media: Media) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.addMedia(vault, media)
    }
  }

  fun clearQuery() {
    queryMedia("")
  }


  fun toggleFavorite(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>,
    favorite: Boolean = false
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      handler.toggleFavorite(result, mediaList, favorite)
    }
  }

  fun toggleSelection(index: Int) {
    viewModelScope.launch(Dispatchers.IO) {
      val item = mediaFlow.value.media[index]
      val selectedPhoto = selectedPhotoState.find { it.id == item.id }
      if (selectedPhoto != null) {
        selectedPhotoState.remove(selectedPhoto)
      } else {
        selectedPhotoState.add(item)
      }
      multiSelectState.update(selectedPhotoState.isNotEmpty())
    }
  }

  fun queryMedia(query: String) {
    viewModelScope.launch(Dispatchers.IO) {
      withContext(Dispatchers.Main) {
        lastQuery.value = query
      }
      if (query.isEmpty()) {
        _searchMediaState.tryEmit(MediaState(isLoading = false))
        return@launch
      } else {
        _searchMediaState.tryEmit(MediaState(isLoading = true))
        _searchMediaState.collectMedia(
          data = mediaFlow.value.media.parseQuery(query),
          error = mediaFlow.value.error,
          albumId = albumId,
          groupByMonth = groupByMonth
        )
      }
    }
  }

  private suspend fun List<Media>.parseQuery(query: String): List<Media> {
    return withContext(Dispatchers.IO) {
      if (query.isEmpty()) return@withContext emptyList()
      val matches = FuzzySearch.extractSorted(query, this@parseQuery, { it.toString() }, 60)
      return@withContext matches.map { it.referent }.ifEmpty { emptyList() }
    }
  }
}