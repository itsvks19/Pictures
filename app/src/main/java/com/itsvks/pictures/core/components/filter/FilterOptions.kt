package com.itsvks.pictures.core.components.filter

import com.itsvks.pictures.util.MediaOrder

data class FilterOption(
  val titleRes: Int = -1,
  val onClick: (MediaOrder) -> Unit = {},
  val filterKind: FilterKind = FilterKind.DATE
)

enum class FilterKind {
  DATE, NAME
}