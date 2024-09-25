package com.itsvks.pictures.screens.settings.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.core.settings.Position
import com.itsvks.pictures.core.settings.SettingsEntity
import com.itsvks.pictures.core.settings.SettingsType
import kotlin.math.roundToLong

@Composable
fun SettingsItem(
  modifier: Modifier = Modifier,
  item: SettingsEntity
) {
  var checked by remember(item.isChecked) {
    mutableStateOf(item.isChecked ?: false)
  }

  val icon: @Composable (() -> Unit)? = if (item.icon == null) null
  else {
    {
      Icon(
        imageVector = item.icon!!,
        modifier = Modifier.size(24.dp),
        contentDescription = null
      )
    }
  }

  val summary: @Composable () -> Unit = {
    require(!item.summary.isNullOrEmpty()) { "Summary at this stage cannot be null or empty" }
    Text(text = item.summary!!)
  }

  val switch: @Composable () -> Unit = {
    Switch(checked = checked, onCheckedChange = null)
  }

  val shape = remember(item.screenPosition) {
    when (item.screenPosition) {
      Position.Alone -> RoundedCornerShape(24.dp)
      Position.Bottom -> RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
      )

      Position.Middle -> RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
      )

      Position.Top -> RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
      )
    }
  }

  val paddingModifier =
    when (item.screenPosition) {
      Position.Alone -> Modifier.padding(bottom = 16.dp)
      Position.Bottom -> Modifier.padding(top = 1.dp, bottom = 16.dp)
      Position.Middle -> Modifier.padding(vertical = 1.dp)
      Position.Top -> Modifier.padding(bottom = 1.dp)
    }

  var currentSeekValue by remember(item.currentValue) {
    mutableStateOf(item.currentValue?.div(item.valueMultiplier))
  }

  val seekTrailing: @Composable () -> Unit = {
    require(item.currentValue != null) { "Current value must not be null" }
    val value = currentSeekValue?.roundToLong()?.times(item.valueMultiplier).toString()
    val text = if (!item.seekSuffix.isNullOrEmpty()) "$value ${item.seekSuffix}" else value
    Text(
      text = text,
      textAlign = TextAlign.End,
      modifier = Modifier.width(42.dp)

    )
  }

  val seekContent: @Composable () -> Unit = {
    if (!item.summary.isNullOrEmpty()) {
      summary()
    }
    require(item.currentValue != null) { "Current value must not be null" }
    require(item.minValue != null) { "Min value must not be null" }
    require(item.maxValue != null) { "Max value must not be null" }
    require(item.onSeek != null) { "onSeek must not be null" }
    Slider(
      value = currentSeekValue!!,
      onValueChange = { currentSeekValue = it },
      valueRange = item.minValue!!..item.maxValue!!,
      onValueChangeFinished = {
        item.onSeek!!.invoke(currentSeekValue!! * item.valueMultiplier)
      },
      steps = item.step
    )
  }

  val supportingContent: (@Composable () -> Unit)? = when (item.type) {
    SettingsType.Default, SettingsType.Switch ->
      if (!item.summary.isNullOrEmpty()) summary else null

    SettingsType.Header -> null
    SettingsType.Seek -> seekContent
  }

  val trailingContent: (@Composable () -> Unit)? = when (item.type) {
    SettingsType.Switch -> switch
    SettingsType.Seek -> seekTrailing
    else -> null
  }

  val clickableModifier = if (item.type != SettingsType.Seek && !item.isHeader) {
    Modifier.clickable(item.enabled) {
      if (item.type == SettingsType.Switch) {
        item.onCheck?.let {
          checked = !checked
          it(checked)
        }
      } else item.onClick?.invoke()
    }
  } else Modifier

  if (item.isHeader) {
    Text(
      text = item.title,
      color = MaterialTheme.colorScheme.primary,
      style = MaterialTheme.typography.titleSmall,
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 40.dp, vertical = 8.dp)
        .padding(bottom = 8.dp)
    )
  } else {
    val alpha by animateFloatAsState(
      targetValue = if (item.enabled) 1f else 0.4f,
      label = "alpha"
    )

    ListItem(
      headlineContent = {
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(bottom = 2.dp)
        )
      },
      supportingContent = supportingContent,
      trailingContent = trailingContent,
      leadingContent = icon,
      modifier = modifier
        .then(paddingModifier)
        .padding(horizontal = 16.dp)
        .clip(shape)
        .background(
          color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
        .then(clickableModifier)
        .padding(8.dp)
        .fillMaxWidth()
        .alpha(alpha),
      colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
  }
}