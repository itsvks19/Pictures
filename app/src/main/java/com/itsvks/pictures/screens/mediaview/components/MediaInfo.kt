package com.itsvks.pictures.screens.mediaview.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MediaInfoRow(
  modifier: Modifier = Modifier,
  label: String,
  content: String,
  trailingContent: @Composable (() -> Unit)? = null,
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null
) {

}