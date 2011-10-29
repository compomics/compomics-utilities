package com.compomics.util.experiment.biology.neutrallosses;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.NeutralLoss;

/**
 * Neutral loss CH4OS, likely to be encountered on fragment from peptides containing an oxidized methionine.
 *
 * @author marc
 */
public class CH4OS extends NeutralLoss {

    /**
     * Constructor
     */
    public CH4OS() {
        this.name = "CH4OS";
        this.mass = Atom.C.mass + 4 * Atom.H.mass + Atom.O.mass + Atom.S.mass;
    }
}
