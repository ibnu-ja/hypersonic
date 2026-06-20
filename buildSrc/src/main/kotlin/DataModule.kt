import org.gradle.api.Project

open class DataExtension {
    val schemaConfigs = mutableListOf<SchemaConfig>()
    val serviceConfigs = mutableListOf<ServiceConfig>()
    val iconConfigs = mutableListOf<IconConfig>()

    fun gschema(file: String, config: SchemaConfig.() -> Unit = {}) {
        schemaConfigs.add(SchemaConfig(file).apply(config))
    }

    fun dbusService(file: String, config: ServiceConfig.() -> Unit = {}) {
        serviceConfigs.add(ServiceConfig(file).apply(config))
    }

    fun icon(file: String) {
        iconConfigs.add(IconConfig(file))
    }
}

open class SchemaConfig(val file: String) {
    var validate: Boolean = false
}

open class ServiceConfig(val file: String) {
    var bindir: String? = null
}

open class IconConfig(val file: String)

internal fun registerDataTasks(project: Project, env: EnvironmentExtension, data: DataExtension) {
    val installPrefix = env.prefix.get()

    val getStagingDirs = {
        val dirs = mutableListOf<String>()
        if (project.tasks.findByName("installDist") != null) dirs.add(project.name)
        if (project.tasks.findByName("installShadowDist") != null) dirs.add("${project.name}-shadow")
        dirs
    }

    data.schemaConfigs.forEach { sc ->
        val safeName = sc.file.replace(Regex("[^a-zA-Z0-9_]"), "_")

        project.tasks.register("installFile_schema_$safeName") {
            group = "install"
            description = "Install schema: ${sc.file}"
            doLast {
                getStagingDirs().forEach { dirName ->
                    val installDir = "${project.layout.buildDirectory.get().asFile.path}/install/$dirName/share/glib-2.0/schemas"
                    project.file(installDir).mkdirs()
                    project.copy { from(sc.file); into(installDir) }
                }
            }
        }
        ensureInstallGlibData(project).dependsOn("installFile_schema_$safeName")

        if (sc.validate) {
            project.tasks.register("validateSchema") {
                group = "verification"
                description = "Validate GSettings schema"
                doLast {
                    val schemaSourceDir = project.file(sc.file).parentFile
                    val process = ProcessBuilder("glib-compile-schemas", "--strict", "--dry-run", schemaSourceDir.absolutePath)
                        .inheritIO().start()
                    val code = process.waitFor()
                    if (code != 0) throw RuntimeException("Schema validation failed")
                }
            }
            project.tasks.named("check") { dependsOn("validateSchema") }
        }
    }

    data.serviceConfigs.forEach { svc ->
        val outputName = svc.file.substringAfterLast("/").removeSuffix(".in")
        val buildFile = project.layout.buildDirectory.file("dbus-services/$outputName").get().asFile
        val safeName = svc.file.replace(Regex("[^a-zA-Z0-9_]"), "_")

        project.tasks.register("installFile_service_$safeName") {
            group = "install"
            description = "Install D-Bus service: ${svc.file}"
            doLast {
                val resolvedBindir = svc.bindir ?: "$installPrefix/bin"
                buildFile.parentFile.mkdirs()
                buildFile.writeText(project.file(svc.file).readText().replace("@bindir@", resolvedBindir))
                getStagingDirs().forEach { dirName ->
                    val installDir = "${project.layout.buildDirectory.get().asFile.path}/install/$dirName/share/dbus-1/services"
                    project.copy { from(buildFile); into(installDir) }
                }
            }
        }
        ensureInstallGlibData(project).dependsOn("installFile_service_$safeName")
    }

    data.iconConfigs.forEach { ic ->
        val category = when {
            ic.file.contains("scalable") -> "scalable"
            ic.file.contains("symbolic") -> "symbolic"
            else -> "scalable"
        }
        val safeName = ic.file.replace(Regex("[^a-zA-Z0-9_]"), "_")

        project.tasks.register("installFile_icon_$safeName") {
            group = "install"
            description = "Install icon: ${ic.file}"
            doLast {
                getStagingDirs().forEach { dirName ->
                    val installDir = "${project.layout.buildDirectory.get().asFile.path}/install/$dirName/share/icons/hicolor/$category/apps"
                    project.copy { from(ic.file); into(installDir) }
                }
            }
        }
        ensureInstallGlibData(project).dependsOn("installFile_icon_$safeName")
    }
}
