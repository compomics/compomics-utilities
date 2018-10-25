package com.compomics.util.experiment;

/**
 * Convenience class listing constants used throughout the library.
 *
 * @author Marc Vaudel
 */
public class Constants {

    /**
     * Empty default constructor
     */
    public Constants() {
    }

    /**
     * The Avogadro constant without exponent.
     */
    public static final double AVOGADRO_NO_EXP = 6.02214129;
    /**
     * The Avogadro constant.
     */
    public static final double Avogadro = AVOGADRO_NO_EXP * Math.pow(10, 23);
    /**
     * The atomic mass unit without exponent.
     */
    public static final double AMU_NO_EXP = 1.660538921;
    /**
     * The atomic mass unit in kg.
     */
    public static final double AMU = AMU_NO_EXP * Math.pow(10, -27);
}
