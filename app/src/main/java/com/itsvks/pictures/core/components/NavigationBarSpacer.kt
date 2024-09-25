package com.itsvks.pictures.core.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itsvks.pictures.extensions.getNavigationBarHeight

@Composable
fun NavigationBarSpacer() {
  Spacer(
    modifier = Modifier
      .fillMaxWidth()
      .height(getNavigationBarHeight())
  )
}