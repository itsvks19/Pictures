package com.itsvks.pictures.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.blankj.utilcode.util.AppUtils
import com.itsvks.pictures.R
import com.itsvks.pictures.core.settings.Position
import com.itsvks.pictures.core.settings.Settings
import com.itsvks.pictures.core.settings.Settings.Misc.rememberAutoHideNavBar
import com.itsvks.pictures.core.settings.Settings.Misc.rememberAutoHideSearchBar
import com.itsvks.pictures.core.settings.Settings.Misc.rememberForcedLastScreen
import com.itsvks.pictures.core.settings.Settings.Misc.rememberLastScreen
import com.itsvks.pictures.core.settings.SettingsEntity
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.screens.settings.components.SettingsAppHeader
import com.itsvks.pictures.screens.settings.components.SettingsItem
import com.itsvks.pictures.ui.theme.Shapes
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  navigate: (route: String) -> Unit
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val showLaunchScreenDialog = rememberSaveable { mutableStateOf(false) }
  val settingsList = rememberSettingsList(navigate, showLaunchScreenDialog)

  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      LargeTopAppBar(
        title = {
          Text(text = stringResource(R.string.settings_title))
        },
        navigationIcon = {
          IconButton(onClick = navigateUp) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back_cd)
            )
          }
        },
        scrollBehavior = scrollBehavior
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = paddingValues
    ) {
      item { SettingsAppHeader() }

      items(
        items = settingsList,
        key = { it.title + it.type.toString() }
      ) { SettingsItem(item = it) }
    }

    if (showLaunchScreenDialog.value) {
      BasicAlertDialog(
        onDismissRequest = { showLaunchScreenDialog.value = false },
        properties = DialogProperties(
          decorFitsSystemWindows = false
        )
      ) {
        Column(
          modifier = Modifier
            .background(
              color = MaterialTheme.colorScheme.surfaceContainerLow,
              shape = Shapes.extraLarge
            )
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CompositionLocalProvider(
            value = LocalTextStyle.provides(
              TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onSurface
              )
            )
          ) {
            val scope = rememberCoroutineScope()
            Text(
              text = stringResource(R.string.set_default_launch_screen),
              modifier = Modifier.padding(vertical = 16.dp),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Medium
            )
            Column(
              modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
              var lastScreen by rememberLastScreen()
              var forcedLastScreen by rememberForcedLastScreen()
              ListItem(
                modifier = Modifier
                  .clip(Shapes.large)
                  .clickable {
                    scope.launch {
                      forcedLastScreen = false
                      lastScreen = Screen.TimelineScreen()
                    }
                  },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                  Text(text = stringResource(R.string.use_last_opened_screen))
                },
                trailingContent = {
                  RadioButton(
                    selected = !forcedLastScreen,
                    onClick = {
                      scope.launch {
                        forcedLastScreen = false
                        lastScreen = Screen.TimelineScreen()
                      }
                    }
                  )
                }
              )
              ListItem(
                modifier = Modifier
                  .clip(Shapes.large)
                  .clickable {
                    scope.launch {
                      forcedLastScreen = true
                      lastScreen = Screen.TimelineScreen()
                    }
                  },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                  Text(text = stringResource(R.string.timeline))
                },
                trailingContent = {
                  RadioButton(
                    selected = forcedLastScreen && lastScreen == Screen.TimelineScreen(),
                    onClick = {
                      scope.launch {
                        forcedLastScreen = true
                        lastScreen = Screen.TimelineScreen()
                      }
                    }
                  )
                }
              )
              ListItem(
                modifier = Modifier
                  .clip(Shapes.large)
                  .clickable {
                    scope.launch {
                      forcedLastScreen = true
                      lastScreen = Screen.AlbumsScreen()
                    }
                  },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                  Text(text = stringResource(R.string.albums))
                },
                trailingContent = {
                  RadioButton(
                    selected = forcedLastScreen && lastScreen == Screen.AlbumsScreen(),
                    onClick = {
                      scope.launch {
                        forcedLastScreen = true
                        lastScreen = Screen.AlbumsScreen()
                      }
                    }
                  )
                }
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showLaunchScreenDialog.value = false }) {
              Text(
                text = stringResource(R.string.done),
                color = MaterialTheme.colorScheme.onPrimary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun rememberSettingsList(
  navigate: (route: String) -> Unit,
  showLaunchScreenDialog: MutableState<Boolean>
): SnapshotStateList<SettingsEntity> {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var forcedTheme by Settings.Misc.rememberForceTheme()
  val forcedThemeValuePref = remember(forcedTheme) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.settings_follow_system_theme_title),
      isChecked = !forcedTheme,
      onCheck = {
        forcedTheme = !it
      },
      screenPosition = Position.Top
    )
  }

  var darkMode by Settings.Misc.rememberIsDarkMode()
  val darkModePref = remember(darkMode, forcedTheme) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.settings_dark_mode_title),
      enabled = forcedTheme,
      isChecked = darkMode,
      onCheck = {
        darkMode = it
      },
      screenPosition = Position.Middle
    )
  }

  var amoledMode by Settings.Misc.rememberIsAmoledMode()
  val amoledModePref = remember(amoledMode) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.amoled_mode_title),
      summary = context.getString(R.string.amoled_mode_summary),
      isChecked = amoledMode,
      onCheck = {
        amoledMode = it
      },
      screenPosition = Position.Middle
    )
  }

  var dynamicColor by Settings.Misc.rememberIsDynamicColor()
  val dynamicColorPref = remember(dynamicColor) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.dynamic_colors_title),
      summary = context.getString(R.string.dynamic_colors_summary),
      isChecked = dynamicColor,
      onCheck = {
        dynamicColor = it
      },
      screenPosition = Position.Bottom
    )
  }

  var trashCanEnabled by Settings.Misc.rememberTrashEnabled()
  val trashCanPref = remember(trashCanEnabled) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.settings_trash_title),
      summary = context.getString(R.string.settings_trash_summary),
      isChecked = trashCanEnabled,
      onCheck = {
        trashCanEnabled = it
      },
      screenPosition = Position.Top
    )
  }

  var secureMode by Settings.Misc.rememberSecureMode()
  val secureModePref = remember(secureMode) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.secure_mode_title),
      summary = context.getString(R.string.secure_mode_summary),
      isChecked = secureMode,
      onCheck = {
        secureMode = it
      },
      screenPosition = Position.Middle
    )
  }

  var allowVibrations by Settings.Misc.rememberAllowVibrations()
  val allowVibrationsPref = remember(allowVibrations) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.allow_vibrations),
      summary = context.getString(R.string.allow_vibrations_summary),
      isChecked = allowVibrations,
      onCheck = {
        allowVibrations = it
      },
      screenPosition = Position.Bottom
    )
  }

  var groupByMonth by Settings.Misc.rememberTimelineGroupByMonth()
  val groupByMonthPref = remember(groupByMonth) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.monthly_timeline_title),
      summary = context.getString(R.string.monthly_timeline_summary),
      isChecked = groupByMonth,
      onCheck = {
        scope.launch {
          scope.async { groupByMonth = it }.await()
          delay(50)
          AppUtils.relaunchApp()
        }
      },
      screenPosition = Position.Top
    )
  }

  val shouldAllowBlur = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
  var allowBlur by Settings.Misc.rememberAllowBlur()
  val allowBlurPref = remember(allowBlur) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.fancy_blur),
      summary = context.getString(R.string.fancy_blur_summary),
      isChecked = allowBlur,
      onCheck = { allowBlur = it },
      enabled = shouldAllowBlur,
      screenPosition = Position.Middle
    )
  }

  var showOldNavbar by Settings.Misc.rememberOldNavbar()
  val showOldNavbarPref = remember(showOldNavbar) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.old_navbar),
      summary = context.getString(R.string.old_navbar_summary),
      isChecked = showOldNavbar,
      onCheck = { showOldNavbar = it },
      screenPosition = Position.Top
    )
  }

  var hideTimelineOnAlbum by Settings.Album.rememberHideTimelineOnAlbum()
  val hideTimelineOnAlbumPref = remember(hideTimelineOnAlbum) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.hide_timeline_for_albums),
      summary = context.getString(R.string.hide_timeline_for_album_summary),
      isChecked = hideTimelineOnAlbum,
      onCheck = { hideTimelineOnAlbum = it },
      screenPosition = Position.Middle
    )
  }

  val lastScreen by rememberLastScreen()
  val forcedLastScreen by rememberForcedLastScreen()
  val summary = remember(lastScreen, forcedLastScreen) {
    if (forcedLastScreen) {
      if (lastScreen == Screen.TimelineScreen())
        context.getString(R.string.launch_on_timeline)
      else
        context.getString(R.string.launch_on_albums)
    } else {
      context.getString(R.string.launch_auto)
    }
  }
  val forcedLastScreenPref = remember(forcedLastScreen, lastScreen) {
    SettingsEntity.Preference(
      title = context.getString(R.string.set_default_screen),
      summary = summary,
      onClick = { showLaunchScreenDialog.value = true },
      screenPosition = Position.Bottom
    )
  }

  var autoHideSearchSetting by rememberAutoHideSearchBar()

  val autoHideSearch = remember(autoHideSearchSetting) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.auto_hide_searchbar),
      summary = context.getString(R.string.auto_hide_searchbar_summary),
      isChecked = autoHideSearchSetting,
      onCheck = { autoHideSearchSetting = it },
      screenPosition = Position.Middle
    )
  }

  var autoHideNavigationSetting by rememberAutoHideNavBar()

  val autoHideNavigation = remember(autoHideNavigationSetting) {
    SettingsEntity.SwitchPreference(
      title = context.getString(R.string.auto_hide_navigationbar),
      summary = context.getString(R.string.auto_hide_navigationbar_summary),
      isChecked = autoHideNavigationSetting,
      onCheck = { autoHideNavigationSetting = it },
      screenPosition = Position.Bottom
    )
  }

  return remember(
    arrayOf(
      forcedTheme,
      darkMode,
      trashCanEnabled,
      groupByMonth,
      amoledMode,
      secureMode
    )
  ) {
    mutableStateListOf<SettingsEntity>().apply {

      add(SettingsEntity.Header(title = context.getString(R.string.settings_theme_header)))

      add(forcedThemeValuePref)
      add(darkModePref)
      add(amoledModePref)
      add(dynamicColorPref)

      add(SettingsEntity.Header(title = context.getString(R.string.settings_general)))

      add(trashCanPref)
      add(secureModePref)
      add(allowVibrationsPref)

      add(SettingsEntity.Header(title = context.getString(R.string.customization)))

      add(groupByMonthPref)
      add(allowBlurPref)
      add(hideTimelineOnAlbumPref)
      add(forcedLastScreenPref)

      add(SettingsEntity.Header(title = context.getString(R.string.navigation)))
      add(showOldNavbarPref)
      add(autoHideSearch)
      add(autoHideNavigation)

    }
  }
}
