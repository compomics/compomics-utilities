package com.compomics.util.experiment.mass_spectrometry.utils;

import com.compomics.util.experiment.biology.atoms.Atom;

/**
 * Enum of standard masses.
 *
 * @author Marc Vaudel
 */
public enum StandardMasses {

    /**
     * Atomic mass of H2O.
     */
    h2o(2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass()),

    /**
     * Atomic mass of NH3.
     */
    nh3(Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass()),

    /**
     * Atomic mass of H2o + NH3.
     */
    h2onh3(Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() + 5 * Atom.H.getMonoisotopicMass()),

    /**
     * Atomic mass of CO.
     */
    co(Atom.C.getMonoisotopicMass() + Atom.O.getMonoisotopicMass());

    /**
     * The mass.
     */
    public final double mass;

    /**
     * Constructor.
     *
     * @param mass the mass
     */
    private StandardMasses(double mass) {
        this.mass = mass;
    }


    /**
     * Empty default constructor
     */
    private StandardMasses() {
        mass = 0;
    }
}
