package com.compomics.util.experiment.io;

import com.compomics.util.experiment.MsExperiment;
import com.compomics.util.experiment.biology.Sample;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumCollection;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This class takes care of the saving and opening of Compomics utilities
 * experiment objects via serialization.
 *
 * @author Marc Vaudel
 */
public class ExperimentIO {

    /**
     * Constructor
     */
    public ExperimentIO() {
    }

    /**
     * Method which saves an experiment
     *
     * @param file          The destination file
     * @param experiment    The experiment to be saved
     * @throws IOException  Exception thrown whenever an error is encountered while writing the file
     */
    public void save(File file, MsExperiment experiment) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(experiment);
        oos.close();
    }

    /**
     * Method which saves an experiment identifications (spectra are removed)
     *
     * @param file          The destination file
     * @param experiment    The experiment to be saved
     * @throws IOException  Exception thrown whenever an error is encountered while writing the file
     */
    public void saveIdentifications(File file, MsExperiment experiment) throws IOException, MzMLUnmarshallerException {

        // peak lists are not serialized to save time and space
        HashMap<String, HashSet<Peak>> backUp = new HashMap<String, HashSet<Peak>>();
        SpectrumCollection spectrumCollection;
        HashSet<Peak> peaks;
        for (Sample sample : experiment.getSamples().values()) {
            for (int replicateNumber : experiment.getAnalysisSet(sample).getReplicateNumberList()) {
                spectrumCollection = experiment.getAnalysisSet(sample).getProteomicAnalysis(replicateNumber).getSpectrumCollection();
                if (spectrumCollection.getSourceType() == SpectrumCollection.MGF) {
                    for (String spectrumKey : spectrumCollection.getAllKeys()) {
                        peaks = new HashSet<Peak>();
                        for (Peak peak : spectrumCollection.getSpectrum(spectrumKey).getPeakList()) {
                            peaks.add(peak);
                        }
                        backUp.put(spectrumKey, peaks);
                        spectrumCollection.getSpectrum(spectrumKey).removePeakList();
                    }
                }
            }
        }

        // Serialize the experiment object
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(experiment);
        oos.close();

        // Put the peak lists back
        for (Sample sample : experiment.getSamples().values()) {
            for (int replicateNumber : experiment.getAnalysisSet(sample).getReplicateNumberList()) {
                spectrumCollection = experiment.getAnalysisSet(sample).getProteomicAnalysis(replicateNumber).getSpectrumCollection();
                if (spectrumCollection.getSourceType() == SpectrumCollection.MGF) {
                    for (String spectrumKey : spectrumCollection.getAllKeys()) {
                        spectrumCollection.getSpectrum(spectrumKey).setPeakList(backUp.get(spectrumKey));
                    }
                }
            }
        }
    }

    /**
     * Method which loads an experiment
     *
     * @param utilitiesFile             File to import
     * @return                          the loaded experiment
     * @throws IOException              Exception thrown if a problem occured while reading the file
     * @throws ClassNotFoundException   Exception thrown if a problem occured while creating the experiment (typically a version issue)
     */
    public MsExperiment loadExperiment(File utilitiesFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(utilitiesFile);
        ObjectInputStream in = new ObjectInputStream(fis);
        MsExperiment experiment = (MsExperiment) in.readObject();
        in.close();
        return experiment;
    }
}
