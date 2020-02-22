package com.compomics.scripts_marc;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfIndex;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.IndexedMgfReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
            MgfIndex mgfIndex = IndexedMgfReader.getMgfIndex(file, null);
            long mgfIndexEnd = Instant.now().getEpochSecond();
            long mgfIndexingTime = mgfIndexEnd - mgfIndexStart;

            ArrayList<String> mgfIndexTitles = mgfIndex.getSpectrumTitles();

            long cmsFileStart = Instant.now().getEpochSecond();
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(file);
            long cmsFileEnd = Instant.now().getEpochSecond();
            long cmsCreationTime = cmsFileEnd - cmsFileStart;

            CmsFileReader cmsFileReader = msFileHandler.getReader(fileName);

            if (mgfIndexTitles.size() != cmsFileReader.titles.length) {

                throw new IllegalArgumentException("Invalid number of spectra.");

            }

            long mgfRead = 0;
            long cmsRead = 0;
            
            int nLoops = 100;

            for (int i = 0; i < nLoops; i++) {

                Collections.shuffle(mgfIndexTitles);

                for (String title : mgfIndexTitles) {
                    
                    try {

                    long mgfReadStart = Instant.now().getEpochSecond();
                    long index = mgfIndex.getIndex(title);
                    Spectrum mgfSpectrum = MgfIndex.getSpectrum(raf, index, fileName);
                    long mgfReadEnd = Instant.now().getEpochSecond();

                    mgfRead += mgfReadEnd - mgfReadStart;

                    long cmsReadStart = Instant.now().getEpochSecond();
                    Spectrum cmsSpectrum = msFileHandler.getSpectrum(fileName, title);
                    long cmsReadEnd = Instant.now().getEpochSecond();

                    cmsRead += cmsReadEnd - cmsReadStart;
                    
                    if (!mgfSpectrum.isSameAs(cmsSpectrum)) {
                        
                        throw new IllegalArgumentException("Spectra are not the same.");
                        
                    }
                    
                    } catch (Throwable t) {
                        
                        throw new IllegalArgumentException("An error occurred when processing spectrum '" + title + "'.");
                        
                    }
                }
                
                System.out.println(i + " of " + nLoops);
                
            }
            
            int nQueries = nLoops * mgfIndexTitles.size();
            
            System.out.println("Mgf parsing: " + mgfIndexingTime + "s.");
            System.out.println("Cms creation: " + cmsCreationTime + "s.");
            System.out.println("Mgf reading: " + mgfRead + "s (" + nQueries + " queries).");
            System.out.println("Cms reading: " + cmsRead + "s (" + nQueries + " queries).");

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
