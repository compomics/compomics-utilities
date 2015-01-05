package com.compomics.util.experiment;

/**
 * convenience class listing constants used throughout the library.
 *
 * @author Marc Vaudel
 */
public class Constants {

    /**
     * The Avogadro constant without exponent.
     */
    public static final double AvogadroNoExp = 6.02214129;
    /**
     * The Avogadro constant.
     */
    public static final double Avogadro = AvogadroNoExp * Math.pow(10, 23);
    /**
     * The atomic mass unit without exponent.
     */
    public static final double amuNoExp = 1.660538921;
    /**
     * The atomic mass unit in kg.
     */
    public static final double amu = amuNoExp * Math.pow(10, -27);
}
