package com.compomics.util.db;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Disable the derby log file.
 *
 * @author Harald Barsnes
 */
public class DerbyUtil {

    /**
     * Disable the derby log.
     */
    public static void disableDerbyLog() {
        System.setProperty("derby.stream.error.method", "com.compomics.util.db.DerbyUtil.disabledDerbyLog");
    }

    /**
     * Returns the disabled Derby log file. Not for direct use. Call the method
     * disableDerbyLog instead.
     *
     * @return the disabled Derby log file.
     */
    public static java.io.OutputStream disabledDerbyLog() {
        return new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
    }
}
