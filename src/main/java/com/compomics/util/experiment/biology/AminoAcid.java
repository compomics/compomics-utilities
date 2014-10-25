package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.aminoacids.*;
import com.compomics.util.preferences.SequenceMatchingPreferences;
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
     * Average mass of the amino acid.
     */
    public double averageMass;
    /**
     * Monoisotopic mass of the amino acid.
     */
    public double monoisotopicMass;
    /**
     * Cache of the indistinguishable amino acids.
     */
    private ArrayList<Character> indistinguishableAACache = null;
    /**
     * The mass tolerance used for the indistinguishable amino acids in cache.
     */
    private Double indistinguishableAACacheMass = null;
    /**
     * The sub amino acids
     */
    protected char[] subAminoAcidsWithoutCombination;
    /**
     * The sub amino acids
     */
    protected char[] subAminoAcidsWithCombination;
    /**
     * The amino acid combinations
     */
    protected char[] aminoAcidCombinations;
    /**
     * The standard genetic code
     */
    protected String[] standardGeneticCode;
    /**
     * The amino acid one letter codes as char array.
     */
    private static final char[] aminoAcidChars = new char[]{'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N',
        'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W', 'B', 'J', 'Z', 'X'};
    /**
     * A char array of the one letter code of amino acids without combinations
     * of amino acids.
     */
    private static final char[] uniqueAminoAcidChars = new char[]{'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N',
        'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W'};
    /**
     * The amino acid one letter codes as string array.
     */
    public static final String[] aminoAcidStrings = new String[]{"A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N",
        "P", "Q", "R", "S", "T", "Y", "U", "O", "V", "W", "B", "J", "Z", "X"};

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
        return getAminoAcid(aa.toUpperCase().charAt(0));
    }

    /**
     * Returns the amino acid corresponding to the letter given, null if not
     * implemented.
     *
     * @param letter the letter given
     * @return the corresponding amino acid.
     */
    public static AminoAcid getAminoAcid(char letter) {
        switch (letter) {
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
                throw new IllegalArgumentException("No amino acid found for letter " + letter + ".");
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
     * including sub combinations. Example: Z -> {G, Q}.
     *
     * @return the actual amino acids
     */
    public char[] getSubAminoAcids() {
        return getSubAminoAcids(true);
    }

    /**
     * In case of a combination of amino acids, returns the comprised amino
     * acids or amino acid groups represented by their single letter code.
     * Example: Z -> {G, Q}.
     *
     * @param includeCombinations if true, sub-amino acids which are amino acids
     * combinations like Z will also be included
     *
     * @return the actual amino acids
     */
    public char[] getSubAminoAcids(boolean includeCombinations) {
        if (includeCombinations) {
            return subAminoAcidsWithCombination;
        } else {
            return subAminoAcidsWithoutCombination;
        }
    }

    /**
     * Returns the amino acids combinations which might represent this amino
     * acid. Example: g -> {Z, X}.
     *
     * @return the amino acids combinations which might represent this amino
     * acid
     */
    public char[] getCombinations() {
        return aminoAcidCombinations;
    }

    /**
     * Returns the amino acids which cannot be distinguished from this amino
     * acid given a mass tolerance. Note that these amino acids may contain the
     * getActualAminoAcids() and getCombinations() amino acids, not
     * comprehensively though, and the amino acid itself.
     *
     * @param massTolerance the mass tolerance for amino acid distinction
     *
     * @return the amino acids which cannot be distinguished using their single
     * character code
     */
    public ArrayList<Character> getIndistinguishableAminoAcids(Double massTolerance) {
        if (massTolerance == null || massTolerance == Double.NaN || massTolerance == Double.NEGATIVE_INFINITY || massTolerance == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Mass tolerance " + massTolerance + " not valid for amino acids comparison.");
        }
        if (indistinguishableAACache == null || indistinguishableAACacheMass.doubleValue() != massTolerance.doubleValue()) {
            setIndistinguishibleAACache(massTolerance);
        }
        return indistinguishableAACache;
    }

    /**
     * Sets the indistinguishibleAACache.
     *
     * @param massTolerance the mass tolerance for amino acid distinction
     */
    public synchronized void setIndistinguishibleAACache(Double massTolerance) {
        ArrayList<Character> result = new ArrayList<Character>();
        for (char aa : getAminoAcids()) {
            if (Math.abs(monoisotopicMass - getAminoAcid(aa).monoisotopicMass) < massTolerance) {
                result.add(aa);
            }
        }
        indistinguishableAACache = result;
        indistinguishableAACacheMass = massTolerance;
    }

    /**
     * Returns a matching amino acid using the given preferences. The amino acid
     * is unique when different possibilities are found, then for instance I is
     * returned for both I and L. The first of the amino acid string array is
     * returned.
     *
     * @param aminoAcid the single letter code of the amino acid of interest
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a matching amino acid using the given matching type and
     * massTolerance
     */
    public static String getMatchingAminoAcid(String aminoAcid, SequenceMatchingPreferences sequenceMatchingPreferences) {
        AminoAcid aa = AminoAcid.getAminoAcid(aminoAcid);
        AminoAcidPattern aaPattern = new AminoAcidPattern(aminoAcid);
        for (String candidateAA : aminoAcidStrings) {
            if (aaPattern.matches(candidateAA, sequenceMatchingPreferences)) {
                if (!aa.iscombination()) {
                    return candidateAA;
                } else {
                    char[] subAas = aa.getSubAminoAcids();
                    boolean subAa = false;
                    for (char aaChar : subAas) {
                        if (aaChar == candidateAA.charAt(0)) {
                            subAa = true;
                            break;
                        }
                    }
                    if (!subAa) {
                        return candidateAA;
                    }
                }
            }
        }
        throw new IllegalArgumentException("No unique amino acid found for amino acid " + aminoAcid);
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
    public static String getMatchingSequence(String sequence, SequenceMatchingPreferences sequenceMatchingPreferences) {
        StringBuilder stringBuilder = new StringBuilder(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            String aa = String.valueOf(sequence.charAt(i));
            aa = getMatchingAminoAcid(aa, sequenceMatchingPreferences);
            stringBuilder.append(aa);
        }
        return stringBuilder.toString();
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
     * Returns the amino acid from the standard genetic code.
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
            return T;
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
        throw new IllegalArgumentException("No amino acid found for genetic code " + geneticCode + ".");
    }

    /**
     * Returns the genetic code as combination of the sub amino acid genetic
     * codes.
     *
     * @return the genetic code as combination of the sub amino acid genetic
     * codes
     */
    protected String[] getStandardGeneticCodeForCombination() {
        ArrayList<String> uniqueCodes = new ArrayList<String>();
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
