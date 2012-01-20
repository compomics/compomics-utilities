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
     * Constructor for the MascotIdfilereader
     *
     * @param aFile a file to read
     */
    public MascotIdfileReader(File aFile) {
        inspectedFile = aFile;
        try {
            iMascotDatfile = MascotDatfileFactory.create(inspectedFile.getCanonicalPath(), MascotDatfileType.MEMORY);
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
        String fileName = Util.getFileName(temp);
        
        if (!fileName.toLowerCase().endsWith("mgf")
                || fileName.toLowerCase().endsWith("mzml")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".mgf";
        }
        
        return fileName;
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
        
        String mgfFileName = getMgfFileName();
        HashSet<SpectrumMatch> assignedPeptideHits = new HashSet<SpectrumMatch>();
        
        try {
            QueryToPeptideMapInf lQueryToPeptideMap = iMascotDatfile.getQueryToPeptideMap();
            //QueryToPeptideMapInf lDecoyQueryToPeptideMap = iMascotDatfile.getDecoyQueryToPeptideMap(false); // @TODO: this method does not exist but ought to, 'false' would be used to remove the 'no decoys found' message
            QueryToPeptideMapInf lDecoyQueryToPeptideMap = iMascotDatfile.getDecoyQueryToPeptideMap();
            
            int numberOfQueries = iMascotDatfile.getNumberOfQueries();
            
            for (int i = 0; i < numberOfQueries; i++) {
                
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
                    Query tempQuery = iMascotDatfile.getQuery(i + 1);
                    String spectrumId = tempQuery.getTitle();
                    String measuredCharge = tempQuery.getChargeString();
                    String sign = String.valueOf(measuredCharge.charAt(1));
                    Charge charge;
                    
                    if (sign.compareTo("+") == 0) {
                        charge = new Charge(Charge.PLUS, Integer.valueOf(measuredCharge.substring(0, 1)));
                    } else {
                        charge = new Charge(Charge.MINUS, Integer.valueOf(measuredCharge.substring(0, 1)));
                    }
                    
                    SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(mgfFileName, spectrumId));
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return assignedPeptideHits;
    }

    /**
     * parses a peptide assumption out of a peptideHit
     * (this is a separated function because in the good old times I used to parse the target and decoy sections separately. Now, for the sake of search engine compatibility the decoy option should be disabled.)
     *
     * @param aPeptideHit  the peptide hit to parse
     * @param charge       the corresponding charge
     * @param rank         the rank of the peptideHit
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
}
