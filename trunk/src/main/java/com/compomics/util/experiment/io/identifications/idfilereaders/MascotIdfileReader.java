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
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.refinementparameters.MascotScore;
import com.compomics.mascotdatfile.util.mascot.Query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.JProgressBar;

/**
 * This reader will import identifications from a Mascot dat file.
 * <p/>
 * @author Marc Vaudel
 */
public class MascotIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The inspected file.
     */
    private File inspectedFile;
    /**
     * Instance of the mascotdatfile parser.
     */
    private MascotDatfileInf iMascotDatfile;

    /**
     * Constructor for the MascotIdfilereader. Using the memory option for the
     * parser.
     *
     * @param aFile a file to read
     */
    public MascotIdfileReader(File aFile) {
        inspectedFile = aFile;
        try {
            iMascotDatfile = MascotDatfileFactory.create(inspectedFile.getCanonicalPath(), MascotDatfileType.MEMORY);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Constructor for the MascotIdilereader.
     *
     * @param aFile a file to read
     * @param index indicating whether the parsing shall be indexed or in memory
     */
    public MascotIdfileReader(File aFile, boolean index) {
        inspectedFile = aFile;
        try {
            if (index) {
                iMascotDatfile = MascotDatfileFactory.create(inspectedFile.getCanonicalPath(), MascotDatfileType.INDEX);
            } else {
                iMascotDatfile = MascotDatfileFactory.create(inspectedFile.getCanonicalPath(), MascotDatfileType.MEMORY);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get the spectrum file name.
     *
     * @return the spectrum file name
     */
    public String getMgfFileName() {
        String temp = iMascotDatfile.getParametersSection().getFile();
        String fileName = Util.getFileName(temp);

        if (!fileName.toLowerCase().endsWith("mgf")
                || fileName.toLowerCase().endsWith("mzml")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".mgf";
        }

        return fileName;
    }

    /**
     * Getter for the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return iMascotDatfile.getFileName();
    }

    @Override
    public HashSet<SpectrumMatch> getAllSpectrumMatches(JProgressBar jProgressBar) throws IOException, IllegalArgumentException, Exception {

        String mgfFileName = getMgfFileName();
        HashSet<SpectrumMatch> assignedPeptideHits = new HashSet<SpectrumMatch>();

        QueryToPeptideMapInf lQueryToPeptideMap = iMascotDatfile.getQueryToPeptideMap();
        QueryToPeptideMapInf lDecoyQueryToPeptideMap = iMascotDatfile.getDecoyQueryToPeptideMap();

        int numberOfQueries = iMascotDatfile.getNumberOfQueries();

        if (jProgressBar != null) {
            jProgressBar.setMaximum(numberOfQueries);
        }

        for (int i = 1; i <= numberOfQueries; i++) {

            Vector<PeptideHit> mascotDecoyPeptideHits = null;
            try {
                mascotDecoyPeptideHits = lDecoyQueryToPeptideMap.getAllPeptideHits(i);
            } catch (Exception e) {
                // Looks like there is no decoy section
            }

            Vector<PeptideHit> mascotPeptideHits = lQueryToPeptideMap.getAllPeptideHits(i);

            // Get spectrum information
            PeptideHit testPeptide = null;

            if (mascotPeptideHits != null) {
                testPeptide = mascotPeptideHits.get(0);
            } else if (mascotDecoyPeptideHits != null) {
                testPeptide = mascotDecoyPeptideHits.get(0);
            }

            if (testPeptide != null) {
                Query tempQuery = iMascotDatfile.getQuery(i);

                String tempName = i + "";
                if (tempQuery.getTitle() != null
                        && !tempQuery.getTitle().startsWith("No title (Query ")) {
                    tempName = tempQuery.getTitle();
                }

                String spectrumId = fixMgfTitle(tempName);
                String measuredCharge = tempQuery.getChargeString();
                String sign = String.valueOf(measuredCharge.charAt(1));
                Charge charge;

                if (sign.compareTo("+") == 0) {
                    charge = new Charge(Charge.PLUS, Integer.valueOf(measuredCharge.substring(0, 1)));
                } else {
                    charge = new Charge(Charge.MINUS, Integer.valueOf(measuredCharge.substring(0, 1)));
                }

                SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(mgfFileName, spectrumId));
                currentMatch.setSpectrumNumber(i); //@TODO: set the spectrum index instead
                HashMap<Double, ArrayList<PeptideHit>> hitMap = new HashMap<Double, ArrayList<PeptideHit>>();

                // Get all hits
                if (mascotPeptideHits != null) {
                    for (PeptideHit peptideHit : mascotPeptideHits) {
                        if (!hitMap.containsKey(peptideHit.getExpectancy())) {
                            hitMap.put(peptideHit.getExpectancy(), new ArrayList<PeptideHit>());
                        }
                        hitMap.get(peptideHit.getExpectancy()).add(peptideHit);
                    }
                }

                if (mascotDecoyPeptideHits != null) {
                    for (PeptideHit peptideHit : mascotDecoyPeptideHits) {
                        if (!hitMap.containsKey(peptideHit.getExpectancy())) {
                            hitMap.put(peptideHit.getExpectancy(), new ArrayList<PeptideHit>());
                        }
                        hitMap.get(peptideHit.getExpectancy()).add(peptideHit);
                    }
                }

                ArrayList<Double> eValues = new ArrayList<Double>(hitMap.keySet());
                Collections.sort(eValues);
                int rank = 1;

                for (Double eValue : eValues) {
                    for (PeptideHit peptideHit : hitMap.get(eValue)) {
                        currentMatch.addHit(Advocate.MASCOT, getPeptideAssumption(peptideHit, charge, rank));
                    }
                    rank += hitMap.get(eValue).size();
                }

                assignedPeptideHits.add(currentMatch);
            }

            if (jProgressBar != null) {
                jProgressBar.setValue(i);
            }
        }

        return assignedPeptideHits;
    }

    /**
     * Parses a peptide assumption out of a peptideHit (this is a separated
     * function because in the good old times I used to parse the target and
     * decoy sections separately. Now, for the sake of search engine
     * compatibility the decoy option should be disabled.)
     *
     * @param aPeptideHit the peptide hit to parse
     * @param charge the corresponding charge
     * @param rank the rank of the peptideHit
     * @return a peptide assumption
     */
    private PeptideAssumption getPeptideAssumption(PeptideHit aPeptideHit, Charge charge, int rank) {

        ArrayList<ModificationMatch> foundModifications = new ArrayList();
        String peptideSequence = aPeptideHit.getSequence();
        int peptideSequenceLength = peptideSequence.length();
        int numberOfModifications = aPeptideHit.getModifications().length;

        int modificationSite;

        for (int l = 0; l < numberOfModifications; l++) {

            if (l == 0) {
                modificationSite = 1;
            } else if (l > peptideSequenceLength) {
                modificationSite = peptideSequenceLength;
            } else {
                modificationSite = l;
            }

            Modification handledModification = aPeptideHit.getModifications()[l];

            if (handledModification != null) {
                // the modification is named mass@residue for later identification
                foundModifications.add(new ModificationMatch(handledModification.getMass() + "@"
                        + peptideSequence.charAt(modificationSite - 1),
                        !handledModification.isFixed(), modificationSite));
            }
        }

        Double mascotEValue = aPeptideHit.getExpectancy();

        int numberOfProteinHits = aPeptideHit.getProteinHits().size();
        ArrayList<String> proteins = new ArrayList(numberOfProteinHits);

        for (int j = 0; j < numberOfProteinHits; j++) {
            String accession = ((ProteinHit) aPeptideHit.getProteinHits().get(j)).getAccession();
            proteins.add(accession);
        }

        Peptide thePeptide;

        try {
            thePeptide = new Peptide(peptideSequence, proteins, foundModifications);
        } catch (IllegalArgumentException e) {
            thePeptide = new Peptide(peptideSequence, aPeptideHit.getPeptideMr(), proteins, foundModifications);
            e.printStackTrace();
        }

        PeptideAssumption currentAssumption = new PeptideAssumption(thePeptide, rank, Advocate.MASCOT, charge, mascotEValue, getFileName());
        MascotScore scoreParam = new MascotScore(aPeptideHit.getIonsScore());
        currentAssumption.addUrParam(scoreParam);

        return currentAssumption;
    }

    /**
     * Returns the fixed mgf title.
     *
     * @param spectrumTitle
     * @return the fixed mgf title
     */
    private String fixMgfTitle(String spectrumTitle) {

        // a special fix for mgf files with titles containing %3b instead if ;
        spectrumTitle = spectrumTitle.replaceAll("%3b", ";");

        // a special fix for mgf files with titles containing \\ instead \
        spectrumTitle = spectrumTitle.replaceAll("\\\\\\\\", "\\\\");

        return spectrumTitle;
    }

    @Override
    public void close() throws IOException {
        iMascotDatfile.finish();
        iMascotDatfile = null;
    }
}
