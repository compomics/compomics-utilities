package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.aminoacids.Alanine;
import com.compomics.util.experiment.biology.aminoacids.Arginine;
import com.compomics.util.experiment.biology.aminoacids.Asparagine;
import com.compomics.util.experiment.biology.aminoacids.AsparticAcid;
import com.compomics.util.experiment.biology.aminoacids.B;
import com.compomics.util.experiment.biology.aminoacids.Cysteine;
import com.compomics.util.experiment.biology.aminoacids.GlutamicAcid;
import com.compomics.util.experiment.biology.aminoacids.Glutamine;
import com.compomics.util.experiment.biology.aminoacids.Glycine;
import com.compomics.util.experiment.biology.aminoacids.Histidine;
import com.compomics.util.experiment.biology.aminoacids.Isoleucine;
import com.compomics.util.experiment.biology.aminoacids.Leucine;
import com.compomics.util.experiment.biology.aminoacids.Lysine;
import com.compomics.util.experiment.biology.aminoacids.Methionine;
import com.compomics.util.experiment.biology.aminoacids.Phenylalanine;
import com.compomics.util.experiment.biology.aminoacids.Proline;
import com.compomics.util.experiment.biology.aminoacids.Selenocysteine;
import com.compomics.util.experiment.biology.aminoacids.Serine;
import com.compomics.util.experiment.biology.aminoacids.Threonine;
import com.compomics.util.experiment.biology.aminoacids.Tryptophan;
import com.compomics.util.experiment.biology.aminoacids.Tyrosine;
import com.compomics.util.experiment.biology.aminoacids.Valine;
import com.compomics.util.experiment.biology.aminoacids.X;
import com.compomics.util.experiment.biology.aminoacids.Z;

/**
 * Class representing amino acids
 *
 * @author Marc
 */
public abstract class AminoAcid {

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
    /**
     * Single letter code of the amino acid
     */
    public String singleLetterCode;
    /**
     * Three letter code of the amino acid
     */
    public String threeLetterCode;
    /**
     * Name of the amino acid
     */
    public String name;
    /**
     * average mass of the amino acid
     */
    public double averageMass;
    /**
     * Monoisotopic mass of the amino acid
     */
    public double monoisotopicMass;

    /**
     * Returns the amino acid corresponding to the letter given, null if not implemented.
     * 
     * @param letter    the letter given
     * @return          the corresponding amino acid.
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
            default:
                return null;
        }
    }
}
