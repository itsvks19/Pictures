package com.itsvks.pictures.domains.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityOptionsCompat
import androidx.work.WorkManager
import com.itsvks.pictures.core.MediaStoreBuckets
import com.itsvks.pictures.core.Resource
import com.itsvks.pictures.core.observer.fileFlowObserver
import com.itsvks.pictures.core.worker.updateDatabase
import com.itsvks.pictures.database.InternalDatabase
import com.itsvks.pictures.database.KeychainHolder
import com.itsvks.pictures.database.KeychainHolder.Companion.VAULT_INFO_FILE_NAME
import com.itsvks.pictures.database.mediastore.queries.AlbumsFlow
import com.itsvks.pictures.database.mediastore.queries.MediaFlow
import com.itsvks.pictures.database.mediastore.queries.MediaUriFlow
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.extensions.copyMedia
import com.itsvks.pictures.extensions.decrypt
import com.itsvks.pictures.extensions.encrypt
import com.itsvks.pictures.extensions.getBytes
import com.itsvks.pictures.extensions.mapAsResource
import com.itsvks.pictures.extensions.overrideImage
import com.itsvks.pictures.extensions.printError
import com.itsvks.pictures.extensions.saveImage
import com.itsvks.pictures.extensions.toEncryptedMedia
import com.itsvks.pictures.extensions.updateMedia
import com.itsvks.pictures.extensions.updateMediaExif
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.AllowedMedia
import com.itsvks.pictures.models.EncryptedMedia
import com.itsvks.pictures.models.ExifAttributes
import com.itsvks.pictures.models.IgnoredAlbum
import com.itsvks.pictures.models.Media
import com.itsvks.pictures.models.PinnedAlbum
import com.itsvks.pictures.models.TimelineSettings
import com.itsvks.pictures.models.Vault
import com.itsvks.pictures.util.MediaOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Serializable

