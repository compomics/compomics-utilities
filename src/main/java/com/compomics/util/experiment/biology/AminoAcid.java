package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.aminoacids.Alanine;
import com.compomics.util.experiment.biology.aminoacids.Arginine;
import com.compomics.util.experiment.biology.aminoacids.Asparagine;
import com.compomics.util.experiment.biology.aminoacids.AsparticAcid;
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
import com.compomics.util.experiment.biology.aminoacids.Serine;
import com.compomics.util.experiment.biology.aminoacids.Threonine;
import com.compomics.util.experiment.biology.aminoacids.Tryptophan;
import com.compomics.util.experiment.biology.aminoacids.Tyrosine;
import com.compomics.util.experiment.biology.aminoacids.Valine;

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

    public String singleLetterCode;
    public String threeLetterCode;
    public String name;
    public double averageMass;
    public double monoisotopicMass;

}
