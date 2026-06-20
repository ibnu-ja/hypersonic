import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import java.io.File

class EnvironmentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<EnvironmentExtension>(
            "environment",
            project
        )

        project.afterEvaluate {
            configureEnvironment(project, extension)
        }
    }

    private fun configureEnvironment(project: Project, extension: EnvironmentExtension) {
        val os = System.getProperty("os.name").lowercase()

        if (!extension.type.isPresent) {
            extension.type.set(when {
                os.contains("windows") -> detectWindowsEnvironment()
                else -> EnvironmentType.NATIVE_POSIX
            })
        }

        val cliPrefix = project.findProperty("prefix")?.toString()

        if (cliPrefix != null) {
            extension.prefix.set(cliPrefix)
        } else if (!extension.prefix.isPresent) {
            extension.prefix.set(when (extension.type.get()) {
                EnvironmentType.NATIVE_POSIX -> "/usr/local"
                EnvironmentType.MSYS2_MINGW64 -> {
                    val localAppData = System.getenv("LOCALAPPDATA") ?: "${System.getProperty("user.home")}/AppData/Local"
                    localAppData.replace("\\", "/")
                }
            })
        }

        val prefix = extension.prefix.get()

        // Configure Library Path
        val libPath = when (extension.type.get()) {
            EnvironmentType.MSYS2_MINGW64 -> {
                val msys2Home = System.getenv("MSYS2_HOME") ?: "C:\\msys64"
                "$msys2Home/mingw64/bin"
            }
            EnvironmentType.NATIVE_POSIX -> "$prefix/lib"
        }

        extension.libraryPath.set(libPath)
    }

    @Suppress("SameReturnValue")
    private fun detectWindowsEnvironment(): EnvironmentType {
        val msys2Home = System.getenv("MSYS2_HOME") ?: "C:\\msys64"
        val msys2Exists = File(msys2Home).exists()

        return if (msys2Exists) {
            EnvironmentType.MSYS2_MINGW64
        } else {
            EnvironmentType.MSYS2_MINGW64
        }
    }
}

open class EnvironmentExtension(private val project: Project) {
    val type: Property<EnvironmentType> = project.objects.property(EnvironmentType::class.java)

    val prefix: Property<String> = project.objects.property(String::class.java)
    val libraryPath: Property<String> = project.objects.property(String::class.java)

    fun isPosixCompliant(): Boolean {
        return type.get() in listOf(EnvironmentType.MSYS2_MINGW64, EnvironmentType.NATIVE_POSIX)
    }
}

enum class EnvironmentType {
    NATIVE_POSIX,
    MSYS2_MINGW64,
}
