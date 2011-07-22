/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.biology.neutrallosses;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.NeutralLoss;

/**
 * A water loss, likely to be found on fragments from peptides containing the amino-acid D, E, S or T
 *
 * @author marc
 */
public class H2O extends NeutralLoss {
    
    /**
     * Constructor
     */
    public H2O() {
        this.name = "H2O";
        this.mass = 2*Atom.H.mass + Atom.O.mass;
    }
}
