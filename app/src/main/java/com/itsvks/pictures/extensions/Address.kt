package com.itsvks.pictures.extensions

import android.location.Address
import androidx.core.text.isDigitsOnly

val Address.formattedAddress: String
  get() {
    var address = ""
    if (!featureName.isNullOrBlank() && !featureName.isDigitsOnly()) address += featureName
    else if (!subLocality.isNullOrBlank()) address += subLocality

    if (!locality.isNullOrBlank()) {
      address += if (address.isEmpty()) locality
      else ", $locality"
    }

    if (!countryName.isNullOrBlank()) {
      address += if (address.isEmpty()) countryName
      else ", $countryName"
    }

    return address
  }

val Address.locationTag: String
  get() {
    return if (!featureName.isNullOrBlank() && !featureName.isDigitsOnly()) featureName
    else if (!subLocality.isNullOrBlank()) subLocality
    else locality
  }