package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.identification.filereaders.MascotFileReader;
import com.compomics.util.experiment.identification.filereaders.OMSSAFileReader;
import com.compomics.util.experiment.identification.filereaders.XTandemFileReader;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 5:33:24 PM
 * This factory will provide the appropriate file reader for each type of file. Null when the format is not supported.
 */
public class FileReaderFactory {

    /**
     * The factory instance
     */
    private static FileReaderFactory singleton = null;

    /**
     * the factory constructor
     */
    private FileReaderFactory() {

    }

    /**
     * A static method to retrieve the instance of the factory
     * @return the factory instance
     */
    public static FileReaderFactory getInstance() {
        if (singleton == null) {
            singleton = new FileReaderFactory();
        }
        return singleton;
    }


    /**
     * This method returns the proper file reader depending on the format of the provided file
     * @param aFile the file to parse
     * @return an adapted file reader
     */
    public FileReader getFileReader(File aFile) {
        String name = aFile.getName().toLowerCase();
        if (name.endsWith("dat")) {
            return new MascotFileReader(aFile);
        } else if (name.endsWith("omx")) {
            return new OMSSAFileReader(aFile);
        } else if (name.endsWith("xml")) {
            return new XTandemFileReader(aFile);
        }
        return null;
    }

}
