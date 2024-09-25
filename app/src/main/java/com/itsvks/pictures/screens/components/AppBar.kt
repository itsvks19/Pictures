package com.itsvks.pictures.screens.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.core.settings.Settings.Misc.rememberAutoHideNavBar
import com.itsvks.pictures.core.settings.Settings.Misc.rememberOldNavbar
import com.itsvks.pictures.models.navigation.NavigationItem
import com.itsvks.pictures.screens.Screen
import com.itsvks.pictures.ui.icon.Albums
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun rememberNavigationItems(): List<NavigationItem> {
  val timelineTitle = stringResource(R.string.nav_timeline)
  val albumsTitle = stringResource(R.string.nav_albums)
  val libraryTitle = stringResource(R.string.library)
  return remember {
    mutableListOf(
      NavigationItem(
        name = timelineTitle,
        route = Screen.TimelineScreen.route,
        icon = Icons.Outlined.Photo,
      ),
      NavigationItem(
        name = albumsTitle,
        route = Screen.AlbumsScreen.route,
        icon = com.itsvks.pictures.ui.Icons.Albums,
      ),
      NavigationItem(
        name = libraryTitle,
        route = Screen.LibraryScreen(),
        icon = Icons.Outlined.PhotoLibrary
      )
    )
  }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Stable
@Composable
fun AppBarContainer(
  navController: NavController,
  bottomBarState: MutableState<Boolean>,
  paddingValues: PaddingValues,
  isScrolling: MutableState<Boolean>,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val windowSizeClass = calculateWindowSizeClass(context as Activity)
  val backStackEntry by navController.currentBackStackEntryAsState()
  val bottomNavItems = rememberNavigationItems()
  val useNavRail by remember(windowSizeClass) {
    mutableStateOf(windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact)
  }
  val useOldNavbar by rememberOldNavbar()

  @Suppress("NAME_SHADOWING")
  val bottomBarState by bottomBarState

  AnimatedVisibility(
    visible = useOldNavbar,
    enter = enterAnimation,
    exit = exitAnimation
  ) {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      val showNavRail = remember(useNavRail, bottomBarState) { useNavRail && bottomBarState }

      AnimatedVisibility(
        visible = showNavRail,
        enter = slideInHorizontally { it * -2 },
        exit = slideOutHorizontally { it * -2 }
      ) {
        ClassicNavigationRail(
          backStackEntry = backStackEntry,
          navigationItems = bottomNavItems,
          onClick = { navigate(navController, it) }
        )
      }

      val animatedPadding by animateDpAsState(
        targetValue = remember(useNavRail, bottomBarState) {
          if (useNavRail && bottomBarState) 80.dp else 0.dp
        },
        label = "animatedPadding"
      )
      Box(
        modifier = Modifier.padding(start = animatedPadding)
      ) {
        content()
      }

      val hideNavBarSetting by rememberAutoHideNavBar()
      var showClassicNavbar by remember {
        mutableStateOf(!useNavRail && bottomBarState && (!isScrolling.value || !hideNavBarSetting))
      }
      LaunchedEffect(useNavRail, isScrolling, bottomBarState, hideNavBarSetting) {
        snapshotFlow {
          !useNavRail && bottomBarState && (!isScrolling.value || !hideNavBarSetting)
        }.distinctUntilChanged().collectLatest {
          showClassicNavbar = it
        }
      }

      AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = showClassicNavbar,
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 }
      ) {
        ClassicNavBar(
          backStackEntry = backStackEntry,
          navigationItems = bottomNavItems,
          onClick = { navigate(navController, it) },
        )
      }
    }
  }

  AnimatedVisibility(
    visible = !useOldNavbar,
    enter = enterAnimation,
    exit = exitAnimation
  ) {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      content()
      val hideNavBarSetting by rememberAutoHideNavBar()
      var showNavbar by remember(bottomBarState, isScrolling, hideNavBarSetting) {
        mutableStateOf(bottomBarState && (!isScrolling.value || !hideNavBarSetting))
      }
      LaunchedEffect(bottomBarState, isScrolling, hideNavBarSetting) {
        snapshotFlow {
          bottomBarState && (!isScrolling.value || !hideNavBarSetting)
        }.distinctUntilChanged().collectLatest {
          showNavbar = it
        }
      }

      AnimatedVisibility(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(bottom = paddingValues.calculateBottomPadding()),
        visible = showNavbar,
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 }
      ) {
        val modifier = remember(useNavRail) {
          if (useNavRail) Modifier.requiredWidth((110 * bottomNavItems.size).dp)
          else Modifier.fillMaxWidth()
        }

        GalleryNavBar(
          modifier = modifier,
          backStackEntry = backStackEntry,
          navigationItems = bottomNavItems,
          onClick = { navigate(navController, it) }
        )
      }
    }
  }
}

