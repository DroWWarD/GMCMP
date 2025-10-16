import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        // === ОБЩИЕ зависимости (здесь и Ktor core) ===
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(compose.materialIconsExtended)
                implementation(libs.datetime)


                // Ktor (общие)
                implementation("io.ktor:ktor-client-core:3.0.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
                implementation("io.ktor:ktor-client-logging:3.0.1")
                implementation("io.ktor:ktor-client-websockets:3.0.1")
                implementation("io.ktor:ktor-client-auth:3.0.1")
                //Сериализация
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

                //Navigation DECOMPOSE
                val decompose = "3.2.0"
                implementation("com.arkivanov.decompose:decompose:$decompose")
                implementation("com.arkivanov.decompose:extensions-compose:$decompose")
                //Логгирование
                implementation("io.github.aakira:napier:2.7.1")

                implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
                //SHA256
                implementation("com.squareup.okio:okio:3.9.0")
                //Resources
                implementation(compose.components.resources)
                //Date-Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            }
        }

        val nonWasmMain by creating {
            dependsOn(commonMain)
            dependencies {
                //KOIN DI
                implementation("io.insert-koin:koin-core:3.5.6")
                implementation("io.insert-koin:koin-compose:1.1.5")

                implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")
            }
        }

        // === ANDROID ===
        val androidMain by getting {
            dependsOn(nonWasmMain)
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                // Движок для Ktor
                implementation("io.ktor:ktor-client-okhttp:3.0.1")
                implementation("com.arkivanov.decompose:extensions-android:3.2.0")
            }
        }

        // === JVM (Desktop) ===
        val jvmMain by getting {
            dependsOn(nonWasmMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)

                // Движок для Ktor
                implementation("io.ktor:ktor-client-okhttp:3.0.1")
            }
        }

        // === iOS (общий сорссет для iosArm64/iosSimulatorArm64) ===
        val iosArm64Main by getting {
            dependsOn(nonWasmMain)
            dependencies { implementation("io.ktor:ktor-client-darwin:3.0.1") }
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(nonWasmMain)
            dependencies { implementation("io.ktor:ktor-client-darwin:3.0.1") }
        }

        // === Wasm JS ===
        val wasmJsMain by getting {
            dependencies {
                // Движок для Ktor
                implementation("io.ktor:ktor-client-js:3.0.1")
            }
        }

        // Тесты
        val commonTest by getting {
            dependencies { implementation(libs.kotlin.test) }
        }
    }
}

android {
    namespace = "ru.acs.grandmap"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.acs.grandmap"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "ru.acs.grandmap.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ru.acs.grandmap"
            packageVersion = "1.0.0"
        }
    }
}
compose.resources {
    packageOfResClass = "ru.acs.grandmap.composeResources"
    publicResClass = true
}