import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.Inet4Address
import java.net.NetworkInterface

fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (address is Inet4Address) {
                    val ip = address.hostAddress
                    if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                        return ip
                    }
                }
            }
        }
    } catch (e: Exception) { }
    return "10.0.2.2" // Fallback
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Navegación
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
            // Red
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            // Serialización
            implementation(libs.kotlinx.serialization.json)
            // Preferencias persistentes (guardar JWT)
            implementation(libs.multiplatform.settings.no.arg)
            // Iconos Material Extended
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            // Motor HTTP para Desktop
            implementation(libs.ktor.client.cio)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            // Motor HTTP para Android
            implementation(libs.ktor.client.okhttp)
            // Escáner de documentos ML Kit
            implementation(libs.play.services.mlkit.document.scanner)
        }
    }
}

android {
    namespace = "com.pokeshophub"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.pokeshophub"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "BACKEND_IP", "\"${getLocalIpAddress()}\"")
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
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.pokeshophub.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.pokeshophub"
            packageVersion = "1.0.0"
        }
    }
}

// Tarea para ejecutar automáticamente adb reverse al compilar/ejecutar la app
tasks.register("runAdbReverse") {
    group = "custom"
    description = "Configura la redirección de puertos ADB automáticamente"
    
    val sdkPath = project.extensions.getByType(com.android.build.gradle.BaseExtension::class.java).sdkDirectory.absolutePath
    
    doLast {
        val adb = if (System.getProperty("os.name").lowercase().contains("windows")) {
            "$sdkPath/platform-tools/adb.exe"
        } else {
            "$sdkPath/platform-tools/adb"
        }
        try {
            val process1 = ProcessBuilder(adb, "reverse", "tcp:8080", "tcp:8080").start()
            process1.waitFor()
            val process2 = ProcessBuilder(adb, "reverse", "tcp:3080", "tcp:8080").start()
            process2.waitFor()
            println(">>> ADB: Redirección de puertos configurada correctamente (8080 y 3080 -> 8080) <<<")
        } catch (e: Exception) {
            println(">>> ADB: No se pudo configurar la redirección de puertos: ${e.message} <<<")
        }
    }
}

// Hacemos que preBuild dependa de ejecutar adb reverse para que se ejecute en cada compile/run
tasks.named("preBuild") {
    dependsOn("runAdbReverse")
}


