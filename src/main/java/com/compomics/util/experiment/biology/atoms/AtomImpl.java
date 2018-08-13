package com.compomics.util.experiment.biology.atoms;


/**
 * Class for a specific atom.
 *
 * @author Marc Vaudel
 */
public class AtomImpl {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 3269643086590455656L;
    /**
     * The reference atom.
     * 
     * @deprecated use the atom name instead.
     */
    private Atom atom;
    /**
     * The reference atom symbol.
     */
    private String atomSymbol;
    /**
     * The isotope, 0 for monoisotope.
     */
    private Integer isotope;

    /**
     * Constructor.
     *
     * @param atomSymbol the symbol of the atom
     * @param isotope the isotope, 0 for monoisotope
     */
    public AtomImpl(String atomSymbol, Integer isotope) {
        this.atomSymbol = atomSymbol;
        this.isotope = isotope;
    }

    /**
     * Constructor.
     *
     * @param atom the reference atom
     * @param isotope the isotope, 0 for monoisotope
     */
    public AtomImpl(Atom atom, Integer isotope) {
        this.atomSymbol = atom.getLetter();
        this.isotope = isotope;
    }

    /**
     * Returns the mass of the atom. Null if not implemented.
     *
     * @return the mass of the atom
     */
    public Double getMass() {
        Atom atom = Atom.getAtom(atomSymbol);
        return atom.getIsotopeMass(isotope);
    }
    
    /**
     * Returns the isotope number corresponding to the given rounded mass. e.g. returns +1 for 13 if the atom is C. Null if no isotope was found.
     * 
     * @param roundedMass the rounded mass as integer
     * 
     * @return the isotope number
     */
    public Integer getIsotopeNumber(Integer roundedMass) {
        Atom atom = Atom.getAtom(atomSymbol);
        for (Integer isotopeNumber : atom.getImplementedIsotopes()) {
            Double isotopeMass = atom.getIsotopeMass(isotopeNumber);
            Integer isotopeRoundedMass = (int) Math.round(isotopeMass);
            if (roundedMass.equals(isotopeRoundedMass)) {
                return isotopeNumber;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        Atom atom = Atom.getAtom(atomSymbol);
        if (isotope == 0) {
            return atom.getLetter();
        } else {
            if (getMass() == null) {
                throw new UnsupportedOperationException("Isotope " + isotope + " not implemented for atom " + atom + ".");
            }
            return Math.round(getMass()) + atom.getLetter();
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
     * Returns the atom symbol as specified in the Atom class.
     * 
     * @return the atom symbol
     */
    public String getAtomSymbol() {
        if (atomSymbol == null) {
            atomSymbol = atom.getLetter();
        }
        return atomSymbol;
    }

    /**
     * Sets the atom symbol as specified in the Atom class.
     * 
     * @param atomSymbol the atom symbol
     */
    public void setAtomSymbol(String atomSymbol) {
        this.atomSymbol = atomSymbol;
    }

    /**
     * Returns the isotope, 0 for monoisotope.
     *
     * @return the isotope
     */
    public Integer getIsotope() {
        return isotope;
    }

    /**
     * Sets the isotope, 0 for monoisotope.
     * 
     * @param isotope the isotope
     */
    public void setIsotope(Integer isotope) {
        this.isotope = isotope;
    }
    
}
