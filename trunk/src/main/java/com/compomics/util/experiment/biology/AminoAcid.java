package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.aminoacids.*;
import java.io.Serializable;
import java.util.ArrayList;

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
    public static final AminoAcid B = new B();
    public static final AminoAcid Z = new Z();
    public static final AminoAcid X = new X();
    public static final AminoAcid U = new Selenocysteine();
    public static final AminoAcid J = new J();
    public static final AminoAcid O = new Pyrrolysine();
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
     * The amino acid one letter codes as char array.
     */
    private static final char[] aminoAcidChars = new char[]{'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N',
        'P', 'Q', 'R', 'S', 'T', 'Y', 'U', 'O', 'V', 'W', 'B', 'J', 'Z', 'X'};
    /**
     * The amino acid one letter codes as string array.
     */
    public static final String[] aminoAcidStrings = new String[]{"A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N",
        "P", "Q", "R", "S", "T", "Y", "U", "O", "V", "W", "B", "J", "Z", "X"};

    /**
     * Cache of the indistinguishable amino acids
     */
    private ArrayList<Character> indistinguishableAACache = null;
    /**
     * The mass tolerance used for the indistinguishable amino acids in cache
     */
    private Double indistinguishableAACacheMass = null;

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
     * Convenience method returning an arrayList of all implemented amino acids.
     *
     * @return an arrayList of all implemented amino acids represented by their
     * character
     */
    public static ArrayList<String> getAminoAcidsList() {
        ArrayList<String> aas = new ArrayList<String>(26);
        for (char aa : getAminoAcids()) {
            aas.add(aa + "");
        }
        return aas;
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
     * In case of a combination of amino acids, returns the comprised amino
     * acids or amino acid groups represented by their single letter code.
     * Example: Z -> {G, Q}.
     *
     * @return the actual amino acids
     */
    public abstract char[] getSubAminoAcids();

    /**
     * Returns the amino acids combinations which might represent this amino
     * acid. Example: g -> {Z, X}.
     *
     * @return the amino acids combinations which might represent this amino
     * acid
     */
    public abstract char[] getCombinations();

    /**
     * Returns the amino acids which cannot be distinguished from this amino
     * acid given a mass tolerance. Note that these amino acids may contain the
     * getActualAminoAcids() and getCombinations() amino acids, not
     * comprehensively though, and the amino acid itself.
     *
     * @param massTolerance the mass tolerance
     *
     * @return the amino acids which cannot be distinguished using their single
     * character code
     */
    public ArrayList<Character> getIndistinguishibleAminoAcids(Double massTolerance) {
        if (massTolerance == null || massTolerance == Double.NaN || massTolerance == Double.NEGATIVE_INFINITY || massTolerance == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Mass tolerance " + massTolerance + " not valid for amino acids comparison.");
        }
        if (indistinguishableAACache == null || indistinguishableAACacheMass != massTolerance) {
            indistinguishableAACache = new ArrayList<Character>();
            for (char aa : getAminoAcids()) {
                if (Math.abs(monoisotopicMass - getAminoAcid(aa).monoisotopicMass) < massTolerance) {
                    indistinguishableAACache.add(aa);
                }
            }
            indistinguishableAACacheMass = massTolerance;
        }
        return indistinguishableAACache;
    }

    /**
     * Returns a matching amino acid using the given matching type and
     * massTolerance. The amino acid is unique for indistinguishable amino acids
     * when considered as such, then for instance I is returned for both I and
     * L. The first of the aminoAcidStrings array is returned.
     *
     * @param aminoAcid the single letter code of the amino acid of interest
     * @param matchingType the matching type
     * @param massTolerance the ms2 mass tolerance
     *
     * @return a matching amino acid using the given matching type and
     * massTolerance
     */
    public static String getMatchingAminoAcid(String aminoAcid, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        AminoAcidPattern aaPattern = new AminoAcidPattern(aminoAcid);
        for (String candidateAA : aminoAcidStrings) {
            if (aaPattern.matches(candidateAA, matchingType, massTolerance)) {
                return candidateAA;
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
     * @param matchingType the matching type
     * @param massTolerance the ms2 mass tolerance
     *
     * @return the matching sequence
     */
    public static String getMatchingSequence(String sequence, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        StringBuilder stringBuilder = new StringBuilder(sequence.length());
        for (int i = 0; i < sequence.length(); i++) {
            String aa = String.valueOf(sequence.charAt(i));
            aa = getMatchingAminoAcid(aa, matchingType, massTolerance);
            stringBuilder.append(aa);
        }
        return stringBuilder.toString();
    }
}
