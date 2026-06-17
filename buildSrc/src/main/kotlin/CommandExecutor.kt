import org.gradle.api.tasks.Exec

fun Exec.executeCommand(env: EnvironmentExtension, vararg command: String) {
    doFirst {
        logger.lifecycle("Running: ${command.joinToString(" ")}")
    }

    when (env.type.get()) {
        EnvironmentType.MSYS2_MINGW64 -> {
            val msysPath = System.getenv("MSYS2_HOME") ?: "C:\\msys64"
            environment("MSYSTEM", "MINGW64")
            val escapedDir = workingDir.absolutePath.replace("\\", "/")

            val bashCommand = "cd \"$escapedDir\" && ${command.joinToString(" ")}"

            commandLine(
                "$msysPath\\usr\\bin\\bash.exe",
                "-lc",
                bashCommand
            )
        }
        EnvironmentType.NATIVE_POSIX -> {
            commandLine(*command)
        }
    }
}
