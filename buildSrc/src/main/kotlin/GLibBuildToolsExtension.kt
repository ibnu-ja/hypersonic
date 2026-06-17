import org.gradle.api.Project
import org.gradle.api.provider.Property

open class GLibBuildToolsExtension(private val project: Project) {
    var configExtension: ConfigExtension? = null
    var gnomeExtension: GnomeExtension? = null
    var i18nExtension: I18nExtension? = null
    var dataExtension: DataExtension? = null

    fun config(config: ConfigExtension.() -> Unit) {
        ConfigExtension(project).apply(config).also { configExtension = it }
    }

    fun gnome(config: GnomeExtension.() -> Unit) {
        GnomeExtension(project).apply(config).also { gnomeExtension = it }
    }

    fun i18n(config: I18nExtension.() -> Unit) {
        I18nExtension(project).apply(config).also { i18nExtension = it }
    }

    fun data(config: DataExtension.() -> Unit) {
        DataExtension().apply(config).also { dataExtension = it }
    }
}

open class ConfigExtension(private val project: Project) {
    val applicationId: Property<String> = project.objects.property(String::class.java)
    val gettextDomain: Property<String> = project.objects.property(String::class.java)
    val configClassName: Property<String> = project.objects.property(String::class.java).convention("Config")
    val gresourceOutput: Property<String> = project.objects.property(String::class.java)
        .convention(applicationId.map { "src/main/gresources/${it}.gresource" })

    val resourceDir: Property<String> = project.objects.property(String::class.java)
    val localeDir: Property<String> = project.objects.property(String::class.java)
    val installPrefix: Property<String> = project.objects.property(String::class.java)
    val resourceFilename: Property<String> = project.objects.property(String::class.java)
}
