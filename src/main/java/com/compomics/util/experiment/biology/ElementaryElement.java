package com.compomics.util.experiment.biology;

/**
 * This class can be used to retrieve elementary elements like a neutron.
 *
 * @author Marc Vaudel
 */
public class ElementaryElement {

    /**
     * The name of the element.
     */
    private String name;
    /*
     * The theoretic mass.
     */
    private double theoreticMass;
    /**
     * A neutron.
     */
    public static final ElementaryElement neutron = new ElementaryElement("Neutron", 1.00866491600);
    /**
     * Cache for the multiples of the neutron mass.
     */
    public static double[] neutronMassMultiples = {0.0, 
        neutron.theoreticMass,
        2* neutron.theoreticMass,
        3* neutron.theoreticMass,
        4* neutron.theoreticMass,
        5* neutron.theoreticMass,
        6* neutron.theoreticMass,
        7* neutron.theoreticMass,
        8* neutron.theoreticMass,
        9* neutron.theoreticMass,
        10* neutron.theoreticMass};
    
    /**
     * Returns the mass of the neutron multiplied by i. If i is smaller or equal to ten a value in cache is used. It is calculated otherwise. Throws an exception for negative i without sanity check.
     * 
     * @param i i
     * 
     * @return the mass of the proton multiplied by i
     */
    public static double getNeutronMassMultiple(int i) {
        return i <= 10 ? neutronMassMultiples[i] : i * neutron.theoreticMass;
    }

    /**
     * Constructor.
     *
     * @param name the name of the element
     * @param theoreticMass the mass of the element
     */
    private ElementaryElement(String name, double theoreticMass) {
        this.name = name;
        this.theoreticMass = theoreticMass;
    }

    /**
     * Returns the name of the element.
     *
     * @return the name of the element
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the mass of the element.
     *
     * @return the mass of the element
     */
    public double getMass() {
        return theoreticMass;
    }
}
