package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.interfaces.QueryToPeptideMapInf;
import com.compomics.mascotdatfile.util.mascot.PeptideHit;
import com.compomics.mascotdatfile.util.mascot.ProteinHit;
import com.compomics.mascotdatfile.util.mascot.enumeration.MascotDatfileType;
import com.compomics.mascotdatfile.util.mascot.factory.MascotDatfileFactory;
import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.refinementparameters.MascotScore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

/**
 * This reader will import identifications from a Mascot dat file.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 9:45:35 AM
 */
public class MascotIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The inspected file
     */
    private File inspectedFile;
    /**
     * Instance of the mascotdatfile parser
     */
    private MascotDatfileInf iMascotDatfile;

    /**
     * constructor for the mascotIdileReader
     */
    public MascotIdfileReader() {
    }

    /**
     * Constructor for the MascotIdilereader
     *
     * @param aFile a file to read
     */
    public MascotIdfileReader(File aFile) {
        inspectedFile = aFile;
        try {
            iMascotDatfile = MascotDatfileFactory.create(inspectedFile.getCanonicalPath(), MascotDatfileType.MEMORY); //getPath might have to be changed into getcanonicalPath
        } catch (IOException e) {
            System.exit(1);
        }
    }

    /**
     * get the spectrum file name
     *
     * @return the spectrum file name
     */
    public String getMgfFileName() {
        String temp = iMascotDatfile.getParametersSection().getFile();
        return Util.getFileName(temp);
    }

    /**
     * getter for the file name
     *
     * @return the file name
     */
    public String getFileName() {
        return iMascotDatfile.getFileName();
    }

    /**
     * a method to get all the spectrum matches
     *
     * @return a set containing all spectrum matches
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches() {

        HashSet<SpectrumMatch> assignedPeptideHits = new HashSet<SpectrumMatch>();
        try {
            QueryToPeptideMapInf lQueryToPeptideMap = iMascotDatfile.getQueryToPeptideMap();
            QueryToPeptideMapInf lDecoyQueryToPeptideMap = iMascotDatfile.getDecoyQueryToPeptideMap();
            for (int i = 0; i < iMascotDatfile.getNumberOfQueries(); i++) {
                Vector<PeptideHit> mascotDecoyPeptideHits = null;
                if (lDecoyQueryToPeptideMap != null) {
                    mascotDecoyPeptideHits = lDecoyQueryToPeptideMap.getAllPeptideHits(i + 1);
                }
                Vector<PeptideHit> mascotPeptideHits = lQueryToPeptideMap.getAllPeptideHits(i + 1);

                // Get spectrum information

                PeptideHit testPeptide = null;
                if (mascotPeptideHits != null) {
                    testPeptide = mascotPeptideHits.get(0);
                } else if (mascotDecoyPeptideHits != null) {
                    testPeptide = mascotDecoyPeptideHits.get(0);
                }
                if (testPeptide != null) {
                    Double measuredMass = testPeptide.getPeptideMr() + testPeptide.getDeltaMass();
                    String measuredCharge = iMascotDatfile.getQuery(i + 1).getChargeString();
                    String sign = String.valueOf(measuredCharge.charAt(1));
                    Charge charge;
                    if (sign.compareTo("+") == 0) {
                        charge = new Charge(Charge.PLUS, Integer.valueOf(measuredCharge.substring(0, 1)));
                    } else {
                        charge = new Charge(Charge.MINUS, Integer.valueOf(measuredCharge.substring(0, 1)));
                    }
                    Precursor precursor = new Precursor(-1, measuredMass, charge); // The RT is not known at this stage
                    String spectrumId = iMascotDatfile.getQuery(i + 1).getTitle();
                    MSnSpectrum spectrum = new MSnSpectrum(2, precursor, spectrumId, getMgfFileName());
                    SpectrumMatch currentMatch = new SpectrumMatch(spectrum.getSpectrumKey());

                    // Get all hits
                    if (mascotPeptideHits != null) {
                        for (PeptideHit peptideHit : mascotPeptideHits) {
                            currentMatch.addHit(Advocate.MASCOT, getPeptideAssumption(peptideHit, i + 1, false));
                        }
                    }
                    if (mascotDecoyPeptideHits != null) {
                        for (PeptideHit peptideHit : mascotDecoyPeptideHits) {
                            currentMatch.addHit(Advocate.MASCOT, getPeptideAssumption(peptideHit, i + 1, true));
                        }
                    }
                    assignedPeptideHits.add(currentMatch);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignedPeptideHits;
    }

    /**
     * parses a peptide assumption out of a peptideHit
     *
     * @param aPeptideHit  the peptide hit to parse
     * @param query        the corresponding query
     * @param decoySection is it in the decoy section?
     * @return a peptide assumption
     */
    private PeptideAssumption getPeptideAssumption(PeptideHit aPeptideHit, int query, boolean decoySection) {
        Double measuredMass = aPeptideHit.getPeptideMr() + aPeptideHit.getDeltaMass();
        ArrayList<ModificationMatch> foundModifications = new ArrayList();
        Modification handledModification;
        int modificationSite;
        for (int l = 0; l < aPeptideHit.getModifications().length; l++) {
            if (l == 0) {
                modificationSite = 1;
            } else if (l > aPeptideHit.getSequence().length()) {
                modificationSite = aPeptideHit.getSequence().length();
            } else {
                modificationSite = l;
            }
            handledModification = aPeptideHit.getModifications()[l];
            if (handledModification != null) {
                // the modification is named mass@residue for later identification
                foundModifications.add(new ModificationMatch(handledModification.getMass() + "@" + aPeptideHit.getSequence().charAt(modificationSite - 1), !handledModification.isFixed(), modificationSite));
            }
        }
        Double mascotEValue = aPeptideHit.getExpectancy();

        ArrayList<String> proteins = new ArrayList();
        for (int j = 0; j < aPeptideHit.getProteinHits().size(); j++) {
            String accession = ((ProteinHit) aPeptideHit.getProteinHits().get(j)).getAccession();
            proteins.add(accession);
        }

        Peptide thePeptide;
        
        try {
            thePeptide = new Peptide(aPeptideHit.getSequence(), proteins, foundModifications); 
        } catch (IllegalArgumentException e) {
            thePeptide = new Peptide(aPeptideHit.getSequence(), aPeptideHit.getPeptideMr(), proteins, foundModifications);
            e.printStackTrace();
        }
        
        PeptideAssumption currentAssumption = new PeptideAssumption(thePeptide, 1, Advocate.MASCOT, measuredMass, mascotEValue, getFileName());
        MascotScore scoreParam = new MascotScore(aPeptideHit.getIonsScore());
        currentAssumption.addUrParam(scoreParam);
        return currentAssumption;
    }
}
