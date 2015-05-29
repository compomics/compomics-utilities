package com.compomics.util.math.statistics;

import java.math.MathContext;
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
     * @param mathContext the math context to use for calculation
     *
     * @return the sensitivity at the given specificity (0.1 is 10%)
     *
     * @throws org.apache.commons.math.MathException if a MathException occurs
     */
    public double getValueAt(double specificity, MathContext mathContext) throws MathException;

    /**
     * Returns the 1-specificity at a given sensitivity.
     *
     * @param sensitivity the sensitivity (0.1 is 10%)
     * @param mathContext the math context to use for calculation
     *
     * @return the corresponding 1-specificity (0.1 is 10%)
     *
     * @throws MathException if a MathException occurs
     */
    public double getSpecificityAt(double sensitivity, MathContext mathContext) throws MathException;

    /**
     * Returns xy values to draw the curve.
     *
     * @param mathContext the math context to use for calculation
     * 
     * @return xy values to draw the curve
     *
     * @throws MathException if a MathException occurs
     */
    public double[][] getxYValues(MathContext mathContext) throws MathException;

    /**
     * Returns an estimation of the area under the curve.
     *
     * @param mathContext the math context to use for calculation
     * 
     * @return an estimation of the area under the curve
     *
     * @throws MathException if a MathException occurs
     */
    public double getAuc(MathContext mathContext) throws MathException;
}
