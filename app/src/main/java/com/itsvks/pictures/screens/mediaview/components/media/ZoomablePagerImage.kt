package com.itsvks.pictures.screens.mediaview.components.media

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.util.Size
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.itsvks.pictures.core.Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION
import com.itsvks.pictures.core.bettery.LocalBatteryStatus
import com.itsvks.pictures.core.bettery.ProvideBatteryStatus
import com.itsvks.pictures.core.settings.Settings
import com.itsvks.pictures.extensions.swipe
import com.itsvks.pictures.models.Media
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
@NonRestartableComposable
@Composable
fun ZoomablePagerImage(
  modifier: Modifier = Modifier,
  media: Media,
  uiEnabled: Boolean,
  onItemClick: () -> Unit,
  onSwipeDown: () -> Unit
) {
  ProvideBatteryStatus {
    val allowBlur by Settings.Misc.rememberAllowBlur()
    val isPowerSavingMode = LocalBatteryStatus.current.isPowerSavingMode
    AnimatedVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && allowBlur && !isPowerSavingMode) {
      val blurAlpha by animateFloatAsState(
        animationSpec = tween(DEFAULT_TOP_BAR_ANIMATION_DURATION),
        targetValue = if (uiEnabled) 0.7f else 0f,
        label = "blurAlpha"
      )
      AsyncImage(
        modifier = Modifier
          .fillMaxSize()
          .alpha(blurAlpha)
          .blur(100.dp),
        request = ComposableImageRequest(media.uri.toString()) {
          size(Size.parseSize("600x600"))
        },
        contentDescription = null,
        filterQuality = FilterQuality.None,
        contentScale = ContentScale.Crop
      )
    }
  }
  val zoomState = rememberSketchZoomState()
  val scope = rememberCoroutineScope()
  LaunchedEffect(LocalConfiguration.current) {
    scope.launch {
      delay(100)
      zoomState.zoomable.reset("alignmentChanged")
    }
  }

  SketchZoomAsyncImage(
    zoomState = zoomState,
    modifier = modifier
      .fillMaxSize()
      .swipe(
        onSwipeDown = onSwipeDown,
        onSwipeUp = null
      ),
    onTap = { onItemClick() },
    alignment = Alignment.Center,
    uri = media.uri.toString(),
    contentDescription = media.label
  )
}