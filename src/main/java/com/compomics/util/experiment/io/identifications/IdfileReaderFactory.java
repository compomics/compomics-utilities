package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.identification.advocates.SearchEngine;
import com.compomics.util.experiment.io.identifications.idfilereaders.AndromedaIdfileReader;
import com.compomics.util.experiment.io.identifications.idfilereaders.MascotIdfileReader;
import com.compomics.util.experiment.io.identifications.idfilereaders.OMSSAIdfileReader;
import com.compomics.util.experiment.io.identifications.idfilereaders.XTandemIdfileReader;
import com.compomics.util.gui.waiting.WaitingHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JProgressBar;
import org.xml.sax.SAXException;

/**
 * This factory will provide the appropriate identification file reader for each type of file.
 * Null when the format is not supported.
 *
 * @author Marc Vaudel
 */
public class IdfileReaderFactory {

    /**
     * The factory instance.
     */
    private static IdfileReaderFactory singleton = null;

    /**
     * the factory constructor
     */
    private IdfileReaderFactory() {
    }

    /**
     * A static method to retrieve the instance of the factory.
     *
     * @return the factory instance
     */
    public static IdfileReaderFactory getInstance() {
        if (singleton == null) {
            singleton = new IdfileReaderFactory();
        }
        return singleton;
    }

    /**
     * This method returns the proper identification file reader depending on the format of the provided file.
     * It is very important to close the file reader after creation.
     * //@TODO: create parsers using waiting handlers and indexed files
     *
     * @param aFile the file to parse
     * @param waitingHandler a waiting handler to display the results. Can be null
     * @return an adapted file reader
     * @throws SAXException  
     * @throws FileNotFoundException  
     * @throws IOException  
     */
    public IdfileReader getFileReader(File aFile, WaitingHandler waitingHandler) throws SAXException, FileNotFoundException, IOException {
        String name = aFile.getName().toLowerCase();
        if (name.endsWith("dat")) {
            return new MascotIdfileReader(aFile);
        } else if (name.endsWith("omx")) {
            return new OMSSAIdfileReader(aFile);
        } else if (name.endsWith("xml")) {
            return new XTandemIdfileReader(aFile);
        } else if (name.endsWith("res")) {
            return new AndromedaIdfileReader(aFile, waitingHandler);
        }
        return null;
    }

    /**
     * This method return the search engine corresponding to the given file.
     * 
     * @param aFile     an identification file
     * @return the index of the search engine
     */
    public int getSearchEngine(File aFile) {
        if (aFile.getName().endsWith("dat")) {
            return SearchEngine.MASCOT;
        } else if (aFile.getName().endsWith("omx")) {
            return SearchEngine.OMSSA;
        } else if (aFile.getName().endsWith("xml")) {
            if (!aFile.getName().equals("mods.xml") && !aFile.getName().equals("usermods.xml")) {
                return SearchEngine.XTANDEM;
            }
        }
        return -1;
    }
}
