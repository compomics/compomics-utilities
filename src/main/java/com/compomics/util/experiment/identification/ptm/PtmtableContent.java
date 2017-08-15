package com.compomics.util.experiment.identification.ptm;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.spectra.MSnSpectrum;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math.MathException;

/**
 * Convenience class for the content of a PTM table.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PtmtableContent {

    /**
     * The content of the table: modification status &gt; fragment ion type
     * according to the peptide fragment ion static fields &gt; aa number &gt;
     * list of intensities.
     */
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> map;
    /**
     * The total intensity.
     */
    private double totalIntensity = 0;
    /**
     * The max intensity.
     */
    private double maxIntensity = 0;

    /**
     * Constructor.
     */
    public PtmtableContent() {
        map = new HashMap<>();
    }

    /**
     * Add intensity.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @param intensity the intensity
     */
    public void addIntensity(int nMod, Integer peptideFragmentIonType, int aa, double intensity) {
        if (!map.containsKey(nMod)) {
            map.put(nMod, new HashMap<>());
        }
        if (!map.get(nMod).containsKey(peptideFragmentIonType)) {
            map.get(nMod).put(peptideFragmentIonType, new HashMap<>());
        }
        if (!map.get(nMod).get(peptideFragmentIonType).containsKey(aa)) {
            map.get(nMod).get(peptideFragmentIonType).put(aa, new ArrayList<>());
        }
        map.get(nMod).get(peptideFragmentIonType).get(aa).add(intensity);
        totalIntensity += intensity;
        if (intensity > maxIntensity) {
            maxIntensity = intensity;
        }
    }

    /**
     * Get intensity.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @return the list of intensities
     */
    public ArrayList<Double> getIntensities(int nMod, Integer peptideFragmentIonType, int aa) {
        if (map.containsKey(nMod)
                && map.get(nMod).containsKey(peptideFragmentIonType)
                && map.get(nMod).get(peptideFragmentIonType).containsKey(aa)) {
            return map.get(nMod).get(peptideFragmentIonType).get(aa);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get the quantile.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @param quantile the quantile
     * @return the quantile
     */
    public Double getQuantile(int nMod, Integer peptideFragmentIonType, int aa, double quantile) {
        ArrayList<Double> intensities = getIntensities(nMod, peptideFragmentIonType, aa);
        if (intensities.size() > 0) {
            int index = (int) (quantile * intensities.size());
            return intensities.get(index);
        } else {
            return 0.0;
        }
    }

    /**
     * Get histogram.
     *
     * @param nMod the modification number
     * @param peptideFragmentIonType the peptide fragment ion type
     * @param aa the amino acid
     * @param bins the bins
     * @return the histogram
     */
    public int[] getHistogram(int nMod, Integer peptideFragmentIonType, int aa, int bins) {
        ArrayList<Double> intensities = getIntensities(nMod, peptideFragmentIonType, aa);

        int[] values = new int[bins];

        if (intensities.size() > 0) {

            for (Double intensity : intensities) {

                double currentIntensity = intensity; // / maxIntensity;
                for (int j = 0; j < bins; j++) {

                    double index = (double) j;

                    if (((index / bins) < currentIntensity) && (currentIntensity < (index + 1) / bins)) {
                        values[j]++;
                    }
                }

                // make sure that the max value is included
                if (currentIntensity == 1) {
                    values[values.length - 1]++;
                }
            }

            return values;
        } else {
            return values;
        }
    }

    /**
     * Get the map.
     *
     * @return the map
     */
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, ArrayList<Double>>>> getMap() {
        return map;
    }

    /**
     * Add all.
     *
     * @param anotherContent another PTM table content
     */
    public void addAll(PtmtableContent anotherContent) {
        for (int nPTM : anotherContent.getMap().keySet()) {
            for (Integer peptideFragmentIonType : anotherContent.getMap().get(nPTM).keySet()) {
                for (int nAA : anotherContent.getMap().get(nPTM).get(peptideFragmentIonType).keySet()) {
                    for (double intensity : anotherContent.getIntensities(nPTM, peptideFragmentIonType, nAA)) {
                        addIntensity(nPTM, peptideFragmentIonType, nAA, intensity);
                    }
                }
            }
        }
    }

    /**
     * Normalize intensities.
     */
    public void normalize() {
        if (totalIntensity > 0) {
            double normalization = totalIntensity;
            totalIntensity = 0;
            maxIntensity = 0;
            ArrayList<Double> tempIntensities;
            for (int nPTM : map.keySet()) {
                for (Integer peptideFragmentIonType : map.get(nPTM).keySet()) {
                    for (int nAA : map.get(nPTM).get(peptideFragmentIonType).keySet()) {
                        tempIntensities = new ArrayList<>();
                        for (double intensity : getIntensities(nPTM, peptideFragmentIonType, nAA)) {
                            tempIntensities.add(intensity / normalization);
                        }
                        map.get(nPTM).get(peptideFragmentIonType).put(nAA, tempIntensities);
                    }
                }
            }
        }
    }

    /**
     * Returns the max intensity.
     *
     * @return the max intensity
     */
    public double getMaxIntensity() {
        return maxIntensity;
    }

    /**
     * Returns the PTM plot series in the JFreechart format for one PSM.
     *
     * @param peptide the peptide of interest
     * @param ptm the PTM to score
     * @param nPTM the amount of times the PTM is expected
     * @param spectrum the corresponding spectrum
     * @param annotationPreferences the annotation preferences
     * @param specificAnnotationPreferences the specific annotation preferences
     *
     * @return the PTM plot series in the JFreechart format for one PSM.
     * 
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public static HashMap<PeptideFragmentIon, ArrayList<IonMatch>> getPTMPlotData(Peptide peptide, Modification ptm, int nPTM, MSnSpectrum spectrum,
            AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences) throws InterruptedException, MathException {

        //@TODO: use Peptide.getNoModPeptide instead
        Peptide noModPeptide = new Peptide(peptide.getSequence(), new ArrayList<>());

        if (peptide.isModified()) {
            for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
                if (!modificationMatch.getModification().equals(ptm.getName())) {
                    noModPeptide.addModificationMatch(modificationMatch);
                }
            }
        }

        PeptideSpectrumAnnotator spectrumAnnotator = new PeptideSpectrumAnnotator();
        HashMap<Integer, ArrayList<Ion>> fragmentIons
                = spectrumAnnotator.getExpectedIons(specificAnnotationPreferences, noModPeptide);
        HashMap<PeptideFragmentIon, ArrayList<IonMatch>> map = new HashMap<>(); //@TODO: refactor using another key for the map

        for (int i = 0; i <= nPTM; i++) {

            spectrumAnnotator.setMassShift(i * ptm.getMass());

            ArrayList<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences, spectrum, noModPeptide)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (IonMatch ionMatch : matches) {
                if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                    PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                    for (Ion noModIon : fragmentIons.get(ionMatch.charge)) {
                        if (noModIon.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION
                                && peptideFragmentIon.isSameAs(noModIon)) {
                            PeptideFragmentIon noModFragmentIon = (PeptideFragmentIon) noModIon;
                            if (!map.containsKey(noModFragmentIon)) {
                                map.put(noModFragmentIon, new ArrayList<>());
                            }
                            map.get(noModFragmentIon).add(ionMatch);
                            break;
                        }
                    }
                }
            }
        }

        return map;
    }

    /**
     * Get the PTM table content.
     *
     * @param peptide the peptide of interest
     * @param ptm the PTM to score
     * @param nPTM the amount of times the PTM is expected
     * @param spectrum the corresponding spectrum
     * @param annotationPreferences the annotation preferences
     * @param specificAnnotationPreferences the specific annotation preferences
     *
     * @return the PtmtableContent object
     * 
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     * @throws SQLException if an SQLException occurs
     * @throws java.lang.InterruptedException exception thrown if the thread is
     * interrupted
     * @throws org.apache.commons.math.MathException exception thrown if a math exception occurred when estimating the noise level 
     */
    public static PtmtableContent getPTMTableContent(Peptide peptide, Modification ptm, int nPTM, MSnSpectrum spectrum,
            AnnotationSettings annotationPreferences, SpecificAnnotationSettings specificAnnotationPreferences) throws IOException, SQLException, ClassNotFoundException, InterruptedException, MathException {

        PtmtableContent ptmTableContent = new PtmtableContent();

        ArrayList<Modification> ptms = new ArrayList<>(1);
        ptms.add(ptm);
        Peptide noModPeptide = Peptide.getNoModPeptide(peptide, ptms);

        NeutralLossesMap lossesMap = new NeutralLossesMap();
        for (String neutralLossName : specificAnnotationPreferences.getNeutralLossesMap().getAccountedNeutralLosses()) {
            NeutralLoss neutralLoss = NeutralLoss.getNeutralLoss(neutralLossName);
            if (Math.abs(neutralLoss.getMass() - ptm.getMass()) > specificAnnotationPreferences.getFragmentIonAccuracyInDa(spectrum.getMaxMz())) {
                lossesMap.addNeutralLoss(neutralLoss, 1, 1);
            }
        }

        PeptideSpectrumAnnotator spectrumAnnotator = new PeptideSpectrumAnnotator();
        spectrumAnnotator.setPeptide(noModPeptide, specificAnnotationPreferences.getPrecursorCharge(), specificAnnotationPreferences);
        
        for (int i = 0; i <= nPTM; i++) {

            spectrumAnnotator.setMassShift(i * ptm.getMass());

            final int index = i;
            Stream<IonMatch> matches = spectrumAnnotator.getSpectrumAnnotation(annotationPreferences, specificAnnotationPreferences, spectrum, noModPeptide);
            matches.filter(ionMatch -> ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION)
                    .forEach(ionMatch -> {
                        PeptideFragmentIon peptideFragmentIon = (PeptideFragmentIon) ionMatch.ion;
                        ptmTableContent.addIntensity(index, peptideFragmentIon.getSubType(), peptideFragmentIon.getAaNumber(peptide.getSequence().length()), ionMatch.peak.intensity);
                            });
            
        }

        return ptmTableContent;
    }
}
