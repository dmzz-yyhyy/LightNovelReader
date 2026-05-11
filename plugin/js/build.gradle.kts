import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "io.nightfish.lightnovelreader.plugin.js"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.nightfish.lightnovelreader.plugin.js"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//    applicationVariants.all {
//        val variant = this
//        variant.outputs.all {
//            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
//            val originalFileName = outputImpl.outputFileName
//            val newFileName = originalFileName.replace(".apk", ".apk.lnrp")
//            outputImpl.outputFileName = newFileName
//        }
//    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.manifests.addStaticManifestFile(
            layout.buildDirectory.file("generated/ksp/${variant.name}/resources/auto_register_manifest.xml").get().toString()
        )
    }
}

afterEvaluate {
    listOf("debug", "release").forEach { variantName ->
        val kspTaskName = "ksp${variantName.replaceFirstChar { it.uppercase() }}Kotlin"
        val manifestTaskName = "process${variantName.replaceFirstChar { it.uppercase() }}MainManifest"
        tasks.findByName(manifestTaskName)?.dependsOn(kspTaskName)
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    // Android lib
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.runtime)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // LNR Api
    ksp(project(":compiler"))
    implementation(project(":api"))

    // Other
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javet.node.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jsoup)

    // Js Test
    testImplementation(libs.caoccao.javet) // Must-have
    testImplementation(libs.javet.node.linux.arm64)
    testImplementation(libs.javet.node.linux.x86.x4)
    testImplementation(libs.javet.node.macos.arm64)
    testImplementation(libs.javet.node.macos.x86.x4)
    testImplementation(libs.javet.node.windows.x86.x4)
}