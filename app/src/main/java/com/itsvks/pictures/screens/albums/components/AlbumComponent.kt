package com.itsvks.pictures.screens.albums.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.itsvks.pictures.R
import com.itsvks.pictures.extensions.formatSize
import com.itsvks.pictures.extensions.rememberFeedbackManager
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.screens.components.media.OptionItem
import com.itsvks.pictures.screens.components.media.OptionSheet
import com.itsvks.pictures.screens.components.rememberAppBottomSheetState
import com.itsvks.pictures.ui.theme.Shapes
import kotlinx.coroutines.launch

@Composable
fun AlbumComponent(
  modifier: Modifier = Modifier,
  album: Album,
  isEnabled: Boolean = true,
  onItemClick: (Album) -> Unit,
  onMoveAlbumToTrash: ((Album) -> Unit)? = null,
  onTogglePinClick: ((Album) -> Unit)? = null,
  onToggleIgnoreClick: ((Album) -> Unit)? = null
) {
  val scope = rememberCoroutineScope()
  val appBottomSheetState = rememberAppBottomSheetState()
  Column(
    modifier = modifier
      .alpha(if (isEnabled) 1f else 0.4f)
      .padding(horizontal = 8.dp),
  ) {
    if (onTogglePinClick != null) {
      val trashTitle = stringResource(R.string.move_album_to_trash)
      val pinTitle = stringResource(R.string.pin)
      val ignoredTitle = stringResource(id = R.string.add_to_ignored)
      val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
      val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
      val primaryContainer = MaterialTheme.colorScheme.primaryContainer
      val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
      val optionList = remember {
        mutableListOf(
          OptionItem(
            text = trashTitle,
            containerColor = primaryContainer,
            contentColor = onPrimaryContainer,
            enabled = onMoveAlbumToTrash != null,
            onClick = {
              scope.launch {
                appBottomSheetState.hide()
                onMoveAlbumToTrash?.invoke(album)
              }
            }
          ),
          OptionItem(
            text = pinTitle,
            containerColor = secondaryContainer,
            contentColor = onSecondaryContainer,
            onClick = {
              scope.launch {
                appBottomSheetState.hide()
                onTogglePinClick(album)
              }
            }
          )
        )
      }
      LaunchedEffect(onToggleIgnoreClick) {
        if (onToggleIgnoreClick != null) {
          optionList.add(
            OptionItem(
              text = ignoredTitle,
              onClick = {
                scope.launch {
                  appBottomSheetState.hide()
                  onToggleIgnoreClick(album)
                }
              }
            )
          )
        }
      }

      OptionSheet(
        state = appBottomSheetState,
        optionList = arrayOf(optionList),
        headerContent = {
          AsyncImage(
            modifier = Modifier
              .size(98.dp)
              .clip(Shapes.large),
            contentScale = ContentScale.Crop,
            uri = album.uri.toString(),
            contentDescription = album.label
          )
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
                append(album.label)
              }
              append("\n")
              withStyle(
                style = SpanStyle(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                  fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                  letterSpacing = MaterialTheme.typography.bodyMedium.letterSpacing
                )
              ) {
                append(
                  stringResource(
                    R.string.s_items,
                    album.count
                  ) + " (${album.size.formatSize()})"
                )
              }
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
              .padding(16.dp)
              .fillMaxWidth()
          )
        }
      )
    }
    Box(
      modifier = Modifier
        .aspectRatio(1f)
    ) {
      AlbumImage(
        album = album,
        isEnabled = isEnabled,
        onItemClick = onItemClick,
        onItemLongClick = if (onTogglePinClick != null) {
          {
            scope.launch {
              appBottomSheetState.show()
            }
          }
        } else null
      )
      if (album.isOnSdcard) {
        Icon(
          modifier = Modifier
            .padding(16.dp)
            .size(24.dp)
            .align(Alignment.BottomEnd),
          imageVector = Icons.Outlined.SdCard,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
    }
    Text(
      modifier = Modifier
        .padding(top = 12.dp)
        .padding(horizontal = 16.dp),
      text = album.label,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1
    )
    if (album.count > 0) {
      Text(
        modifier = Modifier
          .padding(top = 2.dp, bottom = 16.dp)
          .padding(horizontal = 16.dp),
        text = pluralStringResource(
          id = R.plurals.item_count,
          count = album.count.toInt(),
          album.count
        ) + " (${album.size.formatSize()})",
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.labelMedium,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumImage(
  album: Album,
  isEnabled: Boolean,
  onItemClick: (Album) -> Unit,
  onItemLongClick: ((Album) -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed = interactionSource.collectIsPressedAsState()
  val radius = if (isPressed.value) 32.dp else 16.dp
  val cornerRadius by animateDpAsState(targetValue = radius, label = "cornerRadius")
  val feedbackManager = rememberFeedbackManager()
  if (album.id == -200L && album.count == 0L) {
    Icon(
      imageVector = Icons.Outlined.AddCircleOutline,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .fillMaxSize()
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
          shape = RoundedCornerShape(cornerRadius)
        )
        .alpha(0.8f)
        .clip(RoundedCornerShape(cornerRadius))
        .combinedClickable(
          enabled = isEnabled,
          interactionSource = interactionSource,
          indication = LocalIndication.current,
          onClick = { onItemClick(album) },
          onLongClick = {
            onItemLongClick?.let {
              feedbackManager.vibrate()
              it(album)
            }
          }
        )
        .padding(48.dp)
    )
  } else {
    AsyncImage(
      modifier = Modifier
        .fillMaxSize()
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
          shape = RoundedCornerShape(cornerRadius)
        )
        .clip(RoundedCornerShape(cornerRadius))
        .combinedClickable(
          enabled = isEnabled,
          interactionSource = interactionSource,
          indication = LocalIndication.current,
          onClick = { onItemClick(album) },
          onLongClick = {
            onItemLongClick?.let {
              feedbackManager.vibrate()
              it(album)
            }
          }
        ),
      uri = album.uri.toString(),
      contentDescription = album.label,
      contentScale = ContentScale.Crop,
    )
  }
}