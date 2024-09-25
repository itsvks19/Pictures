package com.itsvks.pictures.activities

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.itsvks.pictures.core.settings.Settings.Misc.getSecureMode
import com.itsvks.pictures.core.settings.Settings.Misc.rememberForceTheme
import com.itsvks.pictures.core.settings.Settings.Misc.rememberIsDarkMode
import com.itsvks.pictures.extensions.toggleOrientation
import com.itsvks.pictures.screens.components.AppBarContainer
import com.itsvks.pictures.screens.components.NavigationComponent
import com.itsvks.pictures.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enforceSecureFlag()
    enableEdgeToEdge()
    setContent {
      AppTheme {
        val navController = rememberNavController()
        val isScrolling = remember { mutableStateOf(false) }
        val bottomBarState = rememberSaveable { mutableStateOf(true) }
        val systemBarFollowThemeState = rememberSaveable { mutableStateOf(true) }
        val forcedTheme by rememberForceTheme()
        val localDarkTheme by rememberIsDarkMode()
        val systemDarkTheme = isSystemInDarkTheme()
        val darkTheme by remember(forcedTheme, localDarkTheme, systemDarkTheme) {
          mutableStateOf(if (forcedTheme) localDarkTheme else systemDarkTheme)
        }

        LaunchedEffect(darkTheme, systemBarFollowThemeState) {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
              Color.TRANSPARENT,
              Color.TRANSPARENT,
            ) { darkTheme || !systemBarFollowThemeState.value },
            navigationBarStyle = SystemBarStyle.auto(
              Color.TRANSPARENT,
              Color.TRANSPARENT,
            ) { darkTheme || !systemBarFollowThemeState.value }
          )
        }

        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
          AppBarContainer(
            navController = navController,
            paddingValues = paddingValues,
            bottomBarState = bottomBarState,
            isScrolling = isScrolling
          ) {
            NavigationComponent(
              navHostController = navController,
              paddingValues = paddingValues,
              bottomBarState = bottomBarState,
              systemBarFollowThemeState = systemBarFollowThemeState,
              toggleRotate = ::toggleOrientation,
              isScrolling = isScrolling
            )
          }
        }
      }
    }
  }

  private fun enforceSecureFlag() {
    lifecycleScope.launch {
      getSecureMode(this@MainActivity).collectLatest { enabled ->
        if (enabled) {
          window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
          window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
      }
    }
  }
}
