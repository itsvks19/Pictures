package com.itsvks.pictures.viewmodel.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.models.LibraryIndicatorState
import com.itsvks.pictures.util.MediaOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
  repository: MediaRepository,
) : ViewModel() {

  val indicatorState = combine(
    repository.getTrashed(),
    repository.getFavorites(MediaOrder.Default)
  ) { trashed, favorites ->
    LibraryIndicatorState(
      trashCount = trashed.data?.size ?: 0,
      favoriteCount = favorites.data?.size ?: 0
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LibraryIndicatorState())
}