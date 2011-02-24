package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.atoms.Carbon;
import com.compomics.util.experiment.biology.atoms.Hydrogen;
import com.compomics.util.experiment.biology.atoms.Nitrogen;
import com.compomics.util.experiment.biology.atoms.Oxygen;

/**
 * This interface contains information about atoms
 *
 * @author Marc
 */
public abstract class Atom {

    public static final Atom H = new Hydrogen();
    public static final Atom N = new Nitrogen();
    public static final Atom O = new Oxygen();
    public static final Atom C = new Carbon();

    public double mass;
    public String name;
    public String letter;
}
