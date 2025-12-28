import java.util.Properties

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "8.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.0")
}
val base = top.mrxiaom.gradle.LibraryHelper(project)

// 版本号配置
val majorVersion: String by project
val minorVersion: String by project
val patchVersion: String by project
val buildNumber: String by project

group = "cn.mcloli.dreamrealms"
version = "$majorVersion.$minorVersion.$patchVersion-build-$buildNumber"
val targetJavaVersion = 21
val pluginBaseModules = listOf("library", "actions", "gui", "paper", "misc", "l10n", "commands")
val pluginBaseVersion = "1.7.0"
val shadowGroup = "cn.mcloli.dreamrealms.libs"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.momirealms.net/snapshots/")
    maven("https://libraries.minecraft.net/") // Mojang authlib
    maven("https://repo.hibiscusmc.com/releases/") // HMCWraps
    maven("https://repo.ghostchu.com/releases/") // QuickShop-Hikari
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.21.8") // NMS
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("com.mojang:authlib:1.5.25")

    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly("net.momirealms:craft-engine-core:0.0.66.10-SNAPSHOT")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.66.10-SNAPSHOT")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    
    // OwnerBind 软依赖
    compileOnly("com.github.MrXiaoM:SweetMail:1.1.2")
    compileOnly("com.github.Maxlego08:zAuctionHouseV3-API:3.1.3.0")
    compileOnly(files("libs/GlobalMarketPlus-v1.2.12.1.jar")) // GlobalMarketPlus 本地依赖
    compileOnly("com.ghostchu:quickshop-api:6.2.0.6") // QuickShop-Hikari

    // GiftPoints 软依赖
    compileOnly(files("libs/SweetCheckout-v1.0.8.jar"))
    compileOnly("top.mrxiaom:qrcode-encoder:1.0.0")
    compileOnly("com.hibiscusmc:HMCCosmetics:2.7.3") // HMCCosmetics


    base.library("net.kyori:adventure-api:4.22.0")
    base.library("net.kyori:adventure-platform-bukkit:4.4.0")
    base.library("net.kyori:adventure-text-minimessage:4.22.0")
    base.library("net.kyori:adventure-text-serializer-plain:4.22.0")
    implementation("com.zaxxer:HikariCP:4.0.3") { isTransitive = false }
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation("top.mrxiaom.pluginbase:$artifact:$pluginBaseVersion")
    }

    implementation("top.mrxiaom:LibrariesResolver-Lite:$pluginBaseVersion")
}
buildConfig {
    className("BuildConstants")
    packageName("cn.mcloli.dreamrealms")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    shadowJar {
        archiveClassifier.set("") // 移除 -all 后缀，直接输出主 jar
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "com.zaxxer.hikari" to "hikari",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
    jar {
        enabled = false // 禁用默认 jar 任务，只输出 shadowJar
    }
    // 清理旧的构建产物，只保留最新的
    val cleanOldArtifacts = create("cleanOldArtifacts") {
        doLast {
            // 清理 build/libs 目录
            val libsDir = file("build/libs")
            if (libsDir.exists()) {
                libsDir.listFiles()?.filter { it.name.endsWith(".jar") }?.forEach { it.delete() }
            }
            // 清理 out 目录
            val outDir = rootProject.file("out")
            if (outDir.exists()) {
                outDir.listFiles()?.filter { it.name.endsWith(".jar") }?.forEach { it.delete() }
            }
        }
    }
    shadowJar {
        dependsOn(cleanOldArtifacts)
    }
    val copyTask = create<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "${project.name}-$version.jar" }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
        // 构建完成后递增 build number
        doLast {
            val propsFile = file("gradle.properties")
            val props = Properties()
            props.load(propsFile.inputStream())
            val currentBuild = props.getProperty("buildNumber").toInt()
            props.setProperty("buildNumber", (currentBuild + 1).toString())
            propsFile.outputStream().use { props.store(it, null) }
            println("Build number incremented to ${currentBuild + 1}")
        }
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
        }
    }
}
