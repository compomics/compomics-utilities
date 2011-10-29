package com.compomics.util.experiment.biology.neutrallosses;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.NeutralLoss;

/**
 * A phosphorylation loss, likely to be found on fragments from phosphorylated peptides on S or T.
 *
 * @author marc
 */
public class HPO3 extends NeutralLoss {

    /**
     * Constructor
     */
    public HPO3() {
        this.name = "HPO3";
        this.mass = Atom.H.mass + Atom.P.mass + 3 * Atom.O.mass;
    }
}
