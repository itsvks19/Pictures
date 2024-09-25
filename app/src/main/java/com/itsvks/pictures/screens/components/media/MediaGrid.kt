package com.itsvks.pictures.screens.components.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dokar.pinchzoomgrid.PinchZoomGridScope
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.core.components.MediaItemHeader
import com.itsvks.pictures.extensions.isBigHeaderKey
import com.itsvks.pictures.extensions.isHeaderKey
import com.itsvks.pictures.extensions.isImage
import com.itsvks.pictures.extensions.rememberFeedbackManager
import com.itsvks.pictures.extensions.update
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaItem
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.components.Error
import com.itsvks.pictures.screens.media.LoadingMedia
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PinchZoomGridScope.MediaGrid(
  gridState: LazyGridState,
  gridCells: GridCells,
  mediaState: State<MediaState>,
  mappedMedia: SnapshotStateList<MediaItem>,
  paddingValues: PaddingValues,
  allowSelection: Boolean,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  toggleSelection: @DisallowComposableCalls (Int) -> Unit,
  canScroll: Boolean,
  allowHeaders: Boolean,
  aboveGridContent: @Composable (() -> Unit)?,
  isScrolling: MutableState<Boolean>,
  emptyContent: @Composable () -> Unit,
  onMediaClick: @DisallowComposableCalls (media: Media) -> Unit
) {
  LaunchedEffect(gridState.isScrollInProgress) {
    snapshotFlow {
      gridState.isScrollInProgress
    }.collectLatest {
      isScrolling.value = it
    }
  }

  val topContent: LazyGridScope.() -> Unit = remember(aboveGridContent) {
    {
      if (aboveGridContent != null) {
        item(
          span = { GridItemSpan(maxLineSpan) },
          key = "aboveGrid"
        ) {
          aboveGridContent.invoke()
        }
      }
    }
  }
  val bottomContent: LazyGridScope.() -> Unit = remember {
    {
      item(
        span = { GridItemSpan(maxLineSpan) },
        key = "loading"
      ) {
        AnimatedVisibility(
          visible = mediaState.value.isLoading,
          enter = enterAnimation,
          exit = exitAnimation
        ) {
          LoadingMedia()
        }
      }

      item(
        span = { GridItemSpan(maxLineSpan) },
        key = "empty"
      ) {
        AnimatedVisibility(
          visible = mediaState.value.media.isEmpty() && !mediaState.value.isLoading,
          enter = enterAnimation,
          exit = exitAnimation
        ) {
          emptyContent()
        }
      }
      item(
        span = { GridItemSpan(maxLineSpan) },
        key = "error"
      ) {
        AnimatedVisibility(visible = mediaState.value.error.isNotEmpty()) {
          Error(errorMessage = mediaState.value.error)
        }
      }
    }
  }

  AnimatedVisibility(
    visible = allowHeaders
  ) {
    MediaGridContentWithHeaders(
      mediaState = mediaState,
      mappedMedia = mappedMedia,
      paddingValues = paddingValues,
      allowSelection = allowSelection,
      selectionState = selectionState,
      selectedMedia = selectedMedia,
      toggleSelection = toggleSelection,
      canScroll = canScroll,
      onMediaClick = onMediaClick,
      topContent = topContent,
      bottomContent = bottomContent
    )
  }

  AnimatedVisibility(
    visible = !allowHeaders
  ) {
    MediaGridContent(
      mediaState = mediaState,
      paddingValues = paddingValues,
      allowSelection = allowSelection,
      selectionState = selectionState,
      selectedMedia = selectedMedia,
      toggleSelection = toggleSelection,
      canScroll = canScroll,
      onMediaClick = onMediaClick,
      topContent = topContent,
      bottomContent = bottomContent
    )
  }
}

