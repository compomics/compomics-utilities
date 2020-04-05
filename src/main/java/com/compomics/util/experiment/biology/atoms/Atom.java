package com.compomics.util.experiment.biology.atoms;

import com.compomics.util.experiment.biology.atoms.impl.Calcium;
import com.compomics.util.experiment.biology.atoms.impl.Carbon;
import com.compomics.util.experiment.biology.atoms.impl.Copper;
import com.compomics.util.experiment.biology.atoms.impl.Fluorine;
import com.compomics.util.experiment.biology.atoms.impl.Helium;
import com.compomics.util.experiment.biology.atoms.impl.Hydrogen;
import com.compomics.util.experiment.biology.atoms.impl.Iodine;
import com.compomics.util.experiment.biology.atoms.impl.Lithium;
import com.compomics.util.experiment.biology.atoms.impl.Iron;
import com.compomics.util.experiment.biology.atoms.impl.Magnesium;
import com.compomics.util.experiment.biology.atoms.impl.Nitrogen;
import com.compomics.util.experiment.biology.atoms.impl.Oxygen;
import com.compomics.util.experiment.biology.atoms.impl.Phosphorus;
import com.compomics.util.experiment.biology.atoms.impl.Potassium;
import com.compomics.util.experiment.biology.atoms.impl.Selenium;
import com.compomics.util.experiment.biology.atoms.impl.Sodium;
import com.compomics.util.experiment.biology.atoms.impl.Sulfur;
import com.compomics.util.experiment.biology.atoms.impl.Zinc;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This interface contains information about atoms.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class Atom extends ExperimentObject {

    /**
     * Empty default constructor
     */
    public Atom() {
    }

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 1059024301538472131L;
    /**
     * The hydrogen atom.
     */
    public static final Atom H = new Hydrogen();
    /**
     * The nitrogen atom.
     */
    public static final Atom N = new Nitrogen();
    /**
     * The oxygen atom.
     */
    public static final Atom O = new Oxygen();
    /**
     * The carbon atom.
     */
    public static final Atom C = new Carbon();
    /**
     * The sulfur atom.
     */
    public static final Atom S = new Sulfur();
    /**
     * The phosphorus atom.
     */
    public static final Atom P = new Phosphorus();
    /**
     * The helium atom.
     */
    public static final Atom He = new Helium();
    /**
     * The phosphorus atom.
     */
    public static final Atom Li = new Lithium();
    /**
     * The sodium atom.
     */
    public static final Atom Na = new Sodium();
    /**
     * The selenium atom.
     */
    public static final Atom Se = new Selenium();
    /**
     * The Iodine atom.
     */
    public static final Atom I = new Iodine();
    /**
     * The Fluorine atom.
     */
    public static final Atom F = new Fluorine();
    /**
     * The Iron atom.
     */
    public static final Atom Fe = new Iron();
    /**
     * The Potassium atom.
     */
    public static final Atom K = new Potassium();
    /**
     * The Calcium atom.
     */
    public static final Atom Ca = new Calcium();
    /**
     * The Zinc atom.
     */
    public static final Atom Zn = new Zinc();
    /**
     * The Magnesium atom.
     */
    public static final Atom Mg = new Magnesium();
    /**
     * The Copper atom.
     */
    public static final Atom Cu = new Copper();
    /**
     * The monoisotopic mass. Access is faster then querying the isotope map.
     */
    protected double monoisotopicMass;
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
     * The symbol for the atom.
     */
    protected String letter;

    /**
     * Returns an array of implemented atoms indicated by their short name.
     *
     * @param includeSelect if true, the first item is set to '- Select -'
     * @return an array of implemented atoms
     */
    public static String[] getImplementedAtoms(boolean includeSelect) {
        if (includeSelect) {
            return new String[]{"- Select -", "C", "H", "I", "N", "O", "S", "P",
                "He", "Li", "Na", "Se", "F", "Fe", "K", "Ca", "Zn", "Mg", "Cu"};
        } else {
            return new String[]{"C", "H", "I", "N", "O", "S", "P", "He", "Li",
                "Na", "Se", "F", "Fe", "K", "Ca", "Zn", "Mg", "Cu"};
        }
    }

    /**
     * Returns the atom corresponding to the given short name.
     *
     * @param shortName the short name of the atom
     *
     * @return the atom corresponding to the given short name
     */
    public static Atom getAtom(String shortName) {

        switch (shortName) {
            case "H":
                return H;
            case "I":
                return I;
            case "N":
                return N;
            case "O":
                return O;
            case "C":
                return C;
            case "S":
                return S;
            case "P":
                return P;
            case "He":
                return He;
            case "Li":
                return Li;
            case "Na":
                return Na;
            case "Se":
                return Se;
            case "F":
                return F;
            case "Fe":
                return Fe;
            case "K":
                return K;
            case "Ca":
                return Ca;
            case "Zn":
                return Zn;
            case "Mg":
                return Mg;
            case "Cu":
                return Cu;
            default:
                break;
        }

        throw new UnsupportedOperationException("Atom " + shortName + " not implemented.");
    }

    /**
     * Returns the monoisotopic mass.
     *
     * @return the monoisotopic mass in Da
     */
    public double getMonoisotopicMass() {
        readDBMode();
        return monoisotopicMass;
    }

    /**
     * Returns the name of the atom.
     *
     * @return the name of the atom
     */
    public String getName() {
        readDBMode();
        return name;
    }

    /**
     * Returns the symbol for the atom.
     *
     * @return the symbol for the atom
     */
    public String getLetter() {
        readDBMode();
        return letter;
    }

    /**
     * returns an unsorted list of isotopes for which a mass is available
     * relative to the monoisotopic peak (+1 for carbon 13).
     *
     * @return a list of isotopes for which a mass is available
     */
    public ArrayList<Integer> getImplementedIsotopes() {
        readDBMode();
        if (isotopeMap != null) {
            return new ArrayList<>(isotopeMap.keySet());
        }
        return new ArrayList<>();
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
        readDBMode();
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
    public double getDifferenceToMonoisotopic(int isotopeNumber) {
        readDBMode();

        if (!isotopeMap.containsKey(isotopeNumber)) {
            throw new IllegalArgumentException(
                    "No isotope mass found for isotope "
                    + isotopeNumber + " of atom " + name + ".");
        }

        double isotopeMass = isotopeMap.get(isotopeNumber);

        return isotopeMass - getMonoisotopicMass();
    }

    @Override
    public String toString() {
        readDBMode();
        return getLetter();
    }
}
