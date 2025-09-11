package BrailleTranscriptionLedger;

import java.io.*;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Logger utility for BrailleTranscriptionLedger.
 * Supports log levels, structured output, and log rotation.
 * @since 2025.07.01
 */
public class Logger {

    /**
     * Log levels for Logger.
     */
    public enum LogLevel {
        /** Debug level. */
        DEBUG,
        /** Info level. */
        INFO,
        /** Warn level. */
        WARN,
        /** Error level. */
        ERROR,
        /** Fatal level. */
        FATAL;

        /**
         * Parse log level from string.
         * @param s String to parse
         * @return LogLevel enum value
         */
        public static LogLevel fromString(String s) {
            try {
                return LogLevel.valueOf(s.trim().toUpperCase());
            } catch (Exception e) {
                return WARN;
            }
        }
    }

    /**
     * Default constructor.
     */
    public Logger() {}

    private static LogLevel currentLogLevel = LogLevel.WARN;
    private static final String LOG_FILE_PATH =
        "BrailleTranscriptionLedger/ledger-debug.log";
    private static final String LOG_FILE_BACKUP_PATH =
        "BrailleTranscriptionLedger/ledger-debug.log.1";
    private static final long MAX_LOG_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Map<LogLevel, String> levelNames = new EnumMap<>(
        LogLevel.class
    );

    static {
        levelNames.put(LogLevel.DEBUG, "DEBUG");
        levelNames.put(LogLevel.INFO, "INFO");
        levelNames.put(LogLevel.WARN, "WARN");
        levelNames.put(LogLevel.ERROR, "ERROR");
        levelNames.put(LogLevel.FATAL, "FATAL");
    }

    /**
     * Set the current log level.
     * @param level LogLevel to set
    * @since 2025.07.01
     */
    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
        logInternal(LogLevel.INFO, "Log level set to " + levelNames.get(level));
    }

    /**
     * Get the current log level.
     * @return current LogLevel
    * @since 2025.07.01
     */
    public static LogLevel getLogLevel() {
        return currentLogLevel;
    }

    /**
     * Log a message at the specified level.
     * @param level LogLevel for the message
     * @param msg Message to log
    * @since 2025.07.01
     */
    public static void log(LogLevel level, String msg) {
        if (level.ordinal() < currentLogLevel.ordinal()) return;
        logInternal(level, msg);
    }

    /**
     * Log a debug message.
     * @param msg Message to log
    * @since 2025.07.01
     */
    public static void debug(String msg) {
        log(LogLevel.DEBUG, msg);
    }

    /**
     * Log an info message.
     * @param msg Message to log
    * @since 2025.07.01
     */
    public static void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    /**
     * Log a warning message.
     * @param msg Message to log
    * @since 2025.07.01
     */
    public static void warn(String msg) {
        log(LogLevel.WARN, msg);
    }

    /**
     * Log an error message.
     * @param msg Message to log
    * @since 2025.07.01
     */
    public static void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    /**
     * Log a fatal message.
     * @param msg Message to log
    * @since 2025.07.01
     */
    public static void fatal(String msg) {
        log(LogLevel.FATAL, msg);
    }

    /**
     * Internal method to write log entry.
     * @param level LogLevel for the message
     * @param msg Message to log
     */
    private static void logInternal(LogLevel level, String msg) {
        String entry = String.format(
            "{\"timestamp\":\"%s\",\"level\":\"%s\",\"message\":%s}",
            LocalDateTime.now(),
            levelNames.get(level),
            jsonEscape(msg)
        );
        writeLog(entry);
        if (level.ordinal() >= LogLevel.ERROR.ordinal()) {
            System.err.println(entry);
        }
    }

    /**
     * Write log entry to file, rotating if needed.
     * @param entry Log entry to write
     */
    private static void writeLog(String entry) {
        rotateLogIfNeeded();
        try (
            FileWriter fw = new FileWriter(LOG_FILE_PATH, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
        ) {
            out.println(entry);
        } catch (IOException e) {
            System.err.println(
                "[LOGGER ERROR] Failed to write log: " + e.getMessage()
            );
        }
    }

    /**
     * Rotate log file if it exceeds maximum size.
     */
    private static void rotateLogIfNeeded() {
        File logFile = new File(LOG_FILE_PATH);
        if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
            File backupFile = new File(LOG_FILE_BACKUP_PATH);
            if (backupFile.exists()) backupFile.delete();
            boolean renamed = logFile.renameTo(backupFile);
            if (!renamed) {
                System.err.println("[LOGGER ERROR] Failed to rotate log file.");
            }
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                System.err.println(
                    "[LOGGER ERROR] Failed to create new log file: " +
                    e.getMessage()
                );
            }
        }
    }

    /**
     * Escape JSON special characters in message.
     * @param msg Message to escape
     * @return Escaped message string
     */
    private static String jsonEscape(String msg) {
        if (msg == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : msg.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
