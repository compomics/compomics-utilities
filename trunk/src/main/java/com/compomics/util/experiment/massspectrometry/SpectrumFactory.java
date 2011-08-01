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

    private static SpectrumFactory instance = null;
    private Spectrum currentSpectrum = null;

    private SpectrumFactory() {
    }

    public static SpectrumFactory getInstance() {
        if (instance == null) {
            instance = new SpectrumFactory();
        }
        return instance;
    }
    private HashMap<String, RandomAccessFile> mgfFilesMap = new HashMap<String, RandomAccessFile>();
    private HashMap<String, MgfIndex> mgfIndexesMap = new HashMap<String, MgfIndex>();
    private HashMap<String, MzMLUnmarshaller> mzMLUnmarshallers = new HashMap<String, MzMLUnmarshaller>();

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

    public Precursor getPrecursor(String fileName, String spectrumTitle) throws Exception {
        if (currentSpectrum != null 
                && Spectrum.getSpectrumKey(fileName, spectrumTitle).equals(currentSpectrum.getSpectrumKey())) {
            return ((MSnSpectrum) currentSpectrum).getPrecursor();
        }
        String name = fileName.toLowerCase();
        if (name.endsWith(".mgf")) {
            return MgfReader.getPrecursor(mgfFilesMap.get(name), mgfIndexesMap.get(name).getIndex(spectrumTitle), fileName);
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
                return null;
            } else {
                return new Precursor(scanTime, mzPrec, new Charge(Charge.PLUS, chargePrec));
            }
        } else {
            throw new Exception("Spectrum file format not supported.");
        }
    }

    public Spectrum getSpectrum(String fileName, String spectrumTitle) throws IOException, Exception {
        if (currentSpectrum == null
                || !Spectrum.getSpectrumKey(fileName, spectrumTitle).equals(currentSpectrum.getSpectrumKey())) {

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
        }
        return currentSpectrum;
    }

    public void writeIndex(MgfIndex mgfIndex, File directory) throws IOException {
        // Serialize the file index as compomics utilities index
        FileOutputStream fos = new FileOutputStream(new File(directory, mgfIndex.getFileName() + ".cui"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(mgfIndex);
        oos.close();
    }

    public MgfIndex getIndex(File mgfIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(mgfIndex);
        ObjectInputStream in = new ObjectInputStream(fis);
        MgfIndex index = (MgfIndex) in.readObject();
        in.close();
        return index;
    }

    public void closeFiles() throws IOException {
        for (RandomAccessFile randomAccessFile : mgfFilesMap.values()) {
            randomAccessFile.close();
        }
    }
}
