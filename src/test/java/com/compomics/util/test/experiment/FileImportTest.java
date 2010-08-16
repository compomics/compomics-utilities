package com.compomics.util.test.experiment;

import junit.framework.TestCase;
import com.compomics.util.experiment.identification.FileReaderFactory;
import com.compomics.util.experiment.identification.FileReader;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.biology.PTMFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 5:40:53 PM
 * This test will import various files and display the result of the parsing.
 */
public class FileImportTest extends TestCase {

    private static final String MASCOT_FILE = "testFiles/orbitrap003956.dat";
    private static final String OMSSA_FILE = "testFiles/orbitrap003956.omx";
    private static final String XTANDEM_FILE = "testFiles/orbitrap003956.xml";
    private static final String MODIFICATION_FILE = "resources/mods.xml";

    private FileReaderFactory fileReaderFactory = FileReaderFactory.getInstance();
    private PTMFactory ptmFactory = PTMFactory.getInstance();


    public void testReading() {

        File modificationFile = new File(MODIFICATION_FILE);
        try {
        ptmFactory.importModifications(modificationFile);
        } catch (Exception e) {
            String report = e.getLocalizedMessage();
        }

        File mascotFile = new File(MASCOT_FILE);
        File omssaFile = new File(OMSSA_FILE);
        File xTandemFile = new File(XTANDEM_FILE);

        FileReader mascotReader = fileReaderFactory.getFileReader(mascotFile);
        HashSet<SpectrumMatch> matches = new HashSet();
        matches = mascotReader.getAllSpectrumMatches();


        /*

        FileReader mascotReader = fileReaderFactory.getFileReader(mascotFile);
        FileReader omssaReader = fileReaderFactory.getFileReader(omssaFile);
        FileReader xTandemReader = fileReaderFactory.getFileReader(xTandemFile);
        HashSet<SpectrumMatch> matches = new HashSet();
        matches = mascotReader.getAllSpectrumMatches();
        matches = new HashSet();
        matches = xTandemReader.getAllSpectrumMatches();
        matches = new HashSet();
        matches = omssaReader.getAllSpectrumMatches();
        matches = new HashSet();
          */
    }
}
