package com.itsvks.pictures.screens.vault.encryption.components.media

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.fetch.newBase64Uri
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.itsvks.pictures.core.Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION
import com.itsvks.pictures.core.bettery.LocalBatteryStatus
import com.itsvks.pictures.core.bettery.ProvideBatteryStatus
import com.itsvks.pictures.core.settings.Settings.Misc.rememberAllowBlur
import com.itsvks.pictures.extensions.swipe
import com.itsvks.pictures.models.EncryptedMedia

@Composable
fun ZoomablePagerImage(
  modifier: Modifier = Modifier,
  media: EncryptedMedia,
  uiEnabled: Boolean,
  onItemClick: () -> Unit,
  onSwipeDown: () -> Unit
) {
  val painter = rememberAsyncImagePainter(
    request = ComposableImageRequest(
      remember(media) {
        newBase64Uri(
          mimeType = media.mimeType,
          imageData = media.bytes
        )
      }
    ) {
      memoryCachePolicy(CachePolicy.ENABLED)
      crossfade()
    },
    contentScale = ContentScale.Fit,
    filterQuality = FilterQuality.None,
  )

  Box(modifier = Modifier.fillMaxSize()) {
    ProvideBatteryStatus {
      val allowBlur by rememberAllowBlur()
      val isPowerSavingMode = LocalBatteryStatus.current.isPowerSavingMode
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && allowBlur && !isPowerSavingMode) {
        val blurAlpha by animateFloatAsState(
          animationSpec = tween(DEFAULT_TOP_BAR_ANIMATION_DURATION),
          targetValue = if (uiEnabled) 0.7f else 0f,
          label = "blurAlpha"
        )
        Image(
          modifier = Modifier
            .fillMaxSize()
            .alpha(blurAlpha)
            .blur(100.dp),
          painter = painter,
          contentDescription = null,
          contentScale = ContentScale.Crop
        )
      }
    }

    SketchZoomAsyncImage(
      modifier = modifier
        .fillMaxSize()
        .swipe(
          onSwipeUp = null,
          onSwipeDown = onSwipeDown
        ),
      onTap = { onItemClick() },
      uri = remember(media) {
        newBase64Uri(mimeType = media.mimeType, imageData = media.bytes)
      },
      contentScale = ContentScale.Fit,
      contentDescription = media.label
    )
  }
}
