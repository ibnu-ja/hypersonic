import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.register

internal fun registerConfigTasks(project: Project, env: EnvironmentExtension, config: ConfigExtension) {
    val appId = config.applicationId.get()
    val domain = config.gettextDomain.getOrElse(appId.substringAfterLast("."))
    val className = config.configClassName.get()
    val packageName = appId.substringBeforeLast(".")
    val outputDir = project.layout.buildDirectory.dir("generated/sources/config/java/main").get()
    val outputFile = outputDir.file("${packageName.replace(".", "/")}/$className.java").asFile
    val gresourceFile = config.gresourceOutput.get()
    val resFilename = config.resourceFilename.getOrElse(gresourceFile.substringAfterLast("/"))

    // Set defaults if not overridden
    if (!config.resourceDir.isPresent) config.resourceDir.set("/usr/local/share/$domain")
    if (!config.localeDir.isPresent) config.localeDir.set("/usr/local/share/locale")
    if (!config.installPrefix.isPresent) config.installPrefix.set("/usr/local")

    project.tasks.register("generateConfig") {
        group = "build"
        description = "Generate Config.java with GLib application constants"

        outputs.file(outputFile)
        outputs.upToDateWhen { false }

        doLast {
            // Read config properties at execution time (wireDevMode may have updated them)
            val resDir = config.resourceDir.get().trimEnd('/') + "/"
            val locDir = config.localeDir.get().trimEnd('/') + "/"
            val instPrefix = config.installPrefix.get()

            outputFile.parentFile.mkdirs()
            outputFile.writeText("""
                package $packageName;

                public class $className {

                    public static final String APPLICATION_ID = "$appId";
                    public static final String GETTEXT_PACKAGE = "$domain";
                    public static final String RESOURCE_FILENAME = "$resFilename";
                    public static final String RESOURCE_DIR = "$resDir";
                    public static final String LOCALE_DIR = "$locDir";
                    public static final String INSTALL_PREFIX = "$instPrefix";

                    private $className() {}
                }
            """.trimIndent())
        }
    }

    project.tasks.named("compileJava") { dependsOn("generateConfig") }
    project.tasks.named("compileKotlin") { dependsOn("generateConfig") }

    project.extensions.configure<JavaPluginExtension>("java") {
        sourceSets.getByName("main") {
            java.srcDir(project.layout.buildDirectory.dir("generated/sources/config/java/main"))
        }
    }
}
