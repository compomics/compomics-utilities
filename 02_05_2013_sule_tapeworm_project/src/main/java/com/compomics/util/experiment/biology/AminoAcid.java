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
     * Convenience method returning an arrayList of all implemented amino-acids.
     *
     * @return an arrayList of all implemented amino-acids represented by their
     * character
     */
    public static ArrayList<String> getAminoAcids() {
        ArrayList<String> aas = new ArrayList<String>();
        aas.add("A");
        aas.add("C");
        aas.add("D");
        aas.add("E");
        aas.add("F");
        aas.add("G");
        aas.add("H");
        aas.add("I");
        aas.add("K");
        aas.add("L");
        aas.add("M");
        aas.add("N");
        aas.add("P");
        aas.add("Q");
        aas.add("R");
        aas.add("S");
        aas.add("T");
        aas.add("V");
        aas.add("W");
        aas.add("Y");
        aas.add("B");
        aas.add("Z");
        aas.add("X");
        aas.add("U");
        aas.add("J");
        aas.add("O");
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
                return AminoAcid.A;
            case 'C':
                return AminoAcid.C;
            case 'D':
                return AminoAcid.D;
            case 'E':
                return AminoAcid.E;
            case 'F':
                return AminoAcid.F;
            case 'G':
                return AminoAcid.G;
            case 'H':
                return AminoAcid.H;
            case 'I':
                return AminoAcid.I;
            case 'K':
                return AminoAcid.K;
            case 'L':
                return AminoAcid.L;
            case 'M':
                return AminoAcid.M;
            case 'N':
                return AminoAcid.N;
            case 'P':
                return AminoAcid.P;
            case 'Q':
                return AminoAcid.Q;
            case 'R':
                return AminoAcid.R;
            case 'S':
                return AminoAcid.S;
            case 'T':
                return AminoAcid.T;
            case 'V':
                return AminoAcid.V;
            case 'W':
                return AminoAcid.W;
            case 'Y':
                return AminoAcid.Y;
            case 'B':
                return AminoAcid.B;
            case 'Z':
                return AminoAcid.Z;
            case 'X':
                return AminoAcid.X;
            case 'U':
                return AminoAcid.U;
            case 'J':
                return AminoAcid.J;
            case 'O':
                return AminoAcid.O;
            default:
                return null;
        }
    }
}
