/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.biology.neutrallosses;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.NeutralLoss;

/**
 * A nitrium loss, likely to be found on fragments from peptides containing the amino-acid K, N, Q or R.
 *
 * @author marc
 */
public class NH3 extends NeutralLoss {
    
    /**
     * Constructor
     */
    public NH3() {
        this.name = "NH3";
        this.mass = Atom.N.mass + 3*Atom.H.mass;
    }
}
