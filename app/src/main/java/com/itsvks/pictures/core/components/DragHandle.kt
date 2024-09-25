package com.itsvks.pictures.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DragHandle(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.padding(vertical = 11.dp),
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    shape = MaterialTheme.shapes.extraLarge
  ) {
    Box(Modifier.size(width = 32.dp, height = 4.dp))
  }
}