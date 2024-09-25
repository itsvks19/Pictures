package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itsvks.pictures.models.IgnoredAlbum

@Composable
fun SetupTypeRegexScreen(
  modifier: Modifier = Modifier,
  onGoBack: () -> Unit,
  onNext: () -> Unit,
  initialRegex: String,
  ignoredAlbums: List<IgnoredAlbum>,
  onRegexChanged: (String) -> Unit
) {

}