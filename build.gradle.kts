import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

// KT-TEST-002 / CI-022: one verify block for every Kover module.
// Floors come from config/kotlin.thresholds.yml. A missing file or key fails closed.
subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlinx.kover") {
        val thresholds = rootProject.file("config/kotlin.thresholds.yml")
        check(thresholds.isFile) { "missing ${thresholds.path}" }
        fun threshold(key: String): Int {
            val line =
                thresholds.readLines().firstOrNull { raw ->
                    raw.trim().startsWith("$key:")
                } ?: error("required threshold '$key' missing in ${thresholds.path}")
            val raw = line.substringAfter(':').substringBefore('#').trim()
            return raw.toIntOrNull() ?: error("threshold '$key' is not an integer: $raw")
        }
        val lineFloor = threshold("statement_coverage")
        val branchFloor = threshold("branch_coverage")
        extensions.configure<KoverProjectExtension>("kover") {
            reports {
                verify {
                    rule {
                        bound {
                            minValue.set(lineFloor)
                            coverageUnits.set(CoverageUnit.LINE)
                        }
                    }
                    rule {
                        bound {
                            minValue.set(branchFloor)
                            coverageUnits.set(CoverageUnit.BRANCH)
                        }
                    }
                }
            }
        }
    }
}
