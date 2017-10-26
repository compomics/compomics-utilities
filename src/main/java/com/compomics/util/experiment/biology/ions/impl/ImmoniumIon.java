package com.compomics.util.experiment.biology.ions.impl;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.mass_spectrometry.utils.StandardMasses;
import com.compomics.util.pride.CvTerm;
import java.util.ArrayList;

/**
 * Represents an immonium ion.
 *
 * @author Marc Vaudel
 */
public class ImmoniumIon extends Ion {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -3403620196563864756L;
    /**
     * Alanine immonium ion.
     */
    public static final ImmoniumIon ALANINE = new ImmoniumIon(0, 'A');
    /**
     * Arginine immonium ion.
     */
    public static final ImmoniumIon ARGININE = new ImmoniumIon(1, 'R');
    /**
     * Asparagine immonium ion.
     */
    public static final ImmoniumIon ASPARAGINE = new ImmoniumIon(2, 'N');
    /**
     * Aspartic acid immonium ion.
     */
    public static final ImmoniumIon ASPARTIC_ACID = new ImmoniumIon(3, 'D');
    /**
     * Cysteine immonium ion.
     */
    public static final ImmoniumIon CYSTEINE = new ImmoniumIon(4, 'C');
    /**
     * Glutamic acid immonium ion.
     */
    public static final ImmoniumIon GLUTAMIC_ACID = new ImmoniumIon(5, 'E');
    /**
     * Glutamine immonium ion.
     */
    public static final ImmoniumIon GLUTAMINE = new ImmoniumIon(6, 'Q');
    /**
     * Glycine immonium ion.
     */
    public static final ImmoniumIon GLYCINE = new ImmoniumIon(7, 'G');
    /**
     * Histidine immonium ion.
     */
    public static final ImmoniumIon HISTIDINE = new ImmoniumIon(8, 'H');
    /**
     * Isoleucine immonium ion.
     */
    public static final ImmoniumIon ISOLEUCINE = new ImmoniumIon(9, 'I');
    /**
     * Leucine immonium ion.
     */
    public static final ImmoniumIon LEUCINE = new ImmoniumIon(10, 'L');
    /**
     * Lysine immonium ion.
     */
    public static final ImmoniumIon LYSINE = new ImmoniumIon(11, 'K');
    /**
     * Methionine immonium ion.
     */
    public static final ImmoniumIon METHIONINE = new ImmoniumIon(12, 'M');
    /**
     * Phenylananine immonium ion.
     */
    public static final ImmoniumIon PHENYLALANINE = new ImmoniumIon(13, 'F');
    /**
     * Proline immonium ion.
     */
    public static final ImmoniumIon PROLINE = new ImmoniumIon(14, 'P');
    /**
     * Selenocysteine immonium ion.
     */
    public static final ImmoniumIon SELENOCYSTEINE = new ImmoniumIon(15, 'U');
    /**
     * Serine immonium ion.
     */
    public static final ImmoniumIon SERINE = new ImmoniumIon(16, 'S');
    /**
     * Threonine immonium ion.
     */
    public static final ImmoniumIon THREONINE = new ImmoniumIon(17, 'T');
    /**
     * Tryptophan immonium ion.
     */
    public static final ImmoniumIon TRYPTOPHAN = new ImmoniumIon(18, 'W');
    /**
     * Tyrosine immonium ion.
     */
    public static final ImmoniumIon TYROSINE = new ImmoniumIon(19, 'Y');
    /**
     * Valine immonium ion.
     */
    public static final ImmoniumIon VALINE = new ImmoniumIon(21, 'V');
    /**
     * Subtype of immonium ion.
     */
    private final int subType;
    /**
     * The amino acid that can generate this ion as single letter code.
     */
    public final char aa;
    /**
     * The PSI CV term.
     */
    private static final CvTerm psiCvTerm = new CvTerm("PSI-MS", "MS:1001239", "frag: immonium ion", null);

    /**
     * Constructor for an immonium ion.
     *
     * @param subType the type of immonium ion as integer as indexed by the
     * static fields
     */
    private ImmoniumIon(int subType, char aa) {
        type = IonType.IMMONIUM_ION;
        this.aa = aa;
        this.subType = subType;
        AminoAcid currentAA = AminoAcid.getAminoAcid(aa);
        theoreticMass1 = currentAA.getMonoisotopicMass() - StandardMasses.co.mass;
    }

    /**
     * Returns the immonium ion corresponding to the given subtype.
     *
     * @param subType the subtype
     * 
     * @return the immonium ion 
     */
    public static ImmoniumIon getImmoniumIon(int subType) {
        switch (subType) {
            case 0:
                return ALANINE;
            case 1:
                return ARGININE;
            case 2:
                return ASPARAGINE;
            case 3:
                return ASPARTIC_ACID;
            case 4:
                return CYSTEINE;
            case 5:
                return GLUTAMIC_ACID;
            case 6:
                return GLUTAMINE;
            case 7:
                return GLYCINE;
            case 8:
                return HISTIDINE;
            case 9:
                return ISOLEUCINE;
            case 10:
                return LEUCINE;
            case 11:
                return LYSINE;
            case 12:
                return METHIONINE;
            case 13:
                return PHENYLALANINE;
            case 14:
                return PROLINE;
            case 15:
                return SELENOCYSTEINE;
            case 16:
                return SERINE;
            case 17:
                return THREONINE;
            case 18:
                return TRYPTOPHAN;
            case 19:
                return TYROSINE;
            case 20:
                return VALINE;
            default:
                throw new UnsupportedOperationException("No immonium ion implemented for subtype " + subType + ".");
        }
    }

