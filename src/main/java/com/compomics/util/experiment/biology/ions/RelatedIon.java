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
 * @author Marc Vaudel
 */
public class RelatedIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -4605345486425465764L;
    /**
     * Subtype counter.
     */
    private static int subTypeCounter = 0;
    /**
     * Map of the implemented related ions.
     */
    private static HashMap<Character, ArrayList<RelatedIon>> implementedIons = new HashMap<Character, ArrayList<RelatedIon>>();
    /**
     * Related ion for R. Cyanamid and ammonia loss from immonium ion (C4H7N).
     */
    public final static RelatedIon RELATED_R_1 = new RelatedIon(AminoAcid.R, AtomChain.getAtomChain("C(4)H(7)N"), subTypeCounter++);
    /**
     * Related ion for R. Cyanamid loss from immonium ion (C4H10N2).
     */
    public final static RelatedIon RELATED_R_2 = new RelatedIon(AminoAcid.R, AtomChain.getAtomChain("C(4)H(10)N(2)"), subTypeCounter++);
    /**
     * Related ion for R. Ammonia loss from immonium ion (C5H9N3).
     */
    public final static RelatedIon RELATED_R_3 = new RelatedIon(AminoAcid.R, AtomChain.getAtomChain("C(5)H(9)N(3)"), subTypeCounter++);
    /**
     * Related ion for R (C5H10N2O).
     */
    public final static RelatedIon RELATED_R_4 = new RelatedIon(AminoAcid.R, AtomChain.getAtomChain("C(5)H(10)N(2)O"), subTypeCounter++);
    /**
     * Related ion for N. Ammonia loss from immonium ion (C3H3NO).
     */
    public final static RelatedIon RELATED_N_1 = new RelatedIon(AminoAcid.N, AtomChain.getAtomChain("C(3)H(3)NO"), subTypeCounter++);
    /**
     * Related ion for D. water loss from immonium ion (C3H3NO).
     */
    public final static RelatedIon RELATED_D_1 = new RelatedIon(AminoAcid.D, AtomChain.getAtomChain("C(3)H(3)NO"), subTypeCounter++);
    /**
     * Related ion for C. Carbamidomethylated immonium ion (C4H8N2SO).
     */
    public final static RelatedIon RELATED_C_1 = new RelatedIon(AminoAcid.C, AtomChain.getAtomChain("C(4)H(8)N(2)SO"), subTypeCounter++);
    /**
     * Related ion for C. Carbamidomethylated and ammonia loss from immonium ion
     * (C4H5NSO).
     */
    public final static RelatedIon RELATED_C_2 = new RelatedIon(AminoAcid.C, AtomChain.getAtomChain("C(4)H(5)NSO"), subTypeCounter++);
    /**
     * Related ion for E. Water loss from immonium ion (C4H5NO).
     */
    public final static RelatedIon RELATED_E_1 = new RelatedIon(AminoAcid.E, AtomChain.getAtomChain("C(4)H(5)NO"), subTypeCounter++);
    /**
     * Related ion for Q. Ammonia loss from immonium ion (C4H5NO).
     */
    public final static RelatedIon RELATED_Q_1 = new RelatedIon(AminoAcid.Q, AtomChain.getAtomChain("C(4)H(5)NO"), subTypeCounter++);
    /**
     * Related ion for Q. Internal b-ion (C5H8N2O2).
     */
    public final static RelatedIon RELATED_Q_2 = new RelatedIon(AminoAcid.Q, AtomChain.getAtomChain("C(5)H(8)N(2)O(2)"), subTypeCounter++);
    /**
     * Related ion for H (C4H5N2).
     */
    public final static RelatedIon RELATED_H_1 = new RelatedIon(AminoAcid.H, AtomChain.getAtomChain("C(4)H(5)N(2)"), subTypeCounter++);
    /**
     * Related ion for H. Internal b-ion (C6H7N3O).
     */
    public final static RelatedIon RELATED_H_2 = new RelatedIon(AminoAcid.H, AtomChain.getAtomChain("C(6)H(7)N(3)O"), subTypeCounter++);
    /**
     * Related ion for K. Ammonia loss from immonium ion (C5H9N).
     */
    public final static RelatedIon RELATED_K_1 = new RelatedIon(AminoAcid.K, AtomChain.getAtomChain("C(5)H(9)N"), subTypeCounter++);
    /**
     * Related ion for K. Ammonia loss from internal b-ion (C6H9NO).
     */
    public final static RelatedIon RELATED_K_2 = new RelatedIon(AminoAcid.K, AtomChain.getAtomChain("C(6)H(9)NO"), subTypeCounter++);
    /**
     * Related ion for K. Internal b-ion (C6H12N2O).
     */
    public final static RelatedIon RELATED_K_3 = new RelatedIon(AminoAcid.K, AtomChain.getAtomChain("C(6)H(12)N(2)O"), subTypeCounter++);
    /**
     * Related ion for M (C2H4S).
     */
    public final static RelatedIon RELATED_M_1 = new RelatedIon(AminoAcid.M, AtomChain.getAtomChain("C(2)H(4)S"), subTypeCounter++);
    /**
     * Related ion for F. Tropylium ion (C7H6).
     */
    public final static RelatedIon RELATED_F_1 = new RelatedIon(AminoAcid.F, AtomChain.getAtomChain("C(7)H(6)"), subTypeCounter++);
    /**
     * Related ion for W (C9H7N).
     */
    public final static RelatedIon RELATED_W_1 = new RelatedIon(AminoAcid.W, AtomChain.getAtomChain("C(9)H(7)N"), subTypeCounter++);
    /**
     * Related ion for Y (C7H7O).
     */
    public final static RelatedIon RELATED_Y_1 = new RelatedIon(AminoAcid.Y, AtomChain.getAtomChain("C(7)H(7)O"), subTypeCounter);
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
            Character aminoAcidChar = aminoAcidTarget.getSingleLetterCodeAsChar();
            ArrayList<RelatedIon> relatedIons = implementedIons.get(aminoAcidChar);
            if (relatedIons == null) {
                relatedIons = new ArrayList<RelatedIon>(1);
                implementedIons.put(aminoAcidTarget.getSingleLetterCodeAsChar(), relatedIons);
            }
            relatedIons.add(this);
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
        // return new CvTerm("PSI-MS", "MS:100????", "frag: related ion", null);
        return null;
    }

    @Override
    public CvTerm getPsiMsCvTerm() {
        // @TODO: implement when the required cv terms are available
        // return new CvTerm("PSI-MS", "MS:100????", "frag: related ion", null);
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
        for (int i = 0; i < subTypeCounter; i++) {
            possibleTypes.add(i);
        }
        return possibleTypes;
    }

    @Override
    public ArrayList<NeutralLoss> getNeutralLosses() {
        return new ArrayList<NeutralLoss>(0);
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
