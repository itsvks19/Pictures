package com.itsvks.pictures.screens.favorites.components

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState

@Composable
fun FavoriteNavActions(
  toggleFavorite: (ActivityResultLauncher<IntentSenderRequest>, List<Media>, Boolean) -> Unit,
  mediaState: MediaState,
  selectedMedia: SnapshotStateList<Media>,
  selectionState: MutableState<Boolean>,
  result: ActivityResultLauncher<IntentSenderRequest>
) {
  val removeAllTitle = stringResource(R.string.remove_all)
  val removeSelectedTitle = stringResource(R.string.remove_selected)
  val title = if (selectionState.value) removeSelectedTitle else removeAllTitle
  AnimatedVisibility(
    visible = mediaState.media.isNotEmpty(),
    enter = enterAnimation,
    exit = exitAnimation
  ) {
    TextButton(
      onClick = {
        toggleFavorite(result, selectedMedia.ifEmpty { mediaState.media }, false)
      }
    ) {
      Text(
        text = title,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}