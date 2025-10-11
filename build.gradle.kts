// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.11.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.5" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    kotlin("plugin.serialization") version "2.1.10"

    id("com.google.firebase.appdistribution") version "5.1.1" apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false

    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("androidx.room") version "2.8.2" apply false
}
