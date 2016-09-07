package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.preferences.IdentificationParameters;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.util.FastMath;

/**
 * This map stores the precursors indexed by mass. Masses and matching are done
 * according to the identification parameters provided.
 *
 * @author Marc Vaudel
 */
public class PrecursorMap {

    /**
     * The precursor mass tolerance.
     */
    double precursorTolerance;
    /**
     * Boolean indicating wheter the precursor mass tolerance is in ppm.
     */
    boolean ppm;
    /**
     * The minimal charge to consider.
     */
    int minCharge;
    /**
     * The maximal charge to consider.
     */
    int maxCharge;
    /**
     * Map of the precursors by bin and mass.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<PrecursorMatch>>> precursorsMap = new HashMap<Integer, HashMap<Double, ArrayList<PrecursorMatch>>>();
    /**
     * A mass anchor to determine the bins in ppm
     */
    private double massAnchor = 1000;
    /**
     * The log of the mass anchor.
     */
    private double massAnchorLog;
    /**
     * The scaling factor used for the bins in ppm
     */
    private double scalingFactor;
    /**
     * The minimal mass found.
     */
    private Double minMass = null;
    /**
     * The maximal mass found.
     */
    private Double maxMass = null;

    /**
     * Builds a precursor map.
     * 
     * @param precursors map of the precursors indexed by spectrum title
     * @param precursorTolerance the precursor mass tolerance to use
     * @param ppm boolean indicating whether the tolerance is in ppm
     * @param minCharge the minimal charge to consider
     * @param maxCharge the maximal charge to consider
     */
    public PrecursorMap(HashMap<String, Precursor> precursors, double precursorTolerance, boolean ppm, int minCharge, int maxCharge) {
        this.precursorTolerance = precursorTolerance;
        this.ppm = ppm;
        this.minCharge = minCharge;
        this.maxCharge = maxCharge;
        if (ppm) {
            massAnchorLog = FastMath.log(massAnchor);
            scalingFactor = FastMath.log((1000000 - precursorTolerance) / (1000000 + precursorTolerance));
        }
        for (String spectrumTitle : precursors.keySet()) {
            Precursor precursor = precursors.get(spectrumTitle);
            for (int chargeValue = minCharge; chargeValue <= maxCharge; chargeValue++) {
                PrecursorMatch spectrumTitleWithCharge = new PrecursorMatch(precursor, spectrumTitle, chargeValue);
                double mass = precursor.getMz() * chargeValue - chargeValue * ElementaryIon.proton.getTheoreticMass();
                if (minMass == null || mass < minMass) {
                    minMass = mass;
                }
                if (maxMass == null || mass > maxMass) {
                    maxMass = mass;
                }
                Integer bin = getBin(mass);
                HashMap<Double, ArrayList<PrecursorMatch>> precursorsInBin = precursorsMap.get(bin);
                if (precursorsInBin == null) {
                    precursorsInBin = new HashMap<Double, ArrayList<PrecursorMatch>>();
                    precursorsMap.put(bin, precursorsInBin);
                }
                ArrayList<PrecursorMatch> precursorsAtMass = precursorsInBin.get(precursor.getMz());
                if (precursorsAtMass == null) {
                    precursorsAtMass = new ArrayList<PrecursorMatch>(1);
                    precursorsInBin.put(mass, precursorsAtMass);
                }
                precursorsAtMass.add(spectrumTitleWithCharge);
            }
        }
    }

    /**
     * Returns the bin corresponding to the given m/z and charge.
     * 
     * @param mz the m/z
     * @param chargeValue the charge
     * 
     * @return the bin
     */
    private Integer getBin(double mz, int chargeValue) {
        if (ppm) {
            return getBinPpm(mz, chargeValue);
        } else {
            return getBinAbsolute(mz, chargeValue);
        }
    }

    /**
     * Returns the bin corresponding to the given m/z and charge with absolute tolerance in Da.
     * 
     * @param mz the m/z
     * @param chargeValue the charge
     * 
     * @return the bin
     */
    private Integer getBinAbsolute(double mz, int chargeValue) {
        double mass = mz * chargeValue - chargeValue * ElementaryIon.proton.getTheoreticMass();
        return getBinAbsolute(mass);
    }

    /**
     * Returns the bin corresponding to the given m/z and charge with relative tolerance in ppm.
     * 
     * @param mz the m/z
     * @param chargeValue the charge
     * 
     * @return the bin
     */
    private Integer getBinPpm(double mz, int chargeValue) {
        double mass = mz * chargeValue - chargeValue * ElementaryIon.proton.getTheoreticMass();
        return getBinPpm(mass);
    }

