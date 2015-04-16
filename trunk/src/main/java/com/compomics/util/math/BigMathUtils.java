package com.compomics.util.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Utils for the manipulation of big numbers.
 *
 * @author Marc Vaudel
 */
public class BigMathUtils {

    /**
     * Big decimal value of E.
     */
    public static final BigDecimal E = new BigDecimal(Math.E);
    /**
     * Big decimal value of 2.
     */
    public static final BigDecimal two = new BigDecimal(2);
    /**
     * Big decimal value of 1000.
     */
    public static final BigDecimal thousand = new BigDecimal(1000);
    /**
     * Big decimal value of the minimal normal value of a double
     */
    public static final BigDecimal minNormalDouble = new BigDecimal(Double.MIN_NORMAL);
    /**
     * Big decimal value of the maximal value of a double
     */
    public static final BigDecimal maxDouble = new BigDecimal(Double.MAX_VALUE);
    /**
     * Big decimal value of ln(10).
     */
    private static BigDecimal lnTen = null;
    /**
     * The precision at which lnTen in cache is calculated. -1 if none.
     */
    private static MathContext lnTenMathContext = null;
    
    /**
     * Returns the value of ln(10) according to the mathContext.
     * 
     * @param mathContext the math context to use for the calculation
     * 
     * @return the value of ln(10)
     */
    public static BigDecimal getLn10(MathContext mathContext) {
       if (lnTen == null || lnTenMathContext.getPrecision() < mathContext.getPrecision() || lnTenMathContext.getRoundingMode() != mathContext.getRoundingMode()) {
           lnTen = BigFunctions.ln(BigDecimal.TEN, mathContext);
                lnTenMathContext = mathContext;
       }
       return lnTen.round(mathContext);
    }
    
}
