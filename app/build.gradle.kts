plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "upv_dap.sep_dic_25.itiid_76129.pgu3_eq09"
    compileSdk = 36

    defaultConfig {
        applicationId = "upv_dap.sep_dic_25.itiid_76129.pgu3_eq09"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.exifinterface)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Picasso para carga de imágenes
    implementation("com.squareup.picasso:picasso:2.8")
}