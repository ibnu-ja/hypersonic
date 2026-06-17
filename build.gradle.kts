@file:Suppress("SpellCheckingInspection")

plugins {
    id("application")
    id("io.freefair.lombok") version "9.0.0"
    id("com.gradleup.shadow") version "9.2.2"
    kotlin("jvm")
    kotlin("plugin.lombok")

    id("moe.seiga.environment")
    id("moe.seiga.glib.buildtools")
}

group = "moe.seiga"
version = "1.0-SNAPSHOT"

val slf4jVersion = "2.0.17"
val log4jVersion = "2.26.0"
val junitVersion = "5.10.0"
val jacksonBomVersion = "2.21.1"
val javaGiVersion = "1.0.0-RC1"
val ktorVersion = "3.3.2"
val subsonicClientVersion = "1.0.0-beta05"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
}

glibBuildTools {
    config {
        applicationId = "$group.Hypersonic"
        gettextDomain = "hypersonic"
    }

    gnome {
        compileResources {
            xml = "src/main/gresources/hypersonicapp.gresource.xml"
        }

        blueprints {
            sourceDir = "src/main/blueprints"
            files = listOf(
                "src/main/blueprints/window.blp",
                "src/main/blueprints/components/player/bar.blp",
                "src/main/blueprints/components/player/playback-controls.blp",
                "src/main/blueprints/components/player/seekbar.blp",
                "src/main/blueprints/components/settings/settings.blp",
                "src/main/blueprints/components/connection/welcome-page.blp",
                "src/main/blueprints/components/sidebar/playlist-item.blp",
                "src/main/blueprints/components/sidebar/sidebar.blp",
            )
        }

        postInstall {
            compileSchemas = true
            updateIconCache = true
            updateDesktopDatabase = true
        }
    }

    i18n {
        gettext {
            preset = "glib"
            poDir  = "po"
            extraArgs = listOf("--add-comments", "--keyword=i18n", "--keyword=i18n:1,2c")
        }

        mergeFile {
            input  = "data/moe.seiga.Hypersonic.desktop.in"
            output = "moe.seiga.Hypersonic.desktop"
            type   = "desktop"
            poDir  = "po"
        }

        mergeFile {
            input  = "data/moe.seiga.Hypersonic.metainfo.xml.in"
            output = "moe.seiga.Hypersonic.metainfo.xml"
            type   = "xml"
            poDir  = "po"
        }
    }

    data {
        gschema("data/moe.seiga.Hypersonic.gschema.xml") {
            validate = true
        }

        dbusService("data/moe.seiga.Hypersonic.service.in") {
            bindir = "/usr/local/bin"
        }

        icon("data/icons/hicolor/scalable/apps/moe.seiga.Hypersonic.svg")
        icon("data/icons/hicolor/symbolic/apps/moe.seiga.Hypersonic-symbolic.svg")
        icon("data/icons/hicolor/scalable/actions/library-album-symbolic.svg")
        icon("data/icons/hicolor/scalable/actions/library-music-symbolic.svg")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val commonJvmArgs = mutableListOf("--enable-native-access=ALL-UNNAMED")

application {
    applicationDefaultJvmArgs += commonJvmArgs
    mainClass.set("moe.seiga.hypersonic.Main")
}

dependencies {
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation(platform("org.apache.logging.log4j:log4j-bom:${log4jVersion}"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:${jacksonBomVersion}"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("org.java-gi:gtk:${javaGiVersion}")
    implementation("org.java-gi:adw:${javaGiVersion}")
    implementation("org.java-gi:gdkpixbuf:${javaGiVersion}")
    implementation("org.java-gi:gstreamer:${javaGiVersion}")
    implementation("org.java-gi:secret:${javaGiVersion}")
    implementation("dev.zt64.subsonic:subsonic-client:${subsonicClientVersion}")
    implementation("io.ktor:ktor-client-apache5:${ktorVersion}")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl")
    runtimeOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}


