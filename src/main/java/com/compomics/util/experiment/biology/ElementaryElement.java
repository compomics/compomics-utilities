/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.biology;

/**
 * This class can be used to retrieve elementary elements like a proton
 *
 * @author Marc
 */
public class ElementaryElement {
    
    /**
     * The name of the ion
     */
    private String name;
    
    /*
     * Ion attribute - the theoretic mass
     */
    private double theoreticMass;
    /**
     * A proton
     */
    public static final ElementaryElement neutron = new ElementaryElement("Neutron", 1.00866491600);
    /**
     * Constructor
     * 
     * @param name the name of the element
     * @param theoreticMass the mass of the element
     */
    private ElementaryElement(String name, double theoreticMass) {
        this.name = name;
        this.theoreticMass = theoreticMass;
    }
    
    /**
     * Returns the name of the element
     * @return the name of the element
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the mass of the element
     * @return the mass of the element
     */
    public double getMass() {
        return theoreticMass;
    }
    
}
