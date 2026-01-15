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
val jacksonBomVersion = "2.20.0"
val javaGiVersion = "0.14.0"
val ktorVersion = "3.3.2"
val subsonicApiVersion = "1.1.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
}

glibBuildTools {
    applicationId.set("io.ibnuja.Hypersonic")

    gettextDomain.set("hypersonic")

    resourceDir.set("src/main/resources")

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

afterEvaluate {
    val libraryPath = environment.libraryPath.get()

    tasks.named<JavaExec>("run") {
        jvmArgs("-Djava.library.path=$libraryPath")
    }

    tasks.named<Test>("test") {
        jvmArgs("-Djava.library.path=$libraryPath")
    }

    application.applicationDefaultJvmArgs = application.applicationDefaultJvmArgs!! +
            listOf("-Djava.library.path=$libraryPath")
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
