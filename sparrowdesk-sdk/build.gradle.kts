plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
}

val sdkVersion: String = project.findProperty("SDK_VERSION") as? String ?: "0.1.0"
val githubOwner: String = project.findProperty("GITHUB_OWNER") as? String ?: "SparrowDesk"
val githubRepo: String = project.findProperty("GITHUB_REPO") as? String ?: "sparrowdesk-mobile-sdk"

kotlin {
    // Suppress expect/actual classes beta warning
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    @Suppress("DEPRECATION")
    val xcf = org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig(project, "SparrowDeskSDK")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SparrowDeskSDK"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // No external dependencies
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            // Uses Android platform WebView APIs
        }

        iosMain.dependencies {
            // Uses iOS platform WebKit APIs via Kotlin/Native
        }
    }
}

android {
    namespace = "com.sparrowdesk.sdk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// ── Publishing ──────────────────────────────────────────────

group = "com.sparrowdesk"
version = sdkVersion

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubOwner/$githubRepo")
            credentials {
                username = project.findProperty("gpr.user") as? String
                    ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as? String
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// ── XCFramework zip for SPM ─────────────────────────────────

tasks.register<Zip>("zipXCFramework") {
    dependsOn("assembleSparrowDeskSDKXCFramework")
    from(layout.buildDirectory.dir("XCFrameworks/release"))
    archiveFileName.set("SparrowDeskSDK.xcframework.zip")
    destinationDirectory.set(layout.buildDirectory.dir("outputs"))
}

tasks.register("computeXCFrameworkChecksum") {
    dependsOn("zipXCFramework")
    doLast {
        val zipFile = layout.buildDirectory.file("outputs/SparrowDeskSDK.xcframework.zip").get().asFile
        val process = ProcessBuilder("swift", "package", "compute-checksum", zipFile.absolutePath)
            .redirectErrorStream(true)
            .start()
        val checksum = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        println("XCFramework checksum: $checksum")
        // Write checksum to file for CI usage
        layout.buildDirectory.file("outputs/checksum.txt").get().asFile.writeText(checksum)
    }
}
