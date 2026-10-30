import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release credentials are local only. Never fall back to the shared debug key.
val releaseCredentials = Properties().apply {
    val credentialsFile = rootProject.file("antireel-signing.properties")
    if (credentialsFile.isFile) credentialsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.rafi.antireelai"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rafi.antireelai"
        minSdk = 30
        targetSdk = 37
        versionCode = 24
        versionName = "1.0.23"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseCredentials.isNotEmpty()) {
            create("privateRelease") {
                storeFile = rootProject.file(releaseCredentials.getProperty("storeFile"))
                storePassword = releaseCredentials.getProperty("storePassword")
                keyAlias = releaseCredentials.getProperty("keyAlias")
                keyPassword = releaseCredentials.getProperty("keyPassword")
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    buildTypes {
        release {
            isDebuggable = false
            if (releaseCredentials.isNotEmpty()) signingConfig = signingConfigs.getByName("privateRelease")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
    val configured = releaseCredentials.isNotEmpty()
    doLast {
        check(configured) {
            "Release signing is missing. Configure antireel-signing.properties with your private keystore before building a release. See docs/SECURITY_REVIEW.md."
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(verifyReleaseSigning) }

// One command for distributable artifacts. Never package the Android Studio Run APK.
tasks.register<Exec>("prepareDistribution") {
    dependsOn("assembleRelease", "bundleRelease", "testDebugUnitTest", "lintRelease")
    workingDir(rootProject.projectDir)
    commandLine("python3", "tools/package_distribution.py")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
