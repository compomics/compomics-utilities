package com.compomics.software.log;

import com.compomics.util.Util;
import com.compomics.util.io.flat.SimpleFileWriter;
import java.io.File;
import java.time.Instant;

/**
 * Simple logger.
 *
 * @author Marc Vaudel
 */
public class CliLogger implements AutoCloseable {

    /**
     * Writer for the general log.
     */
    private final SimpleFileWriter logWriter;

    /**
     * Constructor.
     *
     * @param logFile the file where to write the log
     */
    public CliLogger(
            File logFile
    ) {
        this(logFile, null, null);
    }

    /**
     * Constructor.
     *
     * @param logFile The file where to write the log.
     * @param cliName The name of the CLI.
     * @param version The version of the CLI.
     */
    public CliLogger(
            File logFile,
            String cliName,
            String version
    ) {

        logWriter = new SimpleFileWriter(logFile, false);
        
        if (cliName != null) {

            logWriter.writeLine("# " + cliName + " version: " + version);

        }

        logWriter.writeLine(
                "time",
                "type",
                "log"
        );
        
    }

    /**
     * Write a comment line as '# key: value'.
     *
     * @param key the key
     * @param value the value
     */
    public void writeComment(
            String key,
            String value
    ) {

        String line = String.join(
                "",
                "# ", key, ": ", value
        );

        logWriter.writeLine(line);
        
    }

    /**
     * Writes the headers to the files.
     */
    public void writeHeaders() {

        logWriter.writeLine(
                "time",
                "type",
                "log"
        );
        
    }

    /**
     * Logs a message.
     *
     * @param message the message
     */
    public void logMessage(
            String message
    ) {

        String now = Instant.now().toString();

        logWriter.writeLine(
                now,
                "Message",
                "\"" + message.replace(Util.LINE_SEPARATOR, " ") + "\""
        );

        System.out.println(now + " - " + message);

    }

    /**
     * Logs an error.
     *
     * @param message the error message
     */
    public void logError(String message) {

        String now = Instant.now().toString();

        logWriter.writeLine(
                now,
                "Error",
                "\"" + message.replace(Util.LINE_SEPARATOR, " ") + "\""
        );

    }

    @Override
    public void close() {

        logWriter.close();
        
    }
}
