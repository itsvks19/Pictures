package com.itsvks.pictures.core.components.exif

import android.media.MediaScannerConnection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.R
import com.itsvks.pictures.core.components.DragHandle
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.extensions.isImage
import com.itsvks.pictures.extensions.isVideo
import com.itsvks.pictures.extensions.rememberActivityResult
import com.itsvks.pictures.extensions.rememberExifInterface
import com.itsvks.pictures.extensions.toastError
import com.itsvks.pictures.extensions.writeRequest
import com.itsvks.pictures.models.ExifAttributes
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.rememberExifAttributes
import com.itsvks.pictures.screens.components.AppBottomSheetState
import com.itsvks.pictures.ui.theme.Shapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataEditSheet(
  modifier: Modifier = Modifier,
  state: AppBottomSheetState,
  media: Media,
  handle: MediaHandleUseCase
) {
  val scope = rememberCoroutineScope { Dispatchers.IO }
  var shouldRemoveMetadata by remember { mutableStateOf(false) }
  var shouldRemoveLocation by remember { mutableStateOf(false) }
  val exifInterface = rememberExifInterface(media, useDirectPath = true)
  val context = LocalContext.current
  val cr = remember(context) { context.contentResolver }
  var exifAttributes by rememberExifAttributes(exifInterface)
  var newLabel by remember { mutableStateOf(media.label) }
  val errorToast = toastError()
  val request = rememberActivityResult {
    scope.launch {
      var done: Boolean
      done = handle.updateMediaExif(media, exifAttributes)
      if (newLabel != media.label && newLabel.isNotBlank()) {
        done = handle.renameMedia(media, newLabel)
      }
      if (done) {
        MediaScannerConnection.scanFile(
          context,
          arrayOf(media.path),
          arrayOf(media.mimeType),
          null
        )
        state.hide()
      } else {
        errorToast.show()
      }
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
      dragHandle = { DragHandle() },
      contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = if (media.isVideo) stringResource(R.string.update_file_name)
          else stringResource(R.string.edit_metadata),
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
        )

        TextField(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
          value = newLabel,
          onValueChange = { newValue ->
            newLabel = newValue
          },
          label = {
            Text(text = stringResource(id = R.string.label))
          },
          singleLine = true,
          shape = Shapes.large,
          colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
          )
        )

        androidx.compose.animation.AnimatedVisibility(
          visible = media.isImage
        ) {
          TextField(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth()
              .height(112.dp),
            value = exifAttributes.imageDescription ?: "",
            onValueChange = { newValue ->
              exifAttributes = exifAttributes.copy(imageDescription = newValue)
            },
            label = {
              Text(text = stringResource(R.string.description))
            },
            shape = Shapes.large,
            colors = TextFieldDefaults.colors(
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent,
              errorIndicatorColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent
            )
          )
        }

        Row(
          modifier = Modifier.padding(24.dp),
          horizontalArrangement = Arrangement
            .spacedBy(24.dp, Alignment.CenterHorizontally)
        ) {
          val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
          val tertiaryOnContainer = MaterialTheme.colorScheme.onTertiaryContainer
          Button(
            onClick = {
              scope.launch { state.hide() }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = tertiaryContainer,
              contentColor = tertiaryOnContainer
            )
          ) {
            Text(text = stringResource(R.string.action_cancel))
          }
          Button(
            onClick = {
              scope.launch(Dispatchers.Main) {
                if (shouldRemoveMetadata) {
                  exifAttributes = ExifAttributes()
                } else if (shouldRemoveLocation) {
                  exifAttributes = exifAttributes.copy(gpsLatLong = null)
                }
                request.launch(media.writeRequest(cr))
              }
            }
          ) {
            Text(text = stringResource(R.string.action_confirm))
          }
        }
      }
      Spacer(modifier = Modifier.navigationBarsPadding())
    }
  }
}