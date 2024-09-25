import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlinAndroid)
  alias(libs.plugins.kspAndroid)
  alias(libs.plugins.roomPlugin)
  alias(libs.plugins.hiltAndroid)
  alias(libs.plugins.baselineProfilePlugin)
  alias(libs.plugins.kotlinCompose)
  id("kotlin-parcelize")
  alias(libs.plugins.kotlinSerialization)
}

android {
  namespace = "com.itsvks.pictures"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.itsvks.pictures"
    minSdk = 30
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }

    base.archivesName.set("Pictures-${versionName}-$versionCode")

    javaCompileOptions {
      annotationProcessorOptions {
        arguments["room.schemaLocation"] = "$projectDir/schemas"
      }
    }
  }

  buildTypes {
    debug {
      manifestPlaceholders += mapOf(
        "appProvider" to "com.itsvks.pictures.media_provider"
      )
      buildConfigField("Boolean", "ALLOW_ALL_FILES_ACCESS", allowAllFilesAccess)
      buildConfigField("String", "CONTENT_AUTHORITY", "\"com.itsvks.pictures.media_provider\"")
    }

    release {
      manifestPlaceholders += mapOf(
        "appProvider" to "com.itsvks.pictures.media_provider"
      )
      buildConfigField("Boolean", "ALLOW_ALL_FILES_ACCESS", allowAllFilesAccess)
      buildConfigField("String", "CONTENT_AUTHORITY", "\"com.itsvks.pictures.media_provider\"")

      isMinifyEnabled = false
      isShrinkResources = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += "-Xcontext-receivers"
  }

  buildFeatures {
    compose = true
    viewBinding = true
    buildConfig = true
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  room {
    schemaDirectory("$projectDir/schemas/")
  }
}

dependencies {
  runtimeOnly(libs.androidx.profileinstaller)

  // Core
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)

  // Core - Lifecycle
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.compose.lifecycle.runtime)

  // Compose
  implementation(libs.compose.activity)
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material.icons.extended)
  implementation(libs.androidx.graphics.shapes)

  // Compose - Shimmer
  implementation(libs.compose.shimmer)
  // Compose - Material3
  implementation(libs.compose.material3)
  implementation(libs.compose.material3.window.size)
  implementation(libs.androidx.adaptive)
  implementation(libs.androidx.adaptive.layout)
  implementation(libs.androidx.adaptive.navigation)

  // Compose - Accompanists
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.accompanist.drawablepainter)

  // Android MDC - Material
  implementation(libs.material)

  // Kotlin - Coroutines
  implementation(libs.kotlinx.coroutines.core)
  runtimeOnly(libs.kotlinx.coroutines.android)

  implementation(libs.kotlinx.serialization.json)

  // Dagger - Hilt
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.dagger.hilt)
  implementation(libs.androidx.hilt.common)
  implementation(libs.androidx.hilt.work)
  ksp(libs.dagger.hilt.compiler)
  ksp(libs.androidx.hilt.compiler)

  // Room
  implementation(libs.room.runtime)
  ksp(libs.room.compiler)

  // Kotlin Extensions and Coroutines support for Room
  implementation(libs.room.ktx)

  // Coil
  implementation(libs.jxl.coder.coil)
  implementation(libs.avif.coder.coil)

  // Sketch
  implementation(libs.sketch.compose)
  implementation(libs.sketch.view)
  implementation(libs.sketch.animated)
  implementation(libs.sketch.extensions.compose)
  implementation(libs.sketch.http.ktor)
  implementation(libs.sketch.svg)
  implementation(libs.sketch.video)

  // Exo Player
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)
  implementation(libs.androidx.media3.session)
  implementation(libs.androidx.media3.exoplayer.dash)
  implementation(libs.androidx.media3.exoplayer.hls)
  implementation(libs.compose.video)

  // Exif Interface
  implementation(libs.androidx.exifinterface)

  // Datastore Preferences
  implementation(libs.datastore.prefs)

  // Fuzzy Search
  implementation(libs.fuzzywuzzy)

  // GPU Image
  implementation(libs.gpuimage)

  // Pinch to zoom
  implementation(libs.pinchzoomgrid)

  // Subsampling
  implementation(libs.zoomimage.sketch)

  // Splashscreen
  implementation(libs.androidx.core.splashscreen)

  // Jetpack Security
  implementation(libs.androidx.security.crypto)
  implementation(libs.androidx.biometric)

  // Composables - Core
  implementation(libs.core)

  // Worker
  implementation(libs.androidx.work.runtime.ktx)

  // Composable - Scrollbar
  implementation(libs.lazycolumnscrollbar)

  // Preferences
  implementation(libs.androidx.preference.ktx)

  // Gson
  implementation(libs.gson)

  // Util code
  implementation(libs.utilcodex)

  // Tests
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.ext.junit)
  debugImplementation(libs.compose.ui.tooling)
  debugRuntimeOnly(libs.compose.ui.test.manifest)
}

val allowAllFilesAccess: String
  get() {
    val fl = rootProject.file("app.properties")

    return try {
      val properties = Properties()
      properties.load(FileInputStream(fl))
      properties.getProperty("ALL_FILES_ACCESS")
    } catch (e: Exception) {
      "true"
    }
  }