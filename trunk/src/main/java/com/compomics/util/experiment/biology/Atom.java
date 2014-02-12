package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.atoms.Carbon;
import com.compomics.util.experiment.biology.atoms.Hydrogen;
import com.compomics.util.experiment.biology.atoms.Nitrogen;
import com.compomics.util.experiment.biology.atoms.Oxygen;
import com.compomics.util.experiment.biology.atoms.Phosphorus;
import com.compomics.util.experiment.biology.atoms.Sulfur;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This interface contains information about atoms.
 *
 * @author Marc Vaudel
 */
public abstract class Atom extends ExperimentObject {

    /**
     * A hydrogen atom.
     */
    public static final Atom H = new Hydrogen();
    /**
     * A nitrogen atom.
     */
    public static final Atom N = new Nitrogen();
    /**
     * An oxygen atom.
     */
    public static final Atom O = new Oxygen();
    /**
     * A carbon atom.
     */
    public static final Atom C = new Carbon();
    /**
     * A sulfur atom.
     */
    public static final Atom S = new Sulfur();
    /**
     * A phosphorys atom.
     */
    public static final Atom P = new Phosphorus();

    /**
     * Map of the isotope masses relative to the monoisotopic peak (+1 for
     * carbon 13).
     */
    protected HashMap<Integer, Double> isotopeMap;
    /**
     * Map of the isotope representative composition of the stable isotopes.
     */
    protected HashMap<Integer, Double> representativeComposition;
    /**
     * The name of the atom.
     */
    protected String name;
    /**
     * The single letter code of the atom.
     */
    protected String letter;

    /**
     * Returns the monoisotopic mass.
     *
     * @return the monoisotopic mass in Da
     */
    public Double getMonoisotopicMass() {
        return getIsotopeMass(0);
    }

    /**
     * Returns the name of the atom.
     *
     * @return the name of the atom
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the single letter code of the atom.
     *
     * @return the single letter code of the atom
     */
    public String getLetter() {
        return letter;
    }

    /**
     * returns a list of isotopes for which a mass is available relative to the
     * monoisotopic peak (+1 for carbon 13).
     *
     * @return a list of isotopes for which a mass is available
     */
    public ArrayList<Integer> getImplementedIsotopes() {
        if (isotopeMap != null) {
            return new ArrayList<Integer>(isotopeMap.keySet());
        }
        return new ArrayList<Integer>();
    }

    /**
     * Returns the mass corresponding to the given isotope number. Null if not
     * found.
     *
     * @param isotopeNumber the isotope number of interest relative to the
     * monoisotopic peak (+1 for carbon 13).
     *
     * @return the corresponding mass
     */
    public Double getIsotopeMass(int isotopeNumber) {
        if (isotopeMap != null) {
            return isotopeMap.get(isotopeNumber);
        }
        return null;
    }

    /**
     * Returns the mass difference between the given isotope and the
     * monoisotopic mass.
     *
     * @param isotopeNumber the isotope number relative to the monoisotopic peak
     * (+1 for carbon 13)
     *
     * @return the mass difference between the given isotope and the
     * monoisotopic mass
     */
    public Double getDifferenceToMonoisotopic(int isotopeNumber) {
        Double isotopeMass = null;
        if (isotopeMap != null) {
            isotopeMass = isotopeMap.get(isotopeNumber);
        }
        if (isotopeMass == null) {
            throw new IllegalArgumentException("No isotope mass found for isotope " + isotopeNumber + " of atom " + name + ".");
        }
        return isotopeMass - getMonoisotopicMass();
    }
}
