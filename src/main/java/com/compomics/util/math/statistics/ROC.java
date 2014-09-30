package com.compomics.util.math.statistics;

import org.apache.commons.math.MathException;

/**
 * This class can be used to draw ROC curves.
 *
 * @author Marc Vaudel
 */
public interface ROC {

    /**
     * Returns the sensitivity at a given 1-specificity, i.e., 1-type 2 error,
     * the number of true healthy for a given type 1 error, the number of false
     * healthy.
     *
     * @param specificity the specificity (0.1 is 10%)
     *
     * @return the sensitivity at the given specificity (0.1 is 10%)
     *
     * @throws org.apache.commons.math.MathException
     */
    public double getValueAt(double specificity) throws MathException;

    /**
     * Returns the 1-specificity at a given sensitivity.
     *
     * @param sensitivity the sensitivity (0.1 is 10%)
     *
     * @return the corresponding 1-specificity (0.1 is 10%)
     *
     * @throws MathException
     */
    public double getSpecificityAt(double sensitivity) throws MathException;

    /**
     * Returns xy values to draw the curve.
     *
     * @return xy values to draw the curve
     *
     * @throws MathException
     */
    public double[][] getxYValues() throws MathException;

    /**
     * Returns an estimation of the area under the curve.
     *
     * @return an estimation of the area under the curve
     */
    public double getAuc()throws MathException;
}
