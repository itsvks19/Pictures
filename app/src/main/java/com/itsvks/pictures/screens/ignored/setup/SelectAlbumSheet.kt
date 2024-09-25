package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.IgnoredAlbum
import com.itsvks.pictures.screens.components.AppBottomSheetState

@Composable
fun SelectAlbumSheet(
  modifier: Modifier = Modifier,
  sheetState: AppBottomSheetState,
  ignoredAlbums: List<IgnoredAlbum>,
  albumState: AlbumState,
  onSelect: (Album) -> Unit
) {

}