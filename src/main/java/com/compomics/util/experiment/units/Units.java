package com.compomics.util.experiment.units;

/**
 * Convenience class providing units.
 *
 * @author Marc Vaudel
 */
public class Units {

    /**
     * Empty default constructor
     */
    public Units() {
    }
    
    /**
     * Femtomol.
     */
    public final static UnitOfMeasurement fmol = new UnitOfMeasurement(StandardUnit.mol, MetricsPrefix.femto);
    
    /**
     * ppm.
     */
    public final static UnitOfMeasurement ppm = new UnitOfMeasurement(StandardUnit.ppm);
    
    /**
     * percent.
     */
    public final static UnitOfMeasurement percent = new UnitOfMeasurement(StandardUnit.percentage);

}