class MediaRepositoryImpl(
  private val context: Context,
  private val workManager: WorkManager,
  private val database: InternalDatabase,
  private val keychainHolder: KeychainHolder
) : MediaRepository {
  private val contentResolver = context.contentResolver

  override suspend fun updateInternalDatabase() {
    workManager.updateDatabase()
  }

  /**
   * TODO: Add media reordering
   */
  override fun getMedia(): Flow<Resource<List<Media>>> =
    MediaFlow(
      contentResolver = contentResolver,
      bucketId = MediaStoreBuckets.MEDIA_STORE_BUCKET_TIMELINE.id
    ).flowData().map {
      Resource.Success(it)
    }.flowOn(Dispatchers.IO)

  override fun getMediaByType(allowedMedia: AllowedMedia): Flow<Resource<List<Media>>> =
    MediaFlow(
      contentResolver = contentResolver,
      bucketId = when (allowedMedia) {
        AllowedMedia.PHOTOS -> MediaStoreBuckets.MEDIA_STORE_BUCKET_PHOTOS.id
        AllowedMedia.VIDEOS -> MediaStoreBuckets.MEDIA_STORE_BUCKET_VIDEOS.id
        AllowedMedia.BOTH -> MediaStoreBuckets.MEDIA_STORE_BUCKET_TIMELINE.id
      },
      mimeType = allowedMedia.toStringAny()
    ).flowData().map {
      Resource.Success(it)
    }.flowOn(Dispatchers.IO)

  override fun getFavorites(mediaOrder: MediaOrder): Flow<Resource<List<Media>>> =
    MediaFlow(
      contentResolver = contentResolver,
      bucketId = MediaStoreBuckets.MEDIA_STORE_BUCKET_FAVORITES.id
    ).flowData().map {
      Resource.Success(it)
    }.flowOn(Dispatchers.IO)

  override fun getTrashed(): Flow<Resource<List<Media>>> =
    MediaFlow(
      contentResolver = contentResolver,
      bucketId = MediaStoreBuckets.MEDIA_STORE_BUCKET_TRASH.id
    ).flowData().map { Resource.Success(it) }.flowOn(Dispatchers.IO)

  override fun getAlbums(mediaOrder: MediaOrder): Flow<Resource<List<Album>>> =
    AlbumsFlow(context).flowData().map {
      withContext(Dispatchers.IO) {
        val data = it.toMutableList().apply {
          replaceAll { album ->
            album.copy(isPinned = database.getPinnedDao().albumIsPinned(album.id))
          }
        }

        Resource.Success(mediaOrder.sortAlbums(data))
      }
    }.flowOn(Dispatchers.IO)

  override suspend fun insertPinnedAlbum(pinnedAlbum: PinnedAlbum) =
    database.getPinnedDao().insertPinnedAlbum(pinnedAlbum)

  override suspend fun removePinnedAlbum(pinnedAlbum: PinnedAlbum) =
    database.getPinnedDao().removePinnedAlbum(pinnedAlbum)

  override fun getPinnedAlbums(): Flow<List<PinnedAlbum>> =
    database.getPinnedDao().getPinnedAlbums()

  override suspend fun addBlacklistedAlbum(ignoredAlbum: IgnoredAlbum) =
    database.getBlacklistDao().addBlacklistedAlbum(ignoredAlbum)

  override suspend fun removeBlacklistedAlbum(ignoredAlbum: IgnoredAlbum) =
    database.getBlacklistDao().removeBlacklistedAlbum(ignoredAlbum)

  override fun getBlacklistedAlbums(): Flow<List<IgnoredAlbum>> =
    database.getBlacklistDao().getBlacklistedAlbums()

  override fun getMediaByAlbumId(albumId: Long): Flow<Resource<List<Media>>> =
    MediaFlow(
      contentResolver = contentResolver,
      bucketId = albumId,
    ).flowData().mapAsResource()

  override fun getMediaByAlbumIdWithType(
    albumId: Long,
    allowedMedia: AllowedMedia
  ): Flow<Resource<List<Media>>> =
    MediaFlow(
      contentResolver = contentResolver,
      bucketId = albumId,
      mimeType = allowedMedia.toStringAny()
    ).flowData().mapAsResource()

  override fun getAlbumsWithType(allowedMedia: AllowedMedia): Flow<Resource<List<Album>>> =
    AlbumsFlow(
      context = context,
      mimeType = allowedMedia.toStringAny()
    ).flowData().mapAsResource()

  override fun getMediaListByUris(
    listOfUris: List<Uri>,
    reviewMode: Boolean
  ): Flow<Resource<List<Media>>> =
    MediaUriFlow(
      contentResolver = contentResolver,
      uris = listOfUris,
      reviewMode = reviewMode
    ).flowData().mapAsResource(errorOnEmpty = true, errorMessage = "Media could not be opened")

  override suspend fun toggleFavorite(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>,
    favorite: Boolean
  ) {
    val intentSender = MediaStore.createFavoriteRequest(
      contentResolver,
      mediaList.map { it.uri },
      favorite
    ).intentSender
    val senderRequest: IntentSenderRequest = IntentSenderRequest.Builder(intentSender)
      .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
      .build()
    result.launch(senderRequest)
  }

  override suspend fun trashMedia(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>,
    trash: Boolean
  ) {
    val intentSender = MediaStore.createTrashRequest(
      contentResolver,
      mediaList.map { it.uri },
      trash
    ).intentSender
    val senderRequest: IntentSenderRequest = IntentSenderRequest.Builder(intentSender)
      .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
      .build()
    result.launch(senderRequest, ActivityOptionsCompat.makeTaskLaunchBehind())
  }

  override suspend fun deleteMedia(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>
  ) {
    val intentSender =
      MediaStore.createDeleteRequest(
        contentResolver,
        mediaList.map { it.uri }).intentSender
    val senderRequest: IntentSenderRequest = IntentSenderRequest.Builder(intentSender)
      .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
      .build()
    result.launch(senderRequest)
  }

  override suspend fun copyMedia(
    from: Media,
    path: String
  ): Boolean = contentResolver.copyMedia(
    from = from,
    path = path
  )

  override suspend fun renameMedia(
    media: Media,
    newName: String
  ): Boolean = contentResolver.updateMedia(
    media = media,
    contentValues = displayName(newName)
  )

  override suspend fun moveMedia(
    media: Media,
    newPath: String
  ): Boolean = contentResolver.updateMedia(
    media = media,
    contentValues = relativePath(newPath)
  )

  override suspend fun updateMediaExif(
    media: Media,
    exifAttributes: ExifAttributes
  ): Boolean = contentResolver.updateMediaExif(
    media = media,
    exifAttributes = exifAttributes
  )

  override fun saveImage(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    mimeType: String,
    relativePath: String,
    displayName: String
  ) = contentResolver.saveImage(bitmap, format, mimeType, relativePath, displayName)

  override fun overrideImage(
    uri: Uri,
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    mimeType: String,
    relativePath: String,
    displayName: String
  ) = contentResolver.overrideImage(uri, bitmap, format, mimeType, relativePath, displayName)

  override fun getVaults(): Flow<Resource<List<Vault>>> =
    context.retrieveInternalFiles {
      with(keychainHolder) {
        filesDir.listFiles()
          ?.filter { it.isDirectory && File(it, VAULT_INFO_FILE_NAME).exists() }
          ?.mapNotNull {
            try {
              File(it, VAULT_INFO_FILE_NAME).decrypt() as Vault
            } catch (e: Exception) {
              null
            }
          }
          ?: emptyList()
      }
    }


  override suspend fun createVault(
    vault: Vault,
    onSuccess: () -> Unit,
    onFailed: (reason: String) -> Unit
  ) = withContext(Dispatchers.IO) { keychainHolder.writeVaultInfo(vault, onSuccess, onFailed) }

  override suspend fun deleteVault(
    vault: Vault,
    onSuccess: () -> Unit,
    onFailed: (reason: String) -> Unit
  ) = withContext(Dispatchers.IO) { keychainHolder.deleteVault(vault, onSuccess, onFailed) }

  override fun getEncryptedMedia(vault: Vault): Flow<Resource<List<EncryptedMedia>>> =
    context.retrieveInternalFiles {
      with(keychainHolder) {
        (vaultFolder(vault).listFiles()?.filter {
          it.name.endsWith("enc")
        }?.mapNotNull {
          try {
            val id = it.nameWithoutExtension.toLong()
            vault.mediaFile(id).decrypt<EncryptedMedia>()
          } catch (e: Exception) {
            null
          }
        } ?: emptyList()).sortedByDescending { it.timestamp }
      }
    }

  override suspend fun addMedia(vault: Vault, media: Media): Boolean =
    withContext(Dispatchers.IO) {
      with(keychainHolder) {
        keychainHolder.checkVaultFolder(vault)
        val output = vault.mediaFile(media.id)
        if (output.exists()) output.delete()
        val encryptedMedia =
          media.uri.getBytes()?.let { bytes -> media.toEncryptedMedia(bytes) }
        return@withContext try {
          encryptedMedia?.let {
            output.encrypt(data = it)
          }
          true
        } catch (e: Exception) {
          e.printStackTrace()
          printError("Failed to add file: ${media.label}")
          false
        }
      }
    }

  override suspend fun restoreMedia(vault: Vault, media: EncryptedMedia): Boolean =
    withContext(Dispatchers.IO) {
      with(keychainHolder) {
        checkVaultFolder(vault)
        return@withContext try {
          val output = vault.mediaFile(media.id)
          val bitmap = BitmapFactory.decodeByteArray(media.bytes, 0, media.bytes.size)
          val restored = saveImage(
            bitmap = bitmap,
            displayName = media.label,
            mimeType = "image/png",
            format = Bitmap.CompressFormat.PNG,
            relativePath = Environment.DIRECTORY_PICTURES + "/Restored"
          ) != null
          val deleted = if (restored) output.delete() else false
          restored && deleted
        } catch (e: Exception) {
          e.printStackTrace()
          printError("Failed to restore file: ${media.label}")
          false
        }
      }
    }

  override suspend fun deleteEncryptedMedia(vault: Vault, media: EncryptedMedia): Boolean =
    withContext(Dispatchers.IO) {
      with(keychainHolder) {
        checkVaultFolder(vault)
        return@withContext try {
          vault.mediaFile(media.id).delete()
        } catch (e: Exception) {
          e.printStackTrace()
          printError("Failed to delete file: ${media.label}")
          false
        }
      }
    }

  override suspend fun deleteAllEncryptedMedia(
    vault: Vault,
    onSuccess: () -> Unit,
    onFailed: (failedFiles: List<File>) -> Unit
  ): Boolean = withContext(Dispatchers.IO) {
    with(keychainHolder) {
      checkVaultFolder(vault)
      val failedFiles = mutableListOf<File>()
      val files = vaultFolder(vault).listFiles()
      files?.forEach { file ->
        try {
          file.delete()
        } catch (e: Exception) {
          e.printStackTrace()
          printError("Failed to delete file: ${file.name}")
          failedFiles.add(file)
        }
      }
      if (failedFiles.isEmpty()) {
        onSuccess()
        true
      } else {
        onFailed(failedFiles)
        false
      }
    }
  }

  override fun getSettings(): Flow<TimelineSettings?> = database.getMediaDao().getTimelineSettings()
  override suspend fun updateSettings(settings: TimelineSettings) {
    database.getMediaDao().setTimelineSettings(settings)
  }

  companion object {
    private fun displayName(newName: String) = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
    }

    private fun relativePath(newPath: String) = ContentValues().apply {
      put(MediaStore.MediaColumns.RELATIVE_PATH, newPath)
    }

    private fun <T : Serializable> Context.retrieveInternalFiles(dataBody: suspend (ContentResolver) -> List<T>) =
      fileFlowObserver().map {
        try {
          Resource.Success(data = dataBody.invoke(contentResolver))
        } catch (e: Exception) {
          Resource.Error(message = e.localizedMessage ?: "An error occurred")
        }
      }.conflate()
  }
}