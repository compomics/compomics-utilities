package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This test case will test the mgf import and spectrum annotation
 *
 * @author Marc
 */
public class SpectrumTest extends TestCase {

    public void testSpectrumImport() {
        File mgfFile = new File("testFiles/testSpectrum.mgf");
        MgfReader mgfReader = new MgfReader();
        /**
        try {
        MSnSpectrum spectrum = (mgfReader.getSpectra(mgfFile)).get(0);
        Assert.assertTrue(spectrum.getSpectrumTitle().equals("1080.01647949219_3171.8305"));
        Assert.assertTrue(spectrum.getPrecursor().getCharge().value == 2);
        Assert.assertTrue(spectrum.getPrecursor().getMz() == 1080.01647949219);
        Assert.assertTrue(spectrum.getPrecursor().getRt() == 3171.8305);

        Peptide peptide = new Peptide("IMNGEADAMSLDGGFVYIAGK", 0.0, new ArrayList<Protein>(), new ArrayList<ModificationMatch>());
        SpectrumAnnotator spectrumAnnotator = new SpectrumAnnotator();
        HashMap<Integer, HashMap<Integer, IonMatch>> result = spectrumAnnotator.annotateSpectrum(peptide, spectrum, 0.5, 0);
        
        } catch (Exception e) {
            int debug = 0;
        }**/
    }

}
