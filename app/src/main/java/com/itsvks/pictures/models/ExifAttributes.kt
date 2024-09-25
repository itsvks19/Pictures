package com.itsvks.pictures.models

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.exifinterface.media.ExifInterface
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExifAttributes(
  var manufacturerName: String? = null,
  var modelName: String? = null,
  var apertureValue: Double? = null,
  var focalLength: Double? = null,
  var isoValue: Int? = null,
  var imageDescription: String? = null,
  var gpsLatLong: DoubleArray? = null
) : Parcelable {

  fun writeExif(exifInterface: ExifInterface) {
    exifInterface.apply {
      setAttribute(ExifInterface.TAG_MAKE, manufacturerName)
      setAttribute(ExifInterface.TAG_MODEL, modelName)
      setAttribute(ExifInterface.TAG_APERTURE_VALUE, apertureValue?.toString())
      setAttribute(ExifInterface.TAG_FOCAL_LENGTH, focalLength?.toString())
      setAttribute(ExifInterface.TAG_ISO_SPEED, isoValue?.toString())
      setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, imageDescription)
      if (gpsLatLong != null) {
        setLatLong(gpsLatLong!![0], gpsLatLong!![1])
      } else {
        setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
        setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
        setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
        setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ExifAttributes

    if (manufacturerName != other.manufacturerName) return false
    if (modelName != other.modelName) return false
    if (apertureValue != other.apertureValue) return false
    if (focalLength != other.focalLength) return false
    if (isoValue != other.isoValue) return false
    if (imageDescription != other.imageDescription) return false
    if (gpsLatLong != null) {
      if (other.gpsLatLong == null) return false
      if (!gpsLatLong.contentEquals(other.gpsLatLong)) return false
    } else if (other.gpsLatLong != null) return false

    return true
  }

  override fun hashCode(): Int {
    var result = manufacturerName?.hashCode() ?: 0
    result = 31 * result + (modelName?.hashCode() ?: 0)
    result = 31 * result + (apertureValue?.hashCode() ?: 0)
    result = 31 * result + (focalLength?.hashCode() ?: 0)
    result = 31 * result + (isoValue ?: 0)
    result = 31 * result + (imageDescription?.hashCode() ?: 0)
    result = 31 * result + (gpsLatLong?.contentHashCode() ?: 0)
    return result
  }

  companion object {
    fun fromExifInterface(exifInterface: ExifInterface): ExifAttributes {
      return with(exifInterface) {
        val manufacturerName = getAttribute(ExifInterface.TAG_MAKE)
        val modelName = getAttribute(ExifInterface.TAG_MODEL)
        val apertureValue = getAttributeDouble(ExifInterface.TAG_APERTURE_VALUE, 0.0)
        val focalLength = getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
        val isoValue = getAttributeInt(ExifInterface.TAG_ISO_SPEED, 0)
        val imageDescription = getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)

        ExifAttributes(
          manufacturerName,
          modelName,
          apertureValue,
          focalLength,
          isoValue,
          imageDescription,
          latLong
        )
      }
    }
  }
}

@Composable
fun rememberExifAttributes(exifInterface: ExifInterface? = null) = remember {
  if (exifInterface != null) mutableStateOf(ExifAttributes.fromExifInterface(exifInterface))
  else mutableStateOf(ExifAttributes())
}
