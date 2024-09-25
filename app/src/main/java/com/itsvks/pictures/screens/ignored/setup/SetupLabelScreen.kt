package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SetupLabelScreen(
  modifier: Modifier = Modifier,
  onGoBack: () -> Unit,
  onNext: () -> Unit,
  isError: Boolean,
  initialLabel: String,
  onLabelChanged: (String) -> Unit
) {

}