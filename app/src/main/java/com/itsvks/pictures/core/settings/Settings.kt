package com.itsvks.pictures.core.settings

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.itsvks.pictures.core.Constants.albumCellsList
import com.itsvks.pictures.core.Constants.cellsList
import com.itsvks.pictures.core.components.filter.FilterKind
import com.itsvks.pictures.extensions.rememberPreference
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.ui.theme.atLeastS
import com.itsvks.pictures.util.OrderType
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

val Context.dataStore by preferencesDataStore(name = Settings.PREFERENCE_NAME)

object Settings {
  const val PREFERENCE_NAME = "settings"

  object Album {
    private val LAST_SORT = stringPreferencesKey("album_last_sort")

    @Serializable
    @Parcelize
    data class LastSort(
      val orderType: OrderType,
      val kind: FilterKind
    ) : Parcelable

    @Composable
    fun rememberLastSort() = rememberPreference(
      key = LAST_SORT,
      defaultValue = LastSort(OrderType.Descending, FilterKind.DATE)
    )

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun rememberAlbumGridSize(): MutableState<Int> {
      val scope = rememberCoroutineScope()
      val context = LocalContext.current
      val prefs = remember(context) {
        context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
      }

      val windowSizeClass = if (context is Activity) calculateWindowSizeClass(context) else null
      val defaultValue = remember(windowSizeClass) {
        albumCellsList.indexOf(
          GridCells.Fixed(
            when (windowSizeClass?.widthSizeClass) {
              WindowWidthSizeClass.Expanded -> 5
              else -> 2
            }
          )
        )
      }

      var storedSize = remember(prefs) {
        prefs.getInt("album_grid_size", defaultValue)
      }

      return remember(storedSize) {
        object : MutableState<Int> {
          override var value: Int
            get() = storedSize
            set(value) {
              scope.launch {
                prefs.edit {
                  putInt("album_grid_size", value)
                  storedSize = value
                }
              }
            }

          override fun component1() = value
          override fun component2(): (Int) -> Unit = { value = it }
        }
      }
    }

    private val HIDE_TIMELINE_ON_ALBUM = booleanPreferencesKey("hide_timeline_on_album")

    @Composable
    fun rememberHideTimelineOnAlbum() = rememberPreference(
      key = HIDE_TIMELINE_ON_ALBUM,
      defaultValue = false
    )
  }

  object Search {
    private val HISTORY = stringSetPreferencesKey("search_history")

    @Composable
    fun rememberSearchHistory() = rememberPreference(key = HISTORY, defaultValue = emptySet())
  }

  object Misc {
    private val USER_CHOICE_MEDIA_MANAGER = booleanPreferencesKey("use_media_manager")

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun rememberIsMediaManager() = rememberPreference(
      key = USER_CHOICE_MEDIA_MANAGER,
      defaultValue = MediaStore.canManageMedia(LocalContext.current)
    )

    private val ENABLE_TRASH = booleanPreferencesKey("enable_trashcan")

    @Composable
    fun rememberTrashEnabled() = rememberPreference(
      key = ENABLE_TRASH,
      defaultValue = true
    )

    fun getTrashEnabled(context: Context) = context.dataStore.data.map {
      it[ENABLE_TRASH] ?: true
    }

    private val LAST_SCREEN = stringPreferencesKey("last_screen")

    @Composable
    fun rememberLastScreen() =
      rememberPreference(key = LAST_SCREEN, defaultValue = Screen.TimelineScreen())

    private val FORCED_LAST_SCREEN = booleanPreferencesKey("forced_last_screen")

    @Composable
    fun rememberForcedLastScreen() =
      rememberPreference(key = FORCED_LAST_SCREEN, defaultValue = false)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun rememberGridSize(): MutableState<Int> {
      val scope = rememberCoroutineScope()
      val context = LocalContext.current
      val prefs = remember(context) {
        context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
      }
      val windowSizeClass = if (context is Activity) calculateWindowSizeClass(context) else null
      val defaultValue = remember(windowSizeClass) {
        cellsList.indexOf(
          GridCells.Fixed(
            when (windowSizeClass?.widthSizeClass) {
              WindowWidthSizeClass.Expanded -> 6
              else -> 4
            }
          )
        )
      }

      var storedSize = remember(prefs) {
        prefs.getInt("media_grid_size", defaultValue)
      }

      return remember(storedSize) {
        object : MutableState<Int> {
          override var value: Int
            get() = storedSize
            set(value) {
              scope.launch {
                prefs.edit {
                  putInt("media_grid_size", value)
                  storedSize = value
                }
              }
            }

          override fun component1() = value
          override fun component2(): (Int) -> Unit = { value = it }
        }
      }
    }

    private val FORCE_THEME = booleanPreferencesKey("force_theme")

    @Composable
    fun rememberForceTheme() = rememberPreference(key = FORCE_THEME, defaultValue = false)

    private val DARK_MODE = booleanPreferencesKey("dark_mode")

    @Composable
    fun rememberIsDarkMode() = rememberPreference(key = DARK_MODE, defaultValue = false)

    private val AMOLED_MODE = booleanPreferencesKey("amoled_mode")

    @Composable
    fun rememberIsAmoledMode() = rememberPreference(key = AMOLED_MODE, defaultValue = false)

    private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")

    @Composable
    fun rememberIsDynamicColor() = rememberPreference(key = DYNAMIC_COLOR, defaultValue = atLeastS)

    private val SECURE_MODE = booleanPreferencesKey("secure_mode")

    @Composable
    fun rememberSecureMode() = rememberPreference(key = SECURE_MODE, defaultValue = false)

    fun getSecureMode(context: Context) = context.dataStore.data.map {
      it[SECURE_MODE] ?: false
    }

    private val TIMELINE_GROUP_BY_MONTH = booleanPreferencesKey("timeline_group_by_month")

    @Composable
    fun rememberTimelineGroupByMonth() =
      rememberPreference(key = TIMELINE_GROUP_BY_MONTH, defaultValue = false)

    private val ALLOW_BLUR = booleanPreferencesKey("allow_blur")

    @Composable
    fun rememberAllowBlur() = rememberPreference(key = ALLOW_BLUR, defaultValue = true)

    private val OLD_NAVBAR = booleanPreferencesKey("old_navbar")

    @Composable
    fun rememberOldNavbar() = rememberPreference(key = OLD_NAVBAR, defaultValue = false)

    private val ALLOW_VIBRATIONS = booleanPreferencesKey("allow_vibrations")

    fun allowVibrations(context: Context) = context.dataStore.data.map {
      it[ALLOW_VIBRATIONS] ?: true
    }

    @Composable
    fun rememberAllowVibrations() = rememberPreference(key = ALLOW_VIBRATIONS, defaultValue = true)

    private val AUTO_HIDE_SEARCHBAR = booleanPreferencesKey("auto_hide_searchbar")

    @Composable
    fun rememberAutoHideSearchBar() = rememberPreference(
      key = AUTO_HIDE_SEARCHBAR,
      defaultValue = true
    )

    private val AUTO_HIDE_NAVIGATION_BAR = booleanPreferencesKey("auto_hide_navigation_bar")

    @Composable
    fun rememberAutoHideNavBar() = rememberPreference(
      key = AUTO_HIDE_NAVIGATION_BAR,
      defaultValue = true
    )
  }
}