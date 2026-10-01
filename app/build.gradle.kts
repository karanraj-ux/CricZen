plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}
android {
  namespace = "com.karanrajux.criczen"
  compileSdk = 35
  defaultConfig {
    applicationId = "com.karanrajux.criczen"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  // Release signing: provide the keystore via local.properties or env vars.
  //   criczen.keystore.path / CRICZEN_KEYSTORE_PATH  (default: <root>/criczen-release.keystore)
  //   criczen.keystore.password / CRICZEN_KEYSTORE_PASSWORD
  //   criczen.key.alias / CRICZEN_KEY_ALIAS          (default: criczen)
  //   criczen.key.password / CRICZEN_KEY_PASSWORD    (default: same as keystore password)
  // The keystore itself is NEVER committed (see .gitignore). Back it up safely —
  // losing it means you can never ship an update to this app again.
  //
  // NOTE: the missing-keystore error is raised only when a release task actually
  // runs (see taskGraph check at the bottom), so debug/CI smoke builds keep working.
  val releaseKeystoreFile = file(
    System.getenv("CRICZEN_KEYSTORE_PATH")
      ?: (findProperty("criczen.keystore.path") as String? ?: "${rootDir}/criczen-release.keystore")
  )
  val releaseKeystorePassword: String? =
    System.getenv("CRICZEN_KEYSTORE_PASSWORD") ?: findProperty("criczen.keystore.password") as String?
  val releaseKeyAlias: String =
    System.getenv("CRICZEN_KEY_ALIAS") ?: (findProperty("criczen.key.alias") as String? ?: "criczen")
  val releaseKeyPassword: String? =
    System.getenv("CRICZEN_KEY_PASSWORD") ?: (findProperty("criczen.key.password") as String? ?: releaseKeystorePassword)
  val hasReleaseKeystore = releaseKeystoreFile.exists()
  signingConfigs {
    create("release") {
      if (hasReleaseKeystore) {
        storeFile = releaseKeystoreFile
        storePassword = releaseKeystorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
        enableV1Signing = true
        enableV2Signing = true
      }
    }
  }
  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
    }
    debug { /* AGP default debug signing */ }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation("androidx.browser:browser:1.8.0")

  implementation(libs.koin.android)
  implementation(libs.koin.androidx.compose)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.glance.appwidget)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}

// Refuse to build a release artifact without the real keystore —
// a debug-signed "release" must never ship.
gradle.taskGraph.whenReady {
  val wantsRelease = allTasks.any {
    it.name.equals("assembleRelease", ignoreCase = true) ||
      it.name.equals("bundleRelease", ignoreCase = true)
  }
  if (wantsRelease) {
    val ksFile = file(
      System.getenv("CRICZEN_KEYSTORE_PATH")
        ?: (findProperty("criczen.keystore.path") as String? ?: "${rootDir}/criczen-release.keystore")
    )
    if (!ksFile.exists()) {
      throw GradleException(
        "Release keystore not found: ${ksFile.absolutePath}\n" +
          "Generate one with:\n" +
          "  keytool -genkeypair -v -keystore criczen-release.keystore -alias criczen " +
          "-keyalg RSA -keysize 2048 -validity 10000\n" +
          "then point criczen.keystore.path (local.properties) or CRICZEN_KEYSTORE_PATH at it."
      )
    }
  }
}
