package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.quantification.Quantification;
import com.compomics.util.experiment.quantification.Quantification.QuantificationMethod;

import java.util.ArrayList;

/**
 * This class models a reporter quantification method.
 * User: Marc
 * Date: Sep 29, 2010
 * Time: 5:52:30 PM
 */
public class ReporterMethod {

    /**
     * the reporter ions of the method
     */
    private ArrayList<ReporterIon> reporterIons;
    /**
     * The correction factors corresponding to the ions
     */
    private ArrayList<CorrectionFactor> correctionFactors;

    /**
     * Constructor for a reporter method
     * @param index                 the index of the method
     * @param name                  the name of the method
     * @param reporterIons          the reporter ions used
     * @param correctionFactors     the correction factors corresponding to the reporter ions
     */
    public ReporterMethod(String name, ArrayList<ReporterIon> reporterIons, ArrayList<CorrectionFactor> correctionFactors) {
        this.reporterIons = reporterIons;
        this.correctionFactors = correctionFactors;
    }

    /**
     * Returns the reporter ions used
     * @return the reporter ions used
     */
    public ArrayList<ReporterIon> getReporterIons() {
        return reporterIons;
    }

    /**
     * Returns the correction factors corresponding to the reporter ions
     * @return the correction factors corresponding to the reporter ions
     */
    public ArrayList<CorrectionFactor> getCorrectionFactors() {
        return correctionFactors;
    }
}
