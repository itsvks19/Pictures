package com.itsvks.pictures.screens.timeline

import android.app.Activity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.components.media.MediaScreen
import com.itsvks.pictures.screens.timeline.components.TimelineNavActions

@Composable
fun TimelineScreen(
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues,
  albumId: Long = -1L,
  albumName: String = stringResource(R.string.app_name),
  handler: MediaHandleUseCase,
  mediaState: State<MediaState>,
  albumsState: State<AlbumState>,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  allowNavBar: Boolean = true,
  allowHeaders: Boolean = true,
  enableStickyHeaders: Boolean = true,
  toggleSelection: (Int) -> Unit,
  navigate: (route: String) -> Unit,
  navigateUp: () -> Unit,
  toggleNavbar: (Boolean) -> Unit,
  isScrolling: MutableState<Boolean>,
  searchBarActive: MutableState<Boolean> = mutableStateOf(false)
) {
  MediaScreen(
    modifier = modifier,
    paddingValues = paddingValues,
    albumId = albumId,
    target = null,
    albumName = albumName,
    handler = handler,
    albumsState = albumsState,
    mediaState = mediaState,
    selectionState = selectionState,
    selectedMedia = selectedMedia,
    toggleSelection = toggleSelection,
    allowHeaders = allowHeaders,
    showMonthlyHeader = true,
    enableStickyHeaders = enableStickyHeaders,
    allowNavBar = allowNavBar,
    navActionsContent = { expandedDropDown: MutableState<Boolean>, _ ->
      TimelineNavActions(
        albumId = albumId,
        handler = handler,
        expandedDropDown = expandedDropDown,
        mediaState = mediaState.value,
        selectedMedia = selectedMedia,
        selectionState = selectionState,
        navigate = navigate,
        navigateUp = navigateUp
      )
    },
    navigate = navigate,
    navigateUp = navigateUp,
    toggleNavbar = toggleNavbar,
    isScrolling = isScrolling,
    searchBarActive = searchBarActive
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      selectedMedia.clear()
      selectionState.value = false
    }
  }
}