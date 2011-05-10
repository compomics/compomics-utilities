package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReader;
import com.compomics.util.experiment.massspectrometry.*;
import com.compomics.util.experiment.personalization.ExperimentObject;
import de.proteinms.xtandemparser.interfaces.Modification;
import de.proteinms.xtandemparser.xtandem.*;
import de.proteinms.xtandemparser.xtandem.Spectrum;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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
     * the PTM factory
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The spectrum collection to complete
     */
    private SpectrumCollection spectrumCollection = null;

    /**
     * Constructor for the reader
     */
    public XTandemIdfileReader() {
    }

    /**
     * constructor for the reader
     *
     * @param aFile the inspected file
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
     * constructor for the reader with a spectrum collection where to put spectrum identification in
     *
     * @param aFile              the inspected file
     * @param spectrumCollection the spectrum collection used
     */
    public XTandemIdfileReader(File aFile, SpectrumCollection spectrumCollection) throws SAXException {
        this.spectrumCollection = spectrumCollection;
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
     * method which returns all spectrum matches found in the file
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
                    File tempFile = new File(xTandemFile.getInputParameters().getSpectrumPath());
                    String filename = tempFile.getName();
                    Charge charge = new Charge(Charge.PLUS, currentSpectrum.getPrecursorCharge());
                    double measuredMass = testPeptide.getDomainMh() + testPeptide.getDomainDeltaMh();
                    Precursor precursor = new Precursor(-1, measuredMass, charge); // The retention time is not known at this stage
                    MSnSpectrum spectrum = new MSnSpectrum(2, precursor, spectrumName, filename);
                    if (spectrumCollection != null) {
                        spectrumCollection.addSpectrum(spectrum);
                    }
                    String spectrumKey = spectrum.getSpectrumKey();
                    SpectrumMatch currentMatch = new SpectrumMatch(spectrumKey);

                    for (Peptide peptide : spectrumPeptides) {
                        currentMatch.addHit(Advocate.XTANDEM, getPeptideAssumption(peptide));
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
     * Returns a utilities peptide assumption from an X!Tandem peptide
     * @param xTandemPeptide the X!Tandem peptide
     * @return the corresponding peptide assumption
     */
    private PeptideAssumption getPeptideAssumption(Peptide xTandemPeptide) {
        ArrayList<Protein> proteins = new ArrayList<Protein>();
        double eValue;
        com.compomics.util.experiment.biology.Peptide peptide;
        double measuredMass = xTandemPeptide.getDomainMh() + xTandemPeptide.getDomainDeltaMh();
        String sequence = xTandemPeptide.getDomainSequence();
        String description = proteinMap.getProteinWithPeptideID(xTandemPeptide.getDomainID()).getLabel();
        String accession = "";
        try {
            int start = description.indexOf("|");
            int end = description.indexOf("|", ++start);
            accession = description.substring(start, end);
        } catch (Exception e) {
            int end = description.indexOf(" ");
            accession = description.substring(0, end);
        }
        proteins.add(new Protein(accession, accession.contains(DECOY_FLAG)));
        eValue = xTandemPeptide.getDomainExpect();
        ArrayList<Modification> foundFixedModifications = modificationMap.getFixedModifications(xTandemPeptide.getDomainID());
        PTM currentPTM;
        ArrayList<ModificationMatch> foundModifications = new ArrayList<ModificationMatch>();
        for (Modification currentModification : foundFixedModifications) {
            String[] parsedName = currentModification.getName().split("@");
            double mass = new Double(parsedName[0]);
            String aa = parsedName[1].toUpperCase();
            currentPTM = ptmFactory.getPTM(mass, aa, sequence);
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
        ArrayList<de.proteinms.xtandemparser.interfaces.Modification> foundVariableModifications = modificationMap.getVariableModifications(xTandemPeptide.getDomainID());
        for (Modification currentModification : foundVariableModifications) {
            String[] parsedName = currentModification.getName().split("@");
            double mass = new Double(parsedName[0]);
            String aa = parsedName[1];
            int location = new Integer(currentModification.getLocation()) - new Integer(xTandemPeptide.getDomainStart()) + 1;
            currentPTM = ptmFactory.getPTM(mass, aa, sequence);
            foundModifications.add(new ModificationMatch(currentPTM, true, location));
        }
        peptide = new com.compomics.util.experiment.biology.Peptide(sequence, xTandemPeptide.getDomainMh(), proteins, foundModifications);
        return new PeptideAssumption(peptide, 1, Advocate.XTANDEM, measuredMass, eValue, getFileName());
    }
}
