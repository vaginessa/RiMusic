plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    android.buildFeatures.buildConfig=true
    compileSdk = 34

    defaultConfig {
        applicationId = "it.fast4x.rimusic"
        minSdk = 21
        targetSdk = 34
        versionCode = 15
        versionName = "0.6.19"
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
        //sourceCompatibility = JavaVersion.VERSION_1_8
        //targetCompatibility = JavaVersion.VERSION_1_8
        //sourceCompatibility = JavaVersion.VERSION_11
        //targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        //jvmTarget = "1.8"
        //jvmTarget = "11"
        jvmTarget = "17"
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
    kapt(libs.room.compiler)
    implementation(projects.innertube)
    implementation(projects.kugou)

    val appcompatVersion = "1.6.1"
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.2.1")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("com.github.therealbush:translator:1.0.2")
    implementation("io.github.azhon:appupdate:4.3.2")


    //End
    coreLibraryDesugaring(libs.desugaring)
}
