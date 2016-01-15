package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a related ion, i.e., an ion that is related to a given amino acid,
 * and is its own factory.
 *
 * @author Harald Barsnes
 */
public class RelatedIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -4605345486425465764L;
    /**
     * Map of the implemented related ions.
     */
    private static HashMap<Character, ArrayList<RelatedIon>> implementedIons = new HashMap<Character, ArrayList<RelatedIon>>();
    /**
     * Related ion for R. Cyanamid and ammonia loss from immonium ion (C4H7N).
     */
    public final static RelatedIon RELATED_R_1 = new RelatedIon(AminoAcid.R, new AtomChain("C4H7N"), 0);
    /**
     * Related ion for R. Cyanamid loss from immonium ion (C4H10N2).
     */
    public final static RelatedIon RELATED_R_2 = new RelatedIon(AminoAcid.R, new AtomChain("C4H10N2"), 1);
    /**
     * Related ion for R. Ammonia loss from immonium ion (C5H9N3).
     */
    public final static RelatedIon RELATED_R_3 = new RelatedIon(AminoAcid.R, new AtomChain("C5H9N3"), 2);
    /**
     * Related ion for R (C5H10N2O).
     */
    public final static RelatedIon RELATED_R_4 = new RelatedIon(AminoAcid.R, new AtomChain("C5H10N2O"), 3);
    /**
     * Related ion for N. Ammonia loss from immonium ion (C3H3NO).
     */
    public final static RelatedIon RELATED_N_1 = new RelatedIon(AminoAcid.N, new AtomChain("C3H3NO"), 4);
    /**
     * Related ion for D. water loss from immonium ion (C3H3NO).
     */
    public final static RelatedIon RELATED_D_1 = new RelatedIon(AminoAcid.D, new AtomChain("C3H3NO"), 5);
    /**
     * Related ion for C. Carbamidomethylated immonium ion (C4H8N2SO).
     */
    public final static RelatedIon RELATED_C_1 = new RelatedIon(AminoAcid.C, new AtomChain("C4H8N2SO"), 6);
    /**
     * Related ion for C. Carbamidomethylated and ammonia loss from immonium ion
     * (C4H5NSO).
     */
    public final static RelatedIon RELATED_C_2 = new RelatedIon(AminoAcid.C, new AtomChain("C4H5NSO"), 7);
    /**
     * Related ion for E. Water loss from immonium ion (C4H5NO).
     */
    public final static RelatedIon RELATED_E_1 = new RelatedIon(AminoAcid.E, new AtomChain("C4H5NO"), 8);
    /**
     * Related ion for Q. Ammonia loss from immonium ion (C4H5NO).
     */
    public final static RelatedIon RELATED_Q_1 = new RelatedIon(AminoAcid.Q, new AtomChain("C4H5NO"), 9);
    /**
     * Related ion for Q. Internal b-ion (C5H8N2O2).
     */
    public final static RelatedIon RELATED_Q_2 = new RelatedIon(AminoAcid.Q, new AtomChain("C5H8N2O2"), 10);
    /**
     * Related ion for H (C4H5N2).
     */
    public final static RelatedIon RELATED_H_1 = new RelatedIon(AminoAcid.H, new AtomChain("C4H5N2"), 11);
    /**
     * Related ion for H. Internal b-ion (C6H7N3O).
     */
    public final static RelatedIon RELATED_H_2 = new RelatedIon(AminoAcid.H, new AtomChain("C6H7N3O"), 12);
    /**
     * Related ion for K. Ammonia loss from immonium ion (C5H9N).
     */
    public final static RelatedIon RELATED_K_1 = new RelatedIon(AminoAcid.K, new AtomChain("C5H9N"), 13);
    /**
     * Related ion for K. Ammonia loss from internal b-ion (C6H9NO).
     */
    public final static RelatedIon RELATED_K_2 = new RelatedIon(AminoAcid.K, new AtomChain("C6H9NO"), 14);
    /**
     * Related ion for K. Internal b-ion (C6H12N2O).
     */
    public final static RelatedIon RELATED_K_3 = new RelatedIon(AminoAcid.K, new AtomChain("C6H12N2O"), 15);
    /**
     * Related ion for M (C2H4S).
     */
    public final static RelatedIon RELATED_M_1 = new RelatedIon(AminoAcid.M, new AtomChain("C2H4S"), 16);
    /**
     * Related ion for F. Tropylium ion (C7H6).
     */
    public final static RelatedIon RELATED_F_1 = new RelatedIon(AminoAcid.F, new AtomChain("C7H6"), 17);
    /**
     * Related ion for W (C9H7N).
     */
    public final static RelatedIon RELATED_W_1 = new RelatedIon(AminoAcid.W, new AtomChain("C9H7N"), 18);
    /**
     * Related ion for Y (C7H7O).
     */
    public final static RelatedIon RELATED_Y_1 = new RelatedIon(AminoAcid.Y, new AtomChain("C7H7O"), 19);
    /**
     * The amino acid target.
     */
    private AminoAcid aminoAcidTarget;
    /**
     * The sub type.
     */
    private int subType;

    /**
     * Constructor for a related ion.
     *
     * @param aminoAcidTarget the amino acid target
     * @param atomChain the atomic composition of this ion
     * @param subType the ion subtype
     */
    public RelatedIon(AminoAcid aminoAcidTarget, AtomChain atomChain, int subType) {
        this(aminoAcidTarget, atomChain, subType, true);
    }

    /**
     * Constructor for a related ion.
     *
     * @param aminoAcidTarget the amino acid target
     * @param atomChain the atomic composition of this ion
     * @param subType the ion subtype
     * @param save if true the related ion will be saved in the static map for
     * later reuse
     */
    public RelatedIon(AminoAcid aminoAcidTarget, AtomChain atomChain, int subType, boolean save) {
        type = IonType.RELATED_ION;
        this.aminoAcidTarget = aminoAcidTarget;
        this.atomChain = atomChain;
        this.subType = subType;
        if (save) {
            ArrayList<RelatedIon> relatedIons = implementedIons.get(aminoAcidTarget.getSingleLetterCodeAsChar());
            if (relatedIons == null) {
                relatedIons = new ArrayList<RelatedIon>(1);
            }
            relatedIons.add(this);
            implementedIons.put(aminoAcidTarget.getSingleLetterCodeAsChar(), relatedIons);
        }
    }

    /**
     * Returns the list of related ions for the given amino acid.
     *
     * @param aminoAcidTarget the amino acid target
     * @return the list of related ions for the given amino acid
     */
    public static ArrayList<RelatedIon> getRelatedIons(AminoAcid aminoAcidTarget) {
        ArrayList<RelatedIon> relatedIons = implementedIons.get(aminoAcidTarget.getSingleLetterCodeAsChar());
        if (relatedIons == null) {
            relatedIons = new ArrayList<RelatedIon>(1);
        }
        return relatedIons;
    }

    @Override
    public String getName() {
        return "r" + aminoAcidTarget.singleLetterCode;
    }

    @Override
    public CvTerm getPrideCvTerm() {
        // @TODO: implement when the required cv terms are available
        return null;
    }

    /**
     * Compares the current related ion with another one based on their
     * composition.
     *
     * @param anotherRelatedIon the other related ion
     * @return a boolean indicating whether compositions are equal
     */
    public boolean isSameAs(RelatedIon anotherRelatedIon) {
        return atomChain.isSameCompositionAs(anotherRelatedIon.getAtomicComposition());
    }

    @Override
    public int getSubType() {
        return subType;
    }

    @Override
    public String getSubTypeAsString() {
        return "r" + subType;
    }

    /**
     * Returns an arraylist of possible subtypes.
     *
     * @return an arraylist of possible subtypes
     */
    public static ArrayList<Integer> getPossibleSubtypes() {
        ArrayList<Integer> possibleTypes = new ArrayList<Integer>();
        for (int i = 0; i < implementedIons.size(); i++) {
            possibleTypes.add(i);
        }
        return possibleTypes;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return new ArrayList<NeutralLoss>();
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        if (anotherIon instanceof RelatedIon) {
            RelatedIon otherIon = (RelatedIon) anotherIon;
            return isSameAs(otherIon);
        }
        return false;
    }
}
