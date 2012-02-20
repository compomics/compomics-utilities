package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.protein.Header;
import de.proteinms.omxparser.OmssaOmxFile;
import de.proteinms.omxparser.util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JProgressBar;

/**
 * This reader will import identifications from an OMSSA omx file.
 *
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 9:45:45 AM
 */
public class OMSSAIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * the inspected OMSSA omx file
     */
    private File identificationFile;
    /**
     * the modification file mods.xml
     */
    private File modsFile;
    /**
     * the modification file usermods.xml
     */
    private File userModsFile;
    /**
     * the PTM factory
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The instance of the inspected omx file
     */
    private OmssaOmxFile omxFile;

    /**
     * constructor for the reader
     */
    public OMSSAIdfileReader() {
    }

    /**
     * Constructor for the reader
     *
     * @param idFile    the inspected file
     */
    public OMSSAIdfileReader(File idFile) {
        this.identificationFile = idFile;
        omxFile = new OmssaOmxFile(idFile.getPath(), false);
    }

    /**
     * get the file name
     *
     * @return the file name
     */
    public String getFileName() {
        if (modsFile != null && userModsFile != null) {
            return identificationFile.getName().concat(", ").concat(modsFile.getName()).concat(", ").concat(userModsFile.getName());
        } else if (modsFile != null) {
            return identificationFile.getName().concat(", ").concat(modsFile.getName());
        } else if (userModsFile != null) {
            return identificationFile.getName().concat(", ").concat(userModsFile.getName());
        } else {
            return identificationFile.getName();
        }
    }

    @Override
    public HashSet<SpectrumMatch> getAllSpectrumMatches(JProgressBar jProgressBar) {

        HashSet<SpectrumMatch> assignedSpectra = new HashSet<SpectrumMatch>();
        HashMap<String,LinkedList<MSPepHit>> peptideToProteinMap = omxFile.getPeptideToProteinMap();

        try {
            List<MSResponse> msSearchResponse = omxFile.getParserResult().MSSearch_response.MSResponse;
            List<MSRequest> msRequest = omxFile.getParserResult().MSSearch_request.MSRequest;

            int searchResponseSize = msSearchResponse.size();
            
            if (jProgressBar != null) {
                jProgressBar.setMaximum(searchResponseSize);
            }

            for (int i = 0; i < searchResponseSize; i++) {

                Map<Integer, MSHitSet> msHitSetMap = msSearchResponse.get(i).MSResponse_hitsets.MSHitSet;
                String tempFile = msRequest.get(i).MSRequest_settings.MSSearchSettings.MSSearchSettings_infiles.MSInFile.MSInFile_infile;

                for (MSHitSet msHitSet : msHitSetMap.values()) {

                    List<MSHits> hitSet = msHitSet.MSHitSet_hits.MSHits;

                    if (hitSet.size() > 0) {

                        HashMap<Double, ArrayList<MSHits>> hitMap = new HashMap<Double, ArrayList<MSHits>>();

                        for (MSHits msHits : hitSet) {
                            if (!hitMap.containsKey(msHits.MSHits_evalue)) {
                                hitMap.put(msHits.MSHits_evalue, new ArrayList<MSHits>());
                            }
                            hitMap.get(msHits.MSHits_evalue).add(msHits);
                        }

                        ArrayList<Double> eValues = new ArrayList<Double>(hitMap.keySet());
                        Collections.sort(eValues);
                        String name = fixMgfTitle(msHitSet.MSHitSet_ids.MSHitSet_ids_E.get(0));
                        SpectrumMatch currentMatch = new SpectrumMatch(Spectrum.getSpectrumKey(Util.getFileName(tempFile), name));
                        int rank = 1;

                        for (double eValue : eValues) {
                            for (MSHits msHits : hitMap.get(eValue)) {
                                currentMatch.addHit(Advocate.OMSSA, getPeptideAssumption(msHits, i, rank, peptideToProteinMap, msRequest));
                            }
                            rank += hitMap.get(eValue).size();
                        }

                        assignedSpectra.add(currentMatch);
                    }
                }
                
                if (jProgressBar != null) {
                    jProgressBar.setValue(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignedSpectra;
    }

    /**
     * Returns a peptide assumption based on the OMSSA MSHits
     * @param currentMsHit  the MSHits of interest
     * @param responseIndex the response index in the msrequest
     * @param rank          the rank of the assumption in the spectrum match
     * @return the corresponding peptide assumption
     */
    private PeptideAssumption getPeptideAssumption(MSHits currentMsHit, int responseIndex, int rank, 
            HashMap<String,LinkedList<MSPepHit>> peptideToProteinMap, List<MSRequest> msRequest) {

        Charge charge = new Charge(Charge.PLUS, currentMsHit.MSHits_charge);
        ArrayList<String> proteins = new ArrayList<String>();
        
        for (MSPepHit msPepHit : (List<MSPepHit>) peptideToProteinMap.get(currentMsHit.MSHits_pepstring)) { // There might be redundancies in the map.
            
            Boolean taken = false;
            String accession = getProteinAccession(msPepHit.MSPepHit_defline);

            if (accession == null) {
                accession = msPepHit.MSPepHit_accession;
            }

            for (String protein : proteins) {
                if (protein.compareTo(accession) == 0) {
                    taken = true;
                    break;
                }
            }

            if (!taken) {
                proteins.add(accession);
            }
        }

        List<MSModHit> msModHits = currentMsHit.MSHits_mods.MSModHit;
        ArrayList<ModificationMatch> modificationsFound = new ArrayList<ModificationMatch>();

        // inspect variable modifications
        for (MSModHit msModHit : msModHits) {
            int msMod = msModHit.MSModHit_modtype.MSMod;
            PTM currentPTM = ptmFactory.getPTM(msMod);   // This has to be changed if the mod file is not OMSSA based anymore
            int location = msModHit.MSModHit_site + 1;
            modificationsFound.add(new ModificationMatch(currentPTM.getName(), true, location));
        }

        // inspect fixed modifications
        List<Integer> fixedMods = msRequest.get(responseIndex).MSRequest_settings.MSSearchSettings.MSSearchSettings_fixed.MSMod;

        String tempSequence = currentMsHit.MSHits_pepstring;
        int tempSequenceLength = tempSequence.length();
        
        for (int msMod : fixedMods) {

            PTM currentPTM = ptmFactory.getPTM(msMod);
            ArrayList<String> residuesArray = currentPTM.getResidues();
            String currentPtmName = currentPTM.getName();

            for (String location : residuesArray) {
                
                if (location.compareTo("[") == 0) {
                    modificationsFound.add(new ModificationMatch(currentPtmName, false, 1));
                } else if (location.compareTo("]") == 0) {
                    modificationsFound.add(new ModificationMatch(currentPtmName, false, tempSequenceLength));
                } else {
                    tempSequence = "#" + tempSequence + "#";
                    String[] sequenceFragments = tempSequence.split(location);
                    
                    if (sequenceFragments.length > 0) {
                        int cpt = 0;
                        for (int f = 0; f < sequenceFragments.length - 1; f++) {
                            cpt = cpt + sequenceFragments[f].length();
                            modificationsFound.add(new ModificationMatch(currentPtmName, false, cpt));
                        }
                    }
                }
            }
        }

        Peptide thePeptide = new Peptide(currentMsHit.MSHits_pepstring, proteins, modificationsFound);
        return new PeptideAssumption(thePeptide, rank, Advocate.OMSSA, charge, currentMsHit.MSHits_evalue, getFileName());
    }

    /**
     * parses omssa description to have the accession
     *
     * @param description   the protein description
     * @return the protein accession
     */
    private String getProteinAccession(String description) {
        try {
            Header header = Header.parseFromFASTA(description);
            if (header.getAccession() != null) {
                return header.getAccession();
            } else {
                return null;
            }
        } catch (Exception e) {
            return description.substring(1);
        }
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
        omxFile = null;
    }
}
