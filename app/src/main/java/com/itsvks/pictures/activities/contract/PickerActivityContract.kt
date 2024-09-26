package com.itsvks.pictures.activities.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import com.itsvks.pictures.activities.PickerActivity
import com.itsvks.pictures.activities.PickerActivity.Companion.EXPORT_AS_MEDIA
import com.itsvks.pictures.activities.PickerActivity.Companion.MEDIA_LIST
import com.itsvks.pictures.models.Media

class PickerActivityContract : ActivityResultContract<Any?, List<Media>>() {
  override fun createIntent(context: Context, input: Any?): Intent {
    return Intent(context, PickerActivity::class.java).apply {
      type = "image/*"
      putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
      putExtra(EXPORT_AS_MEDIA, true)
    }
  }

  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  override fun parseResult(resultCode: Int, intent: Intent?): List<Media> {
    if (resultCode != Activity.RESULT_OK || intent == null) {
      return emptyList()
    }

    val list: List<Media> = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
      intent.getParcelableArrayExtra(MEDIA_LIST, Media::class.java)?.toList() ?: emptyList()
    } else {
      (intent.getParcelableArrayExtra(MEDIA_LIST) as Array<out Media>?)?.toList() ?: emptyList()
    }
    return list
  }
}