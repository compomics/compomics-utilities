package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.aminoacids.impl.Proline;
import com.compomics.util.experiment.biology.aminoacids.impl.Glycine;
import com.compomics.util.experiment.biology.aminoacids.impl.GlutamicAcid;
import com.compomics.util.experiment.biology.aminoacids.impl.Phenylalanine;
import com.compomics.util.experiment.biology.aminoacids.impl.Isoleucine;
import com.compomics.util.experiment.biology.aminoacids.impl.B;
import com.compomics.util.experiment.biology.aminoacids.impl.Selenocysteine;
import com.compomics.util.experiment.biology.aminoacids.impl.AsparticAcid;
import com.compomics.util.experiment.biology.aminoacids.impl.Valine;
import com.compomics.util.experiment.biology.aminoacids.impl.Tyrosine;
import com.compomics.util.experiment.biology.aminoacids.impl.Methionine;
import com.compomics.util.experiment.biology.aminoacids.impl.Tryptophan;
import com.compomics.util.experiment.biology.aminoacids.impl.Serine;
import com.compomics.util.experiment.biology.aminoacids.impl.Arginine;
import com.compomics.util.experiment.biology.aminoacids.impl.Cysteine;
import com.compomics.util.experiment.biology.aminoacids.impl.Glutamine;
import com.compomics.util.experiment.biology.aminoacids.impl.Threonine;
import com.compomics.util.experiment.biology.aminoacids.impl.Leucine;
import com.compomics.util.experiment.biology.aminoacids.impl.Lysine;
import com.compomics.util.experiment.biology.aminoacids.impl.Histidine;
import com.compomics.util.experiment.biology.aminoacids.impl.Pyrrolysine;
import com.compomics.util.experiment.biology.aminoacids.impl.J;
import com.compomics.util.experiment.biology.aminoacids.impl.Alanine;
import com.compomics.util.experiment.biology.aminoacids.impl.Z;
import com.compomics.util.experiment.biology.aminoacids.impl.X;
import com.compomics.util.experiment.biology.aminoacids.impl.Asparagine;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing amino acids.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class AminoAcid implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -3158896310928354857L;
    
    public static final AminoAcid A = new Alanine();
    public static final AminoAcid C = new Cysteine();
    public static final AminoAcid D = new AsparticAcid();
    public static final AminoAcid E = new GlutamicAcid();
    public static final AminoAcid F = new Phenylalanine();
    public static final AminoAcid G = new Glycine();
    public static final AminoAcid H = new Histidine();
    public static final AminoAcid I = new Isoleucine();
    public static final AminoAcid K = new Lysine();
    public static final AminoAcid L = new Leucine();
    public static final AminoAcid M = new Methionine();
    public static final AminoAcid N = new Asparagine();
    public static final AminoAcid P = new Proline();
    public static final AminoAcid Q = new Glutamine();
    public static final AminoAcid R = new Arginine();
    public static final AminoAcid S = new Serine();
    public static final AminoAcid T = new Threonine();
    public static final AminoAcid V = new Valine();
    public static final AminoAcid W = new Tryptophan();
    public static final AminoAcid Y = new Tyrosine();
    public static final AminoAcid U = new Selenocysteine();
    public static final AminoAcid O = new Pyrrolysine();
    public static final AminoAcid B = new B();
    public static final AminoAcid J = new J();
    public static final AminoAcid Z = new Z();
    public static final AminoAcid X = new X();

    /**
     * Single letter code of the amino acid.
     */
    public String singleLetterCode;
    /**
     * Three letter code of the amino acid.
     */
    public String threeLetterCode;
    /**
     * Name of the amino acid.
     */
    public String name;
    /**
     * The monoisotopic atom chain.
     */
    protected AtomChain monoisotopicAtomChain;
    /**
     * The mass tolerance used for the indistinguishable amino acids in cache.
     */
    private final Double indistinguishableAACacheMass = null;
    /**
     * The sub amino acids.
     */
    protected char[] subAminoAcidsWithoutCombination;
    /**
     * The sub amino acids.
     */
    protected char[] subAminoAcidsWithCombination;
    /**
     * The amino acid combinations.
     */
    protected char[] aminoAcidCombinations;
    /**
     * The standard genetic code.
     */
    protected String[] standardGeneticCode;
    /**
     * The amino acid one letter codes as char array.
     */
    private static final char[] aminoAcidChars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    /**
     * A char array of the one letter code of amino acids without combinations
     * of amino acids.
     */
    private static final char[] uniqueAminoAcidChars = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y'};
    /**
     * The amino acid one letter codes as string array.
     */
    public static final String[] aminoAcidStrings = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     * Convenience method returning an array of all implemented amino acids
     * represented by their singe letter code.
     *
     * @return an array of all implemented amino acids
     */
    public static char[] getAminoAcids() {
        return aminoAcidChars;
    }

    /**
     * Returns the single letter code as character.
     *
     * @return the single letter code as character
     */
    public char getSingleLetterCodeAsChar() {
        return singleLetterCode.charAt(0);
    }

    /**
     * Convenience method returning an arrayList of all implemented amino acids.
     *
     * @return an arrayList of all implemented amino acids represented by their
     * character
     */
    public static List<String> getAminoAcidsList() {
        return Arrays.asList(aminoAcidStrings);
    }

    /**
     * Returns a char array of the one letter code of amino acids without
     * combinations of amino acids.
     *
     * @return a char array of the one letter code of amino acids without
     * combinations of amino acids
     */
    public static char[] getUniqueAminoAcids() {
        return uniqueAminoAcidChars;
    }

    /**
     * Returns the amino acid corresponding to the letter given, null if not
     * implemented. If more than one letter is given only the first one will be
     * accounted for.
     *
     * @param aa the amino acid single letter code as a String
     * @return the corresponding amino acid.
     */
    public static AminoAcid getAminoAcid(String aa) {
        return getAminoAcid(aa.charAt(0));
    }

    /**
     * Returns the amino acid corresponding to the single letter code given, null if not
     * implemented.
     *
     * @param aa the single letter code of the amino acid
     * 
     * @return the corresponding amino acid.
     */
    public static AminoAcid getAminoAcid(char aa) {
        switch (aa) {
            case 'A':
            case 'a':
                return AminoAcid.A;
            case 'C':
            case 'c':
                return AminoAcid.C;
            case 'D':
            case 'd':
                return AminoAcid.D;
            case 'E':
            case 'e':
                return AminoAcid.E;
            case 'F':
            case 'f':
                return AminoAcid.F;
            case 'G':
            case 'g':
                return AminoAcid.G;
            case 'H':
            case 'h':
                return AminoAcid.H;
            case 'I':
            case 'i':
                return AminoAcid.I;
            case 'K':
            case 'k':
                return AminoAcid.K;
            case 'L':
            case 'l':
                return AminoAcid.L;
            case 'M':
            case 'm':
                return AminoAcid.M;
            case 'N':
            case 'n':
                return AminoAcid.N;
            case 'P':
            case 'p':
                return AminoAcid.P;
            case 'Q':
            case 'q':
                return AminoAcid.Q;
            case 'R':
            case 'r':
                return AminoAcid.R;
            case 'S':
            case 's':
                return AminoAcid.S;
            case 'T':
            case 't':
                return AminoAcid.T;
            case 'V':
            case 'v':
                return AminoAcid.V;
            case 'W':
            case 'w':
                return AminoAcid.W;
            case 'Y':
            case 'y':
                return AminoAcid.Y;
            case 'B':
            case 'b':
                return AminoAcid.B;
            case 'Z':
            case 'z':
                return AminoAcid.Z;
            case 'X':
            case 'x':
                return AminoAcid.X;
            case 'U':
            case 'u':
                return AminoAcid.U;
            case 'J':
            case 'j':
                return AminoAcid.J;
            case 'O':
            case 'o':
                return AminoAcid.O;
            default:
                throw new IllegalArgumentException("No amino acid found for letter " + aa + ".");
        }
    }

    /**
     * Indicates whether the amino acid object refers to a combination of amino
     * acids like B, J, Z or X.
     *
     * @return an boolean indicating whether the amino acid object refers to a
     * combination of amino acids like B, J, Z or X
     */
    public abstract boolean iscombination();

    /**
     * In case of a combination of amino acids, returns the comprised amino
     * acids or amino acid groups represented by their single letter code
     * including sub combinations. Example: Z &gt; {G, Q}.
     *
     * @return the actual amino acids
     */
    public char[] getSubAminoAcids() {
        return getSubAminoAcids(true);
    }

    /**
     * In case of a combination of amino acids, returns the comprised amino
     * acids or amino acid groups represented by their single letter code.
     * Example: Z &gt; {G, Q}.
     *
     * @param includeCombinations if true, sub-amino acids which are amino acids
     * combinations like Z will also be included
     *
     * @return the actual amino acids
     */
    public char[] getSubAminoAcids(boolean includeCombinations) {
        
        return includeCombinations ? subAminoAcidsWithCombination : subAminoAcidsWithoutCombination;
    }

    /**
     * Returns the amino acids combinations which might represent this amino
     * acid. Example: g &gt; {Z, X}.
     *
     * @return the amino acids combinations which might represent this amino
     * acid
     */
    public char[] getCombinations() {
        return aminoAcidCombinations;
    }

    /**
     * Returns a matching amino acid using the given preferences. The amino acid
     * is unique when different possibilities are found, then for instance I is
     * returned for both I and L. The first of the amino acid string array is
     * returned.
     *
     * @param aa the single letter code of the amino acid of interest
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a matching amino acid using the given matching type and
     * massTolerance
     */
    public static char getMatchingAminoAcid(char aa, SequenceMatchingParameters sequenceMatchingPreferences) {
        if (sequenceMatchingPreferences.getSequenceMatchingType() == SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids
                && aa == 'L') {
            return 'I';
        }
        return aa;
    }

    /**
     * Returns the matching sequence of a given sequence. For example both
     * PEPTLDE and PEPTIDE will return PEPTIDE when I and L are considered as
     * indistinguishable. See getMatchingAminoAcid for more details.
     *
     * @param sequence the sequence of interest
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return the matching sequence
     */
    public static String getMatchingSequence(String sequence, SequenceMatchingParameters sequenceMatchingPreferences) {
        
        if (sequenceMatchingPreferences.getSequenceMatchingType() != SequenceMatchingParameters.MatchingType.indistiguishableAminoAcids) {
            return sequence;
        }
        
        char[] aas = sequence.toCharArray();
        for (int i = 0 ; i < aas.length ; i++) {
            if (aas[i] == 'L') {
                aas[i] = 'I';
            }
        }
        
        return new String(aas);
    }

    /**
     * Returns the standard genetic triplets associated to this amino acid.
     *
     * @return the standard genetic triplets associated to this amino acid
     */
    public String[] getStandardGeneticCode() {
        return standardGeneticCode;
    }

    /**
     * Returns the amino acid from the standard genetic code. Null if not coding for an amino acid.
     *
     * @param geneticCode the three letter genetic code of the desired amino
     * acid
     *
     * @return the amino acid from the standard genetic code
     */
    public static AminoAcid getAminoAcidFromGeneticCode(String geneticCode) {
        if (geneticCode.equals("TTT") || geneticCode.equals("TTC")) {
            return F;
        } else if (geneticCode.equals("TTA") || geneticCode.equals("TTG") || geneticCode.equals("CTT") || geneticCode.equals("CTC") || geneticCode.equals("CTA") || geneticCode.equals("CTG")) {
            return L;
        } else if (geneticCode.equals("ATT") || geneticCode.equals("ATC") || geneticCode.equals("ATA")) {
            return I;
        } else if (geneticCode.equals("ATG")) {
            return M;
        } else if (geneticCode.startsWith("GT")) {
            return V;
        } else if (geneticCode.startsWith("TC")) {
            return S;
        } else if (geneticCode.startsWith("CC")) {
            return P;
        } else if (geneticCode.startsWith("AC")) {
            return T;
        } else if (geneticCode.startsWith("GC")) {
            return A;
        } else if (geneticCode.equals("TAT") || geneticCode.equals("TAC")) {
            return Y;
        } else if (geneticCode.equals("CAT") || geneticCode.equals("CAC")) {
            return H;
        } else if (geneticCode.equals("CAA") || geneticCode.equals("CAG")) {
            return Q;
        } else if (geneticCode.equals("AAT") || geneticCode.equals("AAC")) {
            return N;
        } else if (geneticCode.equals("AAA") || geneticCode.equals("AAG")) {
            return K;
        } else if (geneticCode.equals("GAT") || geneticCode.equals("GAC")) {
            return D;
        } else if (geneticCode.equals("GAA") || geneticCode.equals("GAG")) {
            return E;
        } else if (geneticCode.equals("TGT") || geneticCode.equals("TGC")) {
            return C;
        } else if (geneticCode.equals("TGG")) {
            return W;
        } else if (geneticCode.startsWith("CG")) {
            return R;
        } else if (geneticCode.equals("AGT") || geneticCode.equals("AGC")) {
            return S;
        } else if (geneticCode.equals("AGA") || geneticCode.equals("AGG")) {
            return R;
        } else if (geneticCode.startsWith("GG")) {
            return G;
        } else if (geneticCode.equals("TAG")) {
            return O;
        } else if (geneticCode.equals("TGA")) {
            return U;
        }
        return null;
    }

    /**
     * Returns the genetic code as combination of the sub amino acid genetic
     * codes.
     *
     * @return the genetic code as combination of the sub amino acid genetic
     * codes
     */
    protected String[] getStandardGeneticCodeForCombination() {
        ArrayList<String> uniqueCodes = new ArrayList<>();
        for (char aa : getSubAminoAcids()) {
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
            if (!aminoAcid.iscombination()) {
                for (String code : aminoAcid.getStandardGeneticCode()) {
                    if (!uniqueCodes.contains(code)) {
                        uniqueCodes.add(code);
                    }
                }
            }
        }
        return uniqueCodes.toArray(new String[uniqueCodes.size()]);
    }
    
    /**
     * Returns the monoisotopic atom chain representing this amino acid.
     * 
     * @return the monoisotopic atom chain representing this amino acid
     */
    public AtomChain getMonoisotopicAtomChain() {
        return monoisotopicAtomChain;
    }
    
    /**
     * Returns the mass of the amino acid.
     * 
     * @return the mass of the amino acid
     */
    public double getMonoisotopicMass() {
        return monoisotopicAtomChain.getMass();
    }
    
    /**
     * Returns the hydrophobicity according to PMID 14730315.
     * 
     * @return the hydrophobicity
     */
    public abstract double getHydrophobicity();
    
    /**
     * Returns the helicity according to PMID 14730315.
     * 
     * @return the helicity
     */
    public abstract double getHelicity();
    
    /**
     * Returns the basicity according to PMID 14730315.
     * 
     * @return the basicity
     */
    public abstract double getBasicity();
    
    /**
     * Returns the pI.
     * 
     * @return the pI
     */
    public abstract double getPI();
    
    /**
     * Returns the pK1.
     * 
     * @return the pK1
     */
    public abstract double getPK1();
    
    /**
     * Returns the pK2.
     * 
     * @return the pK2
     */
    public abstract double getPK2();
    
    /**
     * Returns the pKa. 0.0 if none.
     * 
     * @return the pKa
     */
    public abstract double getPKa();
    
    /**
     * Returns the van der Waals volume in Å3.
     * 
     * @return the van der Waals volume
     */
    public abstract int getVanDerWaalsVolume();
    
    /**
     * Properties of the amino acids.
     */
    public enum Property {
        
        mass("Mass"), 
        hydrophobicity("Hydrophobicity"), 
        helicity("Helicity"), 
        basicity("Basicity"), 
        pI("pI"), 
        pK1("pK1"), 
        pK2("pK2"), 
        pKa("pKa"), 
        vanDerWaalsVolume("Van der Waals volume");
        
        /**
         * The name of the property.
         */
        public final String name;
        
        /**
         * Constructor.
         * 
         * @param index the index of the property
         * @param name the name of the property
         */
        private Property(String name) {
            this.name = name;
        }
        
        /**
         * Returns the number of implemented properties.
         * 
         * @return the number of implemented properties
         */
        public static int getNProperties() {
            return values().length;
        }
        
        /**
         * Returns the property at index.
         * 
         * @param index the index
         * 
         * @return the property at index
         */
        public Property getProperty(int index) {
            return values()[index];
        }
    }
    
    /**
     * Returns a property of the amino acid.
     * 
     * @param property the property of interest
     * 
     * @return the property of the amino acid
     */
    public double getProperty(Property property) {
        switch(property) {
            case mass: return getMonoisotopicMass();
            case hydrophobicity: return getHydrophobicity();
            case helicity: return getHelicity();
            case basicity: return getBasicity();
            case pI: return getPI();
            case pK1: return getPK1();
            case pK2: return getPK2();
            case pKa: return getPKa();
            case vanDerWaalsVolume: return getVanDerWaalsVolume();
            default: throw new UnsupportedOperationException("Property " + property + " not implemented.");
        }
    }
    
    /**
     * Returns the number of amino acids excluding combinations.
     * 
     * @return the number of amino acids excluding combinations
     */
    public static int getNUnique() {
        return 22;
    }
    
    /**
     * Convenience array of the amino acid indexes excluding combinations.
     */
    private static final int[] aaIndexes = {0, -1, 1, 2, 3, 4, 5, 6, 7, -1, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, -1, 21, -1};
    
    /**
     * Returns an index for the amino acid excluding combinations. The amino acid must be provided as upper case single letter code. No sanity check is done.
     * 
     * @param aa the upper case single letter code of the amino acid.
     * 
     * @return an index for the amino acid
     */
    public static int getUniqueIndex(char aa) {
        int index = ((int) aa) - 65;
        return aaIndexes[index];
    }
    
    /**
     * Returns an index for the amino acid excluding combinations. The amino acid must be provided as upper case single letter code. No sanity check is done.
     * 
     * @param aa the upper case single letter code of the amino acid.
     * 
     * @return an index for the amino acid
     */
    public static int getIndex(char aa) {
        return ((int) aa) - 65;
    }
    
    /**
     * Returns a boolean indicating whether the given character is a supported amino acid.
     * 
     * @param aa the amino acid as single character code
     * 
     * @return a boolean indicating whether the given character is a supported amino acid
     */
    public static boolean isAa(char aa) {
        
        // Accept all capital letters between A and Z
        int aaInt = (int) aa;
        return aaInt >= 65 && aaInt <= 90;
    }
    
    /**
     * Returns a boolean indicating whether the given character is a supported amino acid excluding combinations.
     * 
     * @param aa the amino acid as single character code
     * 
     * @return a boolean indicating whether the given character is a supported amino acid excluding combinations
     */
    public static boolean isUniqueAa(char aa) {
        
        // Accept all capital letters between A and Z except B, J, X, and Z
        int aaInt = (int) aa;
        return aaInt >= 65 && aaInt <= 90 && aaInt != 66 && aaInt != 74 && aaInt != 88;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AminoAcid) {
            if (((AminoAcid) obj).singleLetterCode.equalsIgnoreCase(singleLetterCode)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
