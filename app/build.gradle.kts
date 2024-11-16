plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.delivery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.delivery"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["YANDEX_MAPKIT_API_KEY"] = "37174936-b5e1-4db7-86b0-9a3a32e1ff5d"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    runtimeOnly("com.cloudinary:cloudinary-android-core:3.0.2")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.0.2")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.cloudinary:cloudinary-android-ui:3.0.2")
    implementation ("com.github.bumptech.glide:glide:4.16.0")


    implementation ("com.yandex.android:maps.mobile:4.8.0-full")

    implementation ("com.google.android.material:material:1.6.0")
}