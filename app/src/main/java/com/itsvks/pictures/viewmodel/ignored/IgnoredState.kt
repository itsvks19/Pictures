package com.itsvks.pictures.viewmodel.ignored

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.itsvks.pictures.models.IgnoredAlbum
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class IgnoredState(
  val albums: List<IgnoredAlbum> = emptyList()
) : Parcelable

