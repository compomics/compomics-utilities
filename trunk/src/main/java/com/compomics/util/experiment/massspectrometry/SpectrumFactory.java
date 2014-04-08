package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.io.SerializationUtils;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.SelectedIonList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This factory will provide the spectra when needed.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SpectrumFactory {

    /**
     * The instance of the factory.
     */
    private static SpectrumFactory instance = null;
    /**
     * Map of already loaded spectra.
     */
    private HashMap<String, HashMap<String, Spectrum>> currentSpectrumMap = new HashMap<String, HashMap<String, Spectrum>>();
    /**
     * Map of already loaded precursors.
     */
    private HashMap<String, HashMap<String, Precursor>> loadedPrecursorsMap = new HashMap<String, HashMap<String, Precursor>>();
    /**
     * Maximal number of spectra in cache.
     */
    private static int nSpectraCache = 10000;
    /**
     * Maximal number of precursors in cache.
     */
    private static int nPrecursorsCache = 1000000;
    /**
     * List of the loaded spectra.
     */
    private LinkedBlockingDeque<String> loadedSpectra = new LinkedBlockingDeque<String>();
    /**
     * List of the loaded precursors.
     */
    private LinkedBlockingDeque<String> loadedPrecursors = new LinkedBlockingDeque<String>();
    /**
     * Map of the random access files of the loaded mgf files (filename ->
     * random access file).
     */
    private HashMap<String, BufferedRandomAccessFile> mgfFilesMap = new HashMap<String, BufferedRandomAccessFile>();
    /**
     * Map of the mgf indexes (fileName -> mgf index).
     */
    private HashMap<String, MgfIndex> mgfIndexesMap = new HashMap<String, MgfIndex>();
    /**
     * Map of the mzML unmarshallers (fileName -> unmarshaller).
     */
    private HashMap<String, MzMLUnmarshaller> mzMLUnmarshallers = new HashMap<String, MzMLUnmarshaller>();
    /**
     * Map of the spectrum file mapped according to the name used by the search
     * engine.
     */
    private HashMap<String, File> idToSpectrumName = new HashMap<String, File>();

    /**
     * Constructor.
     */
    private SpectrumFactory() {
    }

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static SpectrumFactory getInstance() {
        if (instance == null) {
            instance = new SpectrumFactory();
        }
        return instance;
    }

    /**
     * Static method returning the instance of the factory with a new cache
     * size.
     *
     * @param nCache
     * @return the instance of the factory with a new cache size
     */
    public static SpectrumFactory getInstance(int nCache) {
        if (instance == null) {
            instance = new SpectrumFactory();
        }
        instance.setCacheSize(nCache);
        return instance;
    }

    /**
     * Clears the factory getInstance() needs to be called afterwards.
     */
    public void clearFactory() {
        currentSpectrumMap.clear();
        loadedPrecursorsMap.clear();
        loadedSpectra.clear();
        loadedPrecursors.clear();
        mgfFilesMap.clear();
        mgfIndexesMap.clear();
        mzMLUnmarshallers.clear();
        idToSpectrumName.clear();
    }

    /**
     * Empties the cache.
     */
    public void emptyCache() {
        currentSpectrumMap.clear();
        loadedPrecursorsMap.clear();
        loadedSpectra.clear();
        loadedPrecursors.clear();
    }

    /**
     * Sets the spectrum cache size.
     *
     * @param nCache the new cache size
     */
    public void setCacheSize(int nCache) {
        SpectrumFactory.nSpectraCache = nCache;
    }

    /**
     * Returns the spectrum cache size.
     *
     * @return the cache size
     */
    public int getCacheSize() {
        return nSpectraCache;
    }

    /**
     * Add spectra to the factory.
     *
     * @param spectrumFile The spectrum file, can be mgf or mzML
     * @throws FileNotFoundException Exception thrown whenever the file was not
     * found
     * @throws IOException Exception thrown whenever an error occurred while
     * reading the file
     * @throws ClassNotFoundException Exception thrown whenever an error
     * occurred while deserializing the index .cui file.
     * @deprecated use the version with the waiting handler instead
     */
    public void addSpectra(File spectrumFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        addSpectra(spectrumFile, null);
    }

    /**
     * Add spectra to the factory.
     *
     * @param spectrumFile The spectrum file, can be mgf or mzML
     * @param waitingHandler the waiting handler
     * @throws FileNotFoundException Exception thrown whenever the file was not
     * found
     * @throws IOException Exception thrown whenever an error occurred while
     * reading the file
     * @throws IllegalArgumentException Exception thrown if an unknown format
     * was detected.
     */
    public void addSpectra(File spectrumFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, IllegalArgumentException {

        String fileName = spectrumFile.getName();

        if (fileName.toLowerCase().endsWith(".mgf")) {

            File indexFile = new File(spectrumFile.getParent(), fileName + ".cui");
            MgfIndex mgfIndex = null;

            if (indexFile.exists()) {
                try {
                    MgfIndex tempIndex = getIndex(indexFile);
                    Long indexLastModified = tempIndex.getLastModified();

                    if (indexLastModified != null) {
                        long fileLastModified = spectrumFile.lastModified();

                        if (indexLastModified == fileLastModified) {
                            mgfIndex = tempIndex;
                        } else {
                            System.err.println("Reindexing: " + fileName + ". (changes in the file detected)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Reindexing: " + fileName + ". (Reason: " + e.getLocalizedMessage() + ")");
                }
            }

            if (mgfIndex == null) {
                mgfIndex = MgfReader.getIndexMap(spectrumFile, waitingHandler);

                if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                    return; // return without saving the partial index
                }

                writeIndex(mgfIndex, spectrumFile.getParentFile());
            }

            if (mgfIndex == null) {
                throw new IllegalArgumentException("An error occurred while indexing " + spectrumFile.getAbsolutePath());
            }

            mgfFilesMap.put(fileName, new BufferedRandomAccessFile(spectrumFile, "r", 1024 * 100));
            mgfIndexesMap.put(fileName, mgfIndex);

        } else if (fileName.toLowerCase().endsWith(".mzml")) {
            MzMLUnmarshaller mzMLUnmarshaller = new MzMLUnmarshaller(spectrumFile);
            mzMLUnmarshallers.put(fileName, mzMLUnmarshaller);
        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
    }

    /**
     * Returns the precursor of the desired spectrum. The value will be saved in
     * cache.
     *
     * @param fileName the name of the spectrum file
     * @param spectrumTitle the title of the spectrum
     *
     * @return the corresponding precursor
     *
     * @throws IOException exception thrown whenever the file was not parsed
     * correctly
     * @throws MzMLUnmarshallerException exception thrown whenever the file was
     * not parsed correctly
     */
    public Precursor getPrecursor(String fileName, String spectrumTitle) throws IOException, MzMLUnmarshallerException {
        return getPrecursor(fileName, spectrumTitle, true);
    }

    /**
     * Returns the precursor of the desired spectrum.
     *
     * @param fileName the name of the spectrum file
     * @param spectrumTitle the title of the spectrum
     * @param save if true the precursor will be saved in cache
     *
     * @return the corresponding precursor
     *
     * @throws IOException exception thrown whenever the file was not parsed
     * correctly
     * @throws MzMLUnmarshallerException exception thrown whenever the file was
     * not parsed correctly
     */
    public Precursor getPrecursor(String fileName, String spectrumTitle, boolean save) throws IOException, MzMLUnmarshallerException {
        HashMap<String, Spectrum> fileSpectrumMap = currentSpectrumMap.get(fileName);
        if (fileSpectrumMap != null) {
            Spectrum spectrum = fileSpectrumMap.get(spectrumTitle);
            if (spectrum != null) {
                return ((MSnSpectrum) spectrum).getPrecursor();
            }
        }
        HashMap<String, Precursor> filePrecursorMap = loadedPrecursorsMap.get(fileName);
        if (filePrecursorMap != null) {
            Precursor currentPrecursor = filePrecursorMap.get(spectrumTitle);
            if (currentPrecursor != null) {
                return currentPrecursor;
            }
        }
        return getPrecursor(fileName, spectrumTitle, save, 0);
    }

    /**
     * Returns the precursor of the desired spectrum. The value will be saved in
     * cache.
     *
     * @param spectrumKey the key of the spectrum
     * @return the corresponding precursor
     * @throws IOException exception thrown whenever the file was not parsed
     * correctly
     * @throws MzMLUnmarshallerException exception thrown whenever the file was
     * not parsed correctly
     * @throws IllegalArgumentException exception thrown whenever the file was
     * not parsed correctly
     */
    public Precursor getPrecursor(String spectrumKey) throws IOException, MzMLUnmarshallerException, IllegalArgumentException {
        return getPrecursor(spectrumKey, true);
    }

    /**
     * Returns the maximum m/z for the desired file.
     *
     * @param fileName the file of interest
     * @return the max m/z
     */
    public Double getMaxMz(String fileName) {
        return mgfIndexesMap.get(fileName).getMaxMz();
    }

    /**
     * Returns the maximum m/z for the whole project.
     *
     * @return the max m/z
     */
    public Double getMaxMz() {

        double maxMz = 0;

        for (MgfIndex mgfIndex : mgfIndexesMap.values()) {
            if (maxMz < mgfIndex.getMaxMz()) {
                maxMz = mgfIndex.getMaxMz();
            }
        }

        return maxMz;
    }

    /**
     * Returns the max precursor charge encountered for the given mgf file.
     *
     * @param fileName the name of the mgf file
     * @return the max precursor charge encountered
     */
    public Integer getMaxCharge(String fileName) {
        return mgfIndexesMap.get(fileName).getMaxCharge();
    }

    /**
     * Returns the max precursor charge encountered among all loaded mgf files.
     *
     * @return the max precursor charge encountered among all loaded mgf files
     */
    public Integer getMaxCharge() {
        int maxCharge = 0;
        for (MgfIndex mgfIndex : mgfIndexesMap.values()) {
            if (mgfIndex.getMaxCharge() > maxCharge) {
                maxCharge = mgfIndex.getMaxCharge();
            }
        }
        return maxCharge;
    }

    /**
     * Returns the max peak count encountered for the given mgf file.
     *
     * @param fileName the name of the mgf file
     * @return the max peak count encountered
     */
    public Integer getMaxPeakCount(String fileName) {
        return mgfIndexesMap.get(fileName).getMaxPeakCount();
    }

    /**
     * Returns the max peak count encountered among all loaded mgf files.
     *
     * @return the max peak count encountered among all loaded mgf files
     */
    public Integer getMaxPeakCount() {
        int maxPeakCount = 0;
        for (MgfIndex mgfIndex : mgfIndexesMap.values()) {
            if (mgfIndex.getMaxPeakCount() > maxPeakCount) {
                maxPeakCount = mgfIndex.getMaxPeakCount();
            }
        }
        return maxPeakCount;
    }

    /**
     * Returns the maximum precursor intensity for the desired file.
     *
     * @param fileName the file of interest
     * @return the max precursor intensity
     */
    public Double getMaxIntensity(String fileName) {
        return mgfIndexesMap.get(fileName).getMaxIntensity();
    }

    /**
     * Returns the maximum precursor intensity for the whole project.
     *
     * @return the max precursor intensity
     */
    public Double getMaxIntensity() {

        double maxIntensity = 0;

        for (MgfIndex mgfIndex : mgfIndexesMap.values()) {
            if (maxIntensity < mgfIndex.getMaxIntensity()) {
                maxIntensity = mgfIndex.getMaxIntensity();
            }
        }

        return maxIntensity;
    }

    /**
     * Returns the maximum RT for the desired file.
     *
     * @param fileName the file of interest
     * @return the max RT
     */
    public Double getMaxRT(String fileName) {
        return mgfIndexesMap.get(fileName).getMaxRT();
    }

    /**
     * Returns the minimum RT for the desired file.
     *
     * @param fileName the file of interest
     * @return the min RT
     */
    public Double getMinRT(String fileName) {
        return mgfIndexesMap.get(fileName).getMinRT();
    }

    /**
     * Returns the maximum RT for the whole project.
     *
     * @return the max RT
     */
    public Double getMaxRT() {

        double maxRT = 0;

        for (MgfIndex mgfIndex : mgfIndexesMap.values()) {
            if (maxRT < mgfIndex.getMaxRT()) {
                maxRT = mgfIndex.getMaxRT();
            }
        }

        return maxRT;
    }

    /**
     * Returns the minimum RT for the whole project.
     *
     * @return the min RT
     */
    public Double getMinRT() {

        double minRT = Double.MAX_VALUE;

        for (MgfIndex mgfIndex : mgfIndexesMap.values()) {
            if (minRT > mgfIndex.getMinRT()) {
                minRT = mgfIndex.getMinRT();
            }
        }

        if (minRT == Double.MAX_VALUE) {
            minRT = 0;
        }

        return minRT;
    }

    /**
     * Returns the number of spectra in the desired file.
     *
     * @param fileName the file of interest
     * @return the number of spectra
     */
    public int getNSpectra(String fileName) {
        return mgfIndexesMap.get(fileName).getNSpectra();
    }

    /**
     * Returns the total number of spectra in all files.
     *
     * @return the total number of spectra in all files
     */
    public int getNSpectra() {

        int totalSpectrumCount = 0;

        for (String fileName : mgfIndexesMap.keySet()) {
            totalSpectrumCount += getNSpectra(fileName);
        }

        return totalSpectrumCount;
    }

    /**
     * Returns the precursor of the desired spectrum.
     *
     * @param spectrumKey the key of the spectrum
     * @param save boolean indicating whether the loaded precursor should be
     * stored in the factory. False by default
     * @return the corresponding precursor
     * @throws IOException exception thrown whenever the file was not parsed
     * correctly
     * @throws MzMLUnmarshallerException exception thrown whenever the file was
     * not parsed correctly
     * @throws IllegalArgumentException exception thrown whenever the file was
     * not parsed correctly
     */
    public Precursor getPrecursor(String spectrumKey, boolean save) throws IOException, MzMLUnmarshallerException, IllegalArgumentException {
        String fileName = Spectrum.getSpectrumFile(spectrumKey);
        String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);
        return getPrecursor(fileName, spectrumTitle, save);
    }

    /**
     * Returns a boolean indicating whether the spectrum file has been loaded.
     *
     * @param fileName the file name
     * @return a boolean indicating whether the spectrum file has been loaded
     */
    public boolean fileLoaded(String fileName) {
        return mgfIndexesMap.containsKey(fileName);
    }

    /**
     * Returns a boolean indicating whether the spectrum is contained in the
     * given spectrum file.
     *
     * @param fileName the name of the spectrum file
     * @param spectrumTitle the title of the spectrum
     * @return a boolean indicating whether the spectrum is contained in the
     * given spectrum file
     */
    public boolean spectrumLoaded(String fileName, String spectrumTitle) {
        // a special fix for mgf files with strange titles...
        spectrumTitle = fixMgfTitle(spectrumTitle, fileName);
        return mgfIndexesMap.containsKey(fileName) && mgfIndexesMap.get(fileName).containsSpectrum(spectrumTitle);
    }

    /**
     * A boolean indicating whether the spectrum is loaded in the factory.
     *
     * @param spectrumKey the spectrum key
     * @return a boolean indicating whether the spectrum is loaded in the
     * factory
     */
    public boolean spectrumLoaded(String spectrumKey) {
        String fileName = Spectrum.getSpectrumFile(spectrumKey);
        String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);
        return spectrumLoaded(fileName, spectrumTitle);
    }

    /**
     * Returns the precursor of the desired spectrum.
     *
     * @param spectrumKey the key of the spectrum
     * @param save boolean indicating whether the loaded precursor should be
     * stored in the factory
     * @param nErrors the number of errors encountered. If less than 100, the
     * method will retry after a tempo of 50ms to avoid network related issues.
     * @return the corresponding precursor
     * @throws IOException exception thrown whenever the file was not parsed
     * correctly
     * @throws MzMLUnmarshallerException exception thrown whenever the file was
     * not parsed correctly
     * @throws IllegalArgumentException exception thrown whenever the file was
     * not parsed correctly
     */
    private synchronized Precursor getPrecursor(String fileName, String spectrumTitle, boolean save, int errorCounter) throws IOException, MzMLUnmarshallerException, IllegalArgumentException {

        Precursor currentPrecursor = null;

        if (fileName.toLowerCase().endsWith(".mgf")) {

            // a special fix for mgf files with strange titles...
            spectrumTitle = fixMgfTitle(spectrumTitle, fileName);

            if (mgfIndexesMap.get(fileName) == null) {
                throw new IOException("Mgf file not found: \'" + fileName + "\'.");
            }
            if (mgfIndexesMap.get(fileName).getIndex(spectrumTitle) == null) {
                throw new IOException("Spectrum \'" + spectrumTitle + "\' in mgf file \'" + fileName + "\' not found.");
            }
            try {
                currentPrecursor = MgfReader.getPrecursor(mgfFilesMap.get(fileName), mgfIndexesMap.get(fileName).getIndex(spectrumTitle), fileName);
            } catch (Exception e) {
                if (errorCounter <= 100) {
                    try {
                        wait(50);
                    } catch (InterruptedException ie) {
                    }
                    return getPrecursor(fileName, spectrumTitle, save, errorCounter + 1);
                } else {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error while loading precursor of spectrum " + spectrumTitle + " of file " + fileName + ".");
                }
            }
        } else if (fileName.toLowerCase().endsWith(".mzml")) {
            uk.ac.ebi.jmzml.model.mzml.Spectrum mzMLSpectrum = mzMLUnmarshallers.get(fileName).getSpectrumById(spectrumTitle);
            int level = 2;
            double mzPrec = 0.0;
            double scanTime = -1.0;
            int chargePrec = 0;
            for (CVParam cvParam : mzMLSpectrum.getCvParam()) {
                if (cvParam.getAccession().equals("MS:1000511")) {
                    level = new Integer(cvParam.getValue());
                    break;
                }
            }
            ScanList scanList = mzMLSpectrum.getScanList();
            if (scanList != null) {
                for (CVParam cvParam : scanList.getScan().get(scanList.getScan().size() - 1).getCvParam()) {
                    if (cvParam.getAccession().equals("MS:1000016")) {
                        scanTime = new Double(cvParam.getValue());
                        break;
                    }
                }
            }
            PrecursorList precursorList = mzMLSpectrum.getPrecursorList();
            if (precursorList != null) {
                if (precursorList.getCount().intValue() == 1) {
                    SelectedIonList sIonList = precursorList.getPrecursor().get(0).getSelectedIonList();
                    if (sIonList != null) {
                        for (CVParam cvParam : sIonList.getSelectedIon().get(0).getCvParam()) {
                            if (cvParam.getAccession().equals("MS:1000744")) {
                                mzPrec = new Double(cvParam.getValue());
                            } else if (cvParam.getAccession().equals("MS:1000041")) {
                                chargePrec = new Integer(cvParam.getValue());
                            }
                        }
                    }
                }
            }
            if (level == 1) {
                throw new IllegalArgumentException("MS1 spectrum");
            } else {
                //@TODO: update the charge here
//                    currentPrecursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
            }
        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
        if (save) {
            HashMap<String, Precursor> fileMap = loadedPrecursorsMap.get(fileName);
            if (fileMap == null) {
                fileMap = new HashMap<String, Precursor>();
                loadedPrecursorsMap.put(fileName, fileMap);
            }
            fileMap.put(spectrumTitle, currentPrecursor);
            String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
            loadedPrecursors.add(spectrumKey);
            while (loadedPrecursors.size() > nPrecursorsCache) {
                String tempKey = loadedPrecursors.pollFirst();
                String tempFileName = Spectrum.getSpectrumFile(tempKey);
                fileMap = loadedPrecursorsMap.get(tempFileName);
                if (fileMap != null) {
                    String tempTitle = Spectrum.getSpectrumTitle(tempKey);
                    fileMap.remove(tempTitle);
                }

            }
        }
        return currentPrecursor;
    }

    /**
     * Returns the desired spectrum.
     *
     * @param spectrumFile name of the spectrum file
     * @param spectrumTitle title of the spectrum
     * @return the desired spectrum
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while parsing the mzML file
     */
    public Spectrum getSpectrum(String spectrumFile, String spectrumTitle) throws IOException, MzMLUnmarshallerException {
        HashMap<String, Spectrum> fileMap = currentSpectrumMap.get(spectrumFile);
        if (fileMap != null) {
            Spectrum currentSpectrum = fileMap.get(spectrumTitle);
            if (currentSpectrum != null) {
                return currentSpectrum;
            }
        }
        return getSpectrum(spectrumFile, spectrumTitle, 0);
    }

    /**
     * Returns the desired spectrum.
     *
     * @param spectrumKey key of the spectrum
     * @return the desired spectrum
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while parsing the file
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while parsing the file
     */
    public Spectrum getSpectrum(String spectrumKey) throws IOException, IllegalArgumentException, MzMLUnmarshallerException {
        String fileName = Spectrum.getSpectrumFile(spectrumKey);
        String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);
        return getSpectrum(fileName, spectrumTitle);
    }

    /**
     * Returns the desired spectrum.
     *
     * @param spectrumKey key of the spectrum
     * @return the desired spectrum
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while parsing the file
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while parsing the file
     */
    private synchronized Spectrum getSpectrum(String spectrumFile, String spectrumTitle, int errorCounter) throws IOException, IllegalArgumentException, MzMLUnmarshallerException {

        Spectrum currentSpectrum = null;

        if (spectrumFile.toLowerCase().endsWith(".mgf")) {

            // a special fix for mgf files with strange titles...
            spectrumTitle = fixMgfTitle(spectrumTitle, spectrumFile);

            if (mgfIndexesMap.get(spectrumFile) == null) {
                throw new FileNotFoundException("Mgf file not found: \'" + spectrumFile + "\'!");
            }
            if (mgfIndexesMap.get(spectrumFile).getIndex(spectrumTitle) == null) {
                throw new IOException("Spectrum \'" + spectrumTitle + "\' in mgf file \'" + spectrumFile + "\' not found!");
            }
            try {
                currentSpectrum = MgfReader.getSpectrum(mgfFilesMap.get(spectrumFile), mgfIndexesMap.get(spectrumFile).getIndex(spectrumTitle), spectrumFile);
            } catch (Exception e) {
                if (errorCounter <= 100) {
                    try {
                        wait(50);
                    } catch (InterruptedException ie) {
                    }
                    return getSpectrum(spectrumFile, spectrumTitle, errorCounter + 1);
                } else {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error while loading spectrum " + spectrumTitle + " of file " + spectrumFile + ".");
                }
            }
        } else if (spectrumFile.toLowerCase().endsWith(".mzml")) {

            if (mzMLUnmarshallers.get(spectrumFile) == null) {
                throw new IOException("mzML file not found: \'" + spectrumFile + "\'!");
            }
            if (mzMLUnmarshallers.get(spectrumFile).getSpectrumById(spectrumTitle) == null) {
                throw new IOException("Spectrum \'" + spectrumTitle + "\' in mzML file \'" + spectrumFile + "\' not found!");
            }

            uk.ac.ebi.jmzml.model.mzml.Spectrum mzMLSpectrum = mzMLUnmarshallers.get(spectrumFile).getSpectrumById(spectrumTitle);
            int level = 2;
            double mzPrec = 0.0;
            double scanTime = -1.0;
            int chargePrec = 0;
            for (CVParam cvParam : mzMLSpectrum.getCvParam()) {
                if (cvParam.getAccession().equals("MS:1000511")) {
                    level = new Integer(cvParam.getValue());
                    break;
                }
            }
            ScanList scanList = mzMLSpectrum.getScanList();
            if (scanList != null) {
                for (CVParam cvParam : scanList.getScan().get(scanList.getScan().size() - 1).getCvParam()) {
                    if (cvParam.getAccession().equals("MS:1000016")) {
                        scanTime = new Double(cvParam.getValue());
                        break;
                    }
                }
            }
            PrecursorList precursorList = mzMLSpectrum.getPrecursorList();
            if (precursorList != null) {
                if (precursorList.getCount().intValue() == 1) {
                    SelectedIonList sIonList = precursorList.getPrecursor().get(0).getSelectedIonList();
                    if (sIonList != null) {
                        for (CVParam cvParam : sIonList.getSelectedIon().get(0).getCvParam()) {
                            if (cvParam.getAccession().equals("MS:1000744")) {
                                mzPrec = new Double(cvParam.getValue());
                            } else if (cvParam.getAccession().equals("MS:1000041")) {
                                chargePrec = new Integer(cvParam.getValue());
                            }
                        }
                    }
                }
            }
            List<BinaryDataArray> bdal = mzMLSpectrum.getBinaryDataArrayList().getBinaryDataArray();
            BinaryDataArray mzBinaryDataArray = (BinaryDataArray) bdal.get(0);
            Number[] mzNumbers = mzBinaryDataArray.getBinaryDataAsNumberArray();
            BinaryDataArray intBinaryDataArray = (BinaryDataArray) bdal.get(1);
            Number[] intNumbers = intBinaryDataArray.getBinaryDataAsNumberArray();
            HashMap<Double, Peak> peakList = new HashMap<Double, Peak>();
            for (int i = 0; i < mzNumbers.length; i++) {
                peakList.put(mzNumbers[i].doubleValue(), new Peak(mzNumbers[i].doubleValue(), intNumbers[i].doubleValue(), scanTime));
            }
            if (level == 1) {
                currentSpectrum = new MS1Spectrum(spectrumFile, spectrumTitle, scanTime, peakList);
            } else {
                //@TODO: update the charge here
                //Precursor precursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
                //currentSpectrum = new MSnSpectrum(level, precursor, spectrumTitle, peakList, fileName, scanTime);
            }
        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
        if (loadedSpectra.size() == nSpectraCache) {
            String tempKey = loadedSpectra.pollFirst();
            String tempFile = Spectrum.getSpectrumFile(tempKey);
            HashMap<String, Spectrum> fileMap = currentSpectrumMap.get(tempFile);
            if (fileMap != null) {
                String tempTitle = Spectrum.getSpectrumTitle(tempKey);
                fileMap.remove(tempTitle);
            }
        }
        HashMap<String, Spectrum> fileMap = currentSpectrumMap.get(spectrumFile);
        if (fileMap == null) {
            fileMap = new HashMap<String, Spectrum>();
            currentSpectrumMap.put(spectrumFile, fileMap);
        }
        fileMap.put(spectrumTitle, currentSpectrum);
        String spectrumKey = Spectrum.getSpectrumKey(spectrumFile, spectrumTitle);
        loadedSpectra.add(spectrumKey);
        return currentSpectrum;
    }

    /**
     * Writes the given mgf file index in the given directory.
     *
     * @param mgfIndex the mgf file index
     * @param directory the destination directory
     * @throws IOException exception thrown whenever an error is encountered
     * while writing the file
     */
    public void writeIndex(MgfIndex mgfIndex, File directory) throws IOException {
        File indexFile = new File(directory, mgfIndex.getFileName() + ".cui");
        SerializationUtils.writeObject(mgfIndex, indexFile);
    }

    /**
     * Deserializes the index of an mgf file.
     *
     * @param mgfIndex the mgf index cui file
     * @return the corresponding mgf index object
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error was encountered
     * while reading the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the object
     */
    public MgfIndex getIndex(File mgfIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
        return (MgfIndex) SerializationUtils.readObject(mgfIndex);
    }

    /**
     * Closes all opened files.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * closing the files
     */
    public void closeFiles() throws IOException {
        for (BufferedRandomAccessFile randomAccessFile : mgfFilesMap.values()) {
            randomAccessFile.close();
        }
    }

    /**
     * Returns a list of loaded mgf files.
     *
     * @return a list of loaded mgf files
     */
    public ArrayList<String> getMgfFileNames() {
        return new ArrayList<String>(mgfFilesMap.keySet());
    }

    /**
     * Returns a list of loaded mzML files.
     *
     * @return a list of loaded mzML files
     */
    public ArrayList<String> getMzMLFileNames() {
        return new ArrayList<String>(mzMLUnmarshallers.keySet());
    }

    /**
     * Returns a list of titles from indexed spectra in the given file.
     *
     * @param mgfFile the name of the mgf file
     * @return a list of titles from indexed spectra in the given file
     */
    public ArrayList<String> getSpectrumTitles(String mgfFile) {
        return mgfIndexesMap.get(mgfFile).getSpectrumTitles();
    }

    /**
     * Returns the spectrum index of the given spectrum in the given file. If
     * the same spectrum title is used more than ones the last index is
     * returned. Null if not found.
     *
     * @param spectrumTitle the spectrum title
     * @param mgfFile the name of the mgf file
     * @return the spectrum index of the given spectrum
     */
    public Integer getSpectrumIndex(String spectrumTitle, String mgfFile) {
        MgfIndex mgfIndex = mgfIndexesMap.get(mgfFile);
        if (mgfIndex == null) {
            return null;
        }
        return mgfIndex.getSpectrumIndex(spectrumTitle);
    }

    /**
     * Returns the spectrum title of the spectrum of the given number in the
     * given file. 0 is the first spectrum. Null if not found.
     *
     * @param mgfFile the name of the mgf file of interest
     * @param spectrumNumber the number of the spectrum in the file
     *
     * @return the title of the spectrum of interest
     */
    public String getSpectrumTitle(String mgfFile, int spectrumNumber) {
        MgfIndex mgfIndex = mgfIndexesMap.get(mgfFile);
        if (mgfIndex == null) {
            return null;
        }
        return mgfIndex.getSpectrumTitle(spectrumNumber);
    }

    /**
     * Returns the fixed mgf title.
     *
     * @param spectrumTitle
     * @param fileName
     * @return the fixed mgf title
     */
    private String fixMgfTitle(String spectrumTitle, String fileName) {

        // a special fix for mgf files with titles containing url encoding, e.g.: %3b instead of ;
        if (mgfIndexesMap.get(fileName).getIndex(spectrumTitle) == null) {
            try {
                spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8"); // @TODO: only required for mascot??
            } catch (UnsupportedEncodingException e) {
                System.out.println("An exception was thrown when trying to decode an mgf title: " + spectrumTitle);
                e.printStackTrace();
            }
        }

        // a special fix for mgf files with titles containing \\ instead \
        if (mgfIndexesMap.get(fileName).getIndex(spectrumTitle) == null) {
            spectrumTitle = spectrumTitle.replaceAll("\\\\\\\\", "\\\\"); // @TODO: only required for omssa???
        }

        return spectrumTitle;
    }

    /**
     * Adds an id to spectrum name in the mapping.
     *
     * @param idName name according to the id file
     * @param spectrumFile the spectrum file
     */
    public void addIdNameMapping(String idName, File spectrumFile) {
        idToSpectrumName.put(idName, spectrumFile);
    }

    /**
     * Returns the spectrum file corresponding to the name of the file used for
     * identification
     *
     * @param idName the name of the spectrum file according to the
     * identification file
     * @return the spectrum file
     */
    public File getSpectrumFileFromIdName(String idName) {
        return idToSpectrumName.get(idName);
    }
}
