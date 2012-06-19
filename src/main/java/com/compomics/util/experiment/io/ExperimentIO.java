package com.compomics.util.experiment.io;

import com.compomics.util.experiment.MsExperiment;

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
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(experiment);
        oos.close();
        bos.close();
        fos.close();
    }

    /**
     * Method which loads an experiment.
     *
     * @param utilitiesFile File to import
     * @return the loaded experiment
     * @throws IOException Exception thrown if a problem occured while reading
     * the file
     * @throws ClassNotFoundException Exception thrown if a problem occured
     * while creating the experiment (typically a version issue)
     */
    public static MsExperiment loadExperiment(File utilitiesFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(utilitiesFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream in = new ObjectInputStream(bis);
        MsExperiment experiment = (MsExperiment) in.readObject();
        in.close();
        fis.close();
        bis.close();
        return experiment;
    }
}
