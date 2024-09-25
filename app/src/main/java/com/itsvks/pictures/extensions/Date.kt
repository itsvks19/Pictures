package com.itsvks.pictures.extensions

import android.os.Parcelable
import com.itsvks.pictures.core.Constants
import kotlinx.parcelize.Parcelize
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun getDateHeader(startDate: Date, endDate: Date): String {
  return if (startDate.year == endDate.year) {
    if (startDate.month == endDate.month) {
      if (startDate.day == endDate.day) {
        "${startDate.month} ${startDate.day}, ${startDate.year}"
      } else "${startDate.month} ${startDate.day} - ${endDate.day}, ${startDate.year}"
    } else
      "${startDate.month} ${startDate.day} - ${endDate.month} ${endDate.day}, ${startDate.year}"
  } else {
    "${startDate.month} ${startDate.day}, ${startDate.year} - ${endDate.month} ${endDate.day}, ${endDate.year}"
  }
}

fun getMonth(date: String): String {
  return try {
    val dateFormatExtended = SimpleDateFormat(Constants.EXTENDED_DATE_FORMAT, Locale.US).parse(date)
    val cal = Calendar.getInstance(Locale.US).apply { timeInMillis = dateFormatExtended!!.time }
    val month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.US)!!
    val year = cal.get(Calendar.YEAR)
    "$month $year"
  } catch (e: ParseException) {
    try {
      val dateFormat = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT, Locale.US).parse(date)
      val cal = Calendar.getInstance(Locale.US).apply { timeInMillis = dateFormat!!.time }
      cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.US)!!
    } catch (e: ParseException) {
      ""
    }
  }
}

fun getYear(date: String): String {
  return try {
    val dateFormatExtended = SimpleDateFormat(Constants.EXTENDED_DATE_FORMAT, Locale.US).parse(date)
    val cal = Calendar.getInstance(Locale.US).apply { timeInMillis = dateFormatExtended!!.time }
    val year = cal.get(Calendar.YEAR)
    year.toString()
  } catch (e: ParseException) {
    val cal = Calendar.getInstance(Locale.US).apply { timeInMillis = System.currentTimeMillis() }
    cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.US)!!
    cal.get(Calendar.YEAR).toString()
  }
}

@Parcelize
data class Date(val month: String, val day: Int, val year: Int) : Parcelable