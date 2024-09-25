package com.itsvks.pictures.extensions

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberGeocoder(): Geocoder? {
  val geocoder = Geocoder(LocalContext.current)
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && Geocoder.isPresent())
    geocoder else null
}

fun Geocoder.getLocation(
  lat: Double,
  long: Double,
  onLocationFound: (Address?) -> Unit
) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    getFromLocation(lat, long, 1) {
      if (it.isEmpty()) onLocationFound(null)
      else onLocationFound(it.first())
    }
  } else onLocationFound(null)
}