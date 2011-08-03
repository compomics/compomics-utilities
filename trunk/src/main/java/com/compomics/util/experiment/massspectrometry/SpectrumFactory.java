/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.SelectedIonList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

/**
 * This factory will provide the spectra when needed
 *
 * @author marc
 */
public class SpectrumFactory {

    /**
     * The instance of the factory
     */
    private static SpectrumFactory instance = null;
    /**
     * Map of already loaded spectra
     */
    private HashMap<String, Spectrum> currentSpectrumMap = new HashMap<String, Spectrum>();
    /**
     * Map of already loaded precursors
     */
    private HashMap<String, Precursor> loadedPrecursors = new HashMap<String, Precursor>();
    /**
     * Amount of proteins in cache, one by default.
     */
    private int nCache = 1;
    /**
     * List of the implemented spectrum keys
     */
    private ArrayList<String> loadedSpectra = new ArrayList<String>();

    /**
     * Constructor
     */
    private SpectrumFactory() {
    }

    /**
     * static method returning the instance of the factory
     * @return the instance of the factory
     */
    public static SpectrumFactory getInstance() {
        if (instance == null) {
            instance = new SpectrumFactory();
        }
        return instance;
    }

    /**
     * Static method returning the instance of the factory with a new cache size
     * @param nCache
     * @return 
     */
    public static SpectrumFactory getInstance(int nCache) {
        if (instance == null) {
            instance = new SpectrumFactory();
        }
        instance.setCacheSize(nCache);
        return instance;
    }
    /**
     * Map of the random access files of the loaded mgf files (filename -> random access file)
     */
    private HashMap<String, RandomAccessFile> mgfFilesMap = new HashMap<String, RandomAccessFile>();
    /**
     * Map of the mgf indexes (fileName -> mgf index)
     */
    private HashMap<String, MgfIndex> mgfIndexesMap = new HashMap<String, MgfIndex>();
    /**
     * Map of the mzML unmarshallers (fileName -> unmarshaller)
     */
    private HashMap<String, MzMLUnmarshaller> mzMLUnmarshallers = new HashMap<String, MzMLUnmarshaller>();

    /**
     * Sets the cache size
     * @param nCache the new cache size
     */
    public void setCacheSize(int nCache) {
        this.nCache = nCache;
    }

    /**
     * returns the cache size
     * @return the cache size 
     */
    public int getCacheSize() {
        return nCache;
    }

    /**
     * Add spectra to the factory
     * 
     * @param spectrumFile              The spectrum file, can be mgf or mzML
     * @throws FileNotFoundException    Exception thrown whenever the file was not found
     * @throws IOException              Exception thrown whenever an error occurred while reading the file
     * @throws ClassNotFoundException   Exception thrown whenever an error occurred while deserializing the index .cui file.
     * @throws Exception                Exception thrown whenever the mgf file was not correctly parsed
     */
    public void addSpectra(File spectrumFile) throws FileNotFoundException, IOException, ClassNotFoundException, Exception {
        String fileName = spectrumFile.getName().toLowerCase();
        if (fileName.endsWith(".mgf")) {
            File indexFile = new File(spectrumFile.getParent(), fileName + ".cui");
            MgfIndex mgfIndex;
            if (indexFile.exists()) {
                mgfIndex = getIndex(indexFile);
            } else {
                mgfIndex = MgfReader.getIndexMap(spectrumFile);
                writeIndex(mgfIndex, spectrumFile.getParentFile());
            }
            mgfFilesMap.put(fileName, new RandomAccessFile(spectrumFile, "r"));
            mgfIndexesMap.put(fileName, mgfIndex);
        } else if (fileName.endsWith(".mzml")) {
            MzMLUnmarshaller mzMLUnmarshaller = new MzMLUnmarshaller(spectrumFile);
            mzMLUnmarshallers.put(fileName, mzMLUnmarshaller);
        } else {
            throw new Exception("Spectrum file format not supported.");
        }
    }

