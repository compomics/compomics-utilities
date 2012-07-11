package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.Util;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.*;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.gui.waiting.WaitingHandler;
import com.compomics.util.protein.Header;
import de.proteinms.xtandemparser.interfaces.Modification;
import de.proteinms.xtandemparser.xtandem.*;
import de.proteinms.xtandemparser.xtandem.Spectrum;
import java.io.IOException;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * This reader will import identifications from an X!Tandem xml result file.
 * <p/>
 * @author Marc Vaudel
 */
public class XTandemIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * The instance of the X!Tandem parser.
     */
    private XTandemFile xTandemFile = null;
    /**
     * The modification map.
     */
    private ModificationMap modificationMap;
    /**
     * The protein map.
     */
    private ProteinMap proteinMap;
    /**
     * The peptide map.
     */
    private PeptideMap peptideMap;

    /**
     * Constructor for the reader.
     */
    public XTandemIdfileReader() {
    }

    /**
     * Xonstructor for the reader.
     *
     * @param aFile the inspected file
     * @throws SAXException
     */
    public XTandemIdfileReader(File aFile) throws SAXException {
        if (!aFile.getName().endsWith("mods.xml") || !aFile.getName().endsWith("usermods.xml")) {
            xTandemFile = new XTandemFile(aFile.getPath());
            modificationMap = xTandemFile.getModificationMap();
            proteinMap = xTandemFile.getProteinMap();
            peptideMap = xTandemFile.getPeptideMap();
        }
    }

    /**
     * Getter for the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        File tempFile = new File(xTandemFile.getFileName());
        return tempFile.getName();
    }

    @Override
    public HashSet<SpectrumMatch> getAllSpectrumMatches(WaitingHandler waitingHandler) throws IOException, IllegalArgumentException, Exception {

        HashSet<SpectrumMatch> foundPeptides = new HashSet<SpectrumMatch>();

        Iterator<Spectrum> spectraIt = xTandemFile.getSpectraIterator();

        if (waitingHandler != null) {
            waitingHandler.setMaxSecondaryProgressValue(xTandemFile.getSpectraNumber());
        }
        PeptideAssumption newAssumption;

        while (spectraIt.hasNext()) {
            Spectrum currentSpectrum = spectraIt.next();
            // spectrumId is the spectrum id in the X!Tandem file and spectrumNumber the id in the parser. If anyone could write a simpler parser, would be just great.
            int spectrumId = currentSpectrum.getSpectrumId();
            int spectrumNumber = currentSpectrum.getSpectrumNumber();
            SupportData supportData = xTandemFile.getSupportData(spectrumNumber);

            String tempName = spectrumId + "";
            if (supportData.getFragIonSpectrumDescription() != null) {
                tempName = supportData.getFragIonSpectrumDescription();
            }

            String spectrumName = fixMgfTitle(tempName);

            ArrayList<Peptide> spectrumPeptides = peptideMap.getAllPeptides(currentSpectrum.getSpectrumNumber());

            if (spectrumPeptides.size() > 0) {

                String tempFile = xTandemFile.getInputParameters().getSpectrumPath();
                String filename = Util.getFileName(tempFile);
                Charge charge = new Charge(Charge.PLUS, currentSpectrum.getPrecursorCharge());
                String spectrumKey = com.compomics.util.experiment.massspectrometry.Spectrum.getSpectrumKey(filename, spectrumName);
                SpectrumMatch currentMatch = new SpectrumMatch(spectrumKey);
                currentMatch.setSpectrumNumber(spectrumId); //@TODO: verify that this work when sorting spectra according to proteins
                HashMap<Double, ArrayList<Domain>> hitMap = new HashMap<Double, ArrayList<Domain>>();

                for (Peptide peptide : spectrumPeptides) {
                    for (Domain domain : peptide.getDomains()) {
                        if (!hitMap.containsKey(domain.getDomainExpect())) {
                            hitMap.put(domain.getDomainExpect(), new ArrayList<Domain>());
                        }
                        hitMap.get(domain.getDomainExpect()).add(domain);
                    }
                }

                ArrayList<Double> eValues = new ArrayList<Double>(hitMap.keySet());
                Collections.sort(eValues);
                int rankIncrease, rank = 1;
                boolean found;
                for (Double eValue : eValues) {
                    rankIncrease = 0;
                    for (Domain domain : hitMap.get(eValue)) {
                        newAssumption = getPeptideAssumption(domain, charge.value, rank);
                        found = false;
                        for (PeptideAssumption loadedAssumption : currentMatch.getAllAssumptions()) {
                            if (loadedAssumption.getPeptide().isSameAs(newAssumption.getPeptide())) {
                                for (String protein : newAssumption.getPeptide().getParentProteins()) {
                                    if (!loadedAssumption.getPeptide().getParentProteins().contains(protein)) {
                                        loadedAssumption.getPeptide().getParentProteins().add(protein);
                                    }
                                }
                                for (String protein : loadedAssumption.getPeptide().getParentProteins()) {
                                    if (!newAssumption.getPeptide().getParentProteins().contains(protein)) {
                                        newAssumption.getPeptide().getParentProteins().add(protein);
                                    }
                                }
                                if (loadedAssumption.getPeptide().sameModificationsAs(newAssumption.getPeptide())) {
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            rankIncrease++;
                            currentMatch.addHit(Advocate.XTANDEM, newAssumption);
                        }
                    }
                    rank += rankIncrease;
                }

                foundPeptides.add(currentMatch);
            }

            if (waitingHandler != null) {
                if (waitingHandler.isRunCanceled()) {
                    break;
                }
                waitingHandler.increaseSecondaryProgressValue();
            }
        }

        return foundPeptides;
    }

    /**
     * Returns a utilities peptide assumption from an X!Tandem peptide.
     *
     * @param domain the domain of the X!Tandem peptide
     * @param charge the charge of the precursor of the inspected spectrum
     * @param rank the rank of the peptide hit
     * @return the corresponding peptide assumption
     */
    private PeptideAssumption getPeptideAssumption(Domain domain, int charge, int rank) {

        ArrayList<String> proteins = new ArrayList<String>();
        String sequence = domain.getDomainSequence();
        String description = proteinMap.getProtein(domain.getProteinKey()).getLabel();
        String accession;

        try {
            Header fastaHeader = Header.parseFromFASTA(description);
            accession = fastaHeader.getAccession();
            if (accession == null) {
                accession = fastaHeader.getRest();
            }
        } catch (Exception e) {
            accession = description;
        }

        // final test to check that the accession number was correctly parsed
        if (accession == null) {
            JOptionPane.showMessageDialog(null, "Unable to extract the accession number from protein description: \n"
                    + "\'" + description + "\'"
                    + "\n\nVerify your FASTA file!", "Unknown Protein!", JOptionPane.ERROR_MESSAGE);

            throw new IllegalArgumentException(
                    "Unable to extract the accession number from protein description: \n"
                    + "\'" + description + "\'.\n"
                    + "Please verify your FASTA file!");
        }

        proteins.add(accession);

        ArrayList<Modification> foundFixedModifications = modificationMap.getFixedModifications(domain.getDomainKey());
        ArrayList<ModificationMatch> foundModifications = new ArrayList<ModificationMatch>();

        for (Modification currentModification : foundFixedModifications) {

            String modificationName = currentModification.getName();
            String[] parsedName = modificationName.split("@");
            String aa = parsedName[1].toUpperCase();

            if (aa.equals("[")) {
                foundModifications.add(new ModificationMatch(modificationName, false, 0));
            } else if (aa.equals("]")) {
                foundModifications.add(new ModificationMatch(modificationName, false, sequence.length() - 1));
            } else {
                String tempSequence = "#" + sequence + "#";
                String[] sequenceFragments = tempSequence.split(aa);

                if (sequenceFragments.length > 0) {
                    int cpt = 0;
                    for (int f = 0; f < sequenceFragments.length - 1; f++) {
                        cpt += sequenceFragments[f].length() + 1;
                        foundModifications.add(new ModificationMatch(modificationName, false, cpt - 1));
                    }
                }
            }
        }

        ArrayList<de.proteinms.xtandemparser.interfaces.Modification> foundVariableModifications = modificationMap.getVariableModifications(domain.getDomainKey());

        for (Modification currentModification : foundVariableModifications) {
            int location = new Integer(currentModification.getLocation()) - domain.getDomainStart() + 1;
            foundModifications.add(new ModificationMatch(currentModification.getName(), true, location));
        }

        com.compomics.util.experiment.biology.Peptide peptide = new com.compomics.util.experiment.biology.Peptide(sequence, proteins, foundModifications);
        return new PeptideAssumption(peptide, rank, Advocate.XTANDEM, new Charge(Charge.PLUS, charge), domain.getDomainExpect(), getFileName());
    }

    /**
     * Returns the fixed mgf title.
     *
     * @param spectrumTitle
     * @return the fixed mgf title
     */
    private String fixMgfTitle(String spectrumTitle) {

        // a special fix for mgf files with titles containing %3b instead of ;
        spectrumTitle = spectrumTitle.replaceAll("%3b", ";");

        // a special fix for mgf files with titles containing \\ instead \
        spectrumTitle = spectrumTitle.replaceAll("\\\\\\\\", "\\\\");

        return spectrumTitle;
    }

    @Override
    public void close() throws IOException {
        xTandemFile = null;
    }
}
