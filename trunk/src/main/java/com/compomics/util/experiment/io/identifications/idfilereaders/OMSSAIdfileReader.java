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
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.protein.Header;
import de.proteinms.omxparser.OmssaOmxFile;
import de.proteinms.omxparser.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
     * the scale used in OMSSA response
     */
    private Integer msResponseScale;
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

    /**
     * returns all spectrum matches found in the inspected file
     *
     * @return a set of all spectrum matches
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches() {
        HashSet<SpectrumMatch> assignedSpectra = new HashSet<SpectrumMatch>();
        try {
            List<MSResponse> msSearchResponse = omxFile.getParserResult().MSSearch_response.MSResponse;
            List<MSRequest> msRequest = omxFile.getParserResult().MSSearch_request.MSRequest;
            for (int i = 0; i < msSearchResponse.size(); i++) {
                msResponseScale = msSearchResponse.get(i).MSResponse_scale;
                Map<Integer, MSHitSet> msHitSetMap = msSearchResponse.get(i).MSResponse_hitsets.MSHitSet;
                String tempFile = msRequest.get(i).MSRequest_settings.MSSearchSettings.MSSearchSettings_infiles.MSInFile.MSInFile_infile;
                for (MSHitSet msHitSet : msHitSetMap.values()) {
                    List<MSHits> hitSet = msHitSet.MSHitSet_hits.MSHits;
                    if (hitSet.size() > 0) {
                        MSHits bestMsHit = hitSet.get(0);
                        for (MSHits msHits : hitSet) {
                            if (msHits.MSHits_evalue < bestMsHit.MSHits_evalue) {
                                bestMsHit = msHits;
                            }
                        }
                        Charge charge = new Charge(Charge.PLUS, bestMsHit.MSHits_charge);
                        String name = msHitSet.MSHitSet_ids.MSHitSet_ids_E.get(0);
                        Double expMass = ((double) bestMsHit.MSHits_mass) / msResponseScale;
                        Precursor precursor = new Precursor(-1, expMass, charge);
                        String filename = Util.getFileName(tempFile);
                        MSnSpectrum spectrum = new MSnSpectrum(2, precursor, name, filename);
                        String spectrumKey = spectrum.getSpectrumKey();
                        SpectrumMatch currentMatch = new SpectrumMatch(spectrumKey, getPeptideAssumption(bestMsHit, i));
                        for (MSHits msHits : hitSet) {
                            if (msHits != bestMsHit) {
                                currentMatch.addHit(Advocate.OMSSA, getPeptideAssumption(msHits, i));
                            }
                        }
                        assignedSpectra.add(currentMatch);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignedSpectra;
    }

    private PeptideAssumption getPeptideAssumption(MSHits currentMsHit, int responseIndex) {
        List<MSRequest> msRequest = omxFile.getParserResult().MSSearch_request.MSRequest;

        Double calcMass = ((double) currentMsHit.MSHits_theomass) / msResponseScale;
        Double expMass = ((double) currentMsHit.MSHits_mass) / msResponseScale;

        ArrayList<String> proteins = new ArrayList<String>();
        HashMap peptideToProteinMap = omxFile.getPeptideToProteinMap();
        for (MSPepHit msPepHit : (List<MSPepHit>) peptideToProteinMap.get(currentMsHit.MSHits_pepstring)) {       // There might be redundancies in the map.
            Boolean taken = false;
            String accession = getProteinAccession(msPepHit.MSPepHit_defline);
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
        ArrayList<ModificationMatch> modificationsFound = new ArrayList();
        PTM currentPTM;
        // inspect variable modifications
        for (MSModHit msModHit : msModHits) {
            int msMod = msModHit.MSModHit_modtype.MSMod;
            currentPTM = ptmFactory.getPTM(msMod);   // This has to be changed if the mod file is not OMSSA based anymore
            int location = msModHit.MSModHit_site + 1;
            modificationsFound.add(new ModificationMatch(currentPTM.getName(), true, location));
        }
        // inspect fixed modifications
        List<Integer> fixedMods = msRequest.get(responseIndex).MSRequest_settings.MSSearchSettings.MSSearchSettings_fixed.MSMod;
        String tempSequence;
        for (int msMod : fixedMods) {
            currentPTM = ptmFactory.getPTM(msMod);
            ArrayList<String> residuesArray = currentPTM.getResidues();
            for (String location : residuesArray) {
                tempSequence = currentMsHit.MSHits_pepstring;
                if (location.compareTo("[") == 0) {
                    modificationsFound.add(new ModificationMatch(currentPTM.getName(), false, 1));
                } else if (location.compareTo("]") == 0) {
                    modificationsFound.add(new ModificationMatch(currentPTM.getName(), false, tempSequence.length()));
                } else {
                    tempSequence = "#" + tempSequence + "#";
                    String[] sequenceFragments = tempSequence.split(location);
                    if (sequenceFragments.length > 0) {
                        int cpt = 0;
                        for (int f = 0; f < sequenceFragments.length - 1; f++) {
                            cpt = cpt + sequenceFragments[f].length();
                            modificationsFound.add(new ModificationMatch(currentPTM.getName(), false, cpt));
                        }
                    }
                }
            }
        }
        double eValue = currentMsHit.MSHits_evalue;
        Peptide thePeptide = new Peptide(currentMsHit.MSHits_pepstring, calcMass, proteins, modificationsFound);
        return new PeptideAssumption(thePeptide, 1, Advocate.OMSSA, expMass, eValue, getFileName());
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
                return header.getRest();
            }
        } catch (Exception e) {
            return description.substring(1);
        }
    }
}
