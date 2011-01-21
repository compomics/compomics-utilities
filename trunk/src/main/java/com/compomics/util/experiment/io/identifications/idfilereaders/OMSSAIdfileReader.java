package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumCollection;
import com.compomics.util.experiment.personalization.ExperimentObject;
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
     * The spectrum collection to complete
     */
    private SpectrumCollection spectrumCollection = null;

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

        modsFile = null;
        userModsFile = null;
        File currentFolder = new File(idFile.getParent());
        File[] modsResult = currentFolder.listFiles();
        if (modsResult != null) {
            for (File file : modsResult) {
                if (file.getName().compareToIgnoreCase("mods.xml") == 0) {
                    modsFile = file;
                }
                if (file.getName().compareToIgnoreCase("usermods.xml") == 0) {
                    userModsFile = file;
                }
            }
        }
    }

    /**
     * Constructor for the reader with a spectrum collection where to put spectrum information in.
     *
     * @param idFile    the inspected file
     * @param spectrumCollection the spectrum collection used
     */
    public OMSSAIdfileReader(File idFile, SpectrumCollection spectrumCollection) {
        this.spectrumCollection = spectrumCollection;
        this.identificationFile = idFile;

        modsFile = null;
        userModsFile = null;
        File currentFolder = new File(idFile.getParent());
        File[] modsResult = currentFolder.listFiles();
        if (modsResult != null) {
            for (File file : modsResult) {
                if (file.getName().compareToIgnoreCase("mods.xml") == 0) {
                    modsFile = file;
                }
                if (file.getName().compareToIgnoreCase("usermods.xml") == 0) {
                    userModsFile = file;
                }
            }
        }
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
            OmssaOmxFile omxFile = getParserInstance();
            List<MSResponse> msSearchResponse = omxFile.getParserResult().MSSearch_response.MSResponse;
            List<MSRequest> msRequest = omxFile.getParserResult().MSSearch_request.MSRequest;
            HashMap peptideToProteinMap = omxFile.getPeptideToProteinMap();
            for (int i = 0; i < msSearchResponse.size(); i++) {
                msResponseScale = msSearchResponse.get(i).MSResponse_scale;

                Map<Integer, MSHitSet> msHitSetMap = msSearchResponse.get(i).MSResponse_hitsets.MSHitSet;

                for (MSHitSet msHitSet : msHitSetMap.values()) {

                    File tempFile = new File(msRequest.get(i).MSRequest_settings.MSSearchSettings.MSSearchSettings_infiles.MSInFile.MSInFile_infile);
                    String name = msHitSet.MSHitSet_ids.MSHitSet_ids_E.get(0);
                    List<MSHits> hitSet = msHitSet.MSHitSet_hits.MSHits;
                    if (hitSet.size() > 0) {
                        MSHits currentMsHit = hitSet.get(0);
                        boolean singleBestHit = true;
                        for (MSHits msHits : hitSet) {  // We keep the best scoring peptide and discard ambiguous cases
                            if (msHits.MSHits_evalue < currentMsHit.MSHits_evalue) {
                                currentMsHit = msHits;
                            } else if ((msHits.MSHits_evalue == currentMsHit.MSHits_evalue) && (msHits.MSHits_pepstring.compareTo(currentMsHit.MSHits_pepstring) != 0)) {
                                singleBestHit = false;
                            }
                        }
                        if (singleBestHit) {
                            Double calcMass = ((double) currentMsHit.MSHits_theomass) / msResponseScale;
                            Double expMass = ((double) currentMsHit.MSHits_mass) / msResponseScale;
                            double deltaMass = Math.abs(1000000 * (expMass - calcMass) / calcMass);

                            ArrayList<Protein> proteins = new ArrayList();
                            boolean reverse = true;
                            for (MSPepHit msPepHit : (List<MSPepHit>) peptideToProteinMap.get(currentMsHit.MSHits_pepstring)) {       // There might be redundancies in the map.
                                Boolean taken = false;
                                String description = msPepHit.MSPepHit_defline;
                                String accession = getProteinAccession(description);
                                for (Protein protein : proteins) {
                                    if (protein.getAccession().compareTo(accession) == 0) {
                                        taken = true;
                                        break;
                                    }
                                }
                                if (!accession.startsWith("REV") && !accession.endsWith("_REV") && !accession.endsWith("_REVERSED")) {
                                    reverse = false;
                                }
                                if (!taken) {
                                    proteins.add(new Protein(accession, description, reverse));
                                }
                            }

                            Charge charge = new Charge(Charge.PLUS, currentMsHit.MSHits_charge);
                            Precursor precursor = new Precursor(-1, expMass, charge);     // RT is not known at the stage of the development
                            MSnSpectrum spectrum = new MSnSpectrum(2, precursor, name, tempFile.getName());
                            String spectrumKey = spectrum.getSpectrumKey();
                            if (spectrumCollection != null) {
                                spectrumCollection.addSpectrum(spectrum);
                            }
                            List<MSModHit> msModHits = currentMsHit.MSHits_mods.MSModHit;
                            ArrayList<ModificationMatch> modificationsFound = new ArrayList();
                            PTM currentPTM;
                            // inspect variable modifications
                            for (MSModHit msModHit : msModHits) {
                                int msMod = msModHit.MSModHit_modtype.MSMod;
                                currentPTM = ptmFactory.getPTM(msMod);
                                int location = msModHit.MSModHit_site;
                                modificationsFound.add(new ModificationMatch(currentPTM, true, location));
                            }
                            // inspect fixed modifications
                            List<Integer> fixedMods = msRequest.get(i).MSRequest_settings.MSSearchSettings.MSSearchSettings_fixed.MSMod;
                            String tempSequence;
                            for (int msMod : fixedMods) {
                                currentPTM = ptmFactory.getPTM(msMod);
                                String[] residuesArray = currentPTM.getResiduesArray();
                                for (String location : residuesArray) {
                                    tempSequence = currentMsHit.MSHits_pepstring;
                                    if (location.compareTo("[") == 0) {
                                        modificationsFound.add(new ModificationMatch(currentPTM, false, 1));
                                    } else if (location.compareTo("]") == 0) {
                                        modificationsFound.add(new ModificationMatch(currentPTM, false, tempSequence.length()));
                                    } else {
                                        tempSequence = "#" + tempSequence + "#";
                                        String[] sequenceFragments = tempSequence.split(location);
                                        if (sequenceFragments.length > 0) {
                                            int cpt = 0;
                                            for (int f = 0; f < sequenceFragments.length - 1; f++) {
                                                cpt = cpt + sequenceFragments[f].length();
                                                modificationsFound.add(new ModificationMatch(currentPTM, false, cpt));
                                            }
                                        }
                                    }
                                }
                            }
                            double eValue = currentMsHit.MSHits_evalue;
                            Peptide thePeptide = new Peptide(currentMsHit.MSHits_pepstring, calcMass, proteins, modificationsFound);
                            PeptideAssumption currentAssumption = new PeptideAssumption(thePeptide, 1, Advocate.OMSSA, deltaMass, eValue, getFileName());
                            // secondary hits are not implemented yet
                            SpectrumMatch currentMatch = new SpectrumMatch(spectrumKey, currentAssumption);
                            assignedSpectra.add(currentMatch);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignedSpectra;
    }

    /**
     * parses omssa description to have the accession
     *
     * @param description   the protein description
     * @return the protein accession
     */
    private String getProteinAccession(String description) {
        try {
            int start = description.indexOf("|");
            int end = description.indexOf("|", ++start);
            return description.substring(start, end);
        } catch (Exception e) {
            int end = description.indexOf(" ");
            return description.substring(0, end);
        }
    }

    /**
     * gives the parser instance
     *
     * @return an omssa omx file
     */
    private OmssaOmxFile getParserInstance() {
        OmssaOmxFile omxFile;
        if (modsFile != null && userModsFile != null) {
            omxFile = new OmssaOmxFile(identificationFile.getPath(), modsFile.getPath(), userModsFile.getPath());
        } else if (modsFile == null && userModsFile != null) {
            omxFile = new OmssaOmxFile(identificationFile.getPath(), null, userModsFile.getPath());
        } else if (modsFile != null) {
            omxFile = new OmssaOmxFile(identificationFile.getPath(), modsFile.getPath(), null);
        } else {
            omxFile = new OmssaOmxFile(identificationFile.getPath(), null, null);
        }
        return omxFile;
    }
}
