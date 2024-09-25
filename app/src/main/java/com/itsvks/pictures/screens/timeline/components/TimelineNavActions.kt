package com.itsvks.pictures.screens.timeline.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.itsvks.pictures.R
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaState
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.screens.components.media.OptionItem
import com.itsvks.pictures.screens.components.media.OptionSheet
import com.itsvks.pictures.screens.components.rememberAppBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun TimelineNavActions(
  modifier: Modifier = Modifier,
  albumId: Long,
  handler: MediaHandleUseCase,
  expandedDropDown: MutableState<Boolean>,
  mediaState: MediaState,
  selectedMedia: SnapshotStateList<Media>,
  selectionState: MutableState<Boolean>,
  navigate: (route: String) -> Unit,
  navigateUp: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val result = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult(),
    onResult = {
      if (it.resultCode == Activity.RESULT_OK) {
        selectedMedia.clear()
        selectionState.value = false
      }
    }
  )
  val context = LocalContext.current
  val appBottomSheetState = rememberAppBottomSheetState()
  LaunchedEffect(appBottomSheetState.isVisible, expandedDropDown.value) {
    scope.launch {
      if (expandedDropDown.value) appBottomSheetState.show()
      else appBottomSheetState.hide()
    }
  }
  val optionList = remember(selectionState.value) {
    mutableListOf(
      OptionItem(
        text = if (selectionState.value)
          context.getString(R.string.unselect_all)
        else
          context.getString(R.string.select_all),
        onClick = {
          selectionState.value = !selectionState.value
          if (selectionState.value)
            selectedMedia.addAll(mediaState.media)
          else
            selectedMedia.clear()
          expandedDropDown.value = false
        }
      )
    ).apply {
      if (albumId != -1L) {
        add(
          OptionItem(
            text = context.getString(R.string.move_album_to_trash),
            enabled = !selectionState.value,
            onClick = {
              scope.launch {
                handler.trashMedia(
                  result = result,
                  mediaList = mediaState.media,
                  trash = true
                )
                navigateUp()
              }
              expandedDropDown.value = false
            }
          )
        )
      }
      add(
        OptionItem(
          text = context.getString(R.string.favorites),
          enabled = !selectionState.value,
          onClick = {
            navigate(Screen.FavoriteScreen.route)
            expandedDropDown.value = false
          }
        )
      )
      add(
        OptionItem(
          text = context.getString(R.string.trash),
          enabled = !selectionState.value,
          onClick = {
            navigate(Screen.TrashedScreen.route)
            expandedDropDown.value = false
          }
        )
      )
    }
  }
  val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
  val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer
  val settingsOption = remember(selectionState.value) {
    mutableStateListOf(
      OptionItem(
        text = context.getString(R.string.settings_title),
        enabled = !selectionState.value,
        containerColor = tertiaryContainer,
        contentColor = onTertiaryContainer,
        onClick = {
          navigate(Screen.SettingsScreen.route)
          expandedDropDown.value = false
        }
      )
    )
  }
  IconButton(onClick = { expandedDropDown.value = !expandedDropDown.value }) {
    Icon(
      imageVector = Icons.Outlined.MoreVert,
      contentDescription = stringResource(R.string.drop_down_cd)
    )
  }

  OptionSheet(
    modifier = modifier,
    state = appBottomSheetState,
    onDismiss = {
      expandedDropDown.value = false
    },
    optionList = arrayOf(optionList, settingsOption)
  )
}