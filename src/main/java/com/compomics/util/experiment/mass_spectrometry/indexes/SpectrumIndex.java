package com.compomics.util.experiment.mass_spectrometry.indexes;

import com.compomics.util.StreamUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.math.util.FastMath;

/**
 * This map stores the fragment ions indexed by mass.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SpectrumIndex {

    /**
     * An m/z anchor to determine the bins in ppm.
     */
    private static final double MZ_ANCHOR = 1000;
    /**
     * The log of the m/z anchor.
     */
    private static final double MZ_ANCHOR_LOG = FastMath.log(MZ_ANCHOR);
    /**
     * Place holder for an empty array when no result is found.
     */
    private static final int[] EMPTY_ARRAY = new int[0];
    /**
     * The mass tolerance.
     */
    public final double tolerance;
    /**
     * Boolean indicating whether the precursor mass tolerance is in ppm.
     */
    private boolean ppm;
    /**
     * Map of the fragment ions index by bin.
     */
    private final HashMap<Integer, ArrayList<Integer>> peaksMap = new HashMap<>();
    /**
     * The mz array of the spectrum.
     */
    public final double[] mzArray;
    /**
     * The mz array of the spectrum.
     */
    public final double[] intensityArray;
    /**
     * The scaling factor used for the bins in ppm.
     */
    private double scalingFactor;
    /**
     * The highest bin in index.
     */
    private Integer binMax;
    /**
     * The lowest bin in index.
     */
    private Integer binMin;
    /**
     * The total intensity above the intensity threshold.
     */
    private double totalIntensity;
    /**
     * The intensity limit used for the index.
     */
    public final double intensityLimit;

    /**
     * Constructor for an empty index.
     */
    public SpectrumIndex() {
        intensityLimit = 0.0;
        tolerance = 0.0;
        mzArray = null;
        intensityArray = null;
    }

    /**
     * Builds a new index.
     *
     * @param mz array of the mz of the fragment ions
     * @param intensity array of the intensity of the fragment ions
     * @param intenstiyLimit a lower limit for the intensity of the peaks to
     * index
     * @param tolerance the tolerance to use
     * @param ppm boolean indicating whether the tolerance is in ppm
     */
    public SpectrumIndex(
            double[] mz,
            double[] intensity,
            double intenstiyLimit,
            double tolerance,
            boolean ppm
    ) {

        this.intensityLimit = intenstiyLimit;
        this.mzArray = mz;
        this.intensityArray = intensity;
        this.tolerance = tolerance;
        this.ppm = ppm;

        if (ppm) {

            scalingFactor = FastMath.log((1000000 - tolerance) / (1000000 + tolerance));

        }

        totalIntensity = 0.0;

        for (int i = 0; i < mz.length; i++) {

            double peakInt = intensity[i];

            if (peakInt >= intenstiyLimit) {

                totalIntensity += peakInt;

                double peakMz = mz[i];
                int bin = getBin(peakMz);

                if (binMax == null || bin > binMax) {

                    binMax = bin;

                }

                if (binMin == null || bin < binMin) {

                    binMin = bin;

                }

                ArrayList<Integer> indexes = peaksMap.get(bin);

                if (indexes == null) {

                    indexes = new ArrayList<>(4);
                    peaksMap.put(bin, indexes);

                }

                indexes.add(i);

            }
        }
    }

    /**
     * Returns the peaks map.
     *
     * @return the peaks map
     */
    public HashMap<Integer, ArrayList<Integer>> getPeaksMap() {

        return peaksMap;
    }

    /**
     * Returns whether the precursor mass tolerance is in ppm.
     *
     * @return whether the precursor mass tolerance is in ppm
     */
    public boolean getPpm() {

        return ppm;
    }

    /**
     * Returns the precursor tolerance.
     *
     * @return the precursor tolerance
     */
    public double getPrecursorToleance() {

        return tolerance;
    }

    /**
     * Returns the scaling factor.
     *
     * @return the scaling factor
     */
    public double getScalingFactor() {

        return scalingFactor;
    }

    /**
     * Returns the bin corresponding to the given m/z.
     *
     * @param mz the m/z
     *
     * @return the bin
     */
    public int getBin(double mz) {

        return ppm ? getBinPpm(mz) : getBinAbsolute(mz);

    }

    /**
     * Returns the bin corresponding to the given m/z with absolute tolerance in
     * Da.
     *
     * @param mz the m/z
     *
     * @return the bin
     */
    private int getBinAbsolute(
            double mz
    ) {

        int bin = (int) (mz / tolerance);

        return bin;
    }

    /**
     * Returns the bin corresponding to the given m/z with relative tolerance in
     * ppm.
     *
     * @param mz the m/z
     *
     * @return the bin
     */
    private int getBinPpm(
            double mz
    ) {

        int bin = (int) ((FastMath.log(mz) - MZ_ANCHOR_LOG) / scalingFactor);

        return bin;
    }

    /**
     * Returns the peaks matching the given m/z.
     *
     * TODO: check only one/two bins when possible.
     *
     * @param queryMz a m/z to query
     *
     * @return the index of the peaks matching the given m/z
     */
    public int[] getMatchingPeaks(
            double queryMz
    ) {

        int bin0;
        if (ppm) {

            bin0 = getBinPpm(queryMz);

        } else {

            bin0 = getBinAbsolute(queryMz);

        }

        ArrayList<Integer> binContent1 = peaksMap.get(bin0 - 1);
        ArrayList<Integer> binContent2 = peaksMap.get(bin0);
        ArrayList<Integer> binContent3 = peaksMap.get(bin0 + 1);

        Stream<Integer> stream = StreamUtil.concatenate(
                binContent1,
                binContent2,
                binContent3
        );

        return stream == null ? EMPTY_ARRAY :
                stream
                .mapToInt(i -> i)
                .filter(
                        i -> isBelowTolerance(queryMz, i)
                )
                .toArray();
    }

    /**
     * Indicates whether the peak at the given index is matching the queried
     * m/z.
     *
     * @param queryMz The queried m/z.
     * @param index The index of the peak.
     *
     * @return A boolean indicating whether the peak at the given index is
     * matching the queried m/z.
     */
    private boolean isBelowTolerance(
            double queryMz,
            int index
    ) {

        double peakMz = mzArray[index];
        double error = ppm ? 1000000 * (peakMz - queryMz) / queryMz : peakMz - queryMz;

        return Math.abs(error) <= tolerance;
    }

    /**
     * Returns the bins in the map as a list. The list is created every time me
     * method is called.
     *
     * @return the bins in the map
     */
    public ArrayList<Integer> getBins() {

        return new ArrayList<>(peaksMap.keySet());

    }

    /**
     * Returns the bins in the map as collection of keys from the map.
     *
     * @return the bins in the map
     */
    public Set<Integer> getRawBins() {

        return peaksMap.keySet();

    }

    /**
     * Returns the indexes of the peaks at the given bin indexed by m/z. Null if
     * none found.
     *
     * @param bin the bin number
     *
     * @return the indexes of the peaks at the given bin
     */
    public ArrayList<Integer> getPeaksInBin(
            int bin
    ) {

        return peaksMap.get(bin);

    }

    /**
     * Returns the mass associated with the given bin, the middle of the bin.
     *
     * @param bin the bin number
     *
     * @return the mass associated with the given bin
     */
    public double getMass(
            int bin
    ) {

        return ppm ? FastMath.exp((scalingFactor * bin) + MZ_ANCHOR_LOG)
                : tolerance * (0.5 + bin);

    }

    /**
     * Returns the highest bin.
     *
     * @return binMax the highest bin
     */
    public Integer getBinMax() {

        return binMax;

    }

    /**
     * Returns the lowest bin.
     *
     * @return binMin the lowest bin
     */
    public Integer getBinMin() {

        return binMin;

    }

    /**
     * Returns the total intensity of the peaks above the intensity threshold.
     *
     * @return the total intensity of the peaks above the intensity threshold
     */
    public double getTotalIntensity() {
        
        return totalIntensity;

    }

    /**
     * Sets the highest bin in index.
     *
     * @param binMax the highest bin in index
     */
    public void setBinMax(
            Integer binMax
    ) {

        this.binMax = binMax;

    }

    /**
     * Sets the lowest bin in index.
     *
     * @param binMin the lowest bin in index
     */
    public void setBinMin(
            Integer binMin
    ) {

        this.binMin = binMin;

    }

    /**
     * Sets whether the precursor mass tolerance is in ppm.
     *
     * @param ppm whether the precursor mass tolerance is in ppm
     */
    public void setPpm(
            boolean ppm
    ) {

        this.ppm = ppm;

    }

    /**
     * Sets the scaling factor.
     *
     * @param scalingFactor the scaling factor
     */
    public void setScalingFactor(
            double scalingFactor
    ) {

        this.scalingFactor = scalingFactor;

    }

    /**
     * Set the total intensity.
     *
     * @param totalIntensity the total intensity
     */
    public void setTotalIntensity(
            double totalIntensity
    ) {

        this.totalIntensity = totalIntensity;

    }
}
