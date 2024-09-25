package com.itsvks.pictures.screens.trashed.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState

@Composable
fun TrashedNavActions(
  modifier: Modifier = Modifier,
  handler: MediaHandleUseCase,
  mediaState: MediaState,
  selectedMedia: SnapshotStateList<Media>,
  selectionState: MutableState<Boolean>
) {

}