package com.itsvks.pictures.extensions

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> fromByteArray(byteArray: ByteArray): T {
  return byteArray.inputStream().use { bais ->
    ObjectInputStream(bais).use { it.readObject() as T }
  }
}

fun Serializable.toByteArray(): ByteArray {
  return ByteArrayOutputStream().use { baos ->
    ObjectOutputStream(baos).use {
      it.writeObject(this)
      it.flush()
      baos.toByteArray()
    }
  }
}