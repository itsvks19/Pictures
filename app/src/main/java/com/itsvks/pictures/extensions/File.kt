package com.itsvks.pictures.extensions

import android.content.Context
import android.security.keystore.UserNotAuthenticatedException
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedFile.FileEncryptionScheme
import androidx.security.crypto.MasterKey
import com.itsvks.pictures.PicturesApp
import com.itsvks.pictures.R
import com.itsvks.pictures.database.KeychainHolder
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.Serializable
import java.math.RoundingMode
import java.security.GeneralSecurityException
import java.text.DecimalFormat

@Throws(
  GeneralSecurityException::class,
  IOException::class,
  FileNotFoundException::class,
  UserNotAuthenticatedException::class
)
fun <T : Serializable> File.decrypt(
  context: Context = PicturesApp.instance,
  key: MasterKey = KeychainHolder.masterKey,
): T = EncryptedFile.Builder(
  context,
  this,
  key,
  FileEncryptionScheme.AES256_GCM_HKDF_4KB
).build().openFileInput().use {
  fromByteArray(it.readBytes())
}

@Throws(
  GeneralSecurityException::class,
  IOException::class,
  FileNotFoundException::class,
  UserNotAuthenticatedException::class
)
fun <T : Serializable> File.encrypt(
  context: Context = PicturesApp.instance,
  key: MasterKey = KeychainHolder.masterKey,
  data: T
) {
  EncryptedFile.Builder(
    context,
    this,
    key,
    FileEncryptionScheme.AES256_GCM_HKDF_4KB
  ).build().openFileOutput().use {
    it.write(data.toByteArray())
  }
}

fun String.toFile() = File(this)

fun File.formattedFileSize(context: Context): String {
  val kb = context.getString(R.string.kb)
  val mb = context.getString(R.string.mb)
  val gb = context.getString(R.string.gb)

  var fileSizeName = kb
  var fileSize = length() / 1024

  if (fileSize > 1024) {
    fileSize /= 1024
    fileSizeName = mb

    if (fileSize > 1024) {
      fileSize /= 1024
      fileSizeName = gb
    }
  }

  val roundingSize = DecimalFormat("#.##").apply {
    roundingMode = RoundingMode.DOWN
  }

  return "${roundingSize.format(fileSize)} $fileSizeName"
}