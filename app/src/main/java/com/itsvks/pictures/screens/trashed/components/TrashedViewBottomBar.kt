package com.itsvks.pictures.screens.trashed.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.Media

@Stable
@NonRestartableComposable
@Composable
fun TrashedViewBottomBar(
  modifier: Modifier = Modifier,
  handler: MediaHandleUseCase,
  showUi: Boolean,
  paddingValues: PaddingValues,
  currentMedia: Media?,
  currentIndex: Int,
  onDeleteMedia: (Int) -> Unit
) {

}