package com.itsvks.pictures.screens.setup

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.itsvks.pictures.BuildConfig
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.core.settings.Settings.Misc.rememberIsMediaManager
import com.itsvks.pictures.extensions.RepeatOnResume
import com.itsvks.pictures.extensions.isManageFilesAllowed
import com.itsvks.pictures.extensions.launchManageFiles
import com.itsvks.pictures.extensions.launchManageMedia
import com.itsvks.pictures.screens.components.SetupWizard
import com.itsvks.pictures.screens.components.media.OptionItem
import com.itsvks.pictures.screens.components.media.OptionLayout
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(
  modifier: Modifier = Modifier,
  onPermissionGranted: () -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  var firstLaunch by remember { mutableStateOf(true) }
  var permissionGranted by remember { mutableStateOf(false) }

  val mediaPermissions = rememberMultiplePermissionsState(Constants.PERMISSIONS) { permissions ->
    firstLaunch = false
    permissionGranted = permissions.all { it.value }
  }

  val appName = "${stringResource(R.string.app_name)} v${BuildConfig.VERSION_NAME}"

  LaunchedEffect(permissionGranted) {
    if (permissionGranted) onPermissionGranted()
    else if (!firstLaunch) Toast.makeText(
      context,
      context.getString(R.string.some_permissions_are_not_granted),
      Toast.LENGTH_LONG
    ).show()
  }

  SetupWizard(
    modifier = modifier,
    painter = painterResource(R.drawable.play_store_512),
    title = stringResource(R.string.welcome),
    subtitle = appName,
    contentPadding = 0.dp,
    bottomBar = {
      OutlinedButton(
        onClick = { (context as Activity).finish() }
      ) {
        Text(text = stringResource(R.string.action_cancel))
      }

      Button(
        onClick = {
          scope.launch {
            mediaPermissions.launchMultiplePermissionRequest()
          }
        }
      ) {
        Text(text = stringResource(R.string.get_started))
      }
    }
  ) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
        .padding(horizontal = 16.dp),
      text = stringResource(R.string.required)
    )

    OptionLayout(
      modifier = Modifier.fillMaxWidth(),
      optionList = context.requiredPermissionsList.map { (title, summary) ->
        OptionItem(
          text = title,
          summary = summary,
          enabled = true,
          onClick = { }
        )
      }
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      var useMediaManager by rememberIsMediaManager()
      var isStorageManager by remember { mutableStateOf(Environment.isExternalStorageManager()) }

      RepeatOnResume {
        isStorageManager = Environment.isExternalStorageManager()
        useMediaManager = MediaStore.canManageMedia(context)
      }

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        text = stringResource(R.string.optional)
      )

      val grantedString = stringResource(R.string.granted)
      val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
      val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer

      val optionsList = remember(useMediaManager, isStorageManager) {
        listOf(
          OptionItem(
            text = context.getString(R.string.permission_manage_media_title),
            summary = if (!useMediaManager) context.getString(R.string.permission_manage_media_summary) else grantedString,
            enabled = !useMediaManager,
            onClick = {
              scope.launch {
                context.launchManageMedia()
              }
            },
            containerColor = secondaryContainer,
            contentColor = onSecondaryContainer
          ),
          OptionItem(
            text = context.getString(R.string.permission_manage_files_title),
            summary = if (!isStorageManager && isManageFilesAllowed) context.getString(R.string.permission_manage_files_summary) else grantedString,
            enabled = !isStorageManager && isManageFilesAllowed,
            onClick = {
              scope.launch {
                context.launchManageFiles()
              }
            },

            containerColor = secondaryContainer,
            contentColor = onSecondaryContainer
          )
        )
      }

      OptionLayout(
        modifier = Modifier.fillMaxWidth(),
        optionList = optionsList
      )
    }
  }
}

private val Context.requiredPermissionsList: Array<Pair<String, String>>
  get() {
    return arrayOf(
      getString(R.string.read_media_images) to getString(R.string.read_media_images_summary),
      getString(R.string.read_media_videos) to getString(R.string.read_media_videos_summary),
      getString(R.string.access_media_location) to getString(R.string.access_media_location_summary),
      getString(R.string.internet) to getString(R.string.internet_summary)
    )
  }