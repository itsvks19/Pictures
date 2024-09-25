package com.itsvks.pictures.screens.components.media

import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.dokar.pinchzoomgrid.PinchZoomGridLayout
import com.dokar.pinchzoomgrid.rememberPinchZoomGridState
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.core.Constants.cellsList
import com.itsvks.pictures.core.components.NavigationActions
import com.itsvks.pictures.core.components.NavigationButton
import com.itsvks.pictures.core.components.SelectionSheet
import com.itsvks.pictures.core.components.search.MainSearchBar
import com.itsvks.pictures.core.settings.Settings.Misc.rememberGridSize
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.screens.components.TwoLinedDateToolbarTitle
import com.itsvks.pictures.screens.media.EmptyMedia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues,
  albumId: Long = remember { -1L },
  target: String? = remember { null },
  albumName: String,
  handler: MediaHandleUseCase,
  albumsState: State<AlbumState>,
  mediaState: State<MediaState>,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  toggleSelection: (Int) -> Unit,
  allowHeaders: Boolean = true,
  showMonthlyHeader: Boolean = false,
  enableStickyHeaders: Boolean = true,
  allowNavBar: Boolean = false,
  navActionsContent: @Composable (RowScope.(expandedDropDown: MutableState<Boolean>, result: ActivityResultLauncher<IntentSenderRequest>) -> Unit),
  emptyContent: @Composable () -> Unit = { EmptyMedia() },
  aboveGridContent: @Composable (() -> Unit)? = remember { null },
  navigate: (route: String) -> Unit,
  navigateUp: () -> Unit,
  toggleNavbar: (Boolean) -> Unit,
  isScrolling: MutableState<Boolean> = remember { mutableStateOf(false) },
  searchBarActive: MutableState<Boolean> = remember { mutableStateOf(false) },
  onActivityResult: (result: ActivityResult) -> Unit,
) {
  val showSearchBar = remember { albumId == -1L && target == null }
  var canScroll by rememberSaveable { mutableStateOf(true) }
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
    state = rememberTopAppBarState(),
    canScroll = { canScroll },
    flingAnimationSpec = null
  )
  var lastCellIndex by rememberGridSize()

  val pinchState = rememberPinchZoomGridState(
    cellsList = cellsList,
    initialCellsIndex = lastCellIndex
  )

  LaunchedEffect(pinchState.isZooming) {
    canScroll = !pinchState.isZooming
    lastCellIndex = cellsList.indexOf(pinchState.currentCells)
  }

  LaunchedEffect(selectionState.value) {
    if (allowNavBar) {
      toggleNavbar(!selectionState.value)
    }
  }

  Box(
    modifier = modifier
  ) {
    Scaffold(
      modifier = Modifier.then(
        if (!showSearchBar) {
          Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else Modifier
      ),
      topBar = {
        if (!showSearchBar) {
          LargeTopAppBar(
            title = {
              TwoLinedDateToolbarTitle(
                albumName = albumName,
                dateHeader = mediaState.value.dateHeader
              )
            },
            navigationIcon = {
              NavigationButton(
                albumId = albumId,
                target = target,
                navigateUp = navigateUp,
                clearSelection = {
                  selectionState.value = false
                  selectedMedia.clear()
                },
                selectionState = selectionState,
                alwaysGoBack = true,
              )
            },
            actions = {
              NavigationActions(
                actions = navActionsContent,
                onActivityResult = onActivityResult
              )
            },
            scrollBehavior = scrollBehavior
          )
        } else {
          MainSearchBar(
            bottomPadding = paddingValues.calculateBottomPadding(),
            navigate = navigate,
            toggleNavbar = toggleNavbar,
            selectionState = remember(selectedMedia) {
              if (selectedMedia.isNotEmpty()) selectionState else null
            },
            isScrolling = isScrolling,
            activeState = searchBarActive
          ) {
            NavigationActions(
              actions = navActionsContent,
              onActivityResult = onActivityResult
            )
          }
        }
      }
    ) {
      PinchZoomGridLayout(state = pinchState) {
        MediaGridView(
          mediaState = mediaState,
          allowSelection = true,
          showSearchBar = showSearchBar,
          searchBarPaddingTop = remember(paddingValues) {
            paddingValues.calculateTopPadding()
          },
          enableStickyHeaders = enableStickyHeaders,
          paddingValues = remember(paddingValues, it) {
            PaddingValues(
              top = it.calculateTopPadding(),
              bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
            )
          },
          canScroll = canScroll,
          selectionState = selectionState,
          selectedMedia = selectedMedia,
          allowHeaders = allowHeaders,
          showMonthlyHeader = showMonthlyHeader,
          toggleSelection = toggleSelection,
          aboveGridContent = aboveGridContent,
          isScrolling = isScrolling,
          emptyContent = emptyContent
        ) {
          val albumRoute = "albumId=$albumId"
          val targetRoute = "target=$target"
          val param = if (target != null) targetRoute else albumRoute
          navigate(Screen.MediaViewScreen.route + "?mediaId=${it.id}&$param")
        }
      }
    }

    if (target != Constants.Target.TARGET_TRASH) {
      SelectionSheet(
        modifier = Modifier.align(Alignment.BottomEnd),
        target = target,
        selectedMedia = selectedMedia,
        selectionState = selectionState,
        albumsState = albumsState,
        handler = handler
      )
    }
  }
}