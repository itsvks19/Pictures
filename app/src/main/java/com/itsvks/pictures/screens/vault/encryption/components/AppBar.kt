package com.itsvks.pictures.screens.vault.encryption.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.core.Constants
import com.itsvks.pictures.screens.components.AppBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun EncryptedMediaViewAppBar(
  showUI: Boolean,
  showDate: Boolean,
  currentDate: String,
  paddingValues: PaddingValues,
  onGoBack: () -> Unit,
  bottomSheetState: AppBottomSheetState,
) {
  val scope = rememberCoroutineScope()
  AnimatedVisibility(
    visible = showUI,
    enter = Constants.Animation.enterAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
    exit = Constants.Animation.exitAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION)
  ) {
    Row(
      modifier = Modifier
        .background(
          Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.scrim, Color.Transparent)
          )
        )
        .padding(top = paddingValues.calculateTopPadding())
        .padding(start = 5.dp, end = 8.dp)
        .padding(vertical = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      IconButton(onClick = onGoBack) {
        Image(
          imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
          colorFilter = ColorFilter.tint(Color.White),
          contentDescription = "Go back",
          modifier = Modifier
            .height(48.dp)
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (showDate) {
          Text(
            text = currentDate.uppercase(),
            modifier = Modifier,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            textAlign = TextAlign.End
          )
        }
        IconButton(
          onClick = {
            scope.launch {
              bottomSheetState.show()
            }
          }
        ) {
          Image(
            imageVector = Icons.Outlined.Info,
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = "info",
            modifier = Modifier
              .height(48.dp)
          )
        }
      }
    }
  }
}