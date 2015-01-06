package com.compomics.util.experiment.units;

import java.io.Serializable;

/**
 * The unit to use for a measure.
 *
 * @author Marc Vaudel
 */
public class UnitOfMeasurement implements Serializable {

    /**
     * The full name of the unit.
     */
    private String fullName;
    /**
     * The abbreviated name of the unit.
     */
    private String abbreviation;
    /**
     * The metrics prefix.
     */
    private MetricsPrefix metricsPrefix;

    /**
     * Constructor.
     *
     * @param fullName the full name of the unit
     * @param abbreviation the abbreviated name of the unit
     * @param metricsPrefix the metrics prefix
     */
    public UnitOfMeasurement(String fullName, String abbreviation, MetricsPrefix metricsPrefix) {
        this.fullName = fullName;
        this.abbreviation = abbreviation;
        this.metricsPrefix = metricsPrefix;
    }

    /**
     * Constructor.
     *
     * @param standardUnit a standard unit
     * @param metricsPrefix the metrics prefix
     */
    public UnitOfMeasurement(StandardUnit standardUnit, MetricsPrefix metricsPrefix) {
        this.fullName = standardUnit.FULL_NAME;
        this.abbreviation = standardUnit.ABBREVIATION;
        this.metricsPrefix = metricsPrefix;
    }

    /**
     * Returns the full name of the unit.
     *
     * @return the full name of the unit
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the abbreviated name of the unit.
     *
     * @return the abbreviated name of the unit
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Returns the metrics prefix.
     *
     * @return the metrics prefix
     */
    public MetricsPrefix getMetricsPrefix() {
        return metricsPrefix;
    }

    /**
     * Returns the name to display, e.g. fmol.
     *
     * @return the name to display
     */
    public String getDisplayName() {
        return metricsPrefix.SYMBOL + abbreviation;
    }
}
