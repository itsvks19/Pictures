package com.itsvks.pictures.models.filter.adjustment

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

@Immutable
data class AdjustmentFilter(
  val tag: Adjustment,
  val name: String,
  val icon: ImageVector,
  val minValue: Float,
  val maxValue: Float,
  val defaultValue: Float,
  val filter: (Float) -> GPUImageFilter
)

enum class Adjustment {
  CONTRAST,
  BRIGHTNESS,
  SATURATION
}
