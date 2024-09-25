package com.itsvks.pictures.screens.albums

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dokar.pinchzoomgrid.PinchZoomGridLayout
import com.dokar.pinchzoomgrid.rememberPinchZoomGridState
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.core.Constants.albumCellsList
import com.itsvks.pictures.core.components.filter.FilterButton
import com.itsvks.pictures.core.components.filter.FilterKind
import com.itsvks.pictures.core.components.filter.FilterOption
import com.itsvks.pictures.core.components.search.MainSearchBar
import com.itsvks.pictures.core.settings.Settings.Album.rememberAlbumGridSize
import com.itsvks.pictures.core.settings.Settings.Album.rememberLastSort
import com.itsvks.pictures.extensions.rememberActivityResult
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.screens.albums.components.AlbumComponent
import com.itsvks.pictures.screens.albums.components.CarouselPinnedAlbums
import com.itsvks.pictures.screens.components.Error
import com.itsvks.pictures.util.MediaOrder

@Composable
fun AlbumsScreen(
  navigate: (route: String) -> Unit,
  toggleNavbar: (Boolean) -> Unit,
  mediaState: State<MediaState>,
  albumsState: State<AlbumState>,
  paddingValues: PaddingValues,
  filterOptions: SnapshotStateList<FilterOption>,
  isScrolling: MutableState<Boolean>,
  searchBarActive: MutableState<Boolean>,
  onAlbumClick: (Album) -> Unit,
  onAlbumLongClick: (Album) -> Unit,
  onMoveAlbumToTrash: (ActivityResultLauncher<IntentSenderRequest>, Album) -> Unit
) {
  var lastCellIndex by rememberAlbumGridSize()

  val pinchState = rememberPinchZoomGridState(
    cellsList = albumCellsList,
    initialCellsIndex = lastCellIndex
  )

  LaunchedEffect(pinchState.isZooming) {
    lastCellIndex = albumCellsList.indexOf(pinchState.currentCells)
  }

  val lastSort by rememberLastSort()
  LaunchedEffect(lastSort) {
    val selectedFilter = filterOptions.first { it.filterKind == lastSort.kind }
    selectedFilter.onClick(
      when (selectedFilter.filterKind) {
        FilterKind.DATE -> MediaOrder.Date(lastSort.orderType)
        FilterKind.NAME -> MediaOrder.Label(lastSort.orderType)
      }
    )
  }

  var finalPaddingValues by remember(paddingValues) { mutableStateOf(paddingValues) }

  Scaffold(
    topBar = {
      MainSearchBar(
        bottomPadding = paddingValues.calculateBottomPadding(),
        navigate = navigate,
        toggleNavbar = toggleNavbar,
        isScrolling = isScrolling,
        activeState = searchBarActive
      ) {
        IconButton(onClick = { navigate(Screen.SettingsScreen.route) }) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.settings_title)
          )
        }
      }
    }
  ) { innerPaddingValues ->
    LaunchedEffect(innerPaddingValues) {
      finalPaddingValues = PaddingValues(
        top = innerPaddingValues.calculateTopPadding(),
        bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
      )
    }
    PinchZoomGridLayout(state = pinchState) {
      LaunchedEffect(gridState.isScrollInProgress) {
        isScrolling.value = gridState.isScrollInProgress
      }
      LazyVerticalGrid(
        state = gridState,
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .fillMaxSize(),
        columns = gridCells,
        contentPadding = finalPaddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item(
          span = { GridItemSpan(maxLineSpan) },
          key = "pinnedAlbums"
        ) {
          AnimatedVisibility(
            visible = albumsState.value.albumsPinned.isNotEmpty(),
            enter = enterAnimation,
            exit = exitAnimation
          ) {
            Column {
              Text(
                modifier = Modifier
                  .pinchItem(key = "pinnedAlbums")
                  .padding(horizontal = 8.dp)
                  .padding(vertical = 24.dp),
                text = stringResource(R.string.pinned_albums_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
              )
              CarouselPinnedAlbums(
                albumList = albumsState.value.albumsPinned,
                onAlbumClick = onAlbumClick,
                onAlbumLongClick = onAlbumLongClick
              )
            }
          }
        }
        item(
          span = { GridItemSpan(maxLineSpan) },
          key = "filterButton"
        ) {
          AnimatedVisibility(
            visible = albumsState.value.albumsUnpinned.isNotEmpty(),
            enter = enterAnimation,
            exit = exitAnimation
          ) {
            FilterButton(
              modifier = Modifier.pinchItem(key = "filterButton"),
              filterOptions = filterOptions.toTypedArray()
            )
          }
        }
        items(
          items = albumsState.value.albumsUnpinned,
          key = { item -> item.toString() }
        ) { item ->
          val trashResult = rememberActivityResult()
          AlbumComponent(
            modifier = Modifier.pinchItem(key = item.toString()),
            album = item,
            onItemClick = onAlbumClick,
            onTogglePinClick = onAlbumLongClick,
            onMoveAlbumToTrash = {
              onMoveAlbumToTrash(trashResult, it)
            }
          )
        }

        item(
          span = { GridItemSpan(maxLineSpan) },
          key = "albumDetails"
        ) {
          AnimatedVisibility(
            visible = mediaState.value.media.isNotEmpty() && albumsState.value.albums.isNotEmpty(),
            enter = enterAnimation,
            exit = exitAnimation
          ) {
            Text(
              modifier = Modifier
                .fillMaxWidth()
                .pinchItem(key = "albumDetails")
                .padding(horizontal = 8.dp)
                .padding(vertical = 24.dp),
              text = stringResource(
                R.string.images_videos,
                mediaState.value.media.size
              ),
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center
            )
          }
        }

        item(
          span = { GridItemSpan(maxLineSpan) },
          key = "emptyAlbums"
        ) {
          AnimatedVisibility(
            visible = albumsState.value.albums.isEmpty() && albumsState.value.error.isEmpty(),
            enter = enterAnimation,
            exit = exitAnimation
          ) {
            EmptyAlbum()
          }
        }

        item(
          span = { GridItemSpan(maxLineSpan) },
          key = "loadingAlbums"
        ) {
          AnimatedVisibility(
            visible = albumsState.value.isLoading,
            enter = enterAnimation,
            exit = exitAnimation
          ) {
            LoadingAlbum()
          }
        }
      }
    }
  }
  /** Error State Handling Block **/
  AnimatedVisibility(
    visible = albumsState.value.error.isNotEmpty(),
    enter = enterAnimation,
    exit = exitAnimation
  ) {
    Error(errorMessage = albumsState.value.error)
  }
  /** ************ **/
}