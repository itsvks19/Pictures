package com.itsvks.pictures.screens.mediaview.components

import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.itsvks.pictures.R
import com.itsvks.pictures.core.Constants.Animation.enterAnimation
import com.itsvks.pictures.core.Constants.Animation.exitAnimation
import com.itsvks.pictures.core.components.DragHandle
import com.itsvks.pictures.core.components.NavigationBarSpacer
import com.itsvks.pictures.core.components.exif.CopyMediaSheet
import com.itsvks.pictures.core.components.exif.MetadataEditSheet
import com.itsvks.pictures.core.components.exif.MoveMediaSheet
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.extensions.connectivityState
import com.itsvks.pictures.extensions.fileExtension
import com.itsvks.pictures.extensions.isFavorite
import com.itsvks.pictures.extensions.isRaw
import com.itsvks.pictures.extensions.isTrashed
import com.itsvks.pictures.extensions.isVideo
import com.itsvks.pictures.extensions.launchEditIntent
import com.itsvks.pictures.extensions.launchMap
import com.itsvks.pictures.extensions.launchOpenWithIntent
import com.itsvks.pictures.extensions.launchUseAsIntent
import com.itsvks.pictures.extensions.printDebug
import com.itsvks.pictures.extensions.readUriOnly
import com.itsvks.pictures.extensions.rememberActivityResult
import com.itsvks.pictures.extensions.rememberExifInterface
import com.itsvks.pictures.extensions.rememberExifMetadata
import com.itsvks.pictures.extensions.rememberMediaInfo
import com.itsvks.pictures.extensions.shareMedia
import com.itsvks.pictures.extensions.writeRequest
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.ExifAttributes
import com.itsvks.pictures.models.LocationData
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.MediaDateCaption
import com.itsvks.pictures.models.Vault
import com.itsvks.pictures.models.VaultState
import com.itsvks.pictures.models.rememberExifAttributes
import com.itsvks.pictures.models.rememberLocationData
import com.itsvks.pictures.models.rememberMediaDateCaption
import com.itsvks.pictures.screens.components.rememberAppBottomSheetState
import com.itsvks.pictures.screens.trashed.components.TrashDialog
import com.itsvks.pictures.screens.trashed.components.TrashDialogAction
import com.itsvks.pictures.screens.vault.components.SelectVaultSheet
import com.itsvks.pictures.ui.theme.Shapes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@Composable
fun MediaViewDetails(
  modifier: Modifier = Modifier,
  albumState: State<AlbumState>,
  vaultState: State<VaultState>,
  currentMedia: Media?,
  handler: MediaHandleUseCase,
  addMediaToVault: (Vault, Media) -> Unit
) {
  Column(
    modifier = modifier
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
      visible = currentMedia != null && !currentMedia.isTrashed,
      enter = enterAnimation,
      exit = exitAnimation
    ) {
      Column {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val exifInterface = rememberExifInterface(currentMedia!!, true)
        val exifMetadata = rememberExifMetadata(currentMedia, exifInterface)
        var exifAttributes by rememberExifAttributes(exifInterface)
        val exifAttributesEditResult = rememberActivityResult(
          onResultOk = {
            scope.launch {
              if (handler.updateMediaExif(currentMedia, exifAttributes)) {
                printDebug("Exif Attributes Updated")
              } else {
                Toast.makeText(context, "Exif Update failed", Toast.LENGTH_SHORT).show()
              }
            }
          }
        )

        val dateCaption = rememberMediaDateCaption(exifMetadata, currentMedia)
        val metadataState = rememberAppBottomSheetState()
        val mediaInfoList = rememberMediaInfo(
          media = currentMedia,
          exifMetadata = exifMetadata,
          onLabelClick = {
            if (!currentMedia.readUriOnly) {
              scope.launch {
                metadataState.show()
              }
            }
          }
        )

        val locationData = rememberLocationData(exifMetadata, currentMedia)

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
          items(
            items = mediaInfoList
          ) {
            MediaInfoRow(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              label = it.label,
              content = it.content,
              trailingContent = {
                if (it.trailingIcon != null && !currentMedia.readUriOnly) {
                  MediaInfoChip2(
                    text = stringResource(R.string.edit),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondary.copy(
                      alpha = 0.1f
                    ),
                    onClick = {
                      scope.launch {
                        metadataState.show()
                      }
                    }
                  )
                }
              },
              onClick = it.onClick
            )
          }
          item {
            Spacer(modifier = Modifier.height(8.dp))
          }
          item {
            MediaViewInfoActions2(
              media = currentMedia,
              albumsState = albumState,
              vaults = vaultState,
              handler = handler,
              addMedia = addMediaToVault
            )
          }
          item {
            LocationItem(
              locationData = locationData
            )
          }
          item {
            androidx.compose.animation.AnimatedVisibility(
              visible = !currentMedia.readUriOnly
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp)
                  .padding(bottom = 16.dp)
                  .clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(1.dp),
              ) {
                AnimatedVisibility(
                  visible = locationData != null
                ) {
                  ListItem(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(2.dp))
                      .clickable {
                        scope.launch {
                          exifAttributes = exifAttributes.copy(
                            gpsLatLong = null
                          )
                          exifAttributesEditResult.launch(
                            currentMedia.writeRequest(context.contentResolver)
                          )
                        }
                      },
                    headlineContent = {
                      Text("Delete Location")
                    },
                    leadingContent = {
                      Icon(
                        imageVector = Icons.Outlined.GpsOff,
                        contentDescription = "Delete Location"
                      )
                    },
                    colors = ListItemDefaults.colors(
                      containerColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f
                      ),
                      headlineColor = MaterialTheme.colorScheme.primary,
                      leadingIconColor = MaterialTheme.colorScheme.primary
                    )
                  )
                }
                AnimatedVisibility(
                  visible = exifMetadata?.lensDescription != null
                ) {
                  ListItem(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(2.dp))
                      .clickable {
                        scope.launch {
                          exifAttributes = ExifAttributes()
                          exifAttributesEditResult.launch(
                            currentMedia.writeRequest(context.contentResolver)
                          )
                        }
                      },
                    headlineContent = {
                      Text("Delete Metadata")
                    },
                    leadingContent = {
                      Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = "Delete Metadata"
                      )
                    },
                    colors = ListItemDefaults.colors(
                      containerColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f
                      ),
                      headlineColor = MaterialTheme.colorScheme.primary,
                      leadingIconColor = MaterialTheme.colorScheme.primary
                    )
                  )
                }
              }
            }
          }
          item {
            NavigationBarSpacer()
          }
        }

        if (metadataState.isVisible) {
          MetadataEditSheet(
            state = metadataState,
            media = currentMedia,
            handle = handler
          )
        }
      }
    }
  }
}

