package com.itsvks.pictures.screens.ignored.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.itsvks.pictures.models.Album
import com.itsvks.pictures.models.AlbumState
import com.itsvks.pictures.models.IgnoredAlbum

@Composable
fun SetupTypeSelectionScreen(
  modifier: Modifier = Modifier,
  onGoBack: () -> Unit,
  onNext: () -> Unit,
  initialAlbum: Album?,
  ignoredAlbums: List<IgnoredAlbum>,
  albumsState: State<AlbumState>,
  onAlbumChanged: (Album) -> Unit
) {

}

private class CustomRotatingMorphShape(
  private val morph: Morph,
  private val percentage: Float,
  private val rotation: Float
) : Shape {
  private val matrix = Matrix()

  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    matrix.apply {
      scale(size.width / 2f, size.height / 2f)
      translate(1f, 1f)
      rotateZ(rotation)
    }

    val path = morph.toPath(progress = percentage).asComposePath()
    path.transform(matrix)

    return Outline.Generic(path)
  }
}