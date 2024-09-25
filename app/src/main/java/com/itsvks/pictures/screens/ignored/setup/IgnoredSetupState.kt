package com.itsvks.pictures.screens.ignored.setup

import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.IgnoredAlbum

data class IgnoredSetupState(
  val label: String = "",
  val location: Int = IgnoredAlbum.ALBUMS_ONLY,
  val type: IgnoredType = IgnoredType.SELECTION(null),
  val matchedAlbums: List<Album> = emptyList(),
  val stage: IgnoredSetupStage = IgnoredSetupStage.LABEL
)
