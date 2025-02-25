import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

val stripeApiKey = localProperties.getProperty("STRIPE_API_KEY", "")
val stripeLocation = localProperties.getProperty("STRIPE_LOCATION", "")

gradle.rootProject {
    extensions.extraProperties["stripeApiKey"] = stripeApiKey
    extensions.extraProperties["stripeLocation"] = stripeLocation
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
