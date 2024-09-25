package com.itsvks.pictures.core.components.search

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class SearchBarElevation(val dp: Dp) {

  data object Collapsed : SearchBarElevation(2.dp)
  data object Expanded : SearchBarElevation(0.dp)

  operator fun invoke() = dp
}