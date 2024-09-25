package com.itsvks.pictures.screens.vault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.core.components.DragHandle
import com.itsvks.pictures.models.Vault
import com.itsvks.pictures.models.VaultState
import com.itsvks.pictures.screens.components.AppBottomSheetState
import com.itsvks.pictures.screens.components.media.OptionItem
import com.itsvks.pictures.screens.components.media.OptionLayout
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectVaultSheet(
  modifier: Modifier = Modifier,
  state: AppBottomSheetState,
  vaultState: VaultState,
  onVaultSelected: (Vault) -> Unit
) {
  val vaults by remember(vaultState) {
    derivedStateOf { vaultState.vaults }
  }
  val scope = rememberCoroutineScope()
  val vaultOptions = remember(vaults, state) {
    vaults.map {
      OptionItem(
        text = it.name,
        onClick = {
          onVaultSelected(it)
          scope.launch {
            state.hide()
          }
        }
      )
    }
  }

  if (state.isVisible) {
    ModalBottomSheet(
      modifier = modifier,
      sheetState = state.sheetState,
      onDismissRequest = {
        scope.launch {
          state.hide()
        }
      },
      containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
      tonalElevation = 0.dp,
      dragHandle = { DragHandle() },
      contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 32.dp, vertical = 16.dp)
          .navigationBarsPadding()
      ) {
        Text(
          text = buildAnnotatedString {
            withStyle(
              style = SpanStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                letterSpacing = MaterialTheme.typography.titleLarge.letterSpacing
              )
            ) {
              append("Select a vault")
            }
          },
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
        )
        OptionLayout(
          modifier = Modifier.fillMaxWidth(),
          optionList = vaultOptions
        )
      }
    }
  }
}