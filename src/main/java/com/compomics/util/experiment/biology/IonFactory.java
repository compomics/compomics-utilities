package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ImmoniumIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This factory generates the expected ions from a peptide.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IonFactory {

    /**
     * The instance of the factory
     */
    private static IonFactory instance = null;
    /**
     * Neutral losses which will be looked for for every peptide independently
     * from the modifications found
     */
    private ArrayList<NeutralLoss> defaultNeutralLosses = new ArrayList<NeutralLoss>();

    /**
     * Constructor
     */
    private IonFactory() {
    }

    /**
     * Static method which returns the instance of the factory
     *
     * @return the instance of the factory
     */
    public static IonFactory getInstance() {
        if (instance == null) {
            instance = new IonFactory();
        }
        return instance;
    }

    /**
     * Adds a default neutral loss to the default neutral losses if the
     * corresponding loss was not here already
     *
     * @param neutralLoss the new neutral loss
     */
    public void addDefaultNeutralLoss(NeutralLoss newNeutralLoss) {
        boolean found = false;
        for (NeutralLoss neutralLoss : defaultNeutralLosses) {
            if (newNeutralLoss.isSameAs(neutralLoss)) {
                found = true;
                break;
            }
        }
        if (!found) {
            defaultNeutralLosses.add(newNeutralLoss);
        }
    }

    /**
     * Returns the default neutral losses
     *
     * @return the default neutral losses
     */
    public ArrayList<NeutralLoss> getDefaultNeutralLosses() {
        return defaultNeutralLosses;
    }

    /**
     * This method returns the theoretic ions expected from a peptide. /!\ this
     * method will work only if the ptm found in the peptide are in the
     * PTMFactory
     *
     * @param peptide The considered peptide
     * @return the expected fragment ions
     */
    public ArrayList<Ion> getFragmentIons(Peptide peptide) {

        ArrayList<Ion> result = new ArrayList<Ion>();

        String sequence = peptide.getSequence().toUpperCase();
        HashMap<Integer, ArrayList<PTM>> modifications = new HashMap<Integer, ArrayList<PTM>>();
        int location;
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ArrayList<String> taken = new ArrayList<String>();
        String ptmName;
        ArrayList<ReporterIon> reporterIons = new ArrayList<ReporterIon>();
        ArrayList<NeutralLoss> possibleNeutralLosses = new ArrayList<NeutralLoss>();
        possibleNeutralLosses.addAll(defaultNeutralLosses);
        boolean found;
        for (ModificationMatch ptmMatch : peptide.getModificationMatches()) {
            location = ptmMatch.getModificationSite();
            ptmName = ptmMatch.getTheoreticPtm();
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (!modifications.containsKey(location)) {
                modifications.put(location, new ArrayList<PTM>());
            }
            modifications.get(location).add(ptm);
            if (!taken.contains(ptmName)) {
                for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                    found = false;
                    for (ReporterIon reporterIon : reporterIons) {
                        if (ptmReporterIon.isSameAs(reporterIon)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        reporterIons.add(ptmReporterIon);
                    }
                }
                for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                    found = false;
                    for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                        if (ptmNeutralLoss.isSameAs(neutralLoss)) { //@TODO: we keep only different neutral losses. We might want to change that when people are working with modifications having reproducible motifs like ubiquitin or some glycons.
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        possibleNeutralLosses.add(ptmNeutralLoss);
                    }
                }
                taken.add(ptmName);
            }
        }

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = getAccountedNeutralLosses(possibleNeutralLosses);

        AminoAcid currentAA;
        double forwardMass = 0;
        double rewindMass = Atom.O.mass;
        int raa, faa;
        taken.clear();
        char aaName;

        for (int aa = 0; aa < sequence.length() - 1; aa++) {

            aaName = sequence.charAt(aa);
            if (!taken.contains(aaName + "")) {
                result.add(new ImmoniumIon(aaName));
                taken.add(aaName + "");
            }

            faa = aa + 1;
            currentAA = AminoAcid.getAminoAcid(aaName);
            forwardMass += currentAA.monoisotopicMass;

            if (modifications.get(faa) != null) {
                for (PTM ptm : modifications.get(faa)) {
                    forwardMass += ptm.getMass();
                }
            }

            // add the a-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.A_ION, faa, forwardMass - Atom.C.mass - Atom.O.mass-getLossesMass(losses), losses));
            }

            // add the b-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.B_ION, faa, forwardMass-getLossesMass(losses), losses));
            }

            // add the c-ion
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.C_ION, faa, forwardMass + Atom.N.mass + 3 * Atom.H.mass-getLossesMass(losses), losses));
            }


            raa = sequence.length() - aa - 1;
            currentAA = AminoAcid.getAminoAcid(sequence.charAt(raa));
            rewindMass += currentAA.monoisotopicMass;

            if (modifications.get(raa + 1) != null) {
                for (PTM ptm : modifications.get(raa + 1)) {
                    rewindMass += ptm.getMass();
                }
            }

            // add the x-ion
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.X_ION, faa, rewindMass + Atom.C.mass + Atom.O.mass-getLossesMass(losses), losses));
            }

            // add the y-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.Y_ION, faa, rewindMass + 2 * Atom.H.mass-getLossesMass(losses), losses));
            }

            // add the z-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.Z_ION, faa, rewindMass - Atom.N.mass-getLossesMass(losses), losses));
            }

        }

        currentAA = AminoAcid.getAminoAcid(sequence.charAt(sequence.length() - 1));
        forwardMass += currentAA.monoisotopicMass;

        if (modifications.get(sequence.length()) != null) {
            for (PTM ptm : modifications.get(sequence.length())) {
                forwardMass += ptm.getMass();
            }
        }
        // add the precursor ion
        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
            result.add(new PrecursorIon(forwardMass + Atom.H.mass + Atom.O.mass-getLossesMass(losses), losses));
        }

        return result;
    }

    /**
     * Convenience method returning the possible neutral losses combination as
     * accounted by the factory. i.e. for now up to two neutral losses per peak.
     *
     * @param possibleNeutralLosses the possible neutral losses
     * @return the possible combinations
     */
    public static ArrayList<ArrayList<NeutralLoss>> getAccountedNeutralLosses(ArrayList<NeutralLoss> possibleNeutralLosses) {
        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = new ArrayList<ArrayList<NeutralLoss>>();
        ArrayList<NeutralLoss> tempList = new ArrayList<NeutralLoss>();
        neutralLossesCombinations.add(tempList);
        for (NeutralLoss neutralLoss1 : possibleNeutralLosses) {
            tempList = new ArrayList<NeutralLoss>();
            tempList.add(neutralLoss1);
            neutralLossesCombinations.add(tempList);
            for (NeutralLoss neutralLoss2 : possibleNeutralLosses) {
                if (!neutralLoss1.isSameAs(neutralLoss2)) {
                    tempList = new ArrayList<NeutralLoss>();
                    tempList.add(neutralLoss1);
                    tempList.add(neutralLoss2);
                }
            }
        }
        return neutralLossesCombinations;
    }
    
    /**
     * Convenience summing the masses of various neutral losses
     * @param neutralLosses list of neutral losses
     * @return the summ of the masses
     */
    public static double getLossesMass(ArrayList<NeutralLoss> neutralLosses) {
        double result = 0;
        for (NeutralLoss neutralLoss : neutralLosses) {
            result += neutralLoss.mass;
        }
        return result;
    }
}
