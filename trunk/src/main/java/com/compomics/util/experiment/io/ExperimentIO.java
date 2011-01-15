package com.compomics.util.experiment.io;

import com.compomics.util.experiment.MsExperiment;

import java.io.*;
import java.util.Date;

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

        oos.writeObject(new Date());
        oos.writeObject(experiment);

        oos.close();
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
        Date date = (Date) in.readObject();
        MsExperiment experiment = (MsExperiment) in.readObject();
        in.close();
        return experiment;
    }

    /**
     * Method which returns the date of serialization of the experiment in the file
     * 
     * @param utilitiesFile             File to import
     * @return                          the date of serialization of the experiment in the file
     * @throws IOException              Exception thrown if a problem occured while reading the file
     * @throws ClassNotFoundException   Exception thrown if a problem occured while creating the experiment (typically a version issue)
     */
    public Date getDate(File utilitiesFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(utilitiesFile);
        ObjectInputStream in = new ObjectInputStream(fis);
        Date date = (Date) in.readObject();
        in.close();
        return date;
    }
}
