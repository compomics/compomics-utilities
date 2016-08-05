package com.compomics.util.experiment.biology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * A chain of atoms.
 *
 * @author Marc Vaudel
 */
public class AtomChain implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 2222259572093523514L;
    /**
     * The chain of atoms.
     */
    private ArrayList<AtomImpl> atomChain;
    /**
     * The mass of the atom chain.
     */
    private Double mass = null;
    /**
     * If true, the atom chain consists of additions, i.e., all atoms are added
     * when calculating the mass, false, means that all atoms are subtracted.
     * This variable also impacts the toString method.
     */
    private Boolean addition = true;
    /**
     * Cache for the string value.
     */
    private String stringValue = null;

    /**
     * Creates an empty atom chain.
     */
    public AtomChain() {
        atomChain = new ArrayList<AtomImpl>(4);
    }

    /**
     * Creates an empty atom chain.
     *
     * @param addition if the atom chain consists of additions
     */
    public AtomChain(boolean addition) {
        atomChain = new ArrayList<AtomImpl>(4);
        this.addition = addition;
    }

    /**
     * Constructor for a monoisotopic chain as a string of additions, e.g. C3PO
     * which is interpreted as three C's, one P and one O.
     *
     * @param chain the atomic chain as a string
     *
     * @throws IllegalArgumentException if an illegal atom is used
     */
    public AtomChain(String chain) throws IllegalArgumentException {
        this(chain, true);
    }

    /**
     * Constructor for a monoisotopic chain as a string, e.g. C3PO which is
     * interpreted as three C's, one P and one O.
     *
     * @param chain the atomic chain as a string
     * @param addition if the atom chain consists of additions
     *
     * @throws IllegalArgumentException if an illegal atom is used
     */
    public AtomChain(String chain, boolean addition) throws IllegalArgumentException {
        atomChain = new ArrayList<AtomImpl>(chain.length());
        this.addition = addition;
        String lastLetter = null;
        Integer lastInt = null;
        Atom lastAtom = null;

        for (char character : chain.toCharArray()) {
            int charAsInt = Character.getNumericValue(character);
            if (charAsInt >= 0 && charAsInt <= 9) {
                if (lastLetter != null) {
                    throw new IllegalArgumentException(lastLetter + " found where an atom was expected.");
                }
                if (lastAtom == null) {
                    throw new IllegalArgumentException(character + " found where an atom was expected.");
                }
                if (lastInt != null) {
                    lastInt = 10 * lastInt + charAsInt;
                } else {
                    lastInt = charAsInt;
                }
            } else {
                if (lastAtom != null) {
                    if (lastInt == null) {
                        lastInt = 1;
                    }
                    append(new AtomImpl(lastAtom, 0), lastInt);
                    lastInt = null;
                    lastAtom = null;
                }
                try {
                    String atomName = "";
                    if (lastLetter != null) {
                        atomName += lastLetter;
                    }
                    atomName += character;
                    lastAtom = Atom.getAtom(atomName + "");
                    lastLetter = null;
                } catch (Exception e) {
                    lastLetter += character;
                }
            }
        }
        if (lastLetter != null) {
            throw new IllegalArgumentException(lastLetter + " found where an atom was expected.");
        }
        if (lastInt == null) {
            lastInt = 1;
        }
        if (lastAtom != null) {
            append(new AtomImpl(lastAtom, 0), lastInt);
        }
    }

    /**
     * Appends an atom to the chain of atoms.
     *
     * @param atom a new atom
     */
    public void append(AtomImpl atom) {
        atomChain.add(atom);
        stringValue = null;
    }

    /**
     * Appends an atom to the chain of atoms.
     *
     * @param atom a new atom
     * @param occurrence the number of times this atom should be added
     */
    public void append(AtomImpl atom, int occurrence) {
        if (occurrence < 0) {
            throw new IllegalArgumentException("Negative occurrence");
        }
        for (int i = 0; i < occurrence; i++) {
            atomChain.add(atom);
        }
        stringValue = null;
    }

    /**
     * Returns the atom chain as a list of AtomImpl.
     *
     * @return the atom chain as a list of AtomImpl
     */
    public ArrayList<AtomImpl> getAtomChain() {
        return atomChain;
    }

    /**
     * Returns the mass of the atomic chain as sum of the individual atoms.
     *
     * @return the mass of the atomic chain as sum of the individual atoms
     */
    public Double getMass() {
        if (mass == null) {
            estimateMass();
        }
        return mass;
    }

    /**
     * Returns the number of atoms in this atom chain.
     *
     * @return the number of atoms in this atom chain
     */
    public int size() {
        return atomChain.size();
    }

    /**
     * Estimates the mass of the atom chain.
     */
    private synchronized void estimateMass() {
        if (mass == null) {
            Double tempMass = 0.0;
            for (AtomImpl atom : atomChain) {
                if (addition) {
                    tempMass += atom.getMass();
                } else {
                    tempMass -= atom.getMass();
                }
            }
            mass = tempMass;
        }
    }

    /**
     * Sets the string value from the stringValue attribute, sets it from the
     * composition if not set.
     *
     * @return the string value
     */
    private synchronized String getStringValue() {

        if (stringValue == null) {
            HashMap<String, Integer> composition = new HashMap<String, Integer>(atomChain.size());

            for (AtomImpl atom : atomChain) {
                String atomName = atom.toString();
                Integer occurrence = composition.get(atomName);
                if (occurrence == null) {
                    occurrence = 0;
                }
                composition.put(atomName, occurrence + 1);
            }

            ArrayList<String> atomNames = new ArrayList<String>(composition.keySet());
            Collections.sort(atomNames);

            StringBuilder compositionAsString = new StringBuilder(atomNames.size());

            for (String atomName : atomNames) {
                if (compositionAsString.length() > 0) {
                    compositionAsString.append(" ");
                }
                compositionAsString.append(atomName);
                Integer occurrence = composition.get(atomName);
                if (addition) {
                    if (occurrence > 1) {
                        compositionAsString.append("(").append(occurrence).append(")");
                    }
                } else {
                    compositionAsString.append("(").append("-").append(occurrence).append(")");
                }
            }

            stringValue = compositionAsString.toString();
        }
        return stringValue;
    }

    /**
     * Returns the occurrence of a given atom in the chain.
     *
     * @param atom the atom of interest
     * @param isotope the isotope to look for
     *
     * @return the occurrence of the atom in this atom chain
     */
    public int getOccurrence(Atom atom, Integer isotope) {
        int occurrence = 0;
        AtomImpl atom1 = new AtomImpl(atom, isotope);
        for (AtomImpl atom2 : atomChain) {
            if (atom1.isSameAs(atom2)) {
                occurrence++;
            }
        }
        return occurrence;
    }

    /**
     * Removes all the occurrences of the given atom.
     *
     * @param atom the atom
     * @param isotope the isotope
     */
    public void remove(Atom atom, Integer isotope) {
        ArrayList<AtomImpl> newAtomChain = new ArrayList<AtomImpl>(atomChain.size());
        AtomImpl atom1 = new AtomImpl(atom, isotope);
        for (AtomImpl atom2 : atomChain) {
            if (!atom1.isSameAs(atom2)) {
                newAtomChain.add(atom2);
            }
        }
        atomChain = newAtomChain;
        mass = null;
        stringValue = null;
    }

    /**
     * Sets the occurrence of a given atom.
     *
     * @param atom the atom
     * @param isotope the isotope number
     * @param occurrence the occurrence
     */
    public void setOccurrence(Atom atom, Integer isotope, Integer occurrence) {
        remove(atom, isotope);
        append(new AtomImpl(atom, isotope), occurrence);
        mass = null;
        stringValue = null;
    }

    /**
     * Indicates whether two atom chains are of the same composition by
     * comparing their string and type. An empty chain is considered to be the
     * same composition as a null chain.
     *
     * @param anotherChain another atom chain
     *
     * @return a boolean indicating whether two atom chains are of the same
     * composition
     */
    public boolean isSameCompositionAs(AtomChain anotherChain) {
        if (!atomChain.isEmpty() && (anotherChain == null || anotherChain.getAtomChain().isEmpty())) {
            return false;
        }
        if (!addition.equals(anotherChain.getAddition())) {
            return false;
        }
        return anotherChain.toString().equals(toString());
    }

    @Override
    public String toString() {
        if (stringValue == null) {
            return getStringValue();
        }
        return stringValue;
    }

    /**
     * Returns the atom chain in Hill notation. Null if the atom chain cannot be
     * written in Hill notation.
     *
     * @return the atom chain in Hill notation.
     */
    public String toHillNotation() {

        if (!addition) {
            return null; // @TODO: anything to be done here?
        }

        HashMap<String, Integer> composition = new HashMap<String, Integer>();

        for (AtomImpl atom : atomChain) {

            if (atom.getIsotope() != 0) { // @TODO: support isotopes?
                return null;
            }

            String atomName = atom.toString();
            Integer occurrence = composition.get(atomName);
            if (occurrence == null) {
                occurrence = 0;
            }
            composition.put(atomName, occurrence + 1);
        }

        ArrayList<String> atomNames = new ArrayList<String>(composition.keySet());
        Collections.sort(atomNames); // @TODO: note that this is only correct Hill notation as long as not atoms sort earlier than C...

        StringBuilder compositionAsString = new StringBuilder(atomNames.size());

        for (String atomName : atomNames) {
            compositionAsString.append(atomName);
            Integer occurrence = composition.get(atomName);
            if (addition) {
                if (occurrence > 1) {
                    compositionAsString.append(occurrence);
                }
            }
        }

        return compositionAsString.toString();
    }

    @Override
    public AtomChain clone() {
        AtomChain result = new AtomChain(addition);
        for (AtomImpl atom : atomChain) {
            result.append(new AtomImpl(atom.getAtom(), atom.getIsotope()));
        }
        return result;
    }

    /**
     * Returns true of the given atomic chain consists of additions, i.e., all
     * atoms are added when calculating the mass, false, means that all atoms
     * are subtracted.
     *
     * @return true of the given atomic consists of additions
     */
    public Boolean getAddition() {
        return addition;
    }

    /**
     * Set if the given atomic chain consists of additions, i.e., all atoms are
     * added when calculating the mass, false, means that all atoms are
     * subtracted.
     *
     * @param addition true if the given atomic chain consists of additions
     */
    public void setAddition(Boolean addition) {
        this.addition = addition;
        mass = null;
        stringValue = null;
    }
}
