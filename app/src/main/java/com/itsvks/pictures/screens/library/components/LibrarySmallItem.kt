package com.itsvks.pictures.screens.library.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun LibrarySmallItem(
  modifier: Modifier = Modifier,
  title: String,
  icon: ImageVector,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  useIndicator: Boolean = false,
  indicatorCounter: Int = 0,
  contentDescription: String = title
) {

}