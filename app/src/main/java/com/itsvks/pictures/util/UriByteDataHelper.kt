package com.itsvks.pictures.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itsvks.pictures.BuildConfig
import java.io.File
import java.io.FileOutputStream

object UriByteDataHelper {
  fun getUri(context: Context, data: ByteArray, extension: String, isVideo: Boolean): Uri {
    // Create a temporary file
    val tempFile =
      File(context.cacheDir, "shared_${if (isVideo) "video" else "image"}.${extension}")

    // Write the ByteArray to the temporary file
    FileOutputStream(tempFile).use { fileOutputStream ->
      fileOutputStream.write(data)
      fileOutputStream.flush()
    }

    // Get the URI of the temporary file using FileProvider
    return FileProvider.getUriForFile(context, BuildConfig.CONTENT_AUTHORITY, tempFile)
  }
}