@Composable
fun GalleryNavBar(
  modifier: Modifier,
  backStackEntry: NavBackStackEntry?,
  navigationItems: List<NavigationItem>,
  onClick: (route: String) -> Unit,
) {
  Row(
    modifier = Modifier
      .padding(horizontal = 32.dp, vertical = 32.dp)
      .then(modifier)
      .height(64.dp)
      .background(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        shape = RoundedCornerShape(percent = 100)
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    navigationItems.forEach { item ->
      val selected = remember(item, backStackEntry) {
        item.route == backStackEntry?.destination?.route
      }
      GalleryNavBarItem(
        navItem = item,
        isSelected = selected,
        onClick = onClick
      )
    }
  }
}

@Stable
@Composable
private fun Label(item: NavigationItem) = Text(
  text = item.name,
  fontWeight = FontWeight.Medium,
  style = MaterialTheme.typography.bodyMedium,
)

@Stable
@Composable
private fun Icon(item: NavigationItem) = Icon(
  imageVector = item.icon,
  contentDescription = item.name,
)

@Composable
fun ClassicNavBar(
  backStackEntry: NavBackStackEntry?,
  navigationItems: List<NavigationItem>,
  onClick: (route: String) -> Unit
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
  ) {
    navigationItems.forEach { item ->

      val selected = item.route == backStackEntry?.destination?.route

      NavigationBarItem(
        selected = selected,
        colors = NavigationBarItemDefaults.colors(
          indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
          selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        onClick = {
          if (!selected) {
            onClick(item.route)
          }
        },
        label = { Label(item) },
        icon = { Icon(item) }
      )
    }
  }
}

@Composable
private fun ClassicNavigationRail(
  backStackEntry: NavBackStackEntry?,
  navigationItems: List<NavigationItem>,
  onClick: (route: String) -> Unit
) {
  NavigationRail(
    containerColor = MaterialTheme.colorScheme.surface
  ) {

    Spacer(modifier = Modifier.weight(1f))

    navigationItems.forEach { item ->
      val selected = item.route == backStackEntry?.destination?.route

      NavigationRailItem(
        selected = selected,
        colors = NavigationRailItemDefaults.colors(
          indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
          selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        onClick = {
          if (!selected) {
            onClick(item.route)
          }
        },
        label = { Label(item) },
        icon = { Icon(item) }
      )
    }
    Spacer(Modifier.weight(1f))
  }
}

@Composable
fun RowScope.GalleryNavBarItem(
  navItem: NavigationItem,
  isSelected: Boolean,
  onClick: (route: String) -> Unit,
) {
  val mutableInteraction = remember { MutableInteractionSource() }
  val selectedColor by animateColorAsState(
    targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
    label = "selectedColor"
  )
  val selectedIconColor by animateColorAsState(
    targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "selectedIconColor"
  )
  Box(
    modifier = Modifier
      .height(64.dp)
      .weight(1f)
      // Dummy clickable to intercept clicks from passing under the container
      .clickable(
        indication = null,
        interactionSource = mutableInteraction,
        onClick = {}
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .height(32.dp)
        .width(64.dp)
        .background(
          color = selectedColor,
          shape = RoundedCornerShape(percent = 100)
        )
        .clip(RoundedCornerShape(100))
        .clickable { if (!isSelected) onClick(navItem.route) },
    )
    Icon(
      modifier = Modifier.size(22.dp),
      imageVector = navItem.icon,
      contentDescription = navItem.name,
      tint = selectedIconColor
    )
  }
}

private fun navigate(navController: NavController, route: String) {
  navController.navigate(route) {
    popUpTo(navController.graph.findStartDestination().id) {
      saveState = true
    }
    launchSingleTop = true
    restoreState = true
  }
}