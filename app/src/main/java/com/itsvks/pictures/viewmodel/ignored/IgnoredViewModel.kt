package com.itsvks.pictures.viewmodel.ignored

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.models.IgnoredAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IgnoredViewModel @Inject constructor(
  private val repository: MediaRepository
) : ViewModel() {

  val blacklistState = repository.getBlacklistedAlbums()
    .map { IgnoredState(it) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), IgnoredState())

  fun removeFromBlacklist(ignoredAlbum: IgnoredAlbum) {
    viewModelScope.launch {
      repository.removeBlacklistedAlbum(ignoredAlbum)
    }
  }
}