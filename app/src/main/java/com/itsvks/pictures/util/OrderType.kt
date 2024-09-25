package com.itsvks.pictures.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
sealed class OrderType : Parcelable {
  @Parcelize
  @Serializable
  data object Ascending : OrderType()

  @Parcelize
  @Serializable
  data object Descending : OrderType()
}