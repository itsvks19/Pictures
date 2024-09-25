package com.itsvks.pictures.extensions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

fun Modifier.verticalFadingEdge(percentage: Float) = this.fadingEdge(
  Brush.verticalGradient(
    0f to Color.Transparent,
    percentage to Color.Red,
    1f - percentage to Color.Red,
    1f to Color.Transparent
  )
)

fun Modifier.horizontalFadingEdge(percentage: Float) = this.fadingEdge(
  Brush.horizontalGradient(
    0f to Color.Transparent,
    percentage to Color.Red,
    1f - percentage to Color.Red,
    1f to Color.Transparent
  )
)

fun Modifier.fadingEdge(brush: Brush) = this
  .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
  .drawWithContent {
    drawContent()
    drawRect(brush = brush, blendMode = BlendMode.DstIn)
  }

@Composable
fun Modifier.swipe(
  enabled: Boolean = true,
  onSwipeDown: () -> Unit,
  onSwipeUp: (() -> Unit)?
): Modifier {
  var delta by remember { mutableFloatStateOf(0f) }
  var isDragging by remember { mutableStateOf(false) }
  val feedbackManager = rememberFeedbackManager()
  var isVibrating by remember { mutableStateOf(false) }
  val draggableState = rememberDraggableState {
    delta += if (onSwipeUp != null) it else if (it > 0) it else 0f
    delta = delta.coerceIn(-185f, 400f)
    if (!isVibrating && (delta == 400f || (onSwipeUp != null && delta == -185f))) {
      feedbackManager.vibrate()
      isVibrating = true
    }
  }
  val animatedDelta by animateFloatAsState(
    label = "animatedDelta",
    targetValue = if (isDragging) delta else 0f,
    animationSpec = tween(
      durationMillis = 200
    )
  )
  return this then Modifier
    .draggable(
      enabled = enabled,
      state = draggableState,
      orientation = Orientation.Vertical,
      onDragStarted = {
        isVibrating = false
        isDragging = true
      },
      onDragStopped = {
        isVibrating = false
        isDragging = false
        if (delta == 400f) {
          onSwipeDown()
        }
        if (onSwipeUp != null && delta == -185f) {
          onSwipeUp()
        }
        delta = 0f
      }
    )
    .offset {
      IntOffset(0, if (isDragging) delta.roundToInt() else animatedDelta.roundToInt())
    }
}

fun Modifier.advancedShadow(
  color: Color = Color.Black,
  alpha: Float = 1f,
  cornersRadius: Dp = 0.dp,
  shadowBlurRadius: Dp = 0.dp,
  offsetY: Dp = 0.dp,
  offsetX: Dp = 0.dp
) = drawBehind {
  val shadowColor = color.copy(alpha = alpha).toArgb()
  val transparentColor = color.copy(alpha = 0f).toArgb()

  drawIntoCanvas {
    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.color = transparentColor
    frameworkPaint.setShadowLayer(
      shadowBlurRadius.toPx(),
      offsetX.toPx(),
      offsetY.toPx(),
      shadowColor
    )
    it.drawRoundRect(
      0f,
      0f,
      this.size.width,
      this.size.height,
      cornersRadius.toPx(),
      cornersRadius.toPx(),
      paint
    )
  }
}