package com.itsvks.pictures.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.ui.theme.Shapes

@Composable
fun SetupWizard(
  modifier: Modifier = Modifier,
  painter: Painter,
  title: String,
  subtitle: String,
  contentPadding: Dp = 32.dp,
  bottomBar: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            color = MaterialTheme.colorScheme.surface
          )
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        bottomBar()
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(paddingValues)
        .padding(top = 24.dp)
        .verticalScroll(state = rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      Image(
        painter = painter,
        contentDescription = painter.toString(),
        modifier = Modifier
          .size(72.dp)
          .clip(Shapes.large)
      )

      Text(
        text = buildAnnotatedString {
          val headLineMedium = MaterialTheme.typography.headlineMedium.toSpanStyle()
          val bodyLarge = MaterialTheme.typography.bodyLarge.toSpanStyle()
          val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

          withStyle(style = ParagraphStyle(textAlign = TextAlign.Center)) {
            withStyle(style = headLineMedium) {
              append(title)
            }
            appendLine()
            withStyle(
              style = bodyLarge.copy(color = onSurfaceVariant)
            ) {
              append(subtitle)
            }
          }
        }
      )

      Column(
        modifier = Modifier
          .padding(horizontal = 32.dp)
          .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        content()
      }
    }
  }
}

@Composable
fun SetupWizard(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  title: String,
  subtitle: String,
  contentPadding: Dp = 32.dp,
  bottomBar: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            color = MaterialTheme.colorScheme.surface
          )
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        bottomBar()
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(paddingValues)
        .padding(top = 24.dp)
        .verticalScroll(state = rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = icon.name,
        modifier = Modifier
          .size(72.dp)
          .clip(Shapes.large)
      )

      Text(
        text = buildAnnotatedString {
          val headLineMedium = MaterialTheme.typography.headlineMedium.toSpanStyle()
          val bodyLarge = MaterialTheme.typography.bodyLarge.toSpanStyle()
          val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

          withStyle(style = ParagraphStyle(textAlign = TextAlign.Center)) {
            withStyle(style = headLineMedium) {
              append(title)
            }
            appendLine()
            withStyle(
              style = bodyLarge.copy(color = onSurfaceVariant)
            ) {
              append(subtitle)
            }
          }
        }
      )

      Column(
        modifier = Modifier
          .padding(horizontal = 32.dp)
          .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        content()
      }
    }
  }
}
