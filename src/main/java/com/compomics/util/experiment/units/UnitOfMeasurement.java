package com.compomics.util.experiment.units;

import com.compomics.util.db.object.DbObject;


/**
 * The unit to use for a measure.
 *
 * @author Marc Vaudel
 */
public class UnitOfMeasurement extends DbObject {

    /**
     * Empty default constructor
     */
    public UnitOfMeasurement() {
    }

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
     * Constructor.
     *
     * @param standardUnit a standard unit
     */
    public UnitOfMeasurement(StandardUnit standardUnit) {
        this.fullName = standardUnit.FULL_NAME;
        this.abbreviation = standardUnit.ABBREVIATION;
    }

    /**
     * Returns the full name of the unit.
     *
     * @return the full name of the unit
     */
    public String getFullName() {
        readDBMode();
        return fullName;
    }

    /**
     * Returns the abbreviated name of the unit.
     *
     * @return the abbreviated name of the unit
     */
    public String getAbbreviation() {
        readDBMode();
        return abbreviation;
    }

    /**
     * Returns the metrics prefix.
     *
     * @return the metrics prefix
     */
    public MetricsPrefix getMetricsPrefix() {
        readDBMode();
        return metricsPrefix;
    }

    /**
     * Returns the name to display, e.g. fmol.
     *
     * @return the name to display
     */
    public String getDisplayName() {
        readDBMode();
        if (metricsPrefix == null) {
            return abbreviation;
        }
        return metricsPrefix.SYMBOL + abbreviation;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Indicates whether the given unit of measurement is the same as another.
     * 
     * @param unitOfMeasurement an other unit of measurement
     * 
     * @return a boolean indicating whether the given unit of measurement is the same as another
     */
    public boolean isSameAs(UnitOfMeasurement unitOfMeasurement) {
        readDBMode();
        if (getMetricsPrefix() == null && unitOfMeasurement.getMetricsPrefix() != null
                || getMetricsPrefix() != null && unitOfMeasurement.getMetricsPrefix() == null) {
            return false;
        }
        if (getMetricsPrefix() != null && unitOfMeasurement.getMetricsPrefix() != null && getMetricsPrefix() != unitOfMeasurement.getMetricsPrefix()) {
            return false;
        }
        return fullName.equals(unitOfMeasurement.getFullName());
    }
}
