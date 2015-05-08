package com.compomics.util.experiment.io;

import com.compomics.util.experiment.MsExperiment;
import com.compomics.util.io.SerializationUtils;

import java.io.*;

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
     * Method which saves an experiment.
     *
     * @param file The destination file
     * @param experiment The experiment to be saved
     * @throws IOException Exception thrown whenever an error is encountered
     * while writing the file
     */
    public static void save(File file, MsExperiment experiment) throws IOException {
        SerializationUtils.writeObject(experiment, file);
    }

    /**
     * Method which loads an experiment.
     *
     * @param utilitiesFile file to import
     * @return the loaded experiment
     * @throws IOException thrown if a problem occurred while reading the file
     * @throws ClassNotFoundException thrown if a problem occurred while
     * creating the experiment (typically a version issue)
     */
    public static MsExperiment loadExperiment(File utilitiesFile) throws IOException, ClassNotFoundException {
        return (MsExperiment) SerializationUtils.readObject(utilitiesFile);
    }
}
