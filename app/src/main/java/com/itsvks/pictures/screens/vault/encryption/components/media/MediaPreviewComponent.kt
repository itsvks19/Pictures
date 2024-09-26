package com.itsvks.pictures.screens.vault.encryption.components.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.itsvks.pictures.models.EncryptedMedia
import com.itsvks.pictures.screens.vault.encryption.components.video.VideoPlayer

@Composable
fun MediaPreviewComponent(
  media: EncryptedMedia,
  uiEnabled: Boolean,
  playWhenReady: Boolean,
  onItemClick: () -> Unit,
  onSwipeDown: () -> Unit,
  videoController: @Composable (ExoPlayer, MutableState<Boolean>, MutableLongState, Long, Int, Float) -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    if (media.isVideo) {
      VideoPlayer(
        media = media,
        playWhenReady = playWhenReady,
        videoController = videoController,
        onItemClick = onItemClick
      )
    } else {
      ZoomablePagerImage(
        media = media,
        uiEnabled = uiEnabled,
        onItemClick = onItemClick,
        onSwipeDown = onSwipeDown
      )
    }
  }
}