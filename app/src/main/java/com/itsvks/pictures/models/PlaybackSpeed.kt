package com.itsvks.pictures.models

data class PlaybackSpeed(
  val speed: Float,
  val label: String,
  val isAuto: Boolean = false
)
