package com.itsvks.pictures.screens.components

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.blankj.utilcode.util.PermissionUtils
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.core.Constants.Animation
import com.itsvks.pictures.core.Constants.Target
import com.itsvks.pictures.core.settings.Settings
import com.itsvks.pictures.core.settings.Settings.Misc.rememberLastScreen
import com.itsvks.pictures.core.settings.Settings.Misc.rememberTimelineGroupByMonth
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.screens.albums.AlbumsScreen
import com.itsvks.pictures.screens.favorites.FavoriteScreen
import com.itsvks.pictures.screens.ignored.IgnoredScreen
import com.itsvks.pictures.screens.ignored.setup.IgnoredSetup
import com.itsvks.pictures.screens.library.LibraryScreen
import com.itsvks.pictures.screens.mediaview.MediaViewScreen
import com.itsvks.pictures.screens.settings.SettingsScreen
import com.itsvks.pictures.screens.setup.SetupScreen
import com.itsvks.pictures.screens.timeline.TimelineScreen
import com.itsvks.pictures.screens.trashed.TrashedScreen
import com.itsvks.pictures.screens.vault.VaultScreen
import com.itsvks.pictures.util.lifecycle.OnLifecycleEvent
import com.itsvks.pictures.viewmodel.ChanneledViewModel
import com.itsvks.pictures.viewmodel.MediaViewModel
import com.itsvks.pictures.viewmodel.albums.AlbumsViewModel
import kotlinx.coroutines.Dispatchers

