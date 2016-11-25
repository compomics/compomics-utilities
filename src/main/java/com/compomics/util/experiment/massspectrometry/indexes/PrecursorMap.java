package com.compomics.util.experiment.massspectrometry.indexes;

import com.compomics.util.experiment.massspectrometry.Precursor;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math.util.FastMath;

/**
 * This map stores the precursors indexed by mass.
 *
 * @author Marc Vaudel
 */
public class PrecursorMap {

    /**
     * The precursor m/z tolerance.
     */
    double precursorTolerance;
    /**
     * Boolean indicating whether the precursor m/z tolerance is in ppm.
     */
    boolean ppm;
    /**
     * Map of the precursors by bin and m/z.
     */
    private HashMap<Integer, HashMap<Double, ArrayList<PrecursorWithTitle>>> precursorsMap = new HashMap<Integer, HashMap<Double, ArrayList<PrecursorWithTitle>>>();
    /**
     * An m/z anchor to determine the bins in ppm
     */
    private static final double mzAnchor = 1000;
    /**
     * The log of the m/z anchor.
     */
    private static final double mzAnchorLog = FastMath.log(mzAnchor);
    /**
     * The scaling factor used for the bins in ppm
     */
    private double scalingFactor;
    /**
     * The minimal m/z found.
     */
    private Double minMz = null;
    /**
     * The maximal m/z found.
     */
    private Double maxMz = null;

    /**
     * Builds a precursor map.
     *
     * @param precursors map of the precursors indexed by spectrum title
     * @param precursorTolerance the precursor mass tolerance to use
     * @param ppm boolean indicating whether the tolerance is in ppm
     */
    public PrecursorMap(HashMap<String, Precursor> precursors, double precursorTolerance, boolean ppm) {
        this.precursorTolerance = precursorTolerance;
        this.ppm = ppm;
        if (ppm) {
            scalingFactor = FastMath.log((1000000 - precursorTolerance) / (1000000 + precursorTolerance));
        }
        for (String spectrumTitle : precursors.keySet()) {
            Precursor precursor = precursors.get(spectrumTitle);
            PrecursorWithTitle precursorWithTitle = new PrecursorWithTitle(precursor, spectrumTitle);
            double mz = precursor.getMz();
            if (minMz == null || mz < minMz) {
                minMz = mz;
            }
            if (maxMz == null || mz > maxMz) {
                maxMz = mz;
            }
            Integer bin = getBin(mz);
            HashMap<Double, ArrayList<PrecursorWithTitle>> precursorsInBin = precursorsMap.get(bin);
            if (precursorsInBin == null) {
                precursorsInBin = new HashMap<Double, ArrayList<PrecursorWithTitle>>(2);
                precursorsMap.put(bin, precursorsInBin);
            }
            ArrayList<PrecursorWithTitle> precursorsAtMz = precursorsInBin.get(mz);
            if (precursorsAtMz == null) {
                precursorsAtMz = new ArrayList<PrecursorWithTitle>(1);
                precursorsInBin.put(mz, precursorsAtMz);
            }
            precursorsAtMz.add(precursorWithTitle);
        }
    }

    /**
     * Returns the bin corresponding to the given m/z.
     *
     * @param mz the m/z
     *
     * @return the bin
     */
    private Integer getBin(double mz) {
        if (ppm) {
            return getBinPpm(mz);
        } else {
            return getBinAbsolute(mz);
        }
    }

    /**
     * Returns the bin corresponding to the given m/z with absolute tolerance in
     * Th.
     *
     * @param mz the m/z
     *
     * @return the bin
     */
    private Integer getBinAbsolute(double mz) {
        Integer bin = (int) (mz / precursorTolerance);
        return bin;
    }

    /**
     * Returns the bin corresponding to the given mz with relative tolerance in
     * ppm.
     *
     * @param mz the m/z
     *
     * @return the bin
     */
    private Integer getBinPpm(double mz) {
        int bin = (int) ((FastMath.log(mz) - mzAnchorLog) / scalingFactor);
        return bin;
    }

    /**
     * Returns a list containing the precursors matching the given m/z.
     * TODO: check only one/two bins when possible
     *
     * @param referenceMz a mz to query
     *
     * @return a list containing the precursors matching the given m/z
     */
    public ArrayList<PrecursorWithTitle> getMatchingSpectra(double referenceMz) {
        
        int bin0 = getBin(referenceMz);
        ArrayList<PrecursorWithTitle> result = new ArrayList<PrecursorWithTitle>(0);
        HashMap<Double, ArrayList<PrecursorWithTitle>> binContent = precursorsMap.get(bin0 - 1);
        if (binContent != null) {
            for (Double precursorMz : binContent.keySet()) {
                double error;
                if (ppm) {
                    error = 1000000 * (precursorMz - referenceMz) / referenceMz;
                } else {
                    error = precursorMz - referenceMz;
                }
                if (Math.abs(error) <= precursorTolerance) {
                    result.addAll(binContent.get(precursorMz));
                }
            }
        }
        binContent = precursorsMap.get(bin0);
        if (binContent != null) {
            for (Double precursorMz : binContent.keySet()) {
                double error;
                if (ppm) {
                    error = 1000000 * (precursorMz - referenceMz) / referenceMz;
                } else {
                    error = precursorMz - referenceMz;
                }
                if (Math.abs(error) <= precursorTolerance) {
                    result.addAll(binContent.get(precursorMz));
                }
            }
        }
        binContent = precursorsMap.get(bin0 + 1);
        if (binContent != null) {
            for (Double precursorMz : binContent.keySet()) {
                double error;
                if (ppm) {
                    error = 1000000 * (precursorMz - referenceMz) / referenceMz;
                } else {
                    error = precursorMz - referenceMz;
                }
                if (Math.abs(error) <= precursorTolerance) {
                    result.addAll(binContent.get(precursorMz));
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
     * Returns the precursors at the given bin indexed by mass. Null if none
     * found.
     *
     * @param bin the bin number
     *
     * @return the precursors at the given bin
     */
    public HashMap<Double, ArrayList<PrecursorWithTitle>> getPrecursorsInBin(int bin) {
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
            return FastMath.exp((scalingFactor * bin) + mzAnchorLog);
        } else {
            return precursorTolerance * (0.5 + bin);
        }
    }

    /**
     * Returns the minimal m/z encountered among the precursors.
     * 
     * @return the minimal m/z encountered among the precursors
     */
    public Double getMinMz() {
        return minMz;
    }

    /**
     * Returns the maximal m/z encountered among the precursors.
     * 
     * @return the maximal m/z encountered among the precursors
     */
    public Double getMaxMz() {
        return maxMz;
    }
    
    /**
     * Convenience class storing the precursor and corresponding spectrum title.
     */
    public class PrecursorWithTitle {

        /**
         * The precursor
         */
        public final Precursor precursor;
        /**
         * The spectrum title
         */
        public final String spectrumTitle;

        /**
         * Constructor.
         *
         * @param precursor the precursor
         * @param spectrumTitle the spectrum title
         */
        public PrecursorWithTitle(Precursor precursor, String spectrumTitle) {
            this.precursor = precursor;
            this.spectrumTitle = spectrumTitle;
        }
    }

}
