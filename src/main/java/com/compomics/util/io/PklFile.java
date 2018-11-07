package com.compomics.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Contains information about the contents of one PKL file.
 *
 * @author Harald Barsnes
 */
public class PklFile {

    /**
     * Empty default constructor
     */
    public PklFile() {
    }

    /**
     * The precursor charge.
     */
    private int precurorCharge;
    /**
     * The precursor m/z.
     */
    private double precursorMz;
    /**
     * The precursor intensity.
     */
    private double precursorIntensity;
    /**
     * The m/z values.
     */
    private double[] mzValues;
    /**
     * The intensity values.
     */
    private double[] intensityValues;
    /**
     * The file name.
     */
    private String fileName;
    /**
     * The spectrum file ID.
     */
    private String spectrumFileId;

    /**
     * Parse a PKL file and store the details in the PKLFile object.
     *
     * @param pklFile the file to parse
     * @throws IOException if an IOException occurs
     */
    public PklFile(File pklFile) throws IOException {

        if(pklFile.isDirectory()){
            throw new IOException("File is a directory!");
        }

        if(!pklFile.getAbsolutePath().toLowerCase().endsWith(".pkl")){
            throw new IOException("File is not a PKL file!");
        }

        FileReader f = new FileReader(pklFile);
        BufferedReader b = new BufferedReader(f);

        fileName = pklFile.getName();
        spectrumFileId = pklFile.getName().substring(0, pklFile.getName().length() - 4);

        // read precursor details
        String precursorLine = b.readLine();

        String[] precursorDetails = precursorLine.split("\t");

        if(precursorDetails.length != 3){
            throw new IOException("File is not a PKL file - incorrect number of precursor paramaters!");
        }

        precursorMz = new Double(precursorDetails[0]);
        precursorIntensity = new Double(precursorDetails[1]);
        precurorCharge = new Integer(precursorDetails[2]);

        HashMap<Double, Double> peaks = new HashMap<>();

        String peakLine = b.readLine();

        while (peakLine != null) {
            String[] peakDetails = peakLine.split("\t");

            if(peakDetails.length != 2){
                throw new IOException("File is not a PKL file - incorrect number of peak paramaters!");
            }
            
            peaks.put(new Double(peakDetails[0]), new Double(peakDetails[1]));
            peakLine = b.readLine();
        }

        // sort the values in increasing order
        TreeSet treeSet = new TreeSet();
        treeSet.clear();
        treeSet.addAll(peaks.keySet());

        Iterator treeSetIterator = treeSet.iterator();

        Double tempMz;
        mzValues = new double[peaks.size()];
        intensityValues = new double[peaks.size()];

        int peakCounter = 0;

        while (treeSetIterator.hasNext()) {
            tempMz = (Double) treeSetIterator.next();
            mzValues[peakCounter] = tempMz;
            intensityValues[peakCounter++] = peaks.get(tempMz);
        }

        b.close();
        f.close();
    }

    /**
     * @return the precurorCharge
     */
    public int getPrecurorCharge() {
        return precurorCharge;
    }

    /**
     * @param precurorCharge the precurorCharge to set
     */
    public void setPrecurorCharge(int precurorCharge) {
        this.precurorCharge = precurorCharge;
    }

    /**
     * @return the precursorMz
     */
    public double getPrecursorMz() {
        return precursorMz;
    }

    /**
     * @param precursorMz the precursorMz to set
     */
    public void setPrecursorMz(double precursorMz) {
        this.precursorMz = precursorMz;
    }

    /**
     * @return the precursorIntensity
     */
    public double getPrecursorIntensity() {
        return precursorIntensity;
    }

    /**
     * @param precursorIntensity the precursorIntensity to set
     */
    public void setPrecursorIntensity(double precursorIntensity) {
        this.precursorIntensity = precursorIntensity;
    }

    /**
     * @return the mzValues
     */
    public double[] getMzValues() {
        return mzValues;
    }

    /**
     * @param mzValues the mzValues to set
     */
    public void setMzValues(double[] mzValues) {
        this.mzValues = mzValues;
    }

    /**
     * @return the intensityValues
     */
    public double[] getIntensityValues() {
        return intensityValues;
    }

    /**
     * @param intensityValues the intensityValues to set
     */
    public void setIntensityValues(double[] intensityValues) {
        this.intensityValues = intensityValues;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the spectrumFileId
     */
    public String getSpectrumFileId() {
        return spectrumFileId;
    }

    /**
     * @param spectrumFileId the spectrumFileId to set
     */
    public void setSpectrumFileId(String spectrumFileId) {
        this.spectrumFileId = spectrumFileId;
    }
}
