plugins {
    alias(libs.plugins.hilt) apply (false)
    alias(libs.plugins.kotlin.ksp) apply (false)
    alias(libs.plugins.kotlin.serialization) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
    }
    dependencies {
        classpath(libs.gradle)
        classpath(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                arrayOf("reports", "metrics").forEach {
                    freeCompilerArgs.add("-P")
                    freeCompilerArgs.add("plugin:androidx.compose.compiler.plugins.kotlin:${it}Destination=${project.layout.buildDirectory}/compose_metrics")
                }
            }
        }
    }
}

// Keep bot-generated build packages small. The Telegram builder runs assembleDebug
// and otherwise archives hundreds of megabytes of Gradle intermediates.
val compactBotBuild by tasks.registering {
    group = "build"
    description = "Keep only the installable DiyyMusic APK and remove build intermediates."

    doLast {
        val appProject = project(":app")
        val apkDirectory = appProject.file("build/outputs/apk/foss/debug")
        val apks = apkDirectory
            .walkTopDown()
            .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
            .toList()

        if (apks.isEmpty()) {
            logger.warn("No FOSS debug APK found; compact cleanup skipped.")
            return@doLast
        }

        val selectedApk = apks.maxBy { it.length() }
        val compactApk = apkDirectory.resolve("DiyyMusic-v0.6.1.apk")
        if (selectedApk.canonicalFile != compactApk.canonicalFile) {
            selectedApk.copyTo(compactApk, overwrite = true)
        }

        // Remove every other artifact from the final APK directory.
        apkDirectory.listFiles()?.forEach { file ->
            if (file.canonicalFile != compactApk.canonicalFile) {
                file.deleteRecursively()
            }
        }

        // Remove app intermediates while preserving the exact APK path most builders scan.
        val appBuild = appProject.file("build")
        appBuild.listFiles()?.forEach { child ->
            if (child.name != "outputs") child.deleteRecursively()
        }
        appProject.file("build/outputs").listFiles()?.forEach { child ->
            if (child.name != "apk") child.deleteRecursively()
        }
        appProject.file("build/outputs/apk").listFiles()?.forEach { child ->
            if (child.name != "foss") child.deleteRecursively()
        }
        appProject.file("build/outputs/apk/foss").listFiles()?.forEach { child ->
            if (child.name != "debug") child.deleteRecursively()
        }

        // Library module intermediates are useless after the APK exists.
        subprojects
            .filter { it.path != ":app" }
            .forEach { it.layout.buildDirectory.get().asFile.deleteRecursively() }

        rootProject.layout.buildDirectory.get().asFile.deleteRecursively()
        rootProject.file(".kotlin").deleteRecursively()
        appProject.file("src/main/java").deleteRecursively() // generated protobuf only

        logger.lifecycle("Compact APK kept at ${compactApk.relativeTo(rootProject.projectDir)} (${compactApk.length() / 1024 / 1024} MB)")
    }
}

gradle.projectsEvaluated {
    project(":app").tasks.matching {
        it.name == "assembleDebug" || it.name == "assembleFossDebug"
    }.configureEach {
        finalizedBy(compactBotBuild)
    }
}
