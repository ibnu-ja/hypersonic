import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

open class I18nExtension(private val project: Project) {
    var gettextConfig: GettextConfig? = null
    val mergeFileConfigs = mutableListOf<MergeFileConfig>()

    fun gettext(domain: String = project.name, config: GettextConfig.() -> Unit = {}) {
        GettextConfig(domain).apply(config).also { gettextConfig = it }
    }

    fun mergeFile(config: MergeFileConfig.() -> Unit) {
        mergeFileConfigs.add(MergeFileConfig().apply(config))
    }
}

open class GettextConfig(val domain: String) {
    var preset: String? = null
    var poDir: String = "po"
    var extraArgs: List<String> = emptyList()
}

open class MergeFileConfig {
    var input: String = ""
    var output: String = ""
    var type: String = "desktop"
    var poDir: String = "po"
    var installDir: String = ""
    var validate: Boolean = false
}

internal fun registerI18nTasks(
    project: Project,
    env: EnvironmentExtension,
    i18n: I18nExtension,
    config: ConfigExtension? = null
) {

    i18n.gettextConfig?.let { gt ->
        val domain = gt.domain
        val poDir = gt.poDir
        val potFile = "$poDir/$domain.pot"
        val potfilesIn = "$poDir/POTFILES.in"
        val buildLocaleDir = "${project.layout.buildDirectory.get().asFile.path}/locale"

        val presetArgs = if (gt.preset == "glib") {
            listOf(
                "--keyword=_",
                "--keyword=N_",
                "--keyword=C_:1c,2",
                "--keyword=NC_:1c,2",
                "--keyword=g_dcgettext:2",
                "--keyword=g_dngettext:2,3",
                "--keyword=g_dpgettext2:2c,3",
                "--flag=N_:1:pass-c-format",
                "--flag=C_:2:pass-c-format",
                "--flag=NC_:2:pass-c-format",
                "--flag=g_dngettext:2:pass-c-format",
                "--flag=g_strdup_printf:1:c-format",
                "--flag=g_string_printf:2:c-format",
                "--flag=g_string_append_printf:2:c-format",
                "--flag=g_error_new:3:c-format",
                "--flag=g_set_error:4:c-format",
                "--flag=g_markup_printf_escaped:1:c-format",
                "--flag=g_log:3:c-format",
                "--flag=g_print:1:c-format",
                "--flag=g_printerr:1:c-format",
                "--flag=g_printf:1:c-format",
                "--flag=g_fprintf:2:c-format",
                "--flag=g_sprintf:2:c-format",
                "--flag=g_snprintf:3:c-format",
            )
        } else emptyList()
        val allArgs = presetArgs + gt.extraArgs

        project.tasks.register<Exec>("extractPot") {
            group = "build"
            description = "Extract translatable strings to .pot template"

            workingDir = project.projectDir
            onlyIf { project.file(potfilesIn).exists() }

            val args = mutableListOf("xgettext", "-o", potFile, "--files-from=$potfilesIn", "--from-code=UTF-8")
            args.addAll(allArgs)

            this.executeCommand(env, *args.toTypedArray())

            inputs.file(project.file(potfilesIn))
            if (project.file(potfilesIn).exists()) {
                project.file(potfilesIn).readLines()
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .forEach { inputs.file(project.file(it)) }
            }
            outputs.file(project.file(potFile))
        }

        val poFiles = project.fileTree(poDir).matching { include("*.po") }.files

        poFiles.forEach { poFile ->
            val lang = poFile.nameWithoutExtension

            project.tasks.register<Exec>("mergePo_$lang") {
                group = "build"
                description = "Merge .pot updates into $lang.po"

                workingDir = project.projectDir
                dependsOn("extractPot")

                commandLine("msgmerge", "--update", poFile.path, potFile)

                inputs.file(project.file(potFile))
                outputs.file(poFile)
            }

            project.tasks.register<Exec>("compileMo_$lang") {
                group = "build"
                description = "Compile $lang.po to .mo binary"

                workingDir = project.projectDir
                dependsOn("mergePo_$lang")

                val moDir = "$buildLocaleDir/$lang/LC_MESSAGES"
                val moFile = "$moDir/$domain.mo"

                commandLine("msgfmt", poFile.path, "-o", moFile)
                doFirst { project.file(moDir).mkdirs() }

                inputs.file(poFile)
                outputs.file(project.file(moFile))
            }

            val installTaskName = "installFile_mo_$lang"
            project.tasks.register(installTaskName) {
                group = "install"
                description = "Install $lang.mo to locale directory"
                dependsOn("compileMo_$lang")
                doLast {
                    val dirs = mutableListOf<String>()
                    if (project.tasks.findByName("installDist") != null) dirs.add(project.name)
                    if (project.tasks.findByName("installShadowDist") != null) dirs.add("${project.name}-shadow")

                    dirs.forEach { dirName ->
                        val installLocaleDir = "${project.layout.buildDirectory.get().asFile.path}/install/$dirName/share/locale"
                        val installMoFile = "$installLocaleDir/$lang/LC_MESSAGES/$domain.mo"
                        project.file(installMoFile).parentFile.mkdirs()
                        project.copy {
                            from(project.file("$buildLocaleDir/$lang/LC_MESSAGES/$domain.mo"))
                            into(project.file(installMoFile).parentFile)
                        }
                    }
                }
            }
            ensureInstallGlibData(project).dependsOn(installTaskName)
        }

        val compileMoTasks = poFiles.map { "compileMo_${it.nameWithoutExtension}" }
        project.tasks.register("compileTranslations") {
            group = "build"
            description = "Compile all .po translation files to .mo binary"
            dependsOn(compileMoTasks)
        }
    }

    i18n.mergeFileConfigs.forEach { mf ->
        val outputFile = project.layout.buildDirectory.file("i18n/${mf.output}").get().asFile
        val taskName = when (mf.type) {
            "desktop" -> "mergeDesktopFile"
            "xml" -> "mergeAppstreamFile"
            else -> "mergeFile_${mf.output.substringBeforeLast(".")}"
        }

        project.tasks.register<Exec>(taskName) {
            group = "build"
            description = "Merge translations into ${mf.output}"

            workingDir = project.projectDir

            commandLine(
                "msgfmt",
                "--${mf.type}",
                "--template", mf.input,
                "-d", mf.poDir,
                "-o", outputFile.path
            )

            inputs.file(project.file(mf.input))
            outputs.file(outputFile)
        }

        if (mf.installDir.isNotEmpty()) {
            val installTaskName = "installFile_$taskName"

            project.tasks.register(installTaskName) {
                group = "install"
                description = "Install merged file to staging"
                dependsOn(taskName)
                doLast {
                    val dirs = mutableListOf<String>()
                    if (project.tasks.findByName("installDist") != null) dirs.add(project.name)
                    if (project.tasks.findByName("installShadowDist") != null) dirs.add("${project.name}-shadow")

                    dirs.forEach { dirName ->
                        val installTarget = "${project.layout.buildDirectory.get().asFile.path}/install/$dirName/share/${mf.installDir}"
                        project.copy { from(outputFile); into(installTarget) }
                    }
                }
            }
            ensureInstallGlibData(project).dependsOn(installTaskName)
        }

        if (mf.validate) {
            val validateCmd = when (mf.type) {
                "desktop" -> "desktop-file-validate"
                "xml" -> "appstreamcli"
                else -> null
            }

            if (validateCmd != null) {
                val validateArgs = if (mf.type == "xml") {
                    listOf("validate", "--no-net", "--explain", outputFile.path)
                } else {
                    listOf(outputFile.path)
                }

                val validateTaskName = "validate${taskName.replaceFirstChar { it.uppercase() }}"

                project.tasks.register<Exec>(validateTaskName) {
                    group = "verification"
                    description = "Validate ${mf.output}"
                    dependsOn(taskName)
                    commandLine(validateCmd, *validateArgs.toTypedArray())
                }

                project.tasks.named("check") { dependsOn(validateTaskName) }
            }
        }
    }
}

private val installGlibDataCreated = mutableSetOf<Project>()

internal fun ensureInstallGlibData(project: Project): org.gradle.api.Task {
    val existing = project.tasks.findByName("installGlibData")
    if (existing != null) return existing
    return project.tasks.register("installGlibData") {
        group = "install"
        description = "Install GNOME/GLib data files to install prefix"
    }.get()
}