    /**
     * Returns the precursor of the desired spectrum
     * @param fileName      the name of the spectrum file
     * @param spectrumTitle the title of the spectrum
     * @return              the corresponding precursor
     * @throws Exception    exception thrown whenever the file was not parsed correctly
     */
    public Precursor getPrecursor(String fileName, String spectrumTitle) throws Exception {
        String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
        if (currentSpectrumMap.containsKey(spectrumKey)) {
            return ((MSnSpectrum) currentSpectrumMap.get(spectrumKey)).getPrecursor();
        }
        Precursor currentPrecursor = loadedPrecursors.get(spectrumKey);
        if (currentPrecursor == null) {
        String name = fileName.toLowerCase();
        if (name.endsWith(".mgf")) {
            currentPrecursor = MgfReader.getPrecursor(mgfFilesMap.get(name), mgfIndexesMap.get(name).getIndex(spectrumTitle), fileName);
        } else if (name.endsWith(".mzml")) {
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
                throw new Exception("MS1 spectrum");
            } else {
                currentPrecursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
            }
        } else {
            throw new Exception("Spectrum file format not supported.");
        }
        loadedPrecursors.put(spectrumKey, currentPrecursor);
        }
        return currentPrecursor;
    }

    /**
     * Returns the spectrum desired
     * 
     * @param fileName      name of the spectrum file
     * @param spectrumTitle title of the spectrum
     * @return  the desired spectrum
     * @throws IOException  exception thrown whenever an error occurred while reading the file
     * @throws Exception    exception thrown whenever an error occurred while parsing the file
     */
    public Spectrum getSpectrum(String fileName, String spectrumTitle) throws IOException, Exception {
        String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
        Spectrum currentSpectrum = currentSpectrumMap.get(spectrumKey);
        if (currentSpectrum == null) {
            String name = fileName.toLowerCase();
            if (name.endsWith(".mgf")) {
                currentSpectrum = MgfReader.getSpectrum(mgfFilesMap.get(name), mgfIndexesMap.get(name).getIndex(spectrumTitle), fileName);
            } else if (name.endsWith(".mzml")) {
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
                HashSet<Peak> peakList = new HashSet<Peak>();
                for (int i = 0; i < mzNumbers.length; i++) {
                    peakList.add(new Peak(mzNumbers[i].doubleValue(), intNumbers[i].doubleValue(), scanTime));
                }
                if (level == 1) {
                    currentSpectrum = new MS1Spectrum(fileName, spectrumTitle, scanTime, peakList);
                } else {
                    Precursor precursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
                    currentSpectrum = new MSnSpectrum(level, precursor, spectrumTitle, peakList, fileName, scanTime);
                }
            } else {
                throw new Exception("Spectrum file format not supported.");
            }
            if (loadedSpectra.size() == nCache) {
                currentSpectrumMap.remove(loadedSpectra.get(0));
                loadedSpectra.remove(0);
            }
            currentSpectrumMap.put(spectrumKey, currentSpectrum);
            loadedSpectra.add(spectrumKey);
        }
        return currentSpectrum;
    }

    /**
     * Writes the given mgf file index in the given directory
     * @param mgfIndex      the mgf file index
     * @param directory     the destination directory
     * @throws IOException  exception thrown whenever an error is encountered while writing the file
     */
    public void writeIndex(MgfIndex mgfIndex, File directory) throws IOException {
        // Serialize the file index as compomics utilities index
        FileOutputStream fos = new FileOutputStream(new File(directory, mgfIndex.getFileName() + ".cui"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(mgfIndex);
        oos.close();
    }

    /**
     * Deserializes the index of an mgf file
     * @param mgfIndex                  the mgf index cuifile
     * @return                          the corresponding mgf index object
     * @throws FileNotFoundException    exception thrown whenever the file was not found
     * @throws IOException              exception thrown whenever an error was encountered while reading the file
     * @throws ClassNotFoundException   exception thrown whenever an error occurred while deserializing the object
     */
    public MgfIndex getIndex(File mgfIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(mgfIndex);
        ObjectInputStream in = new ObjectInputStream(fis);
        MgfIndex index = (MgfIndex) in.readObject();
        in.close();
        return index;
    }

    /**
     * Closes all opened files
     * @throws IOException exception thrown whenever an error occurred while closing the files
     */
    public void closeFiles() throws IOException {
        for (RandomAccessFile randomAccessFile : mgfFilesMap.values()) {
            randomAccessFile.close();
        }
    }
}
