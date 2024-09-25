package com.itsvks.pictures.extensions

import android.text.format.DateFormat
import com.itsvks.pictures.core.Constants
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

fun Long.getDate(): Date {
  val mediaDate = Calendar.getInstance(Locale.US)
  mediaDate.timeInMillis = this * 1000L
  return Date(
    month = mediaDate.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.US)!!,
    day = mediaDate.get(Calendar.DAY_OF_MONTH),
    year = mediaDate.get(Calendar.YEAR)
  )
}

fun Long.getDate(
  format: CharSequence = Constants.DEFAULT_DATE_FORMAT
): String {
  val mediaDate = Calendar.getInstance(Locale.US)
  mediaDate.timeInMillis = this * 1000L
  return DateFormat.format(format, mediaDate).toString()
}

fun Long.getDate(
  format: CharSequence = Constants.DEFAULT_DATE_FORMAT,
  weeklyFormat: CharSequence = Constants.WEEKLY_DATE_FORMAT,
  extendedFormat: CharSequence = Constants.EXTENDED_DATE_FORMAT,
  stringToday: String,
  stringYesterday: String
): String {
  val currentDate = Calendar.getInstance(Locale.US)
  currentDate.timeInMillis = System.currentTimeMillis()
  val mediaDate = Calendar.getInstance(Locale.US)
  mediaDate.timeInMillis = this * 1000L
  val different: Long = System.currentTimeMillis() - mediaDate.timeInMillis
  val secondsInMilli: Long = 1000
  val minutesInMilli = secondsInMilli * 60
  val hoursInMilli = minutesInMilli * 60
  val daysInMilli = hoursInMilli * 24

  val daysDifference = different / daysInMilli

  return when (daysDifference.toInt()) {
    0 -> {
      if (currentDate.get(Calendar.DATE) != mediaDate.get(Calendar.DATE)) {
        stringYesterday
      } else {
        stringToday
      }
    }

    1 -> {
      stringYesterday
    }

    else -> {
      if (daysDifference.toInt() in 2..5) {
        DateFormat.format(weeklyFormat, mediaDate).toString()
      } else {
        if (currentDate.get(Calendar.YEAR) > mediaDate.get(Calendar.YEAR)) {
          DateFormat.format(extendedFormat, mediaDate).toString()
        } else DateFormat.format(format, mediaDate).toString()
      }
    }
  }
}

fun Long.getMonth(): String {
  val currentDate =
    Calendar.getInstance(Locale.US).apply { timeInMillis = System.currentTimeMillis() }
  val mediaDate = Calendar.getInstance(Locale.US).apply { timeInMillis = this@getMonth * 1000L }
  val month = mediaDate.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.US)!!
  val year = mediaDate.get(Calendar.YEAR)
  return if (currentDate.get(Calendar.YEAR) != mediaDate.get(Calendar.YEAR))
    "$month $year"
  else month
}

fun Long.formatMinSec(): String {
  return if (this == 0L) {
    "00:00"
  } else {
    String.format(
      Locale.getDefault(),
      "%02d:%02d",
      TimeUnit.MILLISECONDS.toMinutes(this),
      TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
        TimeUnit.MILLISECONDS.toMinutes(
          this
        )
      )
    )
  }
}

fun Long.formatSize(): String {
  if (this <= 0) return "0 B"

  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()

  val formattedSize = this / 1024.0.pow(digitGroups.toDouble())
  return String.format(Locale.getDefault(), "%.2f %s", formattedSize, units[digitGroups])
}