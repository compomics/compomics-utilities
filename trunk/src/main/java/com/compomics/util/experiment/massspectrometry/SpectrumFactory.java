package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.io.SerializationUtils;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private HashMap<String, Spectrum> currentSpectrumMap = new HashMap<String, Spectrum>();
    /**
     * Map of already loaded precursors.
     */
    private HashMap<String, Precursor> loadedPrecursorsMap = new HashMap<String, Precursor>();
    /**
     * Amount of spectra in cache, one by default.
     */
    private static int nSpectraCache = 1;
    /**
     * Amount of precursors in cache.
     */
    private static int nPrecursorsCache = 10000;
    /**
     * List of the loaded spectra.
     */
    private ArrayList<String> loadedSpectra = new ArrayList<String>();
    /**
     * List of the loaded precursors.
     */
    private ArrayList<String> loadedPrecursors = new ArrayList<String>();
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
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mgfIndex == null) {
                mgfIndex = MgfReader.getIndexMap(spectrumFile, waitingHandler);
                try {
                    writeIndex(mgfIndex, spectrumFile.getParentFile());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
     * Returns the precursor of the desired spectrum.
     *
     * @param fileName the name of the spectrum file
     * @param spectrumTitle the title of the spectrum
     * @return the corresponding precursor
     * @throws IOException exception thrown whenever the file was not parsed
     * correctly
     * @throws MzMLUnmarshallerException exception thrown whenever the file was
     * not parsed correctly
     */
    public Precursor getPrecursor(String fileName, String spectrumTitle) throws IOException, MzMLUnmarshallerException {
        return getPrecursor(Spectrum.getSpectrumKey(fileName, spectrumTitle));
    }

    /**
     * Returns the precursor of the desired spectrum.
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
        return getPrecursor(spectrumKey, false);
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

        Iterator<String> keys = mgfIndexesMap.keySet().iterator();

        while (keys.hasNext()) {

            String tempFileName = mgfIndexesMap.get(keys.next()).getFileName();

            if (getMaxMz(tempFileName) > maxMz) {
                maxMz = getMaxMz(tempFileName);
            }
        }

        return maxMz;
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

        Iterator<String> keys = mgfIndexesMap.keySet().iterator();

        while (keys.hasNext()) {

            String tempFileName = mgfIndexesMap.get(keys.next()).getFileName();

            if (getMaxIntensity(tempFileName) > maxIntensity) {
                maxIntensity = getMaxIntensity(tempFileName);
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

        Iterator<String> keys = mgfIndexesMap.keySet().iterator();

        while (keys.hasNext()) {

            String tempFileName = mgfIndexesMap.get(keys.next()).getFileName();

            if (getMaxRT(tempFileName) > maxRT) {
                maxRT = getMaxRT(tempFileName);
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

        Iterator<String> keys = mgfIndexesMap.keySet().iterator();

        while (keys.hasNext()) {

            String tempFileName = mgfIndexesMap.get(keys.next()).getFileName();

            if (getMinRT(tempFileName) < minRT) {
                minRT = getMinRT(tempFileName);
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
        if (currentSpectrumMap.containsKey(spectrumKey)) {
            return ((MSnSpectrum) currentSpectrumMap.get(spectrumKey)).getPrecursor();
        }
        Precursor currentPrecursor = loadedPrecursorsMap.get(spectrumKey);
        if (currentPrecursor != null) {
            return currentPrecursor;
        }
        return getPrecursor(spectrumKey, save, 0);
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
    private synchronized Precursor getPrecursor(String spectrumKey, boolean save, int errorCounter) throws IOException, MzMLUnmarshallerException, IllegalArgumentException {

        Precursor currentPrecursor = null;
        String fileName = Spectrum.getSpectrumFile(spectrumKey);
        String name = fileName;
        String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);

        if (name.toLowerCase().endsWith(".mgf")) {

            // a special fix for mgf files with strange titles...
            spectrumTitle = fixMgfTitle(spectrumTitle, name);

            if (mgfIndexesMap.get(name) == null) {
                throw new IOException("Mgf file not found: \'" + name + "\'.");
            }
            if (mgfIndexesMap.get(name).getIndex(spectrumTitle) == null) {
                throw new IOException("Spectrum \'" + spectrumTitle + "\' in mgf file \'" + name + "\' not found.");
            }
            try {
                currentPrecursor = MgfReader.getPrecursor(mgfFilesMap.get(name), mgfIndexesMap.get(name).getIndex(spectrumTitle), fileName);
            } catch (Exception e) {
                if (errorCounter <= 100) {
                    try {
                        wait(50);
                    } catch (InterruptedException ie) {
                    }
                    return getPrecursor(spectrumKey, save, errorCounter + 1);
                } else {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error while loading precursor of spectrum " + spectrumKey);
                }
            }
        } else if (name.toLowerCase().endsWith(".mzml")) {
            uk.ac.ebi.jmzml.model.mzml.Spectrum mzMLSpectrum = mzMLUnmarshallers.get(name).getSpectrumById(spectrumTitle);
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
            loadedPrecursorsMap.put(spectrumKey, currentPrecursor);
            loadedPrecursors.add(spectrumKey);
            while (loadedPrecursors.size() > nPrecursorsCache) {
                loadedPrecursorsMap.remove(loadedPrecursors.get(0));
                loadedPrecursors.remove(0);
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
        return getSpectrum(Spectrum.getSpectrumKey(spectrumFile, spectrumTitle));
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
        Spectrum currentSpectrum = currentSpectrumMap.get(spectrumKey);
        if (currentSpectrum != null) {
            return currentSpectrum;
        }
        return getSpectrum(spectrumKey, 0);
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
    private synchronized Spectrum getSpectrum(String spectrumKey, int errorCounter) throws IOException, IllegalArgumentException, MzMLUnmarshallerException {

        Spectrum currentSpectrum = null;
        String fileName = Spectrum.getSpectrumFile(spectrumKey);
        String name = fileName;
        String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);

        if (name.toLowerCase().endsWith(".mgf")) {

            // a special fix for mgf files with strange titles...
            spectrumTitle = fixMgfTitle(spectrumTitle, name);

            if (mgfIndexesMap.get(name) == null) {
                throw new FileNotFoundException("Mgf file not found: \'" + name + "\'!");
            }
            if (mgfIndexesMap.get(name).getIndex(spectrumTitle) == null) {
                throw new IOException("Spectrum \'" + spectrumTitle + "\' in mgf file \'" + name + "\' not found!");
            }
            try {
                currentSpectrum = MgfReader.getSpectrum(mgfFilesMap.get(name), mgfIndexesMap.get(name).getIndex(spectrumTitle), fileName);
            } catch (Exception e) {
                if (errorCounter <= 100) {
                    try {
                        wait(50);
                    } catch (InterruptedException ie) {
                    }
                    return getSpectrum(spectrumKey, errorCounter + 1);
                } else {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error while loading spectrum " + spectrumKey);
                }
            }
        } else if (name.toLowerCase().endsWith(".mzml")) {

            if (mzMLUnmarshallers.get(name) == null) {
                throw new IOException("mzML file not found: \'" + name + "\'!");
            }
            if (mzMLUnmarshallers.get(name).getSpectrumById(spectrumTitle) == null) {
                throw new IOException("Spectrum \'" + spectrumTitle + "\' in mzML file \'" + name + "\' not found!");
            }

            uk.ac.ebi.jmzml.model.mzml.Spectrum mzMLSpectrum = mzMLUnmarshallers.get(name).getSpectrumById(spectrumTitle);
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
                currentSpectrum = new MS1Spectrum(fileName, spectrumTitle, scanTime, peakList);
            } else {
                //@TODO: update the charge here
                //Precursor precursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
                //currentSpectrum = new MSnSpectrum(level, precursor, spectrumTitle, peakList, fileName, scanTime);
            }
        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
        if (loadedSpectra.size() == nSpectraCache) {
            currentSpectrumMap.remove(loadedSpectra.get(0));
            loadedSpectra.remove(0);
        }
        currentSpectrumMap.put(spectrumKey, currentSpectrum);
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
