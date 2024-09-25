package com.itsvks.pictures.screens.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAppBottomSheetState(): AppBottomSheetState {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  return rememberSaveable(saver = AppBottomSheetState.Saver(density = LocalDensity.current)) {
    AppBottomSheetState(sheetState)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
class AppBottomSheetState(val sheetState: SheetState) {
  var isVisible by mutableStateOf(false)
    private set

  internal constructor(sheetState: SheetState, isVisible: Boolean) : this(sheetState) {
    this.isVisible = isVisible
  }

  suspend fun show() {
    if (!isVisible) {
      isVisible = true
      delay(10)
      sheetState.show()
    }
  }

  suspend fun hide() {
    if (isVisible) {
      sheetState.hide()
      delay(10)
      isVisible = false
    }
  }

  companion object {
    fun Saver(
      skipPartiallyExpanded: Boolean = true,
      confirmValueChange: (SheetValue) -> Boolean = { true },
      density: Density
    ) = Saver<AppBottomSheetState, Pair<SheetValue, Boolean>>(
      save = { Pair(it.sheetState.currentValue, it.isVisible) },
      restore = { savedValue ->
        AppBottomSheetState(
          sheetState = SheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
            density = density,
            initialValue = savedValue.first,
            confirmValueChange = confirmValueChange
          ),
          savedValue.second
        )
      }
    )
  }
}