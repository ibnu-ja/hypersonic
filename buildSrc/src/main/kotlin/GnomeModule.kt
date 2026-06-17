import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

open class GnomeExtension(private val project: Project) {
    var blueprintConfig: BlueprintConfig? = null
    var resourceConfig: CompileResourcesConfig? = null
    var postInstallConfig: PostInstallConfig? = null

    fun compileResources(config: CompileResourcesConfig.() -> Unit) {
        CompileResourcesConfig().apply(config).also { resourceConfig = it }
    }

    fun blueprints(config: BlueprintConfig.() -> Unit) {
        BlueprintConfig().apply(config).also { blueprintConfig = it }
    }

    fun postInstall(config: PostInstallConfig.() -> Unit) {
        PostInstallConfig().apply(config).also { postInstallConfig = it }
    }
}

open class CompileResourcesConfig {
    var xml: String = ""
    var sourceDirs: List<String> = emptyList()
    var buildDir: String = "build/gresources"
    var includeBlueprintOutput: Boolean = true
    private var _output: String = ""
    var output: String
        get() {
            if (_output.isNotEmpty()) return _output
            val base = xml.substringAfterLast("/")
                .removeSuffix(".gresource.xml")
                .removeSuffix(".xml")
            return "$buildDir/$base.gresource"
        }
        set(v) { _output = v }
}

open class BlueprintConfig {
    var sourceDir: String = "src/main/blueprints"
    var outputDir: String = "build/blueprints"
    var files: List<String> = emptyList()
}

open class PostInstallConfig {
    var compileSchemas: Boolean = false
    var updateIconCache: Boolean = false
    var updateDesktopDatabase: Boolean = false
}

internal fun registerGnomeTasks(project: Project, env: EnvironmentExtension, gnome: GnomeExtension, config: ConfigExtension? = null) {
    gnome.blueprintConfig?.let { bp ->
        project.tasks.register<Exec>("compileBlueprints") {
            group = "build"
            description = "Compile Blueprint files to GtkBuilder XML"

            workingDir = project.projectDir
            onlyIf { bp.files.isNotEmpty() }

            val args = mutableListOf("blueprint-compiler", "batch-compile", bp.outputDir, bp.sourceDir)
            args.addAll(bp.files)

            this.executeCommand(env, *args.toTypedArray())

            inputs.files(bp.files.map { project.file(it) })
            outputs.dir(project.file(bp.outputDir))
        }
    }

    gnome.resourceConfig?.let { rc ->
        val dependsOnBlueprints = gnome.blueprintConfig != null
        val gresourceOutputPath = rc.output
        val domain = config?.gettextDomain?.getOrElse(project.name) ?: project.name

        project.tasks.register<Exec>("compileGResources") {
            group = "build"
            description = "Compile GResource XML to binary bundle"

            if (dependsOnBlueprints) dependsOn("compileBlueprints")
            workingDir = project.projectDir

            val args = mutableListOf("glib-compile-resources")
            val xmlDir = project.file(rc.xml).parent
            args.add("--sourcedir=$xmlDir")
            rc.sourceDirs.forEach { args.add("--sourcedir=$it") }
            if (gnome.blueprintConfig != null) {
                args.add("--sourcedir=${gnome.blueprintConfig!!.outputDir}")
            }
            args.add("--target=$gresourceOutputPath")
            args.add(rc.xml)

            this.executeCommand(env, *args.toTypedArray())

            inputs.file(project.file(rc.xml))
            outputs.file(project.file(gresourceOutputPath))
        }

        val installTaskName = "installFile_gresource"
        project.tasks.register(installTaskName) {
            group = "install"
            description = "Install compiled GResource bundle"
            dependsOn("compileGResources")
            doLast {
                val targetDir = if (config?.resourceDir?.isPresent == true) {
                    config.resourceDir.get()
                } else {
                    "${env.prefix.get()}/share/$domain"
                }
                project.file(gresourceOutputPath).let { src ->
                    project.copy { from(src); into(targetDir) }
                }
            }
        }
        ensureInstallGlibData(project).dependsOn(installTaskName)
    }

    gnome.postInstallConfig?.let { pi ->
        project.tasks.register("gnomePostInstall") {
            group = "install"
            description = "Run GNOME post-install hooks (compile schemas, update caches)"

            onlyIf { env.installLocation.get() == InstallLocation.SYSTEM }

            doLast {
                val destdir = System.getenv("DESTDIR")
                if (destdir != null && destdir.isNotEmpty()) {
                    logger.lifecycle("DESTDIR is set, skipping post-install hooks")
                    return@doLast
                }

                val datadir = "${env.prefix.get()}/share"

                fun runCmd(vararg cmd: String) {
                    val process = ProcessBuilder(*cmd).inheritIO().start()
                    val code = process.waitFor()
                    if (code != 0) throw RuntimeException("Failed: ${cmd.joinToString(" ")}")
                }

                if (pi.compileSchemas) runCmd("glib-compile-schemas", "$datadir/glib-2.0/schemas")
                if (pi.updateIconCache) {
                    val iconTool = if (project.file("/usr/bin/gtk4-update-icon-cache").exists()
                        || project.file("/usr/local/bin/gtk4-update-icon-cache").exists()) {
                        "gtk4-update-icon-cache"
                    } else {
                        "gtk-update-icon-cache"
                    }
                    runCmd(iconTool, "-q", "-t", "-f", "$datadir/icons/hicolor")
                }
                if (pi.updateDesktopDatabase) runCmd("update-desktop-database", "-q", "$datadir/applications")
            }
        }
    }

}
