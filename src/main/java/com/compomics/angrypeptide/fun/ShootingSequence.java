/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide.fun;

import com.compomics.angrypeptide.bijection.MatchingParameters;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import java.util.ArrayList;

/**
 * This class groups and generate sequences of shots
 *
 * @author Marc
 */
public class ShootingSequence {

    public static ArrayList<Shot> getSequenceFromAminoAcids(String sequence, MatchingParameters matchingParameters) {
        ArrayList<Shot> result = new ArrayList<Shot>();
            Peptide peptide = new Peptide(sequence, new ArrayList<String>(), new ArrayList<ModificationMatch>());
        double maxEnergy = Shot.getMinEnergyForDistance(peptide.getMass() + matchingParameters.getMs2Tolerance());
        double mass = peptide.getMass() - Atom.O.mass - 2*Atom.H.mass;
        for (int i = sequence.length()-1; i > 0; i--) {
            String aa = sequence.charAt(i) + "";
            AminoAcid currentAA = AminoAcid.getAminoAcid(aa);
            mass -= currentAA.monoisotopicMass;
            double angle = Shot.getRandomAngleForDistance(mass, maxEnergy);
            double energy = Shot.getEnergyForDistance(mass, angle);
            if (energy > maxEnergy) {
                throw new IllegalArgumentException("not enough energy for shot.");
            }
            Shot shot = new Shot(energy, angle);
            double distance = shot.getDistance();
            if (Math.abs(distance - mass) > matchingParameters.getMs2Tolerance()) {
                throw new IllegalArgumentException("Target out of range.");
            }
            result.add(shot);
            maxEnergy = Shot.getMinEnergyForDistance(mass - matchingParameters.getMinFragmentMass() + matchingParameters.getMs2Tolerance()) ;
        }
        return result;
    }
}
