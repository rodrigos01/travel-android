// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.11.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.6" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    kotlin("plugin.serialization") version "2.2.21"

    id("com.google.firebase.appdistribution") version "5.2.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false

    id("com.google.devtools.ksp") version "2.3.3" apply false
    id("androidx.room") version "2.8.4" apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            // Spotless uses the default ktlint under the hood which enforces the JetBrains/Google convention
            ktlint().editorConfigOverride(
                mapOf(
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_value-argument-comment" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_max-line-length" to "disabled"
                )
            )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }
}