@Composable
private fun PinchZoomGridScope.MediaGridContentWithHeaders(
  mediaState: State<MediaState>,
  mappedMedia: SnapshotStateList<MediaItem>,
  paddingValues: PaddingValues,
  allowSelection: Boolean,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  toggleSelection: @DisallowComposableCalls (Int) -> Unit,
  canScroll: Boolean,
  onMediaClick: @DisallowComposableCalls (media: Media) -> Unit,
  topContent: LazyGridScope.() -> Unit,
  bottomContent: LazyGridScope.() -> Unit
) {
  val scope = rememberCoroutineScope()
  val stringToday = stringResource(id = R.string.header_today)
  val stringYesterday = stringResource(id = R.string.header_yesterday)
  val feedbackManager = rememberFeedbackManager()
  TimelineScroller(
    modifier = Modifier
      .padding(paddingValues)
      .padding(top = 32.dp)
      .padding(vertical = 32.dp),
    mappedData = mappedMedia,
    headers = remember(mediaState.value) {
      mediaState.value.headers.toMutableStateList()
    },
    state = gridState,
  ) {
    LazyVerticalGrid(
      state = gridState,
      modifier = Modifier.fillMaxSize(),
      columns = gridCells,
      contentPadding = paddingValues,
      userScrollEnabled = canScroll,
      horizontalArrangement = Arrangement.spacedBy(1.dp),
      verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      topContent()

      items(
        items = mappedMedia,
        key = { item -> item.key },
        contentType = { item -> item.key.startsWith("media_") },
        span = { item ->
          GridItemSpan(if (item.key.isHeaderKey) maxLineSpan else 1)
        }
      ) { it ->
        if (it is MediaItem.Header) {
          val isChecked = rememberSaveable { mutableStateOf(false) }
          if (allowSelection) {
            LaunchedEffect(selectionState.value) {
              // Uncheck if selectionState is set to false
              isChecked.value = isChecked.value && selectionState.value
            }
            LaunchedEffect(selectedMedia.size) {
              // Partial check of media items should not check the header
              isChecked.value = selectedMedia.map { it.id }.containsAll(it.data)
            }
          }
          MediaItemHeader(
            modifier = Modifier
              .animateItem(
                fadeInSpec = null
              )
              .pinchItem(key = it.key),
            date = remember {
              it.text
                .replace("Today", stringToday)
                .replace("Yesterday", stringYesterday)
            },
            showAsBig = remember { it.key.isBigHeaderKey },
            isCheckVisible = selectionState,
            isChecked = isChecked
          ) {
            if (allowSelection) {
              feedbackManager.vibrate()
              scope.launch {
                isChecked.value = !isChecked.value
                if (isChecked.value) {
                  val toAdd = it.data.toMutableList().apply {
                    // Avoid media from being added twice to selection
                    removeIf {
                      selectedMedia.map { media -> media.id }.contains(it)
                    }
                  }
                  selectedMedia.addAll(mediaState.value.media.filter {
                    toAdd.contains(
                      it.id
                    )
                  })
                } else selectedMedia.removeAll { media -> it.data.contains(media.id) }
                selectionState.update(selectedMedia.isNotEmpty())
              }
            }
          }
        } else if (it is MediaItem.MediaViewItem) {
          MediaImage(
            modifier = Modifier
              .animateItem(
                fadeInSpec = null
              )
              .pinchItem(key = it.key),
            media = it.media,
            selectionState = selectionState,
            selectedMedia = selectedMedia,
            canClick = canScroll,
            onItemClick = {
              if (selectionState.value && allowSelection) {
                feedbackManager.vibrate()
                toggleSelection(mediaState.value.media.indexOf(it))
              } else onMediaClick(it)
            }
          ) {
            if (allowSelection) {
              feedbackManager.vibrate()
              toggleSelection(mediaState.value.media.indexOf(it))
            }
          }
        }
      }


      bottomContent()
    }
  }
}

@Composable
private fun PinchZoomGridScope.MediaGridContent(
  mediaState: State<MediaState>,
  paddingValues: PaddingValues,
  allowSelection: Boolean,
  selectionState: MutableState<Boolean>,
  selectedMedia: SnapshotStateList<Media>,
  toggleSelection: @DisallowComposableCalls (Int) -> Unit,
  canScroll: Boolean,
  onMediaClick: @DisallowComposableCalls (media: Media) -> Unit,
  topContent: LazyGridScope.() -> Unit,
  bottomContent: LazyGridScope.() -> Unit
) {
  val feedbackManager = rememberFeedbackManager()
  LazyVerticalGrid(
    state = gridState,
    modifier = Modifier.fillMaxSize(),
    columns = gridCells,
    contentPadding = paddingValues,
    userScrollEnabled = canScroll,
    horizontalArrangement = Arrangement.spacedBy(1.dp),
    verticalArrangement = Arrangement.spacedBy(1.dp)
  ) {
    topContent()

    itemsIndexed(
      items = mediaState.value.media,
      key = { _, item -> item.toString() },
      contentType = { _, item -> item.isImage }
    ) { index, media ->
      MediaImage(
        modifier = Modifier
          .animateItem(
            fadeInSpec = null
          )
          .pinchItem(key = media.toString()),
        media = media,
        selectionState = selectionState,
        selectedMedia = selectedMedia,
        canClick = canScroll,
        onItemClick = {
          if (selectionState.value && allowSelection) {
            feedbackManager.vibrate()
            toggleSelection(index)
          } else onMediaClick(it)
        },
        onItemLongClick = {
          if (allowSelection) {
            feedbackManager.vibrate()
            toggleSelection(index)
          }
        }
      )
    }

    bottomContent()
  }
}