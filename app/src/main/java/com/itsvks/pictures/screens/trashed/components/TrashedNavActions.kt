package com.itsvks.pictures.screens.trashed.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.extensions.rememberIsMediaManager
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.components.rememberAppBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun TrashedNavActions(
  modifier: Modifier = Modifier,
  handler: MediaHandleUseCase,
  mediaState: MediaState,
  selectedMedia: SnapshotStateList<Media>,
  selectionState: MutableState<Boolean>
) {
  val scope = rememberCoroutineScope()
  val isMediaManager = rememberIsMediaManager()
  val deleteSheetState = rememberAppBottomSheetState()
  val restoreSheetState = rememberAppBottomSheetState()
  val result = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult(),
    onResult = {
      if (it.resultCode == Activity.RESULT_OK) {
        selectedMedia.clear()
        selectionState.value = false
        if (isMediaManager) {
          scope.launch {
            deleteSheetState.hide()
            restoreSheetState.hide()
          }
        }
      }
    }
  )
  AnimatedVisibility(
    modifier = modifier,
    visible = mediaState.media.isNotEmpty(),
    enter = enterAnimation,
    exit = exitAnimation
  ) {
    Row {
      TextButton(
        onClick = {
          scope.launch {
            if (isMediaManager) {
              restoreSheetState.show()
            } else {
              handler.trashMedia(
                result,
                selectedMedia.ifEmpty { mediaState.media },
                false
              )
            }
          }
        }
      ) {
        Text(
          text = stringResource(R.string.trash_restore),
          color = MaterialTheme.colorScheme.primary
        )
      }
      if (selectionState.value) {
        TextButton(
          onClick = {
            scope.launch {
              if (isMediaManager) {
                deleteSheetState.show()
              } else {
                handler.deleteMedia(result, selectedMedia)
              }
            }
          }
        ) {
          Text(
            text = stringResource(R.string.trash_delete),
            color = MaterialTheme.colorScheme.primary
          )
        }
      } else {
        TextButton(
          onClick = {
            scope.launch {
              if (isMediaManager) {
                deleteSheetState.show()
              } else {
                handler.deleteMedia(result, mediaState.media)
              }
            }
          }
        ) {
          Text(
            text = stringResource(R.string.trash_empty),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
  TrashDialog(
    appBottomSheetState = deleteSheetState,
    data = selectedMedia.ifEmpty { mediaState.media },
    action = TrashDialogAction.DELETE
  ) {
    handler.deleteMedia(result, it)
  }
  TrashDialog(
    appBottomSheetState = restoreSheetState,
    data = selectedMedia.ifEmpty { mediaState.media },
    action = TrashDialogAction.RESTORE
  ) {
    handler.trashMedia(result, it, false)
  }
}