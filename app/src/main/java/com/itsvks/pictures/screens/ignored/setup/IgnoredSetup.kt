package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.itsvks.pictures.core.Constants.Animation.navigateInAnimation
import com.itsvks.pictures.core.Constants.Animation.navigateUpAnimation
import com.itsvks.pictures.extensions.matchesAlbum
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.IgnoredAlbum
import com.itsvks.pictures.viewmodel.ignored.setup.IgnoredSetupViewModel
import java.util.UUID

@Composable
fun IgnoredSetup(
  modifier: Modifier = Modifier,
  onCancel: () -> Unit,
  albumsState: State<AlbumState>
) {
  val vm = hiltViewModel<IgnoredSetupViewModel>()
  val navController = rememberNavController()

  val uiState by vm.uiState.collectAsStateWithLifecycle()

  NavHost(
    modifier = modifier.fillMaxSize(),
    navController = navController,
    startDestination = IgnoredSetupDestination.Label(),
    enterTransition = { navigateInAnimation },
    exitTransition = { navigateUpAnimation },
    popEnterTransition = { navigateInAnimation },
    popExitTransition = { navigateUpAnimation }
  ) {
    composable(
      route = IgnoredSetupDestination.Label()
    ) {
      SetupLabelScreen(
        onGoBack = {
          vm.reset()
          onCancel()
        },
        onNext = {
          if (vm.isLabelError) return@SetupLabelScreen
          navController.navigate(IgnoredSetupDestination.Location())
        },
        isError = vm.isLabelError,
        initialLabel = uiState.label,
        onLabelChanged = { newLabel ->
          vm.setLabel(newLabel)
        }
      )
    }
    composable(
      route = IgnoredSetupDestination.Location()
    ) {
      SetupLocationScreen(
        onGoBack = navController::navigateUp,
        onNext = {
          navController.navigate(IgnoredSetupDestination.Type())
        },
        initialLocation = uiState.location,
        initialType = uiState.type,
        isError = false,
        onLocationChanged = { newLocation ->
          vm.setLocation(newLocation)
        },
        onTypeChanged = { newType ->
          vm.setType(newType)
        }
      )
    }
    composable(
      route = IgnoredSetupDestination.Type()
    ) {
      val ignoredAlbums by vm.blacklistState.collectAsStateWithLifecycle()
      when (uiState.type) {
        is IgnoredType.SELECTION -> {
          SetupTypeSelectionScreen(
            onGoBack = navController::navigateUp,
            onNext = {
              navController.navigate(IgnoredSetupDestination.MatchedAlbums())
            },
            initialAlbum = (uiState.type as IgnoredType.SELECTION).selectedAlbum,
            onAlbumChanged = {
              vm.setType(IgnoredType.SELECTION(it))
            },
            ignoredAlbums = ignoredAlbums.albums,
            albumsState = albumsState,
          )
        }

        is IgnoredType.REGEX -> {
          SetupTypeRegexScreen(
            onGoBack = navController::navigateUp,
            onNext = {
              navController.navigate(IgnoredSetupDestination.MatchedAlbums())
            },
            initialRegex = (uiState.type as IgnoredType.REGEX).regex,
            ignoredAlbums = ignoredAlbums.albums,
            onRegexChanged = {
              vm.setType(IgnoredType.REGEX(it))
            }
          )
        }
      }
    }
    composable(
      route = IgnoredSetupDestination.MatchedAlbums()
    ) {
      LaunchedEffect(uiState) {
        if (uiState.type is IgnoredType.REGEX) {
          if ((uiState.type as IgnoredType.REGEX).regex.isEmpty()) return@LaunchedEffect
          try {
            val regex = (uiState.type as IgnoredType.REGEX).regex.toRegex()
            val matchedAlbums = albumsState.value.albumsWithBlacklisted.filter(regex::matchesAlbum)
            vm.setMatchedAlbums(matchedAlbums)
          } catch (e: Exception) {
            e.printStackTrace()
            vm.setMatchedAlbums(emptyList())
          }
        } else if (uiState.type is IgnoredType.SELECTION) {
          val selectedAlbum = (uiState.type as IgnoredType.SELECTION).selectedAlbum!!
          vm.setMatchedAlbums(listOf(selectedAlbum))
        }
      }

      SetupConfirmationScreen(
        onGoBack = navController::navigateUp,
        onNext = {
          val ignored = IgnoredAlbum(
            id = if (uiState.type is IgnoredType.SELECTION) (uiState.type as IgnoredType.SELECTION).selectedAlbum!!.id else UUID.randomUUID().mostSignificantBits,
            label = uiState.label,
            location = uiState.location,
            wildcard = if (uiState.type is IgnoredType.REGEX) (uiState.type as IgnoredType.REGEX).regex else null,
            matchedAlbums = uiState.matchedAlbums.map { it.label }
              .ifEmpty { listOf((uiState.type as IgnoredType.SELECTION).selectedAlbum!!.label) }
          )
          vm.addToIgnored(ignored)
          onCancel()
        },
        matchedAlbums = uiState.matchedAlbums,
        location = uiState.location,
        type = uiState.type
      )
    }
  }
}