@Composable
fun NavigationComponent(
  modifier: Modifier = Modifier,
  navHostController: NavHostController,
  paddingValues: PaddingValues,
  bottomBarState: MutableState<Boolean>,
  systemBarFollowThemeState: MutableState<Boolean>,
  toggleRotate: () -> Unit,
  isScrolling: MutableState<Boolean>
) {
  val searchBarActive = rememberSaveable { mutableStateOf(false) }
  val bottomNavEntries = rememberNavigationItems()
  val navBackStackEntry by navHostController.currentBackStackEntryAsState()
  val navPipe = hiltViewModel<ChanneledViewModel>()
  navPipe.initWithNav(
    navController = navHostController,
    bottomBarState = bottomBarState
  ).collectAsStateWithLifecycle(LocalLifecycleOwner.current)

  val groupTimelineByMonth by rememberTimelineGroupByMonth()
  var permissionState = rememberSaveable {
    PermissionUtils.isGranted(*Constants.PERMISSIONS.toTypedArray())
  }
  var lastScreen by rememberLastScreen()

  val startDest by rememberSaveable(permissionState, lastScreen) {
    mutableStateOf(
      if (permissionState) {
        lastScreen
      } else Screen.SetupScreen()
    )
  }
  val currentDest = remember(navHostController.currentDestination) {
    navHostController.currentDestination?.route ?: lastScreen
  }

  OnLifecycleEvent { _, event ->
    if (event == Lifecycle.Event.ON_STOP) {
      if (currentDest == Screen.TimelineScreen() || currentDest == Screen.AlbumsScreen()) {
        lastScreen = currentDest
      }
    }
  }

  var lastShouldDisplay by rememberSaveable {
    mutableStateOf(bottomNavEntries.find { it.route == currentDest } != null)
  }
  val shouldSkipAuth = rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(navBackStackEntry) {
    navBackStackEntry?.destination?.route?.let {
      val shouldDisplayBottomBar = bottomNavEntries.find { item -> item.route == it } != null

      if (shouldDisplayBottomBar != lastShouldDisplay) {
        bottomBarState.value = shouldDisplayBottomBar
        lastShouldDisplay = shouldDisplayBottomBar
      }

      if (it != Screen.VaultScreen()) shouldSkipAuth.value = false
      systemBarFollowThemeState.value = !it.contains(Screen.MediaViewScreen.route)
    }
  }

  val albumsViewModel = hiltViewModel<AlbumsViewModel>()
  val albumState = albumsViewModel.albumsFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)

  val mediaViewModel = hiltViewModel<MediaViewModel>()
  mediaViewModel.CollectDatabaseUpdates()

  val timelineState = mediaViewModel.mediaFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)

  LaunchedEffect(groupTimelineByMonth) {
    mediaViewModel.groupByMonth = groupTimelineByMonth
  }

  NavHost(
    navController = navHostController,
    startDestination = startDest,
    enterTransition = { Animation.navigateInAnimation },
    exitTransition = { Animation.navigateUpAnimation },
    popEnterTransition = { Animation.navigateInAnimation },
    popExitTransition = { Animation.navigateUpAnimation },
  ) {
    composable(Screen.SetupScreen()) {
      LaunchedEffect(Unit) {
        navPipe.toggleNavBar(false)
      }

      SetupScreen {
        permissionState = true
        navPipe.navigate(Screen.TimelineScreen())
      }
    }

    composable(Screen.TimelineScreen()) {
      TimelineScreen(
        modifier = modifier,
        paddingValues = paddingValues,
        mediaState = timelineState,
        albumsState = albumState,
        handler = mediaViewModel.handler,
        selectionState = mediaViewModel.multiSelectState,
        selectedMedia = mediaViewModel.selectedPhotoState,
        toggleSelection = mediaViewModel::toggleSelection,
        navigate = navPipe::navigate,
        navigateUp = navPipe::navigateUp,
        toggleNavbar = navPipe::toggleNavBar,
        isScrolling = isScrolling,
        searchBarActive = searchBarActive
      )
    }

    composable(Screen.TrashedScreen()) {
      val vm = hiltViewModel<MediaViewModel>().apply {
        target = Target.TARGET_TRASH
      }
      val trashedMediaState = vm.mediaFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)

      TrashedScreen(
        modifier = modifier,
        paddingValues = paddingValues,
        mediaState = trashedMediaState,
        albumsState = albumState,
        handler = vm.handler,
        selectionState = vm.multiSelectState,
        selectedMedia = vm.selectedPhotoState,
        toggleSelection = vm::toggleSelection,
        navigate = navPipe::navigate,
        navigateUp = navPipe::navigateUp,
        toggleNavBar = navPipe::toggleNavBar
      )
    }

    composable(Screen.FavoriteScreen()) {
      val vm = hiltViewModel<MediaViewModel>().apply {
        target = Target.TARGET_FAVORITES
      }
      val favoritesMediaState = vm.mediaFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)

      FavoriteScreen(
        modifier = modifier,
        paddingValues = paddingValues,
        mediaState = favoritesMediaState,
        albumsState = albumState,
        handler = vm.handler,
        selectionState = vm.multiSelectState,
        selectedMedia = vm.selectedPhotoState,
        toggleFavorite = vm::toggleFavorite,
        toggleSelection = vm::toggleSelection,
        navigate = navPipe::navigate,
        navigateUp = navPipe::navigateUp,
        toggleNavBar = navPipe::toggleNavBar
      )
    }

    composable(Screen.AlbumsScreen()) {
      AlbumsScreen(
        navigate = navPipe::navigate,
        toggleNavbar = navPipe::toggleNavBar,
        mediaState = timelineState,
        albumsState = albumState,
        paddingValues = paddingValues,
        isScrolling = isScrolling,
        searchBarActive = searchBarActive,
        onAlbumClick = albumsViewModel.onAlbumClick(navPipe::navigate),
        onAlbumLongClick = albumsViewModel.onAlbumLongClick,
        filterOptions = albumsViewModel.rememberFilters(),
        onMoveAlbumToTrash = albumsViewModel::moveAlbumToTrash
      )
    }

    composable(
      route = Screen.AlbumViewScreen.albumAndName(),
      arguments = listOf(
        navArgument(name = "albumId") {
          type = NavType.LongType
          defaultValue = -1L
        },
        navArgument(name = "albumName") {
          type = NavType.StringType
          defaultValue = ""
        }
      )
    ) {
      val appName = stringResource(R.string.app_name)
      val argumentAlbumName = remember(it) { it.arguments?.getString("albumName") ?: appName }
      val argumentAlbumId = remember(it) { it.arguments?.getLong("albumId") ?: -1L }

      val vm = hiltViewModel<MediaViewModel>().apply { albumId = argumentAlbumId }
      val hideTimeline by Settings.Album.rememberHideTimelineOnAlbum()
      val mediaState = vm.mediaFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)

      TimelineScreen(
        modifier = modifier,
        paddingValues = paddingValues,
        albumId = argumentAlbumId,
        albumName = argumentAlbumName,
        mediaState = mediaState,
        albumsState = albumState,
        handler = vm.handler,
        selectionState = vm.multiSelectState,
        selectedMedia = vm.selectedPhotoState,
        allowNavBar = false,
        allowHeaders = !hideTimeline,
        enableStickyHeaders = !hideTimeline,
        toggleSelection = vm::toggleSelection,
        navigate = navPipe::navigate,
        navigateUp = navPipe::navigateUp,
        toggleNavbar = navPipe::toggleNavBar,
        isScrolling = isScrolling
      )
    }

    composable(
      route = Screen.MediaViewScreen.idAndAlbum(),
      arguments = listOf(
        navArgument(name = "mediaId") {
          type = NavType.LongType
          defaultValue = -1L
        },
        navArgument(name = "albumId") {
          type = NavType.LongType
          defaultValue = -1L
        }
      )
    ) {
      val mediaId = remember(it) { it.arguments?.getLong("mediaId") ?: -1L }
      val albumId = remember(it) { it.arguments?.getLong("albumId") ?: -1L }

      val entryName = remember(it) {
        if (albumId == -1L) Screen.TimelineScreen.route
        else Screen.AlbumViewScreen.route
      }

      val parentEntry = remember(it) { navHostController.getBackStackEntry(entryName) }

      val vm = hiltViewModel<MediaViewModel>(parentEntry).apply {
        this.albumId = albumId
      }

      val mediaState = if (entryName == Screen.AlbumViewScreen()) {
        vm.mediaFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)
      } else timelineState

      val vaultState = mediaViewModel.vaultsFlow.collectAsStateWithLifecycle(
        context = Dispatchers.IO
      )

      MediaViewScreen(
        modifier = modifier,
        navigateUp = navPipe::navigateUp,
        toggleRotate = toggleRotate,
        paddingValues = paddingValues,
        mediaId = mediaId,
        mediaState = mediaState,
        albumState = albumState,
        vaultState = vaultState,
        addMedia = vm::addMedia,
        handler = vm.handler
      )
    }

    composable(
      route = Screen.MediaViewScreen.idAndTarget(),
      arguments = listOf(
        navArgument(name = "mediaId") {
          type = NavType.LongType
          defaultValue = -1L
        },
        navArgument(name = "target") {
          type = NavType.StringType
          defaultValue = ""
        }
      )
    ) {
      val mediaId = remember(it) { it.arguments?.getLong("mediaId") ?: -1L }
      val target = remember(it) { it.arguments?.getString("target") }

      val entryName = remember(target) {
        when (target) {
          Target.TARGET_TRASH -> Screen.TrashedScreen.route
          Target.TARGET_FAVORITES -> Screen.FavoriteScreen.route
          else -> Screen.TrashedScreen.route
        }
      }

      val parentEntry = remember(it) { navHostController.getBackStackEntry(entryName) }

      val vm = hiltViewModel<MediaViewModel>(parentEntry).apply {
        this.target = target
      }

      val mediaState = vm.mediaFlow.collectAsStateWithLifecycle(context = Dispatchers.IO)

      val vaultState = mediaViewModel.vaultsFlow.collectAsStateWithLifecycle(
        context = Dispatchers.IO
      )

      MediaViewScreen(
        modifier = modifier,
        navigateUp = navPipe::navigateUp,
        toggleRotate = toggleRotate,
        paddingValues = paddingValues,
        mediaId = mediaId,
        target = target,
        mediaState = mediaState,
        albumState = albumState,
        vaultState = vaultState,
        addMedia = vm::addMedia,
        handler = vm.handler
      )
    }

    composable(
      route = Screen.MediaViewScreen.idAndQuery(),
      arguments = listOf(
        navArgument(name = "mediaId") {
          type = NavType.LongType
          defaultValue = -1L
        },
        navArgument(name = "query") {
          type = NavType.BoolType
          defaultValue = true
        }
      )
    ) {
      val mediaId = remember(it) { it.arguments?.getLong("mediaId") ?: -1L }
      val query = remember(it) { it.arguments?.getBoolean("query") ?: true }

      val parentEntry = remember(it) {
        navHostController.getBackStackEntry(Screen.TimelineScreen.route)
      }

      val vm = hiltViewModel<MediaViewModel>(parentEntry)

      val mediaState = remember(query) {
        if (query) vm.searchMediaState else vm.mediaFlow
      }.collectAsStateWithLifecycle(context = Dispatchers.IO)

      val vaultState = mediaViewModel.vaultsFlow.collectAsStateWithLifecycle(
        context = Dispatchers.IO
      )

      MediaViewScreen(
        modifier = modifier,
        navigateUp = navPipe::navigateUp,
        toggleRotate = toggleRotate,
        paddingValues = paddingValues,
        mediaId = mediaId,
        mediaState = mediaState,
        albumState = albumState,
        vaultState = vaultState,
        addMedia = vm::addMedia,
        handler = vm.handler
      )
    }

    composable(Screen.SettingsScreen()) {
      SettingsScreen(
        navigate = navPipe::navigate,
        navigateUp = navPipe::navigateUp
      )
    }

    composable(Screen.IgnoredScreen()) {
      IgnoredScreen(
        navigateUp = navPipe::navigateUp,
        startSetup = { navPipe.navigate(Screen.IgnoredSetupScreen()) },
        albumsState = albumState
      )
    }

    composable(Screen.IgnoredSetupScreen()) {
      IgnoredSetup(
        onCancel = navPipe::navigateUp,
        albumsState = albumState
      )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      composable(Screen.VaultScreen()) {
        VaultScreen(
          modifier = modifier,
          paddingValues = paddingValues,
          toggleRotate = toggleRotate,
          shouldSkipAuth = shouldSkipAuth,
          navigateUp = navPipe::navigateUp
        )
      }
    }

    composable(Screen.LibraryScreen()) {
      LibraryScreen(
        modifier = modifier,
        toggleNavbar = navPipe::toggleNavBar,
        navigate = navPipe::navigate,
        paddingValues = paddingValues,
        isScrolling = isScrolling,
        searchBarActive = searchBarActive
      )
    }
  }
}