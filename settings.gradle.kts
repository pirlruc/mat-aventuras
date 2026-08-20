rootProject.name = "mat-aventuras"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":dominio")

val sdkDir = resolverSdk()
if (sdkDir != null) {
    include(":dados")
    include(":app")
}

fun resolverSdk(): String? {
    val env = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (!env.isNullOrBlank() && file(env).isDirectory) return env
    val local = file("local.properties")
    if (!local.exists()) return null
    val linha = local.readLines().firstOrNull { it.startsWith("sdk.dir=") } ?: return null
    val caminho = linha.removePrefix("sdk.dir=").trim().replace("\\\\", "/")
    return caminho.takeIf { file(it).isDirectory }
}
