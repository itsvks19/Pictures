package com.itsvks.pictures.models

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.itsvks.pictures.models.filter.adjustment.AdjustmentFilter

@Immutable
data class ImageModification(
  val croppedImage: Bitmap? = null,
  val filter: ImageFilter? = null,
  val adjustment: Pair<AdjustmentFilter, Float>? = null
)
