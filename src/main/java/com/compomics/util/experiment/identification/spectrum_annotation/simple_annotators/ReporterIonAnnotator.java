package com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Annotator for reporter ions.
 *
 * @author Marc Vaudel
 */
public class ReporterIonAnnotator {

    private double[] reporterIonsMz;
    
    private ReporterIon[] reporterIons;
    
    public ReporterIonAnnotator(ReporterIon[] reporterIons) {
        
        this.reporterIons = reporterIons;
        this.reporterIonsMz = new double[reporterIons.length];
        
        for (int i = 0 ; i < reporterIons.length ; i++) {
            reporterIonsMz[i] = reporterIons[i].getTheoreticMass() + ElementaryIon.proton.getTheoreticMass();
        }
    }

    /**
     * Returns the ions matched in the given spectrum.
     *
     * @param spectrumIndex the index of the spectrum
     *
     * @return the ions matched in the given spectrum
     */
    public ArrayList<IonMatch> getIonMatches(SpectrumIndex spectrumIndex) {

        ArrayList<IonMatch> results = new ArrayList<IonMatch>(reporterIons.length);

        for (int i = 0; i < reporterIons.length; i++) {

            ReporterIon reporterIon = reporterIons[i];
            double ionMz = reporterIonsMz[i];
            ArrayList<Peak> peaks = spectrumIndex.getMatchingPeaks(ionMz);

            for (Peak peak : peaks) {

                results.add(new IonMatch(peak, reporterIon, 1));

            }
        }

        return results;
    }
    
}