@Composable
fun DateHeader(
  modifier: Modifier = Modifier,
  mediaDateCaption: MediaDateCaption
) {
  Text(
    text = buildAnnotatedString {
      withStyle(
        style = MaterialTheme.typography.titleLarge.copy(
          color = MaterialTheme.colorScheme.onSurface
        ).toSpanStyle()
      ) {
        appendLine(mediaDateCaption.date)
      }
      mediaDateCaption.deviceInfo?.let { deviceInfo ->
        withStyle(
          style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
          ).toSpanStyle()
        ) {
          appendLine(deviceInfo)
        }
      }
      withStyle(
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        ).toSpanStyle()
      ) {
        append(
          mediaDateCaption.description.ifEmpty {
            stringResource(R.string.image_add_description)
          }
        )
      }
    },
    overflow = TextOverflow.Ellipsis,
    modifier = modifier
      .padding(top = 16.dp)
      .padding(horizontal = 32.dp),
  )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun LocationItem(
  modifier: Modifier = Modifier,
  locationData: LocationData?
) {
  AnimatedVisibility(
    visible = locationData != null,
    enter = enterAnimation,
    exit = exitAnimation
  ) {
    if (locationData != null) {
      val context = LocalContext.current
      Row(
        modifier = modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(bottom = 8.dp)
          .background(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = Shapes.large
          )
          .clip(Shapes.large)
          .clickable {
            context.launchMap(locationData.latitude, locationData.longitude)
          },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(
          modifier = Modifier
            .weight(2f)
            .padding(start = 16.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
          Spacer(modifier = Modifier.height(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = stringResource(R.string.open_with),
            tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = stringResource(R.string.location),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = locationData.location,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
          )
          Spacer(modifier = Modifier.height(4.dp))
        }

        val connection by connectivityState()

        AnimatedVisibility(
          modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(Shapes.large),
          visible = remember(connection) {
            connection.isConnected() // && BuildConfig.MAPS_TOKEN != "DEBUG"
          }
        ) {
//          AsyncImage(
//            uri = MapBoxURL(
//              latitude = locationData.latitude,
//              longitude = locationData.longitude,
//              darkTheme = isSystemInDarkTheme()
//            ),
//            contentScale = ContentScale.Crop,
//            contentDescription = stringResource(R.string.location_map_cd),
//            modifier = Modifier
//              .weight(1f)
//              .aspectRatio(1f)
//              .clip(Shapes.large)
//          )
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaInfoChip2(
  modifier: Modifier = Modifier,
  text: String,
  contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
  containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
  outlineInLightTheme: Boolean = true,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
) {
  Text(
    modifier = modifier
      .background(
        color = containerColor,
        shape = Shapes.extraLarge
      )
      .then(
        if (!isSystemInDarkTheme() && outlineInLightTheme) Modifier.border(
          width = 0.5.dp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
          shape = Shapes.extraLarge
        ) else Modifier
      )
      .clip(Shapes.extraLarge)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .padding(horizontal = 16.dp, vertical = 8.dp),
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = contentColor
  )
}

@Composable
fun MediaViewInfoActions2(
  media: Media,
  albumsState: State<AlbumState>,
  vaults: State<VaultState>,
  handler: MediaHandleUseCase,
  addMedia: (Vault, Media) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {

    ShareButton(media, followTheme = true)

    if (!media.isVideo) {
      HideButton(media, vaults = vaults, addMedia = addMedia, followTheme = true)
    }

    OpenAsButton(media, followTheme = true)

    CopyButton(media, albumsState.value, handler, followTheme = true)

    MoveButton(media, albumsState.value, handler, followTheme = true)

    EditButton(media, followTheme = true)
  }
}

@Composable
fun MediaViewActions2(
  currentIndex: Int,
  currentMedia: Media,
  handler: MediaHandleUseCase,
  onDeleteMedia: ((Int) -> Unit)?,
  showDeleteButton: Boolean
) {
  if (currentMedia.isTrashed) {
    val scope = rememberCoroutineScope()
    val result = rememberActivityResult()

    BottomBarColumn(
      currentMedia = currentMedia,
      imageVector = Icons.Outlined.RestoreFromTrash,
      title = stringResource(id = R.string.trash_restore)
    ) {
      scope.launch {
        onDeleteMedia?.invoke(currentIndex)
        handler.trashMedia(result = result, arrayListOf(it), trash = false)
      }
    }

    BottomBarColumn(
      currentMedia = currentMedia,
      imageVector = Icons.Outlined.DeleteOutline,
      title = stringResource(id = R.string.trash_delete)
    ) {
      scope.launch {
        onDeleteMedia?.invoke(currentIndex)
        handler.deleteMedia(result = result, arrayListOf(it))
      }
    }
  } else {

    ShareButton(currentMedia)

    FavoriteButton(currentMedia, handler)

    EditButton(currentMedia)

    if (showDeleteButton) {
      TrashButton(currentIndex, currentMedia, handler, false, onDeleteMedia)
    }
  }
}

@Composable
fun HideButton(
  media: Media,
  vaults: State<VaultState>,
  addMedia: (Vault, Media) -> Unit,
  followTheme: Boolean = true
) {
  val sheetState = rememberAppBottomSheetState()
  val scope = rememberCoroutineScope()
  BottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.Lock,
    followTheme = followTheme,
    enabled = remember(vaults.value) {
      vaults.value.vaults.isNotEmpty()
    },
    title = stringResource(R.string.hide)
  ) {
    scope.launch {
      sheetState.show()
    }
  }
  val context = LocalContext.current
  val result = rememberActivityResult(onResultOk = {
    scope.launch {
      sheetState.hide()
    }
  })
  val vaultState by remember(vaults.value) { vaults }
  SelectVaultSheet(
    state = sheetState,
    vaultState = vaultState,
    onVaultSelected = { vault ->
      scope.launch {
        addMedia(vault, media).also {
          val intentSender =
            MediaStore.createDeleteRequest(
              context.contentResolver,
              listOf(media.uri)
            ).intentSender
          val senderRequest: IntentSenderRequest =
            IntentSenderRequest.Builder(intentSender)
              .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
              .build()
          result.launch(senderRequest)
        }
      }
    }
  )
}

@Composable
fun CopyButton(
  media: Media,
  albumsState: AlbumState,
  handler: MediaHandleUseCase,
  followTheme: Boolean = true
) {
  val copySheetState = rememberAppBottomSheetState()
  val scope = rememberCoroutineScope()
  BottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.CopyAll,
    followTheme = followTheme,
    title = stringResource(R.string.copy)
  ) {
    scope.launch {
      copySheetState.show()
    }
  }

  CopyMediaSheet(
    sheetState = copySheetState,
    mediaList = listOf(media),
    albumsState = albumsState,
    handler = handler,
    onFinish = { }
  )
}

@Composable
fun MoveButton(
  media: Media,
  albumsState: AlbumState,
  handler: MediaHandleUseCase,
  followTheme: Boolean = true
) {
  val moveSheetState = rememberAppBottomSheetState()
  val scope = rememberCoroutineScope()
  BottomBarColumn(
    currentMedia = media,
    imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
    followTheme = followTheme,
    title = stringResource(R.string.move)
  ) {
    scope.launch {
      moveSheetState.show()
    }
  }

  MoveMediaSheet(
    sheetState = moveSheetState,
    mediaList = listOf(media),
    albumState = albumsState,
    handler = handler,
    onFinish = { }
  )
}

@Composable
fun ShareButton(
  media: Media,
  followTheme: Boolean = true
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  BottomBarColumn(
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
fun FavoriteButton(
  media: Media,
  handler: MediaHandleUseCase,
  followTheme: Boolean = true
) {
  val scope = rememberCoroutineScope()
  var lastFavorite = remember(media) { media.isFavorite }
  val result = rememberActivityResult(
    onResultOk = {
      lastFavorite = !lastFavorite
    }
  )
  val favoriteIcon by remember(lastFavorite) {
    mutableStateOf(
      if (lastFavorite)
        Icons.Filled.Favorite
      else Icons.Outlined.FavoriteBorder
    )
  }
  if (!media.readUriOnly) {
    BottomBarColumn(
      currentMedia = media,
      imageVector = favoriteIcon,
      followTheme = followTheme,
      title = stringResource(R.string.favorite)
    ) {
      scope.launch {
        handler.toggleFavorite(result = result, arrayListOf(it), it.favorite != 1)
      }
    }
  }
}

@Composable
fun EditButton(
  media: Media,
  followTheme: Boolean = true
) {
  val context = LocalContext.current
  BottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.Edit,
    followTheme = followTheme,
    title = stringResource(R.string.edit)
  ) {
    context.launchEditIntent(it)
  }
}

@Composable
fun OpenAsButton(
  media: Media,
  followTheme: Boolean = true
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  if (media.isVideo) {
    BottomBarColumn(
      currentMedia = media,
      imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
      followTheme = followTheme,
      title = stringResource(R.string.open_with)
    ) {
      scope.launch { context.launchOpenWithIntent(it) }
    }
  } else {
    BottomBarColumn(
      currentMedia = media,
      imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
      followTheme = followTheme,
      title = stringResource(R.string.use_as)
    ) {
      scope.launch { context.launchUseAsIntent(it) }
    }
  }
}

@Composable
fun TrashButton(
  index: Int,
  media: Media,
  handler: MediaHandleUseCase,
  followTheme: Boolean = true,
  onDeleteMedia: ((Int) -> Unit)?
) {
  var shouldMoveToTrash by rememberSaveable { mutableStateOf(true) }
  val state = rememberAppBottomSheetState()
  val scope = rememberCoroutineScope()
  val result = rememberActivityResult {
    scope.launch {
      state.hide()
      shouldMoveToTrash = true
      onDeleteMedia?.invoke(index)
    }
  }
  BottomBarColumn(
    currentMedia = media,
    imageVector = Icons.Outlined.DeleteOutline,
    followTheme = followTheme,
    title = stringResource(id = R.string.trash),
    onItemLongClick = {
      shouldMoveToTrash = false
      scope.launch {
        state.show()
      }
    },
    onItemClick = {
      shouldMoveToTrash = true
      scope.launch {
        state.show()
      }
    }
  )

  TrashDialog(
    appBottomSheetState = state,
    data = listOf(media),
    action = if (shouldMoveToTrash) TrashDialogAction.TRASH else TrashDialogAction.DELETE
  ) {
    if (shouldMoveToTrash) {
      handler.trashMedia(result, it, true)
    } else {
      handler.deleteMedia(result, it)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarColumn(
  currentMedia: Media?,
  imageVector: ImageVector,
  title: String,
  enabled: Boolean = true,
  followTheme: Boolean = false,
  onItemLongClick: ((Media) -> Unit)? = null,
  onItemClick: (Media) -> Unit
) {
  val alpha = if (enabled) 1f else 0.5f
  val tintColor =
    if (followTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = alpha) else Color.White.copy(
      alpha = alpha
    )
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .defaultMinSize(
        minWidth = 90.dp,
        minHeight = 80.dp
      )
      .combinedClickable(
        enabled = enabled,
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
      modifier = Modifier.height(32.dp)
    )
    Spacer(modifier = Modifier.size(4.dp))
    Text(
      text = title,
      modifier = Modifier,
      fontWeight = FontWeight.Medium,
      style = MaterialTheme.typography.bodyMedium,
      color = tintColor,
      textAlign = TextAlign.Center,
    )
  }
}