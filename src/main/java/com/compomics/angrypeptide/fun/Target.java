/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide.fun;

/**
 * This class represents a target
 *
 * @author Marc
 */
public class Target {
    
    /**
     * The position of the target
     */
    private double position;
    
    /**
     * The value of the target
     */
    private double value;
/**
 * Constructor
 * @param position the position
 * @param value the initial value
 */
    public Target(double position, double value) {
        this.position = position;
        this.value = value;
    }
    /**
     * Returns the position of the target
     * @return 
     */
    public double getPosition() {
        return position;
    }

    /**
     * Sets the position of the target
     * @param position 
     */
    public void setPosition(double position) {
        this.position = position;
    }

    /**
     * Returns the value of the target
     * @return 
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value of the target
     * @param value 
     */
    public void setValue(double value) {
        this.value = value;
    }
    
    /**
     * Increments the value by the given amount
     * @param increment the value to add
     */
    public void addValue(double increment) {
        this.value += increment;
    }
    
}
