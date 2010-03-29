package com.compomics.util.io;

import com.compomics.util.enumeration.CompomicsTools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.*;

/**
 * Created by IntelliJ IDEA. User: kenny Date: Mar 29, 2010 Time: 5:23:10 PM
 * <p/>
 * This class
 */
public class CompomicsToolsLogger {

    /**
     * Returns the FileHandler for the specific tool.
     *
     * @param aTool The required compomics tool FileHandler .
     * @return FileHandler instance for the tool.
     */
    public static FileHandler getLoggingFileHandler(CompomicsTools aTool) throws IOException {
        PropertiesManager lManager = PropertiesManager.getInstance();
        FileHandler lFileHandler = new FileHandler(lManager.getApplicationFolder(aTool)
                + File.separator
                + aTool.getName()
                + ".log", true);
        lFileHandler.setFormatter(new SimpleFormatter());

        return lFileHandler;
    }
}
