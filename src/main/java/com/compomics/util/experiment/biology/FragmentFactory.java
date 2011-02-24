package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This factory generates the expected fragment ions from a peptide sequence.
 *
 * @author Marc
 */
public class FragmentFactory {

    /**
     * The instance of the factory
     */
    private static FragmentFactory instance = null;

    /**
     * Constructor
     */
    private FragmentFactory() {
    }

    /**
     * Static method which returns the instance of the factory
     * @return  the instance of the factory
     */
    public static FragmentFactory getInstance() {
        if (instance == null) {
            instance = new FragmentFactory();
        }
        return instance;
    }

    /**
     * This method returns the theoretic fragment ions expected from a peptide sequence.
     * @param peptide       The considered peptide
     * @return              the expected fragment ions
     */
    public ArrayList<PeptideFragmentIon> getFragmentIons(Peptide peptide) {

        String sequence = peptide.getSequence().toUpperCase();
        HashMap<Integer, ArrayList<PTM>> modifications = new HashMap<Integer, ArrayList<PTM>>();
        int location;
        for (ModificationMatch ptmMatch : peptide.getModificationMatches()) {
            location = ptmMatch.getModificationSite();
            PTM ptm = ptmMatch.getTheoreticPtm();
            if (!modifications.containsKey(location)) {
                modifications.put(location, new ArrayList<PTM>());
            }
            modifications.get(location).add(ptm);
        }

        ArrayList<PeptideFragmentIon> result = new ArrayList<PeptideFragmentIon>();

        AminoAcid currentAA;
        PeptideFragmentIon currentFragment;
        double forwardMass = 0;
        double rewindMass = Atom.O.mass;
        int raa, faa;
        for (int aa = 0; aa < sequence.length() - 1; aa++) {
            faa = aa + 1;
            currentAA = getAminoAcid(sequence.charAt(aa));
            forwardMass += currentAA.monoisotopicMass;
            if (modifications.get(aa) != null) {
                for (PTM ptm : modifications.get(aa)) {
                    forwardMass += ptm.getMass();
                }
            }

            result.add(new PeptideFragmentIon(PeptideFragmentIon.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass));
            result.add(new PeptideFragmentIon(PeptideFragmentIon.ANH3_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - Atom.N.mass - 3 * Atom.H.mass));
            result.add(new PeptideFragmentIon(PeptideFragmentIon.AH2O_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - 2 * Atom.H.mass - Atom.O.mass));

            result.add(new PeptideFragmentIon(PeptideFragmentIon.B_ION, faa, forwardMass));
            result.add(new PeptideFragmentIon(PeptideFragmentIon.BNH3_ION, faa, forwardMass - Atom.N.mass - 3 * Atom.H.mass));
            result.add(new PeptideFragmentIon(PeptideFragmentIon.BH2O_ION, faa, forwardMass - 2 * Atom.H.mass - Atom.O.mass));

            result.add(new PeptideFragmentIon(PeptideFragmentIon.C_ION, faa, forwardMass + Atom.N.mass + 3 * Atom.H.mass));

            raa = sequence.length() - aa - 1;
            currentAA = getAminoAcid(sequence.charAt(raa));
            rewindMass += currentAA.monoisotopicMass;
            if (modifications.get(raa) != null) {
                for (PTM ptm : modifications.get(raa)) {
                    forwardMass += ptm.getMass();
                }
            }

            result.add(new PeptideFragmentIon(PeptideFragmentIon.X_ION, faa, rewindMass + Atom.C.mass + Atom.O.mass));

            result.add(new PeptideFragmentIon(PeptideFragmentIon.Y_ION, faa, rewindMass + 2 * Atom.H.mass));
            result.add(new PeptideFragmentIon(PeptideFragmentIon.YNH3_ION, faa, rewindMass - Atom.N.mass - Atom.H.mass));
            result.add(new PeptideFragmentIon(PeptideFragmentIon.YH2O_ION, faa, rewindMass - Atom.O.mass));

            result.add(new PeptideFragmentIon(PeptideFragmentIon.Z_ION, faa, rewindMass - Atom.N.mass));

        }

        currentAA = getAminoAcid(sequence.charAt(sequence.length() - 1));
        forwardMass += currentAA.monoisotopicMass;
        if (modifications.get(sequence.length() - 1) != null) {
            for (PTM ptm : modifications.get(sequence.length() - 1)) {
                forwardMass += ptm.getMass();
            }
        }
        result.add(new PeptideFragmentIon(PeptideFragmentIon.MH_ION, sequence.length(), forwardMass + 2 * Atom.H.mass + Atom.O.mass));
        result.add(new PeptideFragmentIon(PeptideFragmentIon.MHNH3_ION, sequence.length(), forwardMass - Atom.N.mass - Atom.H.mass + Atom.O.mass));
        result.add(new PeptideFragmentIon(PeptideFragmentIon.MHH2O_ION, sequence.length(), forwardMass));

        return result;
    }

    /**
     * Returns the amino acid corresponding to the letter given, null if not implemented.
     * @param letter    the letter given
     * @return          the corresponding amino acid.
     */
    private AminoAcid getAminoAcid(char letter) {
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
            default:
                return null;
        }
    }
}
