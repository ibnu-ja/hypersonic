package moe.seiga.hypersonic;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

@Slf4j
public class LoggingBootstrap {

    private LoggingBootstrap() {}

    private static final String LOGGING_LEVEL_PREFIX = "logging.level.";
    private static final String ENV_LOGGING_LEVEL_PREFIX = "LOGGING_LEVEL_";

    public static void init() {
        log.trace("Initializing logging configuration...");

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith(LOGGING_LEVEL_PREFIX)) {
                String loggerName = keyStr.substring(LOGGING_LEVEL_PREFIX.length());
                Level level = Level.toLevel(value.toString().toUpperCase());
                log.trace(
                        "Setting logger '{}' to level {} (from system property)",
                        loggerName,
                        level
                );
                setLogLevel(config, loggerName, level);
            }
        });

        System.getenv().forEach((key, value) -> {
            if (key.startsWith(ENV_LOGGING_LEVEL_PREFIX)) {
                String loggerName = key.substring(ENV_LOGGING_LEVEL_PREFIX.length())
                        .replace("_", ".")
                        .toLowerCase();
                Level level = Level.toLevel(value.toUpperCase());
                log.trace(
                        "Setting logger '{}' to level {} (from environment variable {})",
                        loggerName,
                        level,
                        key
                );
                setLogLevel(config, loggerName, level);
            }
        });

        String simpleLogLevel = System.getenv("LOG_LEVEL");
        if (simpleLogLevel != null) {
            log.trace(
                    "Setting root logger to level {} (from LOG_LEVEL)",
                    simpleLogLevel.toUpperCase()
            );
            setLogLevel(config, "root", Level.toLevel(simpleLogLevel.toUpperCase()));
        }

        context.updateLoggers();
        log.trace("Logging configuration complete.");
    }

    private static void setLogLevel(Configuration config, String loggerName, Level level) {
        if ("root".equalsIgnoreCase(loggerName)) {
            LoggerConfig rootConfig = config.getRootLogger();
            rootConfig.setLevel(level);
        } else {
            LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
            if (!loggerName.equals(loggerConfig.getName())) {
                loggerConfig = new LoggerConfig(loggerName, level, true);
                config.addLogger(loggerName, loggerConfig);
            } else {
                loggerConfig.setLevel(level);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void setLevel(String loggerName, String level) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        setLogLevel(config, loggerName, Level.toLevel(level.toUpperCase()));
        context.updateLoggers();
    }
}
