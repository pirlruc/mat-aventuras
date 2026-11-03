plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "pt.mataventuras.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "pt.mataventuras.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.3.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        ndk {
            // Drop 32-bit x86; keep ARM tablets and x86_64 emulators.
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }
    // Godot stores project files (and optional hidden dirs) under assets/.
    androidResources {
        noCompress += listOf("gd", "tscn", "godot", "cfg", "uid", "import", "json")
        ignoreAssetsPattern =
            "!.svn:!.git:!.gitignore:!.ds_store:!*.scc:<dir>_*:!CVS:!thumbs.db:!picasa.ini:!*~:!<dir>godot"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    sourceSets.named("main") {
        assets.srcDir(rootProject.file("domain/src/main/resources"))
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            pickFirsts += "**/libc++_shared.so"
            // Godot ships uncompressed .so files that some devices fail to map
            // unless they are extracted at install time.
            useLegacyPackaging = true
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
    lint {
        abortOnError = true
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

androidComponents {
    beforeVariants { variant ->
        if (variant.buildType == "release") {
            (variant as com.android.build.api.variant.HasHostTestsBuilder)
                .hostTests
                .getValue(com.android.build.api.variant.HostTestBuilder.UNIT_TEST_TYPE)
                .enable = false
        }
    }
}

dependencies {
    implementation(project(":data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.godot)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt.yml"))
    source.setFrom("src/main/kotlin")
}

kover {
    useJacoco()
    reports {
        filters {
            excludes {
                classes("pt.mataventuras.app.BuildConfig")
                classes("*ComposableSingletons*")
                // Godot JNI host: Robolectric cannot load libgodot_android.so.
                // Instrumented tests cover the plugin process (ANDROID-TEST-001).
                classes("pt.mataventuras.app.engine.godot.*")
            }
        }
    }
}

afterEvaluate {
    tasks.named("testDebugUnitTest").configure {
        finalizedBy("koverXmlReportDebug")
    }
}
