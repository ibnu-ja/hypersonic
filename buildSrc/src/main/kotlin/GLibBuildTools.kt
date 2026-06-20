import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class GLibBuildTools : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(EnvironmentPlugin::class.java)

        val extension = project.extensions.create(
            "glibBuildTools", GLibBuildToolsExtension::class.java, project
        )

        project.afterEvaluate {
            val env = project.extensions.getByType<EnvironmentExtension>()
            val cliPrefix = project.findProperty("prefix")?.toString()

            if (cliPrefix != null) {
                val domain = extension.configExtension?.gettextDomain
                    ?.getOrElse(project.name) ?: project.name
                extension.configExtension?.installPrefix?.set(cliPrefix)
                extension.configExtension?.resourceDir?.set("$cliPrefix/share/$domain/")
                extension.configExtension?.localeDir?.set("$cliPrefix/share/locale/")
            }

            // Sync gresource filename from compileResources output to config
            val configExt = extension.configExtension
            val gnomeExt = extension.gnomeExtension
            if (configExt != null && gnomeExt?.resourceConfig != null) {
                if (!configExt.resourceFilename.isPresent) {
                    configExt.resourceFilename.set(
                        gnomeExt.resourceConfig!!.output.substringAfterLast("/")
                    )
                }
            }

            configExt?.let { registerConfigTasks(project, env, it) }
            gnomeExt?.let { registerGnomeTasks(project, env, it, configExt) }
            extension.i18nExtension?.let { registerI18nTasks(project, env, it, extension.configExtension) }
            extension.dataExtension?.let { registerDataTasks(project, env, it) }

            configureBuildDependencies(project)
            configureInstallDependencies(project, env)
            prepareRunTask(project, extension)
        }
    }

    private fun prepareRunTask(project: Project, extension: GLibBuildToolsExtension) {
        val glibTasks = mutableListOf<org.gradle.api.Task>()

        project.tasks.findByName("compileGResources")?.let { glibTasks.add(it) }
        project.tasks.findByName("compileBlueprints")?.let { glibTasks.add(it) }
        project.tasks.findByName("mergeDesktopFile")?.let { glibTasks.add(it) }
        project.tasks.findByName("mergeAppstreamFile")?.let { glibTasks.add(it) }
        project.tasks.findByName("compileTranslations")?.let { glibTasks.add(it) }

        // GSchema compilation for run
        val dataExt = extension.dataExtension
        if (dataExt != null && dataExt.schemaConfigs.isNotEmpty()) {
            val schemaDir = project.file(dataExt.schemaConfigs.first().file).parent
            val schemaOutputDir = project.layout.buildDirectory.dir("schemas").get().asFile.path

            val schemaTask = project.tasks.register<Exec>("compileGSchemas") {
                group = "build"
                description = "Compile GSettings schemas for development"

                workingDir = project.projectDir

                commandLine("glib-compile-schemas", "--targetdir=$schemaOutputDir", schemaDir)

                inputs.dir(schemaDir)
                outputs.file("$schemaOutputDir/gschemas.compiled")
            }
            glibTasks.add(schemaTask.get())
        }

        val runTask = project.tasks.findByName("run") as? JavaExec
        if (runTask != null) {
            glibTasks.forEach { runTask.dependsOn(it) }
            if (dataExt != null && dataExt.schemaConfigs.isNotEmpty()) {
                val schemaOutputDir = project.layout.buildDirectory.dir("schemas").get().asFile.path
                runTask.environment("GSETTINGS_SCHEMA_DIR", schemaOutputDir)
            }
        }

        project.gradle.taskGraph.whenReady {
            if (hasTask(":run")) {
                val buildDir = project.layout.buildDirectory.get().asFile.path
                val config = extension.configExtension
                if (config != null) {
                    config.resourceDir.set("$buildDir/gresources/")
                    config.localeDir.set("$buildDir/locale/")
                    config.installPrefix.set("$buildDir/install/${project.name}")
                }
            }
        }
    }

    private fun configureBuildDependencies(project: Project) {
        val buildDeps = mutableListOf<String>()

        if (project.tasks.findByName("compileGResources") != null) buildDeps.add("compileGResources")
        if (project.tasks.findByName("compileBlueprints") != null) buildDeps.add("compileBlueprints")
        if (project.tasks.findByName("mergeDesktopFile") != null) buildDeps.add("mergeDesktopFile")
        if (project.tasks.findByName("mergeAppstreamFile") != null) buildDeps.add("mergeAppstreamFile")
        if (project.tasks.findByName("compileTranslations") != null) buildDeps.add("compileTranslations")

        if (buildDeps.isNotEmpty()) {
            project.tasks.named("build") { dependsOn(buildDeps) }
        }
    }

    private fun configureInstallDependencies(project: Project, env: EnvironmentExtension) {
        val syncTasks = project.tasks.matching { it.name in listOf("installDist", "installShadowDist") }

        project.tasks.matching { it.name.startsWith("installFile_") }.configureEach {
            dependsOn(syncTasks)
        }

        project.tasks.register("install") {
            group = "install"
            description = "Install the application and GNOME data files to the prefix"

            val useShadow = project.tasks.findByName("installShadowDist") != null
            val stagingDir = if (useShadow) "${project.name}-shadow" else project.name

            dependsOn("installGlibData")
            finalizedBy("gnomePostInstall")

            doLast {
                val destdir = System.getenv("DESTDIR") ?: ""
                val prefix = env.prefix.get()
                val installTarget = destdir + prefix
                val staging = project.layout.buildDirectory.dir("install/$stagingDir").get().asFile.path

                project.copy { from(staging); into(installTarget) }
            }
        }
    }
}
