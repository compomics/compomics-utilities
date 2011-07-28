package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This factory generates the expected fragment ions from a peptide sequence.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
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
     *
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
     * 
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

            // add the immonium ion
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.IMMONIUM, currentAA.singleLetterCode, currentAA.monoisotopicMass - Atom.C.mass - Atom.O.mass));

            // add the a-ions
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass));

            // add the b-ions
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass));

            // add the c-ion
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.C_ION, faa, forwardMass + Atom.N.mass + 3 * Atom.H.mass));

            // add the ions with a neutral loss (multiple losses are not all taken into account but can be easily added)
            ArrayList<NeutralLoss> loss = new ArrayList<NeutralLoss>();
            NeutralLoss neutralLoss = NeutralLoss.H2O;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.NH3;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.HPO3;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.H3PO4;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.CH4OS;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.H2O;
            loss.add(neutralLoss);
            NeutralLoss neutralLoss2 = NeutralLoss.NH3;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.HPO3;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.H3PO4;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.NH3;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.HPO3;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.H3PO4;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.HPO3;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.H3PO4;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.H3PO4;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.B_ION, faa, forwardMass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();


            raa = sequence.length() - aa - 1;
            currentAA = getAminoAcid(sequence.charAt(raa));
            rewindMass += currentAA.monoisotopicMass;

            if (modifications.get(raa) != null) {
                for (PTM ptm : modifications.get(raa)) {
                    rewindMass += ptm.getMass();
                }
            }

            // add the x-ion
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.X_ION, faa, rewindMass + Atom.C.mass + Atom.O.mass));

            // add the y-ions
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass));

            // add the z-ions
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Z_ION, faa, rewindMass - Atom.N.mass));


            // add the ions with a neutral loss (multiple losses are not all taken into account but can be easily added)
            loss.clear();
            neutralLoss = NeutralLoss.H2O;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.NH3;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.HPO3;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.H3PO4;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.CH4OS;
            loss.add(neutralLoss);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.H2O;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.NH3;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.HPO3;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.H3PO4;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.NH3;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.HPO3;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.H3PO4;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.HPO3;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.H3PO4;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
            neutralLoss = NeutralLoss.H3PO4;
            loss.add(neutralLoss);
            neutralLoss2 = NeutralLoss.CH4OS;
            loss.add(neutralLoss2);
            result.add(new PeptideFragmentIon(PeptideFragmentIonType.Y_ION, faa, rewindMass + 2 * Atom.H.mass - neutralLoss.mass - neutralLoss2.mass, loss));
            loss.clear();
        }

        currentAA = getAminoAcid(sequence.charAt(sequence.length() - 1));
        forwardMass += currentAA.monoisotopicMass;

        if (modifications.get(sequence.length() - 1) != null) {
            for (PTM ptm : modifications.get(sequence.length() - 1)) {
                forwardMass += ptm.getMass();
            }
        }
        // add the precursor ion
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass));

        // add the ions with a neutral loss (multiple losses are not all taken into account but can be easily added)
        ArrayList<NeutralLoss> loss = new ArrayList<NeutralLoss>();
        NeutralLoss neutralLoss = NeutralLoss.H2O;
        loss.add(neutralLoss);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.NH3;
        loss.add(neutralLoss);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.HPO3;
        loss.add(neutralLoss);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.H3PO4;
        loss.add(neutralLoss);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.CH4OS;
        loss.add(neutralLoss);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.H2O;
        loss.add(neutralLoss);
        NeutralLoss neutralLoss2 = NeutralLoss.NH3;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.HPO3;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.H3PO4;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.CH4OS;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.NH3;
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.HPO3;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.H3PO4;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.CH4OS;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.HPO3;
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.H3PO4;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.CH4OS;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();
        neutralLoss = NeutralLoss.H3PO4;
        loss.add(neutralLoss);
        neutralLoss2 = NeutralLoss.CH4OS;
        loss.add(neutralLoss2);
        result.add(new PeptideFragmentIon(PeptideFragmentIonType.PRECURSOR_ION, sequence.length(), forwardMass + Atom.H.mass + Atom.O.mass - neutralLoss.mass - neutralLoss2.mass, loss));
        loss.clear();


        return result;
    }

    /**
     * Returns the amino acid corresponding to the letter given, null if not implemented.
     * 
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
