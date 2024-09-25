package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SetupLocationScreen(
  modifier: Modifier = Modifier,
  onGoBack: () -> Unit,
  onNext: () -> Unit,
  isError: Boolean,
  initialLocation: Int,
  initialType: IgnoredType,
  onLocationChanged: (Int) -> Unit,
  onTypeChanged: (IgnoredType) -> Unit
) {

}