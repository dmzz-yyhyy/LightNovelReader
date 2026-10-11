plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.nightfish.lightnovelreader.ktor.browser"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.encoding)
    implementation("org.brotli:dec:0.1.2")
    implementation("com.github.luben:zstd-jni:1.5.7-12@aar")
}
