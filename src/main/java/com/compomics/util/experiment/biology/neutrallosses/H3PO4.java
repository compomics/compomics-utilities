/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.biology.neutrallosses;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.NeutralLoss;

/**
 * A phosphorylation loss, likely to be found on fragments from phosphorylated peptides on Y.
 *
 * @author marc
 */
public class H3PO4 extends NeutralLoss {
    
    /**
     * Constructor
     */
    public H3PO4() {
        this.name = "H3PO4";
        this.mass = 3*Atom.H.mass + Atom.P.mass + 4*Atom.O.mass;
    }
}
