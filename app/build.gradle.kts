plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    android.buildFeatures.buildConfig=true
    compileSdk = 33

    defaultConfig {
        applicationId = "it.fast4x.rimusic"
        minSdk = 21
        targetSdk = 33
        versionCode = 10
        versionName = "0.6.15.1"
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    namespace = "it.vfsfitvnm.vimusic"

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "RiMusic-Debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appName"] = "RiMusic"
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "1.8"
    }

    androidResources {
        generateLocaleConfig = true
    }



}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

/*
android {
    lint {
        baseline = file("lint-baseline.xml")
        //checkReleaseBuilds = false
    }
}
*/

dependencies {
    implementation(projects.composePersist)
    implementation(projects.composeRouting)
    implementation(projects.composeReordering)

    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ripple)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)

    implementation(libs.palette)

    implementation(libs.exoplayer)

    implementation(libs.room)
    implementation("androidx.media3:media3-datasource-okhttp:1.1.1")
    kapt(libs.room.compiler)

    implementation(projects.innertube)
    implementation(projects.kugou)

    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    
    val appcompat_version = "1.6.1"

    implementation("androidx.appcompat:appcompat:$appcompat_version")
    // For loading and tinting drawables on older versions of the platform
    implementation("androidx.appcompat:appcompat-resources:$appcompat_version")

    implementation("com.github.therealbush:translator:1.0.2")

    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")

    implementation("io.github.gautamchibde:audiovisualizer:2.2.7")

    implementation("androidx.compose.material:material:1.1.0")

    //implementation("androidx.compose.foundation:foundation:1.4.0")
    // Fix Duplicate class
    //implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

    coreLibraryDesugaring(libs.desugaring)
}
