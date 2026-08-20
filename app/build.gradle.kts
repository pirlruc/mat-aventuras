plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

android {
    namespace = "pt.mataventuras.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "pt.mataventuras.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        ndk {
            // Drop 32-bit x86; keep ARM tablets and x86_64 emulators.
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }
    // Godot stores project files (and optional hidden dirs) under assets/.
    androidResources {
        ignoreAssetsPattern =
            "!.svn:!.git:!.gitignore:!.ds_store:!*.scc:<dir>_*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
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
        lintConfig = file("lint.xml")
    }
}

androidComponents {
    beforeVariants { variant ->
        if (variant.buildType == "release") {
            variant.enableUnitTest = false
        }
    }
}

dependencies {
    implementation(project(":data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
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
}

kover {
    reports {
        filters {
            excludes {
                classes("pt.mataventuras.app.BuildConfig")
                classes("*ComposableSingletons*")
                // Compose compiler restart-group branches cannot be exhausted under Robolectric.
                // Screen rules live in UiLogic and stay inside the 95% gate.
                annotatedBy("androidx.compose.runtime.Composable")
                classes("*Kt$*")
                classes("*Activity$*")
                // Godot JNI host: Robolectric cannot load libgodot_android.so.
                classes("pt.mataventuras.app.engine.godot.*")
                // Device-only GL/Compose surfaces; Robolectric uses session/loop without these views.
                classes("pt.mataventuras.app.engine.NativeKartHost")
                classes("pt.mataventuras.app.engine.NativeRunnerHost")
            }
        }
        verify {
            rule {
                bound {
                    minValue.set(95)
                    coverageUnits.set(kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE)
                }
            }
            rule {
                bound {
                    minValue.set(95)
                    coverageUnits.set(kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
                }
            }
        }
    }
}

afterEvaluate {
    tasks.named("testDebugUnitTest").configure {
        finalizedBy("koverXmlReportDebug")
    }
}
