package com.itsvks.pictures.screens.trashed

import android.app.Activity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Target.TARGET_TRASH
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.components.media.MediaScreen
import com.itsvks.pictures.screens.trashed.components.AutoDeleteFooter
import com.itsvks.pictures.screens.trashed.components.EmptyTrash
import com.itsvks.pictures.screens.trashed.components.TrashedNavActions

@Composable
fun TrashedScreen(
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues,
  albumName: String = stringResource(R.string.trash),
  handler: MediaHandleUseCase,
  mediaState: State<MediaState>,
  albumsState: State<AlbumState>,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  toggleSelection: (Int) -> Unit,
  navigate: (route: String) -> Unit,
  navigateUp: () -> Unit,
  toggleNavBar: (Boolean) -> Unit
) {
  MediaScreen(
    modifier = modifier,
    paddingValues = paddingValues,
    target = TARGET_TRASH,
    albumName = albumName,
    handler = handler,
    albumsState = albumsState,
    mediaState = mediaState,
    selectionState = selectionState,
    selectedMedia = selectedMedia,
    toggleSelection = toggleSelection,
    allowHeaders = false,
    enableStickyHeaders = false,
    navActionsContent = { _, _ ->
      TrashedNavActions(
        modifier = Modifier,
        handler,
        mediaState.value,
        selectedMedia,
        selectionState
      )
    },
    emptyContent = { EmptyTrash() },
    aboveGridContent = { AutoDeleteFooter() },
    navigate = navigate,
    navigateUp = navigateUp,
    toggleNavbar = toggleNavBar
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      selectedMedia.clear()
      selectionState.value = false
    }
  }
}