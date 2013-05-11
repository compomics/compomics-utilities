/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide.fun;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the targets.
 *
 * @author Marc
 */
public class Targets {
    
    /**
     * position to target map: position -> target
     */
    private HashMap<Double, Target> positionToTargetMap = new HashMap<Double, Target>();
    
    /**
     * Constructor
     */
    public Targets() {
        
    }
    
    /**
     * Adds a target to the model
     * @param target 
     */
    public void addTarget(double position, double value) {
        Target target = positionToTargetMap.get(position);
        if (target == null) {
            target = new Target(position, value);
            positionToTargetMap.put(position, target);
        } else {
            target.addValue(value);
        }
    }
    
    /**
     * Returns the number of targets
     * @return 
     */
    public int getNTargets() {
        return positionToTargetMap.size();
    }
    
    /**
     * Returns all possible target values
     * @return 
     */
    public ArrayList<Double> getPositions() {
        return new ArrayList<Double>(positionToTargetMap.keySet());
    }
    
    /**
     * Returns the target associated to a position. Null if none.
     * 
     * @param value
     * @return 
     */
    public Target getTargetAtPosition(double position) {
        return positionToTargetMap.get(position);
    }
    
}
