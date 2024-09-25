package com.itsvks.pictures.screens.ignored

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.itsvks.pictures.models.AlbumState

@Composable
fun IgnoredScreen(
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  startSetup: () -> Unit,
  albumsState: State<AlbumState>
) {

}

@Composable
fun NoIgnoredAlbums(modifier: Modifier = Modifier) {

}