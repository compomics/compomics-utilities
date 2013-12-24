package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ReporterIon;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains information relative to a reporter quantification method.
 *
 * @author Marc Vaudel Date: Sep 29, 2010 Time: 5:52:30 PM
 */
public class ReporterMethod {

    /**
     * The reporter ions of the method.
     */
    private HashMap<Integer, ReporterIon> reporterIons = new HashMap<Integer, ReporterIon>();
    /**
     * The correction factors corresponding to the ions.
     */
    private ArrayList<CorrectionFactor> correctionFactors;
    /**
     * The name of the method.
     */
    private String name;

    /**
     * Constructor for a reporter method.
     *
     * @param name the name of the method
     * @param reporterIons the reporter ions used
     * @param correctionFactors the correction factors corresponding to the
     * reporter ions
     */
    public ReporterMethod(String name, ArrayList<ReporterIon> reporterIons, ArrayList<CorrectionFactor> correctionFactors) {
        this.correctionFactors = correctionFactors;
        this.name = name;
        for (ReporterIon reporterIon : reporterIons) {
            this.reporterIons.put(reporterIon.getIndex(), reporterIon);
        }
    }
    
    /**
     * Returns a list containing the indexes of the reporter ions.
     * 
     * @return a list containing the indexes of the reporter ions
     */
    public ArrayList<Integer> getReporterIonIndexes() {
        return new ArrayList<Integer>(reporterIons.keySet());
    }
    
    /**
     * Returns the reporter ion indexed by the given index, null if not found.
     * 
     * @param reporterIonIndex the index of the reporter ion
     * 
     * @return the reporter ion of interest
     */
    public ReporterIon getReporterIon(int reporterIonIndex) {
        return reporterIons.get(reporterIonIndex);
    }

    /**
     * Returns the correction factors corresponding to the reporter ions.
     *
     * @return the correction factors corresponding to the reporter ions
     */
    public ArrayList<CorrectionFactor> getCorrectionFactors() {
        return correctionFactors;
    }

    /**
     * Returns the name of the method.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }
}
