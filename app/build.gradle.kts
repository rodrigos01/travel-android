import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("kotlinx-serialization")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
    id("com.google.protobuf") version "0.9.6"
    id("androidx.room")
}

android {
    namespace = "travel.vola.android"
    compileSdk = 36

    signingConfigs {
        create("release") {
            // You need to specify either an absolute path or include the
            // keystore file in the same directory as the build.gradle file.
            storeFile = file("travel-release.jks")
            storePassword = "SZoyCEhfiC1mBX10"
            keyAlias = "travel-release-key"
            keyPassword = "SZoyCEhfiC1mBX10"
        }
    }
    defaultConfig {
        applicationId = "travel.vola.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName =
            "1.0" + System.getenv("BUILD_NUMBER")?.let { ".$it" } + System.getenv("BRANCH_NAME")
                ?.let { ".$it" }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
                "String", "SERVER_URL", "\"https://travel-api-master-rlbhlyi7ja-uc.a.run.app\""
            )
            buildConfigField("boolean", "REQUIRES_AUTH", "true")
        }
        create("local") {
            dimension = "host"
            buildConfigField("String", "SERVER_URL", "\"http://10.0.2.2:5000\"")
            buildConfigField("boolean", "REQUIRES_AUTH", "false")
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
                    events = setOf(
                        TestLogEvent.PASSED,
                        TestLogEvent.SKIPPED,
                        TestLogEvent.FAILED,
                        TestLogEvent.STANDARD_OUT,
                        TestLogEvent.STANDARD_ERROR
                    )
                }
                it.jvmArgs("-Djava.locale.providers=COMPAT", "-Dfile.encoding=UTF-8")
            }
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles =
        listOf(rootProject.layout.projectDirectory.file("stability_config.conf"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.18.0"
    }
    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
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
    implementation(libs.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.ai)
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
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
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    implementation(libs.androidx.palette.ktx)
    // Room
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    implementation(libs.material.kolor) // Or latest version
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)
}
