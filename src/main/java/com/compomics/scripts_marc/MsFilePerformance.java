package com.compomics.scripts_marc;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfIndex;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfReader;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This script test the speed of the ms file access.
 *
 * @author Marc Vaudel
 */
public class MsFilePerformance {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

            File file = new File("C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf");
            File indexFile = new File("C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf.cui");
            File cmsFile = new File("C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf.cms");

            indexFile.delete();
            if (indexFile.exists()) {

                throw new IllegalArgumentException("Cui file not deleted.");

            }

            cmsFile.delete();
            if (cmsFile.exists()) {

                throw new IllegalArgumentException("Cms file not deleted.");

            }

            String fileName = file.getName();
                    
            long mgfIndexStart = Instant.now().getEpochSecond();
            BufferedRandomAccessFile raf = new BufferedRandomAccessFile(file, "r", 1024 * 100);
            MgfIndex mgfIndex = MgfReader.getIndexMap(file, null);
            long mgfIndexEnd = Instant.now().getEpochSecond();

            long cmsFileStart = Instant.now().getEpochSecond();
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(cmsFile);
            long cmsFileEnd = Instant.now().getEpochSecond();
            
            CmsFileReader cmsFileReader = msFileHandler.getReader(fileName);

            ArrayList<String> mgfIndexTitles = mgfIndex.getSpectrumTitles();
            
            if (mgfIndexTitles.size() != cmsFileReader.titles.length) {

                throw new IllegalArgumentException("Invalid number of spectra.");

            }
            
            
            

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
