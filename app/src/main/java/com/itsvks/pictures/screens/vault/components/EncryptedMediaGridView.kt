package com.itsvks.pictures.screens.vault.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dokar.pinchzoomgrid.PinchZoomGridScope
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.core.settings.Settings.Misc.rememberAutoHideSearchBar
import com.itsvks.pictures.extensions.isHeaderKey
import com.itsvks.pictures.extensions.isIgnoredKey
import com.itsvks.pictures.extensions.roundDpToPx
import com.itsvks.pictures.extensions.roundSpToPx
import com.itsvks.pictures.models.EncryptedMedia
import com.itsvks.pictures.models.EncryptedMediaState
import com.itsvks.pictures.screens.components.media.StickyHeaderGrid
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinchZoomGridScope.EncryptedMediaGridView(
  mediaState: State<EncryptedMediaState>,
  paddingValues: PaddingValues = PaddingValues(0.dp),
  searchBarPaddingTop: Dp = 0.dp,
  showSearchBar: Boolean = remember { false },
  allowSelection: Boolean = remember { false },
  selectionState: MutableState<Boolean> = remember { mutableStateOf(false) },
  selectedMedia: SnapshotStateList<EncryptedMedia> = remember { mutableStateListOf() },
  toggleSelection: @DisallowComposableCalls (Int) -> Unit = {},
  canScroll: Boolean = true,
  allowHeaders: Boolean = true,
  enableStickyHeaders: Boolean = false,
  showMonthlyHeader: Boolean = false,
  aboveGridContent: @Composable (() -> Unit)? = null,
  isScrolling: MutableState<Boolean>,
  emptyContent: @Composable () -> Unit,
  onMediaClick: @DisallowComposableCalls (media: EncryptedMedia) -> Unit = {},
) {
  val mappedData by remember(mediaState.value, showMonthlyHeader) {
    derivedStateOf {
      (if (showMonthlyHeader) mediaState.value.mappedMediaWithMonthly
      else mediaState.value.mappedMedia).toMutableStateList()
    }
  }

  LaunchedEffect(showMonthlyHeader, mediaState.value) {
    snapshotFlow { mediaState.value }
      .distinctUntilChanged()
      .collectLatest {
        mappedData.clear()
        mappedData.addAll(
          if (showMonthlyHeader) mediaState.value.mappedMediaWithMonthly
          else mediaState.value.mappedMedia
        )
      }
  }

  BackHandler(
    enabled = selectionState.value && allowSelection,
    onBack = {
      selectionState.value = false
      selectedMedia.clear()
    }
  )

  /**
   * Workaround for a small bug
   * That shows the grid at the bottom after content is loaded
   */
  val lastLoadingState by remember { mutableStateOf(mediaState.value.isLoading) }
  LaunchedEffect(gridState, mediaState.value) {
    snapshotFlow { mediaState.value.isLoading }
      .distinctUntilChanged()
      .collectLatest { isLoading ->
        if (!isLoading && lastLoadingState) {
          gridState.scrollToItem(0)
        }
      }
  }

  AnimatedVisibility(
    visible = enableStickyHeaders
  ) {
      val stickyHeaderItem by rememberEncryptedStickyHeaderItem(
      gridState = gridState,
      headers = remember(mediaState.value) {
        mediaState.value.headers.toMutableStateList()
      },
      mappedData = mappedData
    )

    val hideSearchBarSetting by rememberAutoHideSearchBar()
    val searchBarPadding by animateDpAsState(
      targetValue = remember(
        isScrolling.value,
        showSearchBar,
        searchBarPaddingTop,
        hideSearchBarSetting
      ) {
        if (showSearchBar && (!isScrolling.value || !hideSearchBarSetting)) {
          SearchBarDefaults.InputFieldHeight + searchBarPaddingTop + 8.dp
        } else if (showSearchBar && isScrolling.value) searchBarPaddingTop else 0.dp
      },
      label = "searchBarPadding"
    )

    val density = LocalDensity.current
    val searchBarHeightPx = WindowInsets.statusBars.getTop(density)
    val searchBarPaddingPx by remember(density, searchBarPadding) {
      derivedStateOf { with(density) { searchBarPadding.roundToPx() } }
    }

    StickyHeaderGrid(
      state = gridState,
      modifier = Modifier.fillMaxSize(),
      headerMatcher = { item -> item.key.isHeaderKey || item.key.isIgnoredKey },
      searchBarOffset = { if (showSearchBar) 28.roundSpToPx(density) + searchBarPaddingPx else 0 },
      toolbarOffset = { if (showSearchBar) 0 else 64.roundDpToPx(density) + searchBarHeightPx },
      stickyHeader = {
        val show by remember(
          mediaState.value,
          stickyHeaderItem
        ) {
          derivedStateOf {
            mediaState.value.media.isNotEmpty() && stickyHeaderItem != null
          }
        }
        AnimatedVisibility(
          visible = show,
          enter = enterAnimation,
          exit = exitAnimation
        ) {
          val text by remember(stickyHeaderItem) {
            derivedStateOf { stickyHeaderItem ?: "" }
          }
          Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .background(
                Brush.verticalGradient(
                  listOf(
                    // 3.dp is the elevation the LargeTopAppBar use
                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                      3.dp
                    ),
                    Color.Transparent
                  )
                )
              )
              .padding(horizontal = 16.dp)
              .padding(top = 24.dp + searchBarPadding, bottom = 24.dp)
              .fillMaxWidth()
          )
        }
      }
    ) {
      EncryptedMediaGrid(
        gridState = gridState,
        gridCells = gridCells,
        mediaState = mediaState,
        mappedData = mappedData,
        paddingValues = paddingValues,
        allowSelection = allowSelection,
        selectionState = selectionState,
        selectedMedia = selectedMedia,
        toggleSelection = toggleSelection,
        canScroll = canScroll,
        allowHeaders = allowHeaders,
        aboveGridContent = aboveGridContent,
        isScrolling = isScrolling,
        emptyContent = emptyContent,
        onMediaClick = onMediaClick
      )
    }
  }
  AnimatedVisibility(
    visible = !enableStickyHeaders
  ) {
    EncryptedMediaGrid(
      gridState = gridState,
      gridCells = gridCells,
      mediaState = mediaState,
      mappedData = mappedData,
      paddingValues = paddingValues,
      allowSelection = allowSelection,
      selectionState = selectionState,
      selectedMedia = selectedMedia,
      toggleSelection = toggleSelection,
      canScroll = canScroll,
      allowHeaders = allowHeaders,
      aboveGridContent = aboveGridContent,
      isScrolling = isScrolling,
      emptyContent = emptyContent,
      onMediaClick = onMediaClick
    )
  }

}