    /**
     * Returns the bin corresponding to the given mass.
     * 
     * @param mz the m/z
     * @param chargeValue the charge
     * 
     * @return the bin
     */
    private Integer getBin(double mass) {
        if (ppm) {
            return getBinPpm(mass);
        } else {
            return getBinAbsolute(mass);
        }
    }

    /**
     * Returns the bin corresponding to the given mass with absolute tolerance in Da.
     * 
     * @param mz the m/z
     * @param chargeValue the charge
     * 
     * @return the bin
     */
    private Integer getBinAbsolute(double mass) {
        Integer bin = (int) (mass / precursorTolerance);
        return bin;
    }

    /**
     * Returns the bin corresponding to the given mass with relative tolerance in ppm.
     * 
     * @param mz the m/z
     * @param chargeValue the charge
     * 
     * @return the bin
     */
    private Integer getBinPpm(double mass) {
        int bin = (int) ((FastMath.log(mass) - massAnchorLog) / scalingFactor);
        return bin;
    }

    /**
     * Returns a list containing the precursors matching the given mass.
     * 
     * @param referenceMass a mass to query
     * 
     * @return a list containing the precursors matching the given mass
     */
    public ArrayList<PrecursorMatch> getMatchingSpectra(double referenceMass) {
        int bin0;
        if (ppm) {
            bin0 = getBinPpm(referenceMass);
        } else {
            bin0 = getBinAbsolute(referenceMass);
        }
        ArrayList<PrecursorMatch> result = new ArrayList<PrecursorMatch>(0);
        HashMap<Double, ArrayList<PrecursorMatch>> binContent = precursorsMap.get(bin0 - 1);
        if (binContent != null) {
            for (Double precursorMass : binContent.keySet()) {
                double error;
                if (ppm) {
                    error = 1000000 * (precursorMass - referenceMass) / referenceMass;
                } else {
                    error = precursorMass - referenceMass;
                }
                if (Math.abs(error) <= precursorTolerance) {
                    result.addAll(binContent.get(precursorMass));
                }
            }
        }
        binContent = precursorsMap.get(bin0);
        if (binContent != null) {
            for (Double precursorMass : binContent.keySet()) {
                double error;
                if (ppm) {
                    error = 1000000 * (precursorMass - referenceMass) / referenceMass;
                } else {
                    error = precursorMass - referenceMass;
                }
                if (Math.abs(error) <= precursorTolerance) {
                    result.addAll(binContent.get(precursorMass));
                }
            }
        }
        binContent = precursorsMap.get(bin0 + 1);
        if (binContent != null) {
            for (Double precursorMass : binContent.keySet()) {
                double error;
                if (ppm) {
                    error = 1000000 * (precursorMass - referenceMass) / referenceMass;
                } else {
                    error = precursorMass - referenceMass;
                }
                if (Math.abs(error) <= precursorTolerance) {
                    result.addAll(binContent.get(precursorMass));
                }
            }
        }
        return result;
    }

    /**
     * Returns the bins in the map.
     * 
     * @return the bins in the map
     */
    public ArrayList<Integer> getBins() {
        return new ArrayList<Integer>(precursorsMap.keySet());
    }
    
    /**
     * Returns the precursors at the given bin indexed by mass. Null if none found.
     * 
     * @param bin the bin number
     * 
     * @return the precursors at the given bin
     */
    public HashMap<Double, ArrayList<PrecursorMatch>> getPrecursorsInBin(int bin) {
        return precursorsMap.get(bin);
    }

    /**
     * Returns the mass associated with the given bin, the middle of the bin.
     * 
     * @param bin the bin number
     * 
     * @return the mass associated with the given bin
     */
    public Double getMass(int bin) {
        if (ppm) {
            return FastMath.exp((scalingFactor * bin) + massAnchorLog);
        } else {
            return precursorTolerance * (0.5 + bin);
        }
    }

    /**
     * Convenience class storing the precursor, corresponding spectrum title, and inferred charge.
     */
    public class PrecursorMatch {

        /**
         * The precursor
         */
        public final Precursor precursor;
        /**
         * The spectrum title
         */
        public final String spectrumTitle;
        /**
         * The inferred charge
         */
        public final int charge;

        /**
         * Constructor.
         * 
         * @param precursor the precursor
         * @param spectrumTitle the spectrum title
         * @param charge the inferred charge
         */
        public PrecursorMatch(Precursor precursor, String spectrumTitle, int charge) {
            this.precursor = precursor;
            this.spectrumTitle = spectrumTitle;
            this.charge = charge;
        }
    }

}
