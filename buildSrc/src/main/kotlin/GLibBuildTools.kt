import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class GLibBuildTools : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(EnvironmentPlugin::class.java)

        val extension = project.extensions.create(
            "glibBuildTools", GLibBuildToolsExtension::class.java, project
        )

        project.afterEvaluate {
            registerTasks(project, extension)
        }
    }

    private fun registerTasks(project: Project, extension: GLibBuildToolsExtension) {
        val env = project.extensions.getByType<EnvironmentExtension>()

        project.tasks.register<Exec>("compileBlueprints") {
            group = "build"
            description = "Compile Blueprint files into GtkBuilder XML"

            workingDir = project.projectDir

            val sourceDir = extension.blueprintSourceDir.get()
            val outputDir = extension.blueprintOutputDir.get()
            val blueprintFiles = extension.blueprintFiles.get()

            onlyIf { blueprintFiles.isNotEmpty() }

            val args = mutableListOf(
                "blueprint-compiler", "batch-compile", outputDir, sourceDir
            )

            args.addAll(blueprintFiles)

            this.executeCommand(env, *args.toTypedArray())

            inputs.files(blueprintFiles.map { project.file(it) })
            outputs.dir(project.file(outputDir))
        }

        project.tasks.register<Exec>("compileGResources") {
            group = "build"
            description = "Compile GResource XML into binary resource file"

            workingDir = project.projectDir
            dependsOn("compileBlueprints")

            val sourceDirs = extension.gresourceSourceDirs.get()
            val gresourceXml = extension.gresourceXml.get()
            val outputFile = extension.gresourceOutput.get()

            val args = mutableListOf("glib-compile-resources")

            sourceDirs.forEach { args.add("--sourcedir=$it") }
            args.add("--target=$outputFile")
            args.add(gresourceXml)

            this.executeCommand(env, *args.toTypedArray())

            inputs.file(project.file(gresourceXml))
            outputs.file(project.file(outputFile))
        }

        project.tasks.register("generateConfig") {
            group = "build"
            description = "Generate configuration constants for GLib application"

            val outputDir = project.layout.buildDirectory.dir("generated/sources/config/java/main").get()
            val packageName = extension.applicationId.get().substringBeforeLast(".")
            val outputFile = outputDir.file("${packageName.replace(".", "/")}/Config.java").asFile

            outputs.file(outputFile)

            val appId = extension.applicationId.get()
            val gresourceOutput = extension.gresourceOutput.get()
            val gresourceFilename = gresourceOutput.substringAfterLast("/")
            val gettextDomain = extension.gettextDomain.getOrElse(appId.substringAfterLast("."))
            val localeDir = extension.localeDir.getOrElse("")
            val resourceDir = extension.resourceDir.get().let {
                if (it.endsWith("/")) it else "$it/"
            }

            inputs.property("applicationId", appId)
            inputs.property("gresourceFilename", gresourceFilename)
            inputs.property("gettextDomain", gettextDomain)
            inputs.property("localeDir", localeDir)
            inputs.property("resourceDir", resourceDir)

            doLast {
                outputFile.parentFile.mkdirs()
                outputFile.writeText(
                    """
            package $packageName;

            public class ${extension.configClassName.get()} {

                public static final String GETTEXT_PACKAGE = "$gettextDomain";
                public static final String LOCALE_DIR = "$localeDir";
                public static final String APPLICATION_ID = "$appId";
                public static final String RESOURCE_DIR = "$resourceDir";
                public static final String RESOURCE_FILENAME = "$gresourceFilename";

                private ${extension.configClassName.get()}() {}
            }
        """.trimIndent()
                )
            }
        }

        project.tasks.named("compileJava") {
            dependsOn("generateConfig")
        }

        project.tasks.named("compileKotlin") {
            dependsOn("generateConfig")
        }

        // Add generated sources to source set
        project.extensions.configure<JavaPluginExtension>("java") {
            sourceSets.getByName("main") {
                java.srcDir(project.layout.buildDirectory.dir("generated/sources/config/java/main"))
            }
        }
    }
}

open class GLibBuildToolsExtension(private val project: Project) {
    val blueprintSourceDir: Property<String> =
        project.objects.property(String::class.java).convention("src/main/gresources")

    val blueprintOutputDir: Property<String> =
        project.objects.property(String::class.java).convention("src/main/gresources/blueprint-compiler")

    val blueprintFiles: ListProperty<String> = project.objects.listProperty(String::class.java)

    val configClassName: Property<String> = project.objects.property(String::class.java).convention("Config")

    val applicationId: Property<String> = project.objects.property(String::class.java)

    val gettextDomain: Property<String> = project.objects.property(String::class.java)

    val localeDir: Property<String> = project.objects.property(String::class.java)

    val resourceDir: Property<String> = project.objects.property(String::class.java).convention(project.provider {
        if (blueprintFiles.isPresent && blueprintFiles.get().isNotEmpty()) {
            blueprintOutputDir.get()
        } else {
            blueprintSourceDir.get()
        }
    })

    val gresourceSourceDirs: ListProperty<String> =
        project.objects.listProperty(String::class.java).convention(project.provider {
            if (blueprintFiles.isPresent && blueprintFiles.get().isNotEmpty()) {
                listOf(blueprintOutputDir.get())
            } else {
                listOf(blueprintSourceDir.get())
            }
        })

    val gresourceXml: Property<String> =
        project.objects.property(String::class.java).convention(applicationId.map { "data/${it}.gresource.xml" })

    val gresourceOutput: Property<String> = project.objects.property(String::class.java)
        .convention(applicationId.map { "src/main/gresources/${it}.gresource" })

    fun blueprints(vararg files: String) {
        blueprintFiles.addAll(*files)
    }
}
