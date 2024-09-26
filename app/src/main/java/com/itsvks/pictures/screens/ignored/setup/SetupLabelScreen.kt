package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.R
import com.itsvks.pictures.screens.components.SetupWizard

@Composable
fun SetupLabelScreen(
  modifier: Modifier = Modifier,
  onGoBack: () -> Unit,
  onNext: () -> Unit,
  isError: Boolean,
  initialLabel: String,
  onLabelChanged: (String) -> Unit
) {
  var label by remember { mutableStateOf(initialLabel) }
  LaunchedEffect(label) {
    if (label != initialLabel)
      onLabelChanged(label)
  }

  SetupWizard(
    modifier = modifier,
    title = stringResource(R.string.setup_label_title),
    subtitle = stringResource(R.string.setup_label_subtitle),
    icon = Icons.Outlined.VisibilityOff,
    contentPadding = 0.dp,
    bottomBar = {
      OutlinedButton(
        onClick = onGoBack
      ) {
        Text(text = stringResource(id = R.string.action_cancel))
      }

      Button(
        onClick = onNext,
        enabled = !isError && label.isNotBlank()
      ) {
        Text(text = stringResource(R.string.get_started))
      }
    },
    content = {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = label,
        onValueChange = { label = it },
        label = { Text(stringResource(R.string.setup_label_input)) },
        placeholder = { Text(stringResource(R.string.setup_label_placeholder)) },
        isError = isError,
        singleLine = true,
      )

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 32.dp)
          .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.large
          )
          .padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        text = stringResource(R.string.setup_label_info)
      )
    }
  )
}