    /**
     * Returns the immonium ion produced by the given amino acid.
     *
     * @param residue the amino acid as char
     * 
     * @return the immonium ion 
     */
    public static ImmoniumIon getImmoniumIon(char residue) {
        switch (residue) {
            case 'A':
                return ALANINE;
            case 'C':
                return CYSTEINE;
            case 'D':
                return ASPARTIC_ACID;
            case 'E':
                return GLUTAMIC_ACID;
            case 'F':
                return PHENYLALANINE;
            case 'G':
                return GLYCINE;
            case 'H':
                return HISTIDINE;
            case 'I':
                return ISOLEUCINE;
            case 'K':
                return LYSINE;
            case 'L':
                return LEUCINE;
            case 'M':
                return METHIONINE;
            case 'N':
                return ASPARAGINE;
            case 'P':
                return PROLINE;
            case 'Q':
                return GLUTAMINE;
            case 'R':
                return ARGININE;
            case 'S':
                return SERINE;
            case 'T':
                return THREONINE;
            case 'U':
                return SELENOCYSTEINE;
            case 'V':
                return VALINE;
            case 'W':
                return TRYPTOPHAN;
            case 'Y':
                return TYROSINE;
            default:
                throw new UnsupportedOperationException("No immonium ion implemented for amino acid " + residue + ".");
        }
    }

    @Override
    public String getName() {
        return "i" + aa;
    }

    @Override
    public CvTerm getPrideCvTerm() {
        // @TODO: replace by MS:1001239? 
        //        will result in issues for the PRIDE XML export 
        //        and also has implications for the mzid export as all immonium ions will 
        //        get the same cv term and this end up being group togeher when iterating 
        //        the terms in the writeSpectrumIdentificationResult method

        switch (aa) {
            case 'A':
                return new CvTerm("PRIDE", "PRIDE:0000240", "immonium A", "0");
            case 'C':
                return new CvTerm("PRIDE", "PRIDE:0000241", "immonium C", "0");
            case 'D':
                return new CvTerm("PRIDE", "PRIDE:0000242", "immonium D", "0");
            case 'E':
                return new CvTerm("PRIDE", "PRIDE:0000243", "immonium E", "0");
            case 'f':
                return new CvTerm("PRIDE", "PRIDE:0000244", "immonium F", "0");
            case 'G':
                return new CvTerm("PRIDE", "PRIDE:0000245", "immonium G", "0");
            case 'H':
                return new CvTerm("PRIDE", "PRIDE:0000246", "immonium H", "0");
            case 'I':
                return new CvTerm("PRIDE", "PRIDE:0000247", "immonium I", "0");
            case 'K':
                return new CvTerm("PRIDE", "PRIDE:0000248", "immonium K", "0");
            case 'L':
                return new CvTerm("PRIDE", "PRIDE:0000249", "immonium L", "0");
            case 'M':
                return new CvTerm("PRIDE", "PRIDE:0000250", "immonium M", "0");
            case 'N':
                return new CvTerm("PRIDE", "PRIDE:0000251", "immonium N", "0");
            case 'P':
                return new CvTerm("PRIDE", "PRIDE:0000252", "immonium P", "0");
            case 'Q':
                return new CvTerm("PRIDE", "PRIDE:0000253", "immonium Q", "0");
            case 'R':
                return new CvTerm("PRIDE", "PRIDE:0000254", "immonium R", "0");
            case 'S':
                return new CvTerm("PRIDE", "PRIDE:0000255", "immonium S", "0");
            case 'T':
                return new CvTerm("PRIDE", "PRIDE:0000256", "immonium T", "0");
            case 'V':
                return new CvTerm("PRIDE", "PRIDE:0000257", "immonium V", "0");
            case 'W':
                return new CvTerm("PRIDE", "PRIDE:0000258", "immonium W", "0");
            case 'Y':
                return new CvTerm("PRIDE", "PRIDE:0000259", "immonium Y", "0");
                default:
                    return null;
        }
    }

    @Override
    public CvTerm getPsiMsCvTerm() {
        return psiCvTerm;
    }

    @Override
    public int getSubType() {
        return subType;
    }

    @Override
    public String getSubTypeAsString() {
        return "Immonium " + aa;
    }

    /**
     * Returns the possible subtypes.
     *
     * @return the possible subtypes
     */
    public static int[] getPossibleSubtypes() {
        int[] possibleTypes = new int[21];
        
        for (int i = 0 ; i <= 20 ; i++) {
            
            possibleTypes[i] = i;
        
        }
        
        return possibleTypes;
    }

    @Override
    public NeutralLoss[] getNeutralLosses() {
        return null;
    }

    @Override
    public boolean isSameAs(Ion anotherIon) {
        return anotherIon.getType() == IonType.IMMONIUM_ION
                && anotherIon.getSubType() == subType;
    }
}
