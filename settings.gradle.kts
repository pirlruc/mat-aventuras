rootProject.name = "mat-aventuras"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Lets the Gradle Daemon Toolchain auto-provision JDK 17 when it is missing.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":domain")

val sdkDir = resolveSdk()
if (sdkDir != null) {
    include(":data")
    include(":app")
}

fun resolveSdk(): String? {
    val env = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (!env.isNullOrBlank() && file(env).isDirectory) return env
    val local = file("local.properties")
    if (!local.exists()) return null
    val line = local.readLines().firstOrNull { it.startsWith("sdk.dir=") } ?: return null
    val path = line.removePrefix("sdk.dir=").trim().replace("\\\\", "/")
    return path.takeIf { file(it).isDirectory }
}
