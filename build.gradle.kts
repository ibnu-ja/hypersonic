@file:Suppress("SpellCheckingInspection")

plugins {
    id("application")
    id("io.freefair.lombok") version "9.0.0"
    id("com.gradleup.shadow") version "9.2.2"
    kotlin("jvm")

    id("io.ibnuja.environment")
    id("io.ibnuja.glib.buildtools")
}

group = "io.ibnuja"
version = "1.0-SNAPSHOT"

val slf4jVersion = "2.0.17"
val log4jVersion = "2.25.3"
val junitVersion = "5.10.0"
val jacksonBomVersion = "2.21.1"
val javaGiVersion = "0.14.1"
val ktorVersion = "3.3.2"
val subsonicApiVersion = "1.1.1"

val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val defaultPrefix    = if (isWindows) "C:/hypersonic" else "/usr/local"
val defaultDatadir   = "src/main/resources"

val mesonPrefix    = project.findProperty("mesonPrefix")?.toString()    ?: defaultPrefix
val mesonDatadir   = project.findProperty("mesonDatadir")?.toString()   ?: defaultDatadir
val mesonLocaledir = project.findProperty("mesonLocaledir")?.toString() ?: "$mesonPrefix/share/locale"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
}

glibBuildTools {
    applicationId.set("io.ibnuja.Hypersonic")
    gettextDomain.set("hypersonic")
    localeDir.set(mesonLocaledir)
    resourceDir.set(mesonDatadir)

    blueprintSourceDir.set("src/main/resources")
    blueprintOutputDir.set("src/main/resources/blueprint-compiler")
    gresourceXml.set("src/main/resources/hypersonicapp.gresource.xml")
    gresourceOutput.set("src/main/resources/hypersonicapp.gresource")
    gresourceSourceDirs.set(listOf("src/main/resources"))

    blueprints(
        "src/main/resources/window.blp",
        "src/main/resources/components/playback/playback_info.blp",
        "src/main/resources/components/playback/playback_controls.blp",
        "src/main/resources/components/playback/playback_widget.blp",
        "src/main/resources/components/selection/selection_toolbar.blp",
        "src/main/resources/components/settings/settings.blp",
        "src/main/resources/components/sidebar/sidebar_row.blp",
        "src/main/resources/pages/home.blp"
    )
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val commonJvmArgs = mutableListOf("--enable-native-access=ALL-UNNAMED")

application {
    applicationDefaultJvmArgs += commonJvmArgs
    mainClass.set("io.ibnuja.hypersonic.Hypersonic")
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
    implementation("ru.stersh:subsonic-api:${subsonicApiVersion}")
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

tasks.named<JavaExec>("run") {
    environment("GSETTINGS_SCHEMA_DIR", "$mesonPrefix/data")
}
