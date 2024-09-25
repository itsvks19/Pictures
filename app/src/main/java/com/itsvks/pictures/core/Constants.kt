package com.itsvks.pictures.core

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.grid.GridCells

object Constants {
  const val TAG = "VivekGallery"

  const val WEEKLY_DATE_FORMAT = "EEEE"
  const val DEFAULT_DATE_FORMAT = "EEE, MMMM d"
  const val EXTENDED_DATE_FORMAT = "EEE, MMM d, yyyy"
  const val FULL_DATE_FORMAT = "EEEE, MMMM d, yyyy, hh:mm a"
  const val HEADER_DATE_FORMAT = "MMMM d, yyyy\nhh:mm a"
  const val EXIF_DATE_FORMAT = "MMMM d, yyyy • h:mm a"

  const val DEFAULT_LOW_VELOCITY_SWIPE_DURATION = 150

  const val DEFAULT_NAVIGATION_ANIMATION_DURATION = 300

  const val DEFAULT_TOP_BAR_ANIMATION_DURATION = 1000

  const val MAX_IMAGE_SIZE = 4096

  private val PERMISSION_COMMON = listOf(Manifest.permission.ACCESS_MEDIA_LOCATION)

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private val PERMISSION_T = PERMISSION_COMMON.toMutableList().apply {
    addAll(
      listOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.ACCESS_MEDIA_LOCATION
      )
    )
  }

  private val PERMISSION_OLD = PERMISSION_COMMON.toMutableList().apply {
    addAll(
      listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    )
  }

  val PERMISSIONS = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> PERMISSION_T
    else -> PERMISSION_OLD
  }

  object Animation {
    val enterAnimation = fadeIn(tween(DEFAULT_LOW_VELOCITY_SWIPE_DURATION))
    val exitAnimation = fadeOut(tween(DEFAULT_LOW_VELOCITY_SWIPE_DURATION))

    val navigateInAnimation = fadeIn(tween(DEFAULT_NAVIGATION_ANIMATION_DURATION))
    val navigateUpAnimation = fadeOut(tween(DEFAULT_NAVIGATION_ANIMATION_DURATION))

    fun enterAnimation(durationMillis: Int): EnterTransition = fadeIn(tween(durationMillis))
    fun exitAnimation(durationMillis: Int): ExitTransition = fadeOut(tween(durationMillis))
  }

  object Target {
    const val TARGET_FAVORITES = "favorites"
    const val TARGET_TRASH = "trash"
  }

  val cellsList = listOf(
    GridCells.Fixed(9),
    GridCells.Fixed(8),
    GridCells.Fixed(7),
    GridCells.Fixed(6),
    GridCells.Fixed(5),
    GridCells.Fixed(4),
    GridCells.Fixed(3),
    GridCells.Fixed(2),
    GridCells.Fixed(1)
  )

  val albumCellsList = listOf(
    GridCells.Fixed(7),
    GridCells.Fixed(6),
    GridCells.Fixed(5),
    GridCells.Fixed(4),
    GridCells.Fixed(3),
    GridCells.Fixed(2),
    GridCells.Fixed(1)
  )
}