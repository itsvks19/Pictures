package com.itsvks.pictures.models

import androidx.compose.runtime.Stable

@Stable
sealed class MediaItem {
  abstract val key: String

  @Stable
  data class Header(
    override val key: String,
    val text: String,
    val data: Set<Long>
  ) : MediaItem()

  @Stable
  data class MediaViewItem(
    override val key: String,
    val media: Media
  ) : MediaItem()
}

@Stable
sealed class EncryptedMediaItem {
  abstract val key: String

  @Stable
  data class Header(
    override val key: String,
    val text: String,
    val data: Set<Long>
  ) : EncryptedMediaItem()

  @Stable
  data class MediaViewItem(
    override val key: String,
    val media: EncryptedMedia
  ) : EncryptedMediaItem()
}

