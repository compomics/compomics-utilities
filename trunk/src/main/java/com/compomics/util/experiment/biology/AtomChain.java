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
     * Creates an empty atom chain.
     */
    public AtomChain() {
        atomChain = new ArrayList<AtomImpl>(4);
    }

    /**
     * Constructor for a monoisotopic chain as a string, e.g. C3PO.
     *
     * @param chain the atomic chain as a string
     * @throws IllegalArgumentException if an illegal atom is used
     */
    public AtomChain(String chain) throws IllegalArgumentException {
        atomChain = new ArrayList<AtomImpl>(chain.length());
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
    private void estimateMass() {
        mass = 0.0;
        for (AtomImpl atom : atomChain) {
            mass += atom.getMass();
        }
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
    }

    /**
     * Indicates whether two atom chains are of the same composition by
     * comparing their string.
     *
     * @param atomChain another atom chain
     *
     * @return a boolean indicating whether two atom chains are of the same
     * composition
     */
    public boolean isSameCompositionAs(AtomChain atomChain) {
        if (atomChain == null) {
            return false;
        }
        return atomChain.toString().equals(toString());
    }

    @Override
    public String toString() {

        HashMap<String, Integer> composition = new HashMap<String, Integer>();
        for (AtomImpl atom : atomChain) {
            String name = atom.toString();
            Integer occurrence = composition.get(name);
            if (occurrence == null) {
                occurrence = 0;
            }
            composition.put(name, occurrence + 1);
        }

        ArrayList<String> names = new ArrayList<String>(composition.keySet());
        Collections.sort(names);
        StringBuilder result = new StringBuilder(names.size());
        for (String name : names) {
            Integer occurrence = composition.get(name);
            result.append(name);
            if (occurrence > 1) {
                result.append(occurrence);
            }
        }

        return result.toString();
    }

    @Override
    public AtomChain clone() {
        AtomChain result = new AtomChain();
        for (AtomImpl atom : atomChain) {
            result.append(new AtomImpl(atom.getAtom(), atom.getIsotope()));
        }
        return result;
    }
}
