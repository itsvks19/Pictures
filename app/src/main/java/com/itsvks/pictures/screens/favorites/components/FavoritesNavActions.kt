package com.itsvks.pictures.screens.favorites.components

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState

@Composable
fun FavoritesNavActions(
  modifier: Modifier = Modifier,
  toggleFavorite: (ActivityResultLauncher<IntentSenderRequest>, List<Media>, Boolean) -> Unit,
  mediaState: MediaState,
  selectedMedia: SnapshotStateList<Media>,
  selectionState: MutableState<Boolean>,
  resultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {

}