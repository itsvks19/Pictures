package com.itsvks.pictures.screens.vault

sealed class VaultScreens(val route: String) {
  data object VaultSetup : VaultScreens("vault_setup")
  data object VaultDisplay : VaultScreens("vault_display")
  data object LoadingScreen : VaultScreens("vault_loading")

  data object EncryptedMediaViewScreen : VaultScreens("vault_media_view_screen") {
    fun id() = "$route?mediaId={mediaId}"

    fun id(id: Long) = "$route?mediaId=$id"
  }

  operator fun invoke() = route
}