package com.itsvks.pictures.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.itsvks.pictures.database.InternalDatabase
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.extensions.isMediaUpToDate
import com.itsvks.pictures.extensions.mediaStoreVersion
import com.itsvks.pictures.extensions.printDebug
import com.itsvks.pictures.models.MediaVersion
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext

fun WorkManager.updateDatabase() {
  val uniqueWork = OneTimeWorkRequestBuilder<DatabaseUpdaterWorker>()
    .setConstraints(
      Constraints.Builder()
        .setRequiresStorageNotLow(true)
        .build()
    )
    .build()

  enqueueUniqueWork("DatabaseUpdaterWorker", ExistingWorkPolicy.KEEP, uniqueWork)
}

@HiltWorker
class DatabaseUpdaterWorker @AssistedInject constructor(
  private val database: InternalDatabase,
  private val repository: MediaRepository,
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters
) :
  CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    if (database.isMediaUpToDate(appContext)) {
      printDebug("Database is up to date")
      return Result.success()
    }
    withContext(Dispatchers.IO) {
      val mediaVersion = appContext.mediaStoreVersion
      printDebug("Database is not up to date. Updating to version $mediaVersion")
      database.getMediaDao().setMediaVersion(MediaVersion(mediaVersion))
      val media = repository.getMedia().map { it.data ?: emptyList() }.single()
      database.getMediaDao().updateMedia(media)
      delay(5000)
    }

    return Result.success()
  }
}
