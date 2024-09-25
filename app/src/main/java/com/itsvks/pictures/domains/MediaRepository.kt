package com.itsvks.pictures.domains

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.itsvks.pictures.core.Resource
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
import kotlinx.coroutines.flow.Flow
import java.io.File

interface MediaRepository {
  suspend fun updateInternalDatabase()
  fun getMedia(): Flow<Resource<List<Media>>>
  fun getMediaByType(allowedMedia: AllowedMedia): Flow<Resource<List<Media>>>
  fun getFavorites(mediaOrder: MediaOrder): Flow<Resource<List<Media>>>
  fun getTrashed(): Flow<Resource<List<Media>>>
  fun getAlbums(mediaOrder: MediaOrder): Flow<Resource<List<Album>>>
  suspend fun insertPinnedAlbum(pinnedAlbum: PinnedAlbum)
  suspend fun removePinnedAlbum(pinnedAlbum: PinnedAlbum)
  fun getPinnedAlbums(): Flow<List<PinnedAlbum>>
  suspend fun addBlacklistedAlbum(ignoredAlbum: IgnoredAlbum)
  suspend fun removeBlacklistedAlbum(ignoredAlbum: IgnoredAlbum)
  fun getBlacklistedAlbums(): Flow<List<IgnoredAlbum>>
  fun getMediaByAlbumId(albumId: Long): Flow<Resource<List<Media>>>

  fun getMediaByAlbumIdWithType(
    albumId: Long,
    allowedMedia: AllowedMedia
  ): Flow<Resource<List<Media>>>

  fun getAlbumsWithType(allowedMedia: AllowedMedia): Flow<Resource<List<Album>>>
  fun getMediaListByUris(listOfUris: List<Uri>, reviewMode: Boolean): Flow<Resource<List<Media>>>

  suspend fun toggleFavorite(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>,
    favorite: Boolean
  )

  suspend fun trashMedia(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>,
    trash: Boolean
  )

  suspend fun copyMedia(
    from: Media,
    path: String
  ): Boolean

  suspend fun deleteMedia(
    result: ActivityResultLauncher<IntentSenderRequest>,
    mediaList: List<Media>
  )

  suspend fun renameMedia(
    media: Media,
    newName: String
  ): Boolean

  suspend fun moveMedia(
    media: Media,
    newPath: String
  ): Boolean

  suspend fun updateMediaExif(
    media: Media,
    exifAttributes: ExifAttributes
  ): Boolean

  fun saveImage(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    mimeType: String,
    relativePath: String,
    displayName: String
  ): Uri?

  fun overrideImage(
    uri: Uri,
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    mimeType: String,
    relativePath: String,
    displayName: String
  ): Boolean

  fun getVaults(): Flow<Resource<List<Vault>>>

  suspend fun createVault(
    vault: Vault,
    onSuccess: () -> Unit,
    onFailed: (reason: String) -> Unit
  )

  suspend fun deleteVault(
    vault: Vault,
    onSuccess: () -> Unit,
    onFailed: (reason: String) -> Unit
  )

  fun getEncryptedMedia(vault: Vault): Flow<Resource<List<EncryptedMedia>>>
  suspend fun addMedia(vault: Vault, media: Media): Boolean
  suspend fun restoreMedia(vault: Vault, media: EncryptedMedia): Boolean
  suspend fun deleteEncryptedMedia(vault: Vault, media: EncryptedMedia): Boolean

  suspend fun deleteAllEncryptedMedia(
    vault: Vault,
    onSuccess: () -> Unit,
    onFailed: (failedFiles: List<File>) -> Unit
  ): Boolean

  fun getSettings(): Flow<TimelineSettings?>
  suspend fun updateSettings(settings: TimelineSettings)
}