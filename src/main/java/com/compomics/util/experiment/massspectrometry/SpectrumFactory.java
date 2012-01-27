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
import java.util.Iterator;
import java.util.List;
import javax.swing.JProgressBar;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.SelectedIonList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

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
    private HashMap<String, Precursor> loadedPrecursors = new HashMap<String, Precursor>();
    /**
     * Amount of proteins in cache, one by default.
     */
    private int nCache = 1;
    /**
     * List of the implemented spectrum keys.
     */
    private ArrayList<String> loadedSpectra = new ArrayList<String>();

    /**
     * Constructor
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
     * Static method returning the instance of the factory with a new cache size.
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
     * Sets the cache size.
     * 
     * @param nCache the new cache size
     */
    public void setCacheSize(int nCache) {
        this.nCache = nCache;
    }

    /**
     * Returns the cache size.
     * 
     * @return the cache size 
     */
    public int getCacheSize() {
        return nCache;
    }

    /**
     * Add spectra to the factory.
     * 
     * @param spectrumFile              The spectrum file, can be mgf or mzML
     * @throws FileNotFoundException    Exception thrown whenever the file was not found
     * @throws IOException              Exception thrown whenever an error occurred while reading the file
     * @throws ClassNotFoundException   Exception thrown whenever an error occurred while deserializing the index .cui file.
     */
    public void addSpectra(File spectrumFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        addSpectra(spectrumFile, null);
    }

    /**
     * Add spectra to the factory.
     * 
     * @param spectrumFile              The spectrum file, can be mgf or mzML
     * @param progressBar               a progress bar showing the progress
     * @throws FileNotFoundException    Exception thrown whenever the file was not found
     * @throws IOException              Exception thrown whenever an error occurred while reading the file
     * @throws IllegalArgumentException Exception thrown if an unknown format was detected.
     */
    public void addSpectra(File spectrumFile, JProgressBar progressBar) throws FileNotFoundException, IOException, IllegalArgumentException {
        
        String fileName = spectrumFile.getName();
        
        if (fileName.endsWith(".mgf")) {
            
            File indexFile = new File(spectrumFile.getParent(), fileName + ".cui");
            MgfIndex mgfIndex;
            
            if (indexFile.exists()) {
                try {
                    mgfIndex = getIndex(indexFile);
                } catch (Exception e) {
                    mgfIndex = MgfReader.getIndexMap(spectrumFile, progressBar);
                    writeIndex(mgfIndex, spectrumFile.getParentFile());
                }
            } else {
                mgfIndex = MgfReader.getIndexMap(spectrumFile, progressBar);
                writeIndex(mgfIndex, spectrumFile.getParentFile());
            }
            
            mgfFilesMap.put(fileName, new RandomAccessFile(spectrumFile, "r"));
            mgfIndexesMap.put(fileName, mgfIndex);
            checkIndexVersion(spectrumFile.getParentFile(), fileName, progressBar);
        } else if (fileName.endsWith(".mzml")) {
            MzMLUnmarshaller mzMLUnmarshaller = new MzMLUnmarshaller(spectrumFile);
            mzMLUnmarshallers.put(fileName, mzMLUnmarshaller);
        } else {
            throw new IllegalArgumentException("Spectrum file format not supported.");
        }
    }

    /**
     * Returns the precursor of the desired spectrum.
     * 
     * @param fileName      the name of the spectrum file
     * @param spectrumTitle the title of the spectrum
     * @return              the corresponding precursor
     * @throws IOException    exception thrown whenever the file was not parsed correctly
     * @throws MzMLUnmarshallerException    exception thrown whenever the file was not parsed correctly
     */
    public Precursor getPrecursor(String fileName, String spectrumTitle) throws IOException, MzMLUnmarshallerException {
        return getPrecursor(Spectrum.getSpectrumKey(fileName, spectrumTitle));
    }

    /**
     * Returns the precursor of the desired spectrum.
     * 
     * @param spectrumKey   the key of the spectrum
     * @return              the corresponding precursor
     * @throws IOException    exception thrown whenever the file was not parsed correctly
     * @throws MzMLUnmarshallerException    exception thrown whenever the file was not parsed correctly
     * @throws IllegalArgumentException    exception thrown whenever the file was not parsed correctly
     */
    public Precursor getPrecursor(String spectrumKey) throws IOException, MzMLUnmarshallerException, IllegalArgumentException {
        return getPrecursor(spectrumKey, true);
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
     * Returns the precursor of the desired spectrum.
     * 
     * @param spectrumKey   the key of the spectrum
     * @param save          boolean indicating whether the loaded precursor should be stored in the factory
     * @return              the corresponding precursor
     * @throws IOException    exception thrown whenever the file was not parsed correctly
     * @throws MzMLUnmarshallerException    exception thrown whenever the file was not parsed correctly
     * @throws IllegalArgumentException    exception thrown whenever the file was not parsed correctly
     */
    public Precursor getPrecursor(String spectrumKey, boolean save) throws IOException, MzMLUnmarshallerException, IllegalArgumentException {
        if (currentSpectrumMap.containsKey(spectrumKey)) {
            return ((MSnSpectrum) currentSpectrumMap.get(spectrumKey)).getPrecursor();
        }
        Precursor currentPrecursor = loadedPrecursors.get(spectrumKey);
        if (currentPrecursor == null) {
            String fileName = Spectrum.getSpectrumFile(spectrumKey);
            String name = fileName;
            String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);
            if (name.endsWith(".mgf")) {

                // a special fix for mgf files with strange titles...
                spectrumTitle = fixMgfTitle(spectrumTitle, name);

                if (mgfIndexesMap.get(name) == null) {
                    throw new IOException("Mgf file not found: \'" + name + "\'!");
                }
                if (mgfIndexesMap.get(name).getIndex(spectrumTitle) == null) {
                    throw new IOException("Spectrum \'" + spectrumTitle + "\' in mgf file \'" + name + "\' not found!");
                }

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
                    throw new IllegalArgumentException("MS1 spectrum");
                } else {
                    //@TODO: update the charge here
//                    currentPrecursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
                }
            } else {
                throw new IllegalArgumentException("Spectrum file format not supported.");
            }
            if (save) {
                loadedPrecursors.put(spectrumKey, currentPrecursor);
            }
        }
        return currentPrecursor;
    }

    /**
     * Returns the desired spectrum.
     * 
     * @param spectrumFile  name of the spectrum file
     * @param spectrumTitle title of the spectrum
     * @return  the desired spectrum
     * @throws IOException  exception thrown whenever an error occurred while reading the file
     * @throws MzMLUnmarshallerException    exception thrown whenever an error occurred while parsing the mzML file
     */
    public Spectrum getSpectrum(String spectrumFile, String spectrumTitle) throws IOException, MzMLUnmarshallerException {
        return getSpectrum(Spectrum.getSpectrumKey(spectrumFile, spectrumTitle));
    }

    /**
     * Returns the desired spectrum.
     * 
     * @param spectrumKey      key of the spectrum 
     * @return the desired spectrum
     * @throws IOException  exception thrown whenever an error occurred while reading the file
     * @throws IllegalArgumentException    exception thrown whenever an error occurred while parsing the file
     * @throws MzMLUnmarshallerException    exception thrown whenever an error occurred while parsing the file
     */
    public Spectrum getSpectrum(String spectrumKey) throws IOException, IllegalArgumentException, MzMLUnmarshallerException {
        Spectrum currentSpectrum = currentSpectrumMap.get(spectrumKey);
        if (currentSpectrum == null) {
            String fileName = Spectrum.getSpectrumFile(spectrumKey);
            String name = fileName;
            String spectrumTitle = Spectrum.getSpectrumTitle(spectrumKey);
            if (name.endsWith(".mgf")) {

                // a special fix for mgf files with strange titles...
                spectrumTitle = fixMgfTitle(spectrumTitle, name);

                if (mgfIndexesMap.get(name) == null) {
                    throw new IOException("Mgf file not found: \'" + name + "\'!");
                }
                if (mgfIndexesMap.get(name).getIndex(spectrumTitle) == null) {
                    throw new IOException("Spectrum \'" + spectrumTitle + "\' in mgf file \'" + name + "\' not found!");
                }

                currentSpectrum = MgfReader.getSpectrum(mgfFilesMap.get(name), mgfIndexesMap.get(name).getIndex(spectrumTitle), fileName);
            } else if (name.endsWith(".mzml")) {

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
                HashSet<Peak> peakList = new HashSet<Peak>();
                for (int i = 0; i < mzNumbers.length; i++) {
                    peakList.add(new Peak(mzNumbers[i].doubleValue(), intNumbers[i].doubleValue(), scanTime));
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
     * Writes the given mgf file index in the given directory.
     * 
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
     * Deserializes the index of an mgf file.
     * 
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
        fis.close();
        in.close();
        return index;
    }

    /**
     * Closes all opened files.
     * 
     * @throws IOException exception thrown whenever an error occurred while closing the files
     */
    public void closeFiles() throws IOException {
        for (RandomAccessFile randomAccessFile : mgfFilesMap.values()) {
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
     * @param mgfFile   the name of the mgf file
     * @return a list of titles from indexed spectra in the given file
     */
    public ArrayList<String> getSpectrumTitles(String mgfFile) {
        return new ArrayList<String>(mgfIndexesMap.get(mgfFile).getIndexes().keySet());
    }

    /**
     * Returns the fixed mgf title.
     * 
     * @param spectrumTitle
     * @param fileName
     * @return the fixed mgf title
     */
    private String fixMgfTitle(String spectrumTitle, String fileName) {

        // a special fix for mgf files with titles containing %3b instead if ;
        if (mgfIndexesMap.get(fileName).getIndex(spectrumTitle) == null) {
            spectrumTitle = spectrumTitle.replaceAll("%3b", ";");
        }

        // a special fix for mgf files with titles containing \\ instead \
        if (mgfIndexesMap.get(fileName).getIndex(spectrumTitle) == null) {
            spectrumTitle = spectrumTitle.replaceAll("\\\\\\\\", "\\\\");
        }

        return spectrumTitle;
    }

    /**
     * Checks and updates the MgfIndex if this one is from an older version.
     * 
     * @param directory the directory where to write the new index in case it has been changed
     * @param mgfIndex the MgfIndex to check
     * @throws IOException Exception thrown whenever an error occurred while reading the mgf file or writing the index. If a reading error happens at this point we are in trouble...
     */
    private void checkIndexVersion(File directory, String fileName, JProgressBar progressBar) throws IOException {

        MgfIndex mgfIndex = mgfIndexesMap.get(fileName);
        
        if (mgfIndex.getMaxRT() == null || mgfIndex.getMinRT() == null || mgfIndex.getMaxMz() == null) {
            
            double rt, maxRT = -1, minRT = Double.MAX_VALUE, maxMz = -1;
            Precursor precursor;
            
            int counter = 0;
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setMaximum(getSpectrumTitles(fileName).size());
            progressBar.setValue(0);
            
            for (String spectrumTitle : getSpectrumTitles(fileName)) {
                
                progressBar.setValue(counter++);
                String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
                try {
                precursor = getPrecursor(spectrumKey, false);
                    rt = precursor.getRt();
                    if (rt > maxRT) {
                        maxRT = rt;
                    }
                    if (rt < minRT) {
                        minRT = rt;
                    }
                    if (precursor.getMz() > maxMz) {
                        maxMz = precursor.getMz();
                    }
                } catch (MzMLUnmarshallerException e) {
                    // Should not happen when working with mgf files
                }
            }
            
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(false);
            
            mgfIndex.setMaxRT(maxRT);
            mgfIndex.setMinRT(minRT);
            mgfIndex.setMaxMz(maxMz);
            writeIndex(mgfIndex, directory);
        }
    }
}
