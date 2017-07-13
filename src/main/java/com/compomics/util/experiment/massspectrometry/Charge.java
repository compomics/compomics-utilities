package com.compomics.util.experiment.massspectrometry;

import com.compomics.util.experiment.personalization.ExperimentObject;

/**
 * This class models a charge.
 *
 * @author Marc Vaudel
 */
public class Charge extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = -7119215126058492261L;
    /**
     * Static int to modele a positive charge.
     */
    public final static int PLUS = +1;
    /**
     * Static int to modele a negative charge.
     */
    public final static int MINUS = -1;
    /**
     * Static int to modele a neutral component.
     */
    public final static int NEUTRAL = 0;
    /**
     * Sign of the charge according to the static fields.
     */
    public int sign;
    /**
     * Value of the charge.
     */
    public int value;

    /**
     * Constructor for a charge.
     *
     * @param sign sign of the charge as specified in static fields
     * @param value value of the charge
     */
    public Charge(int sign, int value) {
        this.sign = sign;
        this.value = value;
    }
    
    public Charge(){}

    /**
     * Returns a string representing the charge. For example 2+.
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

    /**
     * Returns the charge as a string of + or -. One for each charge. A charge
     * of 1 however returns the empty string.
     *
     * @return the charge as a string of +
     */
    public String getChargeAsFormattedString() {

        String temp = "";

        for (int i = 0; i < value; i++) {
            if (sign == PLUS) {
                temp += "+";
            } else if (sign == MINUS) {
                temp += "-";
            }
        }

        if (value == 1) {
            temp = "";
        }

        return temp;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.sign;
        hash = 97 * hash + this.value;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Charge other = (Charge) obj;
        if (this.sign != other.sign) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        return true;
    }
}
