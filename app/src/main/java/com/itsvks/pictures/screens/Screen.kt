package com.itsvks.pictures.screens

sealed class Screen(val route: String) {
  data object TimelineScreen : Screen("timeline_screen")
  data object AlbumsScreen : Screen("albums_screen")

  data object AlbumViewScreen : Screen("album_view_screen") {
    fun albumAndName() = "$route?albumId={albumId}&albumName={albumName}"
  }

  data object MediaViewScreen : Screen("media_screen") {
    fun idAndTarget() = "$route?mediaId={mediaId}&target={target}"
    fun idAndAlbum() = "$route?mediaId={mediaId}&albumId={albumId}"
    fun idAndQuery() = "$route?mediaId={mediaId}&query={query}"
  }

  data object TrashedScreen : Screen("trashed_screen")
  data object FavoriteScreen : Screen("favorite_screen")
  data object SettingsScreen : Screen("settings_screen")
  data object IgnoredScreen : Screen("ignored_screen")
  data object IgnoredSetupScreen : Screen("ignored_setup_screen")
  data object SetupScreen : Screen("setup_screen")
  data object VaultScreen : Screen("vault_screen")
  data object LibraryScreen : Screen("library_screen")

  operator fun invoke() = route
}
