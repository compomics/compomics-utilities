package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SequenceDataBase;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.*;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.protein.Header;
import de.proteinms.xtandemparser.interfaces.Modification;
import de.proteinms.xtandemparser.xtandem.*;
import de.proteinms.xtandemparser.xtandem.Spectrum;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JOptionPane;

/**
 * This reader will import identifications from an X!Tandem xml result file.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 9:45:54 AM
 */
public class XTandemIdfileReader extends ExperimentObject implements IdfileReader {

    /**
     * the instance of the X!Tandem parser
     */
    private XTandemFile xTandemFile = null;
    /**
     * the modification map
     */
    private ModificationMap modificationMap;
    /**
     * the protein map
     */
    private ProteinMap proteinMap;
    /**
     * the peptide map
     */
    private PeptideMap peptideMap;

    /**
     * Constructor for the reader
     */
    public XTandemIdfileReader() {
    }

    /**
     * constructor for the reader
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
     * getter for the file name
     *
     * @return the file name
     */
    public String getFileName() {
        File tempFile = new File(xTandemFile.getFileName());
        return tempFile.getName();
    }

    /**
     * Method which returns all spectrum matches found in the file.
     *
     * @return a set containing all spectrum matches
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches() {
        HashSet<SpectrumMatch> foundPeptides = new HashSet<SpectrumMatch>();
        try {
            Iterator<Spectrum> spectraIt = xTandemFile.getSpectraIterator();
            while (spectraIt.hasNext()) {
                Spectrum currentSpectrum = spectraIt.next();
                int nSpectrum = currentSpectrum.getSpectrumNumber();
                SupportData supportData = xTandemFile.getSupportData(nSpectrum);
                String spectrumName = supportData.getFragIonSpectrumDescription();
                ArrayList<Peptide> spectrumPeptides = peptideMap.getAllPeptides(currentSpectrum.getSpectrumNumber());
                if (spectrumPeptides.size() > 0) {
                    Peptide testPeptide = spectrumPeptides.get(0);
                    Domain testDomain = testPeptide.getDomains().get(0);
                    File tempFile = new File(xTandemFile.getInputParameters().getSpectrumPath());
                    String filename = tempFile.getName();
                    Charge charge = new Charge(Charge.PLUS, currentSpectrum.getPrecursorCharge());
                    double measuredMass = testDomain.getDomainMh() + testDomain.getDomainDeltaMh();
                    Precursor precursor = new Precursor(-1, measuredMass, charge); // The retention time is not known at this stage
                    MSnSpectrum spectrum = new MSnSpectrum(2, precursor, spectrumName, filename);
                    String spectrumKey = spectrum.getSpectrumKey();
                    SpectrumMatch currentMatch = new SpectrumMatch(spectrumKey);

                    for (Peptide peptide : spectrumPeptides) {
                        for (Domain domain : peptide.getDomains()) {
                            currentMatch.addHit(Advocate.XTANDEM, getPeptideAssumption(domain));
                        }
                    }
                    foundPeptides.add(currentMatch);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foundPeptides;
    }

    /**
     * Returns a utilities peptide assumption from an X!Tandem peptide.
     * 
     * @param domain the domain of the X!Tandem peptide
     * @return the corresponding peptide assumption
     */
    private PeptideAssumption getPeptideAssumption(Domain domain) {
        ArrayList<Protein> proteins = new ArrayList<Protein>();
        double eValue;
        com.compomics.util.experiment.biology.Peptide peptide;
        double measuredMass = domain.getDomainMh() + domain.getDomainDeltaMh();
        String sequence = domain.getDomainSequence();
        String description = proteinMap.getProteinWithPeptideID(domain.getDomainID()).getLabel();
        String accession = "";
        try {
            Header fastaHeader = Header.parseFromFASTA(description);
            accession = fastaHeader.getAccession();
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
        
        proteins.add(new Protein(accession, SequenceDataBase.isDecoy(accession)));
        eValue = domain.getDomainExpect();
        ArrayList<Modification> foundFixedModifications = modificationMap.getFixedModifications(domain.getDomainID());
        PTM currentPTM;
        ArrayList<ModificationMatch> foundModifications = new ArrayList<ModificationMatch>();
        for (Modification currentModification : foundFixedModifications) {
            String[] parsedName = currentModification.getName().split("@");
            double mass = new Double(parsedName[0]);
            String aa = parsedName[1].toUpperCase();
            currentPTM = new PTM(0, currentModification.getName(), mass, new String[]{aa});
            for (String residue : currentPTM.getResiduesArray()) {
                if (residue.equals("[")) {
                    foundModifications.add(new ModificationMatch(currentPTM, false, 0));
                } else if (residue.equals("]")) {
                    foundModifications.add(new ModificationMatch(currentPTM, false, sequence.length() - 1));
                } else {
                    String tempSequence = "#" + sequence + "#";
                    String[] sequenceFragments = tempSequence.split(residue);
                    if (sequenceFragments.length > 0) {
                        int cpt = 0;
                        for (int f = 0; f < sequenceFragments.length - 1; f++) {
                            cpt = cpt + sequenceFragments[f].length();
                            foundModifications.add(new ModificationMatch(currentPTM, false, cpt - 1));
                        }
                    }
                }
            }
        }
        ArrayList<de.proteinms.xtandemparser.interfaces.Modification> foundVariableModifications = modificationMap.getVariableModifications(domain.getDomainID());
        for (Modification currentModification : foundVariableModifications) {
            String[] parsedName = currentModification.getName().split("@");
            double mass = new Double(parsedName[0]);
            String aa = parsedName[1];
            int location = new Integer(currentModification.getLocation()) - domain.getDomainStart() + 1;
            currentPTM = new PTM(0, currentModification.getName(), mass, new String[]{aa});
            foundModifications.add(new ModificationMatch(currentPTM, true, location));
        }
        peptide = new com.compomics.util.experiment.biology.Peptide(sequence, domain.getDomainMh(), proteins, foundModifications);
        return new PeptideAssumption(peptide, 1, Advocate.XTANDEM, measuredMass, eValue, getFileName());
    }
}
