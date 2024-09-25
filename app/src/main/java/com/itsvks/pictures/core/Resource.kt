package com.itsvks.pictures.core

sealed class Resource<T>(var data: T? = null, val message: String? = null) {
  class Success<T>(data: T) : Resource<T>(data)
  class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
