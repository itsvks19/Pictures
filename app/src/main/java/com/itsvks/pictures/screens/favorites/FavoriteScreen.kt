package com.itsvks.pictures.screens.favorites

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Target.TARGET_FAVORITES
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.components.media.MediaScreen
import com.itsvks.pictures.screens.favorites.components.EmptyFavorites
import com.itsvks.pictures.screens.favorites.components.FavoriteNavActions

@Composable
fun FavoriteScreen(
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues,
  albumName: String = stringResource(R.string.favorites),
  handler: MediaHandleUseCase,
  mediaState: State<MediaState>,
  albumsState: State<AlbumState>,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  toggleFavorite: (ActivityResultLauncher<IntentSenderRequest>, List<Media>, Boolean) -> Unit,
  toggleSelection: (Int) -> Unit,
  navigate: (route: String) -> Unit,
  navigateUp: () -> Unit,
  toggleNavBar: (Boolean) -> Unit
) {
  MediaScreen(
    modifier = modifier,
    paddingValues = paddingValues,
    target = TARGET_FAVORITES,
    albumName = albumName,
    handler = handler,
    albumsState = albumsState,
    mediaState = mediaState,
    selectionState = selectionState,
    selectedMedia = selectedMedia,
    toggleSelection = toggleSelection,
    navActionsContent = { _, result ->
      FavoriteNavActions(toggleFavorite, mediaState.value, selectedMedia, selectionState, result)
    },
    emptyContent = { EmptyFavorites() },
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