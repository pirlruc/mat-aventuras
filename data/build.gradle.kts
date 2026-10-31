plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "pt.mataventuras.data"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
    sourceSets {
        getByName("test") {
            assets.srcDir("$projectDir/schemas")
        }
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
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

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt.yml"))
    source.setFrom("src/main/kotlin")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    // Keep Java *_Impl so Kover filters and existing tests stay valid.
    arg("room.generateKotlin", "false")
}

dependencies {
    api(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlinx.coroutines.android)
}

kover {
    reports {
        filters {
            excludes {
                classes("pt.mataventuras.data.BuildConfig")
                classes("pt.mataventuras.data.local.*_Impl")
                classes("pt.mataventuras.data.local.*_Impl\$*")
            }
        }
    }
}

afterEvaluate {
    tasks.named("testDebugUnitTest").configure {
        finalizedBy("koverXmlReportDebug")
    }
}
