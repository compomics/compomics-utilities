package com.compomics.scripts_marc;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfIndex;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.IndexedMgfReader;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

            File mgfFile = new File("C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf");
            File indexFile = new File("C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf.cui");
            File cmsFile = new File("C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.cms");

            indexFile.delete();
            if (indexFile.exists()) {

                throw new IllegalArgumentException("Cui file not deleted.");

            }

            cmsFile.delete();
            if (cmsFile.exists()) {

                throw new IllegalArgumentException("Cms file not deleted.");

            }

            String fileName = mgfFile.getName();

            long mgfIndexStart = Instant.now().getEpochSecond();
            BufferedRandomAccessFile raf = new BufferedRandomAccessFile(mgfFile, "r", 1024 * 100);
            MgfIndex mgfIndex = IndexedMgfReader.getMgfIndex(mgfFile, null);
            long mgfIndexEnd = Instant.now().getEpochSecond();
            long mgfIndexingTime = mgfIndexEnd - mgfIndexStart;

            ArrayList<String> mgfIndexTitles = mgfIndex.getSpectrumTitles();

            long cmsFileStart = Instant.now().getEpochSecond();
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(mgfFile, null);
            long cmsFileEnd = Instant.now().getEpochSecond();
            long cmsCreationTime = cmsFileEnd - cmsFileStart;

            CmsFileReader cmsFileReader = msFileHandler.getReader(fileName);

            if (mgfIndexTitles.size() != cmsFileReader.titles.length) {

                throw new IllegalArgumentException("Invalid number of spectra.");

            }

            long mgfRead = 0;
            long cmsRead = 0;
            long cmsReadParallel = 0;
            long mgfIteration = 0;
            long cmsIteration = 0;
            long cmsIterationParallel = 0;

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

                long cmsReadParallelStart = Instant.now().getEpochSecond();
                mgfIndexTitles.parallelStream()
                        .forEach(
                                title -> msFileHandler.getSpectrum(fileName, title)
                        );
                long cmsReadParallelEnd = Instant.now().getEpochSecond();

                cmsReadParallel += cmsReadParallelEnd - cmsReadParallelStart;
                
                long mgfIterationStart = Instant.now().getEpochSecond();
                MgfFileIterator mgfFileIterator = new MgfFileIterator(mgfFile, null);
                String title;
                while ((title = mgfFileIterator.next()) != null) {
                    
                    mgfFileIterator.getSpectrum();
                    
                }
                long mgfIterationEnd = Instant.now().getEpochSecond();
                
                mgfIteration += mgfIterationEnd - mgfIterationStart;
                
                long cmsIterationStart = Instant.now().getEpochSecond();
                Arrays.stream(msFileHandler.getSpectrumTitles(fileName))
                        .forEach(
                                spectrumTitle -> msFileHandler.getSpectrum(fileName, spectrumTitle)
                        );
                long cmsIterationEnd = Instant.now().getEpochSecond();
                
                cmsIteration += cmsIterationEnd - cmsIterationStart;
                
                long cmsIterationParallelStart = Instant.now().getEpochSecond();
                Arrays.stream(msFileHandler.getSpectrumTitles(fileName))
                        .parallel()
                        .forEach(
                                spectrumTitle -> msFileHandler.getSpectrum(fileName, spectrumTitle)
                        );
                long cmsIterationParallelEnd = Instant.now().getEpochSecond();
                
                cmsIterationParallel += cmsIterationParallelEnd - cmsIterationParallelStart;

                System.out.println(i + " of " + nLoops);

            }

            int nQueries = nLoops * mgfIndexTitles.size();
            double mgfSizePerSpectrum = ((double) mgfFile.length()) / mgfIndexTitles.size();
            double cmsSizePerSpectrum = ((double) cmsFile.length()) / mgfIndexTitles.size();
            double mgfReadTimePerQuery = 1000000.0 * ((double) mgfRead) / nQueries;
            double cmsReadTimePerQuery = 1000000.0 * ((double) cmsRead) / nQueries;
            double cmsParallelReadTimePerQuery = 1000000.0 * ((double) cmsReadParallel) / nQueries;
            double mgfIterationTimePerQuery = 1000000.0 * ((double) mgfIteration) / nQueries;
            double cmsIterationTimePerQuery = 1000000.0 * ((double) cmsIteration) / nQueries;
            double cmsParallelIterationTimePerQuery = 1000000.0 * ((double) cmsIterationParallel) / nQueries;

            System.out.println("Mgf parsing: " + mgfIndexingTime + " s, " + mgfSizePerSpectrum + " B per spectrum.");
            System.out.println("Cms creation: " + cmsCreationTime + " s, " + cmsSizePerSpectrum + " B per spectrum.");
            System.out.println("Mgf reading: " + mgfReadTimePerQuery + " us per spectrum (" + nQueries + " queries in " + mgfRead + " s).");
            System.out.println("Cms reading: " + cmsReadTimePerQuery + " us per spectrum (" + nQueries + " queries in " + cmsRead + " s).");
            System.out.println("Cms reading parallel: " + cmsParallelReadTimePerQuery + " us per spectrum (" + nQueries + " queries in " + cmsReadParallel + " s).");
            System.out.println("Mgf iteration: " + mgfIterationTimePerQuery + " us per spectrum (" + nQueries + " queries in " + mgfIteration + " s).");
            System.out.println("Cms iteration: " + cmsIterationTimePerQuery + " us per spectrum (" + nQueries + " queries in " + cmsIteration + " s).");
            System.out.println("Cms iteration parallel: " + cmsParallelIterationTimePerQuery + " us per spectrum (" + nQueries + " queries in " + cmsIterationParallel + " s).");

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
