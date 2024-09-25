package com.itsvks.pictures.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.itsvks.pictures.database.InternalDatabase
import com.itsvks.pictures.database.KeychainHolder
import com.itsvks.pictures.domains.MediaHandleUseCase
import com.itsvks.pictures.domains.MediaRepository
import com.itsvks.pictures.domains.repository.MediaRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
    return context.contentResolver
  }

  @Provides
  @Singleton
  fun provideDatabase(app: Application): InternalDatabase {
    return Room.databaseBuilder(app, InternalDatabase::class.java, InternalDatabase.NAME)
      .build()
  }

  @Provides
  @Singleton
  fun provideKeychainHolder(@ApplicationContext context: Context): KeychainHolder {
    return KeychainHolder(context)
  }

  @Provides
  @Singleton
  fun provideMediaHandleUseCase(
    repository: MediaRepository,
    @ApplicationContext context: Context
  ): MediaHandleUseCase {
    return MediaHandleUseCase(repository, context)
  }

  @Provides
  @Singleton
  fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
    return WorkManager.getInstance(context)
  }

  @Provides
  @Singleton
  fun provideMediaRepository(
    @ApplicationContext context: Context,
    workManager: WorkManager,
    database: InternalDatabase,
    keychainHolder: KeychainHolder
  ): MediaRepository {
    return MediaRepositoryImpl(context, workManager, database, keychainHolder)
  }
}
