package com.itsvks.pictures

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.decode.supportAnimatedGif
import com.github.panpf.sketch.decode.supportAnimatedHeif
import com.github.panpf.sketch.decode.supportAnimatedWebp
import com.github.panpf.sketch.decode.supportSvg
import com.github.panpf.sketch.decode.supportVideoFrame
import com.github.panpf.sketch.http.KtorStack
import com.github.panpf.sketch.request.supportPauseLoadWhenScrolling
import com.github.panpf.sketch.request.supportSaveCellularTraffic
import com.github.panpf.sketch.util.appCacheDirectory
import dagger.hilt.android.HiltAndroidApp
import okio.FileSystem
import javax.inject.Inject

@HiltAndroidApp
class PicturesApp : Application(), SingletonSketch.Factory, Configuration.Provider {

  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

  companion object {
    @JvmStatic
    val instance by lazy { PicturesApp() }
  }

  override fun createSketch(context: PlatformContext): Sketch {
    return Sketch.Builder(this).apply {
      httpStack(KtorStack())
      components {
        supportSaveCellularTraffic()
        supportPauseLoadWhenScrolling()
        supportSvg()
        supportVideoFrame()
        supportAnimatedGif()
        supportAnimatedHeif()
        supportAnimatedWebp()
      }

      val diskCache = DiskCache.Builder(context, FileSystem.SYSTEM)
        .directory(context.appCacheDirectory())
        .maxSize(150 * 1024 * 1024).build()

      resultCache(diskCache)
      downloadCache(diskCache)
    }.build()
  }
}