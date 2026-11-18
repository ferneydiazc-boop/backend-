import java.net.URI
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val productionUrl = providers.gradleProperty("ACORDES_API_URL").orElse("https://acordes-del-barrio-api.onrender.com/").get()
val debugUrl = providers.gradleProperty("ACORDES_DEBUG_API_URL").orElse(productionUrl).get()
if (gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }) {
    require(productionUrl.startsWith("https://") && URI(productionUrl).host != null) {
        "Configura -PACORDES_API_URL=https://URL-VERIFICADA/ antes de generar release."
    }
}
android {
    namespace = "com.acordes.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.acordes.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes {
        getByName("debug") { buildConfigField("String", "API_URL", "\"" + debugUrl.trimEnd('/') + "/\"") }
        getByName("release") { buildConfigField("String", "API_URL", "\"" + productionUrl.replace("\"", "").trimEnd('/') + "/\"") }
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
