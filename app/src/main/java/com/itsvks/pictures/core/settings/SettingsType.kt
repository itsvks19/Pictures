package com.itsvks.pictures.core.settings

sealed class SettingsType {
  data object Seek : SettingsType()
  data object Switch : SettingsType()
  data object Header : SettingsType()
  data object Default : SettingsType()
}