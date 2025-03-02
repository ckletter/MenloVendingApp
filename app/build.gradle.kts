plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.menlovending"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.menlovending"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "STRIPE_API_KEY", "\"${rootProject.extra["stripeApiKey"]}\"")
            buildConfigField("String", "STRIPE_LOCATION", "\"${rootProject.extra["stripeLocation"]}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "STRIPE_API_KEY", "\"${rootProject.extra["stripeApiKey"]}\"")
            buildConfigField("String", "STRIPE_LOCATION", "\"${rootProject.extra["stripeLocation"]}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }
}

dependencies {
    implementation(libs.java.native.jssc)
    implementation(libs.spark.core)
    implementation(libs.gson)
    implementation(libs.firebase.database)
    // Stripe SDK for Android
    implementation(libs.stripe.android)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.junit.v114)
    androidTestImplementation(libs.androidx.espresso.core.v350)
    implementation(libs.jserialcomm)
    implementation(libs.stripeterminal)
    implementation(libs.stripe.java)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}