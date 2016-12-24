package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * A precursor ion.
 *
 * @author Marc Vaudel
 */
public class PrecursorIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2630586959372309153L;
    /**
     * For now only one type of precursor implemented.
     */
    public static final int PRECURSOR = 0;
    /**
     * The neutral losses found on the ion.
     */
    private NeutralLoss[] neutralLosses = null;
    /**
     * The CV term of the reporter ion, null if not set.
     */
    private CvTerm cvTerm = null;
    /**
     * The PSI MS CV term of the reporter ion, null if not set.
     */
    private CvTerm psiCvTerm = null;

    /**
     * Constructor.
     *
     * @param theoreticMass the theoretic mass
     * @param neutralLosses the neutral losses
     */
    public PrecursorIon(double theoreticMass, NeutralLoss[] neutralLosses) {
        this.neutralLosses = neutralLosses;
        type = IonType.PRECURSOR_ION;
        this.theoreticMass = theoreticMass;
    }

    /**
     * Constructor for a generic ion.
     *
     * @param neutralLosses the neutral losses
     */
    public PrecursorIon(NeutralLoss[] neutralLosses) {
        this.neutralLosses = neutralLosses;
        type = IonType.PRECURSOR_ION;
    }

    /**
     * Constructor for a generic ion without neutral losses.
     */
    public PrecursorIon() {
        type = IonType.PRECURSOR_ION;
    }

    /**
     * Constructor for a generic ion without neutral losses.
     *
     * @param theoreticMass the theoretic mass of the precursor
     */
    public PrecursorIon(double theoreticMass) {
        this.theoreticMass = theoreticMass;
        type = IonType.PRECURSOR_ION;
    }

    @Override
    public String getName() {
        return getSubTypeAsString() + getNeutralLossesAsString();
    }

    @Override
    public CvTerm getPrideCvTerm() {
        
        if (cvTerm != null) {
            return cvTerm;
        }
        
        if (neutralLosses == null || neutralLosses.length == 0) {
            cvTerm = new CvTerm("PSI-MS", "MS:1001523", "frag: precursor ion", "0");
        } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.H2O)) {
            cvTerm = new CvTerm("PSI-MS", "MS:1001521", "frag: precursor ion - H2O", "0");
        } else if (neutralLosses.length == 1 && neutralLosses[0].isSameAs(NeutralLoss.NH3)) {
            cvTerm = new CvTerm("PSI-MS", "MS:1001522", "frag: precursor ion - NH3", "0");
        }
        
        return cvTerm;
    }
    
    @Override
    public CvTerm getPsiMsCvTerm() {
        if (psiCvTerm != null) {
            return psiCvTerm;
        }
        psiCvTerm = new CvTerm("PSI-MS", "MS:1001523", "frag: precursor ion", null);
        return cvTerm;
    }

    @Override
    public int getSubType() {
        return PRECURSOR;
    }

    @Override
    public String getSubTypeAsString() {
        return "Prec";
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>();
        possibleTypes.add(PRECURSOR);
        return possibleTypes;
    }

    @Override
    public NeutralLoss[] getNeutralLosses() {
        return neutralLosses;
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.PRECURSOR_ION
                && anotherIon.getNeutralLossesAsString().equals(getNeutralLossesAsString());
    }
}
