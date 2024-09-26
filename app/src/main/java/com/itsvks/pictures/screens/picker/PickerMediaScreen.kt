package com.itsvks.pictures.screens.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itsvks.pictures.R
import com.itsvks.pictures.core.components.MediaItemHeader
import com.itsvks.pictures.extensions.isHeaderKey
import com.itsvks.pictures.extensions.rememberFeedbackManager
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaItem
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.components.media.MediaImage
import com.itsvks.pictures.ui.theme.Dimens
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun PickerMediaScreen(
  mediaState: StateFlow<MediaState>,
  selectedMedia: SnapshotStateList<Media>,
  allowSelection: Boolean,
) {
  val scope = rememberCoroutineScope()
  val stringToday = stringResource(id = R.string.header_today)
  val stringYesterday = stringResource(id = R.string.header_yesterday)
  val state by mediaState.collectAsStateWithLifecycle()
  val gridState = rememberLazyGridState()
  val isCheckVisible = rememberSaveable { mutableStateOf(allowSelection) }
  val feedbackManager = rememberFeedbackManager()

  LazyVerticalGrid(
    state = gridState,
    modifier = Modifier.fillMaxSize(),
    columns = GridCells.Adaptive(Dimens.Photo()),
    horizontalArrangement = Arrangement.spacedBy(1.dp),
    verticalArrangement = Arrangement.spacedBy(1.dp),
  ) {
    items(
      items = state.mappedMedia,
      key = { if (it is MediaItem.MediaViewItem) it.media.toString() else it.key },
      contentType = { it.key.startsWith("media_") },
      span = { item ->
        GridItemSpan(if (item.key.isHeaderKey) maxLineSpan else 1)
      }
    ) { item ->
      when (item) {
        is MediaItem.Header -> {
          val isChecked = rememberSaveable { mutableStateOf(false) }
          if (allowSelection) {
            LaunchedEffect(selectedMedia.size) {
              // Partial check of media items should not check the header
              isChecked.value = selectedMedia.map { it.id }.containsAll(item.data)
            }
          }
          val title = item.text
            .replace("Today", stringToday)
            .replace("Yesterday", stringYesterday)
          MediaItemHeader(
            date = title,
            showAsBig = item.key.contains("big"),
            isCheckVisible = isCheckVisible,
            isChecked = isChecked
          ) {
            if (allowSelection) {
              feedbackManager.vibrate()
              scope.launch {
                isChecked.value = !isChecked.value
                if (isChecked.value) {
                  val toAdd = item.data.toMutableList().apply {
                    // Avoid media from being added twice to selection
                    removeIf { selectedMedia.map { it.id }.contains(it) }
                  }
                  selectedMedia.addAll(mediaState.value.media.filter { toAdd.contains(it.id) })
                } else selectedMedia.removeAll { item.data.contains(it.id) }
              }
            }
          }
        }

        is MediaItem.MediaViewItem -> {
          val selectionState = remember { mutableStateOf(true) }
          MediaImage(
            modifier = Modifier.animateItem(),
            media = item.media,
            selectionState = selectionState,
            selectedMedia = selectedMedia,
            canClick = true,
            onItemClick = {
              feedbackManager.vibrate()
              if (allowSelection) {
                if (selectedMedia.contains(it)) selectedMedia.remove(it)
                else selectedMedia.add(it)
              } else if (!selectedMedia.contains(it) && selectedMedia.size == 1) {
                selectedMedia[0] = it
              } else if (selectedMedia.isEmpty()) {
                selectedMedia.add(it)
              } else {
                selectedMedia.remove(it)
              }
            }
          ) {
            feedbackManager.vibrate()
            if (allowSelection) {
              if (selectedMedia.contains(it)) selectedMedia.remove(it)
              else selectedMedia.add(it)
            } else if (!selectedMedia.contains(it) && selectedMedia.size == 1) {
              selectedMedia[0] = it
            } else if (selectedMedia.isEmpty()) {
              selectedMedia.add(it)
            } else {
              selectedMedia.remove(it)
            }
          }
        }
      }
    }
  }
}