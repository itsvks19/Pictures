package com.itsvks.pictures.screens.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun LibraryScreen(
  modifier: Modifier = Modifier,
  navigate: (route: String) -> Unit,
  toggleNavbar: (Boolean) -> Unit,
  paddingValues: PaddingValues,
  isScrolling: MutableState<Boolean>,
  searchBarActive: MutableState<Boolean>
) {

}