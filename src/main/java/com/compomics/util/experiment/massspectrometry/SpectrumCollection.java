package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.identification.identifications.Ms2Identification;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.io.File;
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
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This class represents a collection of spectra acquired during a proteomicAnalysis
 *
 * @author Marc
 */
public class SpectrumCollection extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -7894588835760914088L;
    /**
     * static index for an mgf collection
     */
    public static final int MGF = 0;
    /**
     * static index for an mzML collection
     */
    public static final int MZML = 1;
    /**
     * Map of all spectra indexed by the acquisition level and the spectrum index
     */
    private HashMap<Integer, HashMap<String, Spectrum>> spectrumMap = new HashMap<Integer, HashMap<String, Spectrum>>();
    /**
     * The source type as indexed by the static fields
     */
    private int sourceType;
    /**
     * unmarshaller for an mzML file
     */
    private HashMap<String, MzMLUnmarshaller> mzMLUnmarshallerMap = new HashMap<String, MzMLUnmarshaller>();

    /**
     * constructor for an empty collection (The import source must be specified afterwards!)
     */
    public SpectrumCollection() {
    }

    /**
     * Constructor
     */
    public SpectrumCollection(int sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Adds a spectrum to the collection
     * @param spectrum the selected spectrum
     */
    public void addSpectrum(MSnSpectrum spectrum) {
        int level = spectrum.getLevel();
        if (!spectrumMap.containsKey(level)) {
            spectrumMap.put(level, new HashMap<String, Spectrum>());
        }
        spectrumMap.get(level).put(spectrum.getSpectrumKey(), spectrum);
    }

    /**
     * Getter for a spectrum
     * @param level         Level at which the spectrum was recorded (2 for MS2 spectra)
     * @param spectrumKey   Key of the spectrum
     * @return  the desired spectrum
     * @throws MzMLUnmarshallerException Exception thrown by the mzML unmarshaller
     */
    public Spectrum getSpectrum(int level, String spectrumKey) throws MzMLUnmarshallerException {
        if (sourceType == MGF) {
            if (spectrumMap.containsKey(level)) {
                return spectrumMap.get(level).get(spectrumKey);
            }
        } else if (sourceType == MZML) {
            return getSpectrum(spectrumKey);
        }
        return null;
    }

    /**
     * Getter for a spectrum
     * @param spectrumKey   Key of the spectrum
     * @return  the desired spectrum
     * @throws MzMLUnmarshallerException Exception thrown by the mzML unmarshaller
     */
    public Spectrum getSpectrum(String spectrumKey) throws MzMLUnmarshallerException {
        if (sourceType == MGF) {
            for (int level : spectrumMap.keySet()) {
                if (spectrumMap.get(level).keySet().contains(spectrumKey)) {
                    return spectrumMap.get(level).get(spectrumKey);
                }
            }
        } else if (sourceType == MZML) {
            String title = Spectrum.getSpectrumTitle(spectrumKey);
            String file = Spectrum.getSpectrumFile(spectrumKey);
            uk.ac.ebi.jmzml.model.mzml.Spectrum mzMLSpectrum = mzMLUnmarshallerMap.get(file).getSpectrumById(title);
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
                return new MS1Spectrum(file, title, scanTime, peakList);
            } else {
                Precursor precursor = new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
                return new MSnSpectrum(level, precursor, title, peakList, file, scanTime);
            }
        }
        return null;
    }

    /**
     * Returns all keys of the spectra loaded at the specified level
     * @param level the specified level
     * @return all keys of the spectra loaded
     */
    public ArrayList<String> getAllKeys(int level) {
        return new ArrayList<String>(spectrumMap.get(level).keySet());
    }

    /**
     * Returns the keys of all loaded spectra
     * @return the keys of all loaded spectra
     */
    public ArrayList<String> getAllKeys() {
        if (sourceType == MGF) {
            ArrayList<String> result = new ArrayList<String>();
            for (int level : spectrumMap.keySet()) {
                result.addAll(spectrumMap.get(level).keySet());
            }
            return result;
        } else if (sourceType == MZML) {
            ArrayList<String> result = new ArrayList<String>();
            for (String file : mzMLUnmarshallerMap.keySet()) {
                for (String key : mzMLUnmarshallerMap.get(file).getSpectrumIDs()) {
                    result.add(Spectrum.getSpectrumKey(file, key));
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Returns the source type as indexed by the static fields
     * @return the source type as indexed by the static fields
     */
    public int getSourceType() {
        return sourceType;
    }

    /**
     * Sets the source type as defined by the static fields
     * @param sourceType the source type
     */
    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Adds all spectra from a file
     * @param file          The file to load spectra from
     * @throws Exception    Exception thrown whenever an error is encountered while loading the file
     */
    public void addSpectra(File file) throws Exception {
        if (file.getName().toLowerCase().endsWith(".mgf")) {
            this.sourceType = MGF;
            MgfReader mgfReader = new MgfReader();
            ArrayList<MSnSpectrum> spectrumList = mgfReader.getSpectra(file);
            for (MSnSpectrum spectrum : spectrumList) {
                addSpectrum(spectrum);
            }
        } else {
            sourceType = MZML;
            MzMLUnmarshaller mzMLUnmarshaller = new MzMLUnmarshaller(file);
            mzMLUnmarshallerMap.put(file.getName(), mzMLUnmarshaller);
        }
    }

    /**
     * Adds all spectra identified in the specified identification
     * @param file              The file containing all identifications
     * @param identification    The identification of the proteomic analysis
     * @throws Exception        Exception thrown whenever an error is encountered why loading the file
     */
    public void addIdentifiedSpectra(File file, Ms2Identification identification) throws Exception {
        ArrayList<String> identifiedSpectra = new ArrayList<String>(identification.getSpectrumIdentification().keySet());
        if (file.getName().toLowerCase().endsWith(".mgf")) {
            this.sourceType = MGF;
            MgfReader mgfReader = new MgfReader();
            ArrayList<MSnSpectrum> spectrumList = mgfReader.getSpectra(file);
            for (MSnSpectrum spectrum : spectrumList) {
                if (identifiedSpectra.contains(spectrum.getSpectrumKey())) {
                    addSpectrum(spectrum);
                }
            }
        } else {
            addSpectra(file);
        }
    }

    /**
     * Returns wheter a spectrum is contained in the collection
     * @param spectrumKey   The spectrum Key
     * @return  a boolean indicating whether the spectrum is contained in the collection
     */
    public boolean contains(String spectrumKey) {
        if (sourceType == MGF) {
            for (int level : spectrumMap.keySet()) {
                if (spectrumMap.get(level).keySet().contains(spectrumKey)) {
                    return true;
                }
            }
        } else if (sourceType == MZML) {
            for (String file : mzMLUnmarshallerMap.keySet()) {
                if (mzMLUnmarshallerMap.get(file).getSpectrumIDs().contains(Spectrum.getSpectrumTitle(spectrumKey))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes all peaklists from the loaded spectra in order to save memory (useless with mzML data)
     */
    public void removePeaks() {
        if (sourceType == MGF) {
            for (int level : spectrumMap.keySet()) {
                for (Spectrum spectrum : spectrumMap.get(level).values()) {
                    spectrum.removePeakList();
                }
            }
        }
    }
}
