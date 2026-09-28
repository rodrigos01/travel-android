import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val mapsApiKey: String = System.getenv("MAPS_API_KEY") ?: localProperties.getProperty("MAPS_API_KEY", "")
val keystorePass: String = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD", "")
val keyAliasValue: String = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS", "")
val keyPass: String = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD", "")

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("kotlinx-serialization")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "travel.vola.android"
    compileSdk = 36

    signingConfigs {
        create("release") {
            // You need to specify either an absolute path or include the
            // keystore file in the same directory as the build.gradle file.
            storeFile = file("travel-release.jks")
            storePassword = keystorePass
            keyAlias = keyAliasValue
            keyPassword = keyPass
        }
    }
    defaultConfig {
        applicationId = "travel.vola.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName =
            "1.0" + System.getenv("BUILD_NUMBER")?.let { ".$it" } +
            System.getenv("BRANCH_NAME")
                ?.let { ".$it" }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            firebaseAppDistribution {
                artifactType = "APK"
                groups = "developers"
            }
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    flavorDimensions += "host"
    productFlavors {
        create("prod") {
            dimension = "host"
            buildConfigField(
                "String",
                "SERVER_URL",
                "\"https://travel-api-master-rlbhlyi7ja-uc.a.run.app\"",
            )
        }
        create("local") {
            dimension = "host"
            buildConfigField("String", "SERVER_URL", "\"http://10.0.2.2:5000\"")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    testOptions {
        unitTests {
            all {
                it.testLogging {
                    it.outputs.upToDateWhen { false }
                    events =
                        setOf(
                            TestLogEvent.PASSED,
                            TestLogEvent.SKIPPED,
                            TestLogEvent.FAILED,
                            TestLogEvent.STANDARD_OUT,
                            TestLogEvent.STANDARD_ERROR,
                        )
                }
                it.jvmArgs("-Djava.locale.providers=COMPAT", "-Dfile.encoding=UTF-8")
            }
        }
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles =
        listOf(rootProject.layout.projectDirectory.file("stability_config.conf"))
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.splashscreen)
    // Lifecycle
    implementation(libs.lifecycle.extensions)
    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.compose)
    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.ai)
    // Sign in with Google (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons)
    implementation(libs.androidx.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.material3.window.size.class1.android)
    implementation(libs.material3.adaptive.android)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.layout.android)
    implementation(libs.androidx.window)
    implementation(libs.compose.text.googlefonts)
    // Google Maps
    implementation(libs.maps.compose)
    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.encoding)
    implementation(libs.androidx.palette.ktx)

    implementation(libs.material.kolor) // Or latest version
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)
}
