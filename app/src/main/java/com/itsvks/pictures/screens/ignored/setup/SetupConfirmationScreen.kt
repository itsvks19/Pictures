package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itsvks.pictures.models.Album

@Composable
fun SetupConfirmationScreen(
  modifier: Modifier = Modifier,
  onGoBack: () -> Unit,
  onNext: () -> Unit,
  location: Int,
  type: IgnoredType,
  matchedAlbums: List<Album>
) {

}

@Composable
fun ConfirmationBlock(
  modifier: Modifier = Modifier,
  title: String,
  subtitle: String,
  extra: String? = null
) {

}