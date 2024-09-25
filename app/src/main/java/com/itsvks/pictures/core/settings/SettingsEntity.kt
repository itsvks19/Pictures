package com.itsvks.pictures.core.settings

import androidx.compose.ui.graphics.vector.ImageVector
import com.itsvks.pictures.type.UnitFun

sealed class SettingsEntity(
  val type: SettingsType = SettingsType.Default,
  open val icon: ImageVector? = null,
  open val title: String,
  open val summary: String? = null,
  open val enabled: Boolean = true,
  open val isChecked: Boolean? = null,
  open val onCheck: ((Boolean) -> Unit)? = null,
  open val onClick: UnitFun? = null,
  open val minValue: Float? = null,
  open val currentValue: Float? = null,
  open val maxValue: Float? = null,
  open val step: Int = 1,
  open val valueMultiplier: Int = 1,
  open val seekSuffix: String? = null,
  open val onSeek: ((Float) -> Unit)? = null,
  open val screenPosition: Position = Position.Alone
) {
  val isHeader = type == SettingsType.Header

  data class Header(
    override val title: String
  ) : SettingsEntity(
    title = title,
    type = SettingsType.Header
  )

  data class Preference(
    override val icon: ImageVector? = null,
    override val title: String,
    override val summary: String? = null,
    override val enabled: Boolean = true,
    override val screenPosition: Position = Position.Alone,
    override val onClick: UnitFun? = null
  ) : SettingsEntity(
    icon = icon,
    title = title,
    summary = summary,
    enabled = enabled,
    screenPosition = screenPosition,
    onClick = onClick,
    type = SettingsType.Default
  )

  data class SwitchPreference(
    override val icon: ImageVector? = null,
    override val title: String,
    override val summary: String? = null,
    override val enabled: Boolean = true,
    override val screenPosition: Position = Position.Alone,
    override val isChecked: Boolean = false,
    override val onCheck: ((Boolean) -> Unit)? = null,
  ) : SettingsEntity(
    icon = icon,
    title = title,
    summary = summary,
    enabled = enabled,
    isChecked = isChecked,
    onCheck = onCheck,
    screenPosition = screenPosition,
    type = SettingsType.Switch
  )

  data class SeekPreference(
    override val icon: ImageVector? = null,
    override val title: String,
    override val summary: String? = null,
    override val enabled: Boolean = true,
    override val screenPosition: Position = Position.Alone,
    override val minValue: Float? = null,
    override val currentValue: Float? = null,
    override val maxValue: Float? = null,
    override val step: Int = 1,
    override val valueMultiplier: Int = 1,
    override val seekSuffix: String? = null,
    override val onSeek: ((Float) -> Unit)? = null,
  ) : SettingsEntity(
    icon = icon,
    title = title,
    summary = summary,
    enabled = enabled,
    screenPosition = screenPosition,
    minValue = minValue,
    currentValue = currentValue,
    maxValue = maxValue,
    step = step,
    valueMultiplier = valueMultiplier,
    seekSuffix = seekSuffix,
    onSeek = onSeek,
    type = SettingsType.Seek
  )
}