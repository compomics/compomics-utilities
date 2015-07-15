package com.compomics.util.experiment.biology;

import com.compomics.util.Util;
import java.io.Serializable;

/**
 * Class for a specific atom
 *
 * @author Marc Vaudel
 */
public class AtomImpl implements Serializable {
    
    /**
     * The reference atom.
     */
    private Atom atom;
    /**
     * The isotope
     */
    private Integer isotope;
    
    /**
     * Constructor.
     * 
     * @param atom the reference atom
     * @param isotope the isotope
     */
    public AtomImpl(Atom atom, Integer isotope) {
        this.atom = atom;
        this.isotope = isotope;
    }
    
    /**
     * Returns the mass of the atom.
     * 
     * @return the mass of the atom
     */
    public Double getMass() {
        return atom.getIsotopeMass(isotope);
    }
    
    @Override
    public String toString() {
        if (isotope == 0) {
            return atom.getLetter();
        } else {
            return atom.getLetter() + "(" + getMass().intValue() + ")";
        }
    }
    
    /**
     * Indicates whether another atom is the same as this one.
     * 
     * @param anotherAtom another atom of interest
     * 
     * @return a boolean indicating whether another atom is the same as this one
     */
    public boolean isSameAs(AtomImpl anotherAtom) {
        return toString().equals(anotherAtom.toString());
    }

    /**
     * Returns the atom.
     * 
     * @return the atom
     */
    public Atom getAtom() {
        return atom;
    }

    /**
     * Returns the Isotope.
     * 
     * @return the Isotope
     */
    public Integer getIsotope() {
        return isotope;
    }

}
