package com.itsvks.pictures.core.settings

sealed class Position {
  data object Top : Position()
  data object Middle : Position()
  data object Bottom : Position()
  data object Alone : Position()
}