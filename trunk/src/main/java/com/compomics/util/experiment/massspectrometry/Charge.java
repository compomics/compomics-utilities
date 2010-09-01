package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.utils.ExperimentObject;

/**
 * This class models a charge.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 10:01:29 AM
 */
public class Charge extends ExperimentObject {

    /**
     * static int to modele a positive charge
     */
    public final static int PLUS = +1;
    /**
     * static int to modele a negative charge
     */
    public final static int MINUS = -1;
    /**
     * static int to modele a neutral component
     */
    public final static int NEUTRAL = 0;
    /**
     * sign of the charge according to the static fields
     */
    public int sign;
    /**
     * value of the charge
     */
    public int value;

    /**
     * constructor for a charge
     *
     * @param sign  sign of the charge as specified in static fields
     * @param value value of the charge
     */
    public Charge(int sign, int value) {
        this.sign = sign;
        this.value = value;
    }

    /**
     * returns a string representing the charge
     * 
     * @return charge as a string
     */
    public String toString() {
        switch (sign) {
            case PLUS:
                return value + "+";
            case MINUS:
                return value + "-";
            default:
                return "";
        }
    }
}
