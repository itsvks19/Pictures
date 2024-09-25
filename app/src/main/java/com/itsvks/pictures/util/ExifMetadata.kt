package com.itsvks.pictures.util

import androidx.exifinterface.media.ExifInterface
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

class ExifMetadata(exifInterface: ExifInterface) {
  val manufacturerName = exifInterface.getAttribute(ExifInterface.TAG_MAKE)
  val modelName = exifInterface.getAttribute(ExifInterface.TAG_MODEL)
  val apertureValue = exifInterface.getAttributeDouble(ExifInterface.TAG_APERTURE_VALUE, 0.0)
  val focalLength = exifInterface.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
  val isoValue = exifInterface.getAttributeInt(ExifInterface.TAG_ISO_SPEED, 0)
  val imageWidth = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)
  val imageHeight = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1)

  val imageMp: String = DecimalFormat("#.#").apply {
    roundingMode = RoundingMode.DOWN
  }.format(imageWidth * imageHeight / 1024000.0)

  val imageDescription = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)
  val lensDescription
    get() = if (!manufacturerName.isNullOrEmpty() && !modelName.isNullOrEmpty() && apertureValue != 0.0) {
      "$manufacturerName $modelName - f/$apertureValue - $imageMp MP"
    } else null

  val gpsLatLong = exifInterface.latLong

  val formattedCoords
    get() = if (gpsLatLong != null) String.format(
      Locale.getDefault(), "%.3f, %.3f", gpsLatLong[0], gpsLatLong[1]
    ) else null
}