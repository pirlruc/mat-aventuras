import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "pt.mataventuras.data"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    api(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
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
        verify {
            rule {
                bound {
                    minValue.set(95)
                    coverageUnits.set(CoverageUnit.LINE)
                }
            }
            rule {
                bound {
                    minValue.set(95)
                    coverageUnits.set(CoverageUnit.BRANCH)
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
