package com.itsvks.pictures.screens.vault.encryption.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.core.components.DragHandle
import com.itsvks.pictures.core.components.NavigationBarSpacer
import com.itsvks.pictures.extensions.shareMedia
import com.itsvks.pictures.models.EncryptedMedia
import com.itsvks.pictures.models.Vault
import com.itsvks.pictures.models.rememberMediaDateCaption
import com.itsvks.pictures.screens.components.rememberAppBottomSheetState
import com.itsvks.pictures.screens.mediaview.components.DateHeader
import com.itsvks.pictures.screens.mediaview.components.MediaInfoChip2
import com.itsvks.pictures.screens.trashed.components.TrashDialogAction
import com.itsvks.pictures.screens.vault.components.EncryptedTrashDialog
import kotlinx.coroutines.launch

@Composable
fun EncryptedMediaViewDetails(
  currentMedia: EncryptedMedia?,
  currentVault: Vault?,
  restoreMedia: (Vault, EncryptedMedia) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(
        RoundedCornerShape(
          topStart = 24.dp,
          topEnd = 24.dp
        )
      )
      .background(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
      )
  ) {

    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      DragHandle()
    }

    androidx.compose.animation.AnimatedVisibility(
      modifier = Modifier.fillMaxWidth(),
      visible = currentMedia != null,
      enter = enterAnimation,
      exit = exitAnimation
    ) {
      Column {
        val dateCaption = rememberMediaDateCaption(null, currentMedia!!)

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
        ) {
          item {
            DateHeader(
              modifier = Modifier.fillMaxWidth(),
              mediaDateCaption = dateCaption
            )
          }
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (currentMedia.isRaw) {
                MediaInfoChip2(
                  text = currentMedia.fileExtension.toUpperCase(Locale.current),
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
              }
            }
          }
          item {
            Spacer(modifier = Modifier.height(8.dp))
          }
          item {
            EncryptedMediaViewInfoActions(
              media = currentMedia,
              restoreMedia = restoreMedia,
              currentVault = currentVault!!
            )
          }
          item {
            NavigationBarSpacer()
          }
        }
      }
    }
  }
}

@Composable
private fun EncryptedMediaViewInfoActions(
  media: EncryptedMedia,
  restoreMedia: (Vault, EncryptedMedia) -> Unit,
  currentVault: Vault
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {

    ShareButton(media, followTheme = true)

    // OpenAsButton(media, followTheme = true)

    RestoreButton(media, currentVault, restoreMedia, followTheme = true)
  }
}

@Composable
fun EncryptedMediaViewActions(
  currentIndex: Int,
  currentMedia: EncryptedMedia,
  currentVault: Vault,
  onDeleteMedia: ((Int) -> Unit)?,
  restoreMedia: (Vault, EncryptedMedia) -> Unit,
  deleteMedia: (Vault, EncryptedMedia) -> Unit
) {

  ShareButton(currentMedia)

  RestoreButton(currentMedia, currentVault, restoreMedia)

  TrashButton(
    index = currentIndex,
    media = currentMedia,
    currentVault = currentVault,
    onDeleteMedia = onDeleteMedia,
    deleteMedia = deleteMedia
  )
}


@Composable
private fun ShareButton(
  media: EncryptedMedia,
  followTheme: Boolean = false
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  EncryptedBottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.Share,
    followTheme = followTheme,
    title = stringResource(R.string.share)
  ) {
    scope.launch {
      context.shareMedia(media = it)
    }
  }
}

@Composable
private fun RestoreButton(
  media: EncryptedMedia,
  currentVault: Vault,
  restoreMedia: (Vault, EncryptedMedia) -> Unit,
  followTheme: Boolean = false
) {
  val scope = rememberCoroutineScope()
  EncryptedBottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.Image,
    followTheme = followTheme,
    title = "Restore"
  ) {
    scope.launch {
      restoreMedia(currentVault, it)
    }
  }
}

//@Composable
//private fun OpenAsButton(
//  media: EncryptedMedia,
//  followTheme: Boolean = false
//) {
//  val context = LocalContext.current
//  val scope = rememberCoroutineScope()
//  if (media.isVideo) {
//    EncryptedBottomBarColumn(
//      currentMedia = media,
//      imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
//      followTheme = followTheme,
//      title = stringResource(R.string.open_with)
//    ) {
//      scope.launch { context.launchOpenWithIntent(it) }
//    }
//  } else {
//    EncryptedBottomBarColumn(
//      currentMedia = media,
//      imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
//      followTheme = followTheme,
//      title = stringResource(R.string.use_as)
//    ) {
//      scope.launch { context.launchUseAsIntent(it) }
//    }
//  }
//}

@Composable
private fun TrashButton(
  index: Int,
  media: EncryptedMedia,
  currentVault: Vault,
  followTheme: Boolean = false,
  onDeleteMedia: ((Int) -> Unit)?,
  deleteMedia: (Vault, EncryptedMedia) -> Unit
) {
  val state = rememberAppBottomSheetState()
  val scope = rememberCoroutineScope()

  EncryptedBottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.DeleteOutline,
    followTheme = followTheme,
    title = stringResource(id = R.string.trash),
    onItemLongClick = {
      scope.launch {
        state.show()
      }
    },
    onItemClick = {
      scope.launch {
        state.show()
      }
    }
  )

  EncryptedTrashDialog(
    appBottomSheetState = state,
    data = listOf(media),
    action = TrashDialogAction.DELETE
  ) {
    it.forEach { media ->
      deleteMedia(currentVault, media)
    }
    onDeleteMedia?.invoke(index)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EncryptedBottomBarColumn(
  currentMedia: EncryptedMedia?,
  imageVector: ImageVector,
  title: String,
  followTheme: Boolean = false,
  onItemLongClick: ((EncryptedMedia) -> Unit)? = null,
  onItemClick: (EncryptedMedia) -> Unit
) {
  val tintColor = if (followTheme) MaterialTheme.colorScheme.onSurface else Color.White
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .defaultMinSize(
        minWidth = 90.dp,
        minHeight = 80.dp
      )
      .combinedClickable(
        onLongClick = {
          currentMedia?.let {
            onItemLongClick?.invoke(it)
          }
        },
        onClick = {
          currentMedia?.let {
            onItemClick.invoke(it)
          }
        }
      )
      .padding(top = 12.dp, bottom = 16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Image(
      imageVector = imageVector,
      colorFilter = ColorFilter.tint(tintColor),
      contentDescription = title,
      modifier = Modifier
        .height(32.dp)
    )
    Spacer(modifier = Modifier.size(4.dp))
    Text(
      text = title,
      modifier = Modifier,
      fontWeight = FontWeight.Medium,
      style = MaterialTheme.typography.bodyMedium,
      color = tintColor,
      textAlign = TextAlign.Center
    )
  }
}