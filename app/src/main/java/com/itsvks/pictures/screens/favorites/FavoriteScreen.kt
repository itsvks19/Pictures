package com.itsvks.pictures.screens.favorites

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
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState

@Composable
fun FavoriteScreen(
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues,
  albumName: String = stringResource(R.string.trash),
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

}