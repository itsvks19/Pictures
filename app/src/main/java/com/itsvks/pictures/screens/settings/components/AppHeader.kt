package com.itsvks.pictures.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsvks.pictures.BuildConfig
import com.itsvks.pictures.R

@Composable
fun SettingsAppHeader(modifier: Modifier = Modifier) {
  val appName = stringResource(id = R.string.app_name)
  val appVersion = remember { "v${BuildConfig.VERSION_NAME}" }
  val buildType = remember { BuildConfig.BUILD_TYPE }
  val appDeveloper = stringResource(R.string.app_dev, stringResource(R.string.app_dev_name))

  val githubImage = painterResource(id = R.drawable.github)
  val githubTitle = stringResource(R.string.github)
  val githubContentDesc = stringResource(R.string.github_button_cd)
  val githubUrl = stringResource(R.string.github_url)

  val uriHandler = LocalUriHandler.current

  Column(
    modifier = modifier
      .padding(all = 16.dp)
      .background(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        shape = RoundedCornerShape(24.dp)
      )
      .padding(all = 24.dp)
      .fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
      Text(
        text = appName,
        fontSize = 22.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "$appVersion-$buildType",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = appDeveloper,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { uriHandler.openUri(githubUrl) },
        colors = ButtonDefaults.buttonColors(
          contentColor = MaterialTheme.colorScheme.onTertiary,
          disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = .12f),
          containerColor = MaterialTheme.colorScheme.tertiary,
          disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = .12f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .height(52.dp)
          .semantics {
            contentDescription = githubContentDesc
          }
      ) {
        Icon(painter = githubImage, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = githubTitle)
      }
    }
  }
}