package com.compomics.util.experiment.biology.ions;

/**
 * This class contains convenience methods for the handling of charges.
 *
 * @author Marc Vaudel
 */
public class Charge {

    /**
     * Empty default constructor
     */
    public Charge() {
    }
    
     /**
     * Returns the charge as a string of + or -. One for each charge. A charge
     * of 1 returns the empty string.
     *
     * @param value the value of the charge
     * 
     * @return the charge as a string of +
     */
    public static String getChargeAsFormattedString(int value) {

        if (value == 1) {
            return "";
        }
        if (value == 0) {
            return "0";
        }
        

        int absValue = Math.abs(value);
        boolean pos = value > 0;
        
        StringBuilder stringValue = new StringBuilder(absValue);
        
        for (int i = 0; i < absValue; i++) {
            if (pos) {
                stringValue.append('+');
            } else {
                stringValue.append('-');
            }
        }

        return stringValue.toString();
    }

    /**
     * Returns a string representing the charge. For example 2+.
     *
     * @param value the value of the charge
     *
     * @return charge as a string
     */
    public static String toString(int value) {
        
        return value > 0 ? "+" + value : "" + value;
    }
}
