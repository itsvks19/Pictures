package com.itsvks.pictures.screens.mediaview.components.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.itsvks.pictures.extensions.isVideo
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.screens.mediaview.components.video.VideoPlayer

@Stable
@NonRestartableComposable
@Composable
fun MediaPreviewComponent(
  modifier: Modifier = Modifier,
  media: Media,
  uiEnabled: Boolean,
  playWhenReady: Boolean,
  onItemClick: () -> Unit,
  onSwipeDown: () -> Unit,
  videoController: @Composable (
    ExoPlayer,
    MutableState<Boolean>,
    MutableLongState,
    Long,
    Int,
    Float
  ) -> Unit
) {
  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    AnimatedVisibility(
      modifier = Modifier.fillMaxSize(),
      visible = media.isVideo,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      VideoPlayer(
        media = media,
        playWhenReady = playWhenReady,
        videoController = videoController,
        onItemClick = onItemClick,
        onSwipeDown = onSwipeDown
      )
    }

    AnimatedVisibility(
      visible = !media.isVideo,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      ZoomablePagerImage(
        media = media,
        uiEnabled = uiEnabled,
        onItemClick = onItemClick,
        onSwipeDown = onSwipeDown
      )
    }
  }
}