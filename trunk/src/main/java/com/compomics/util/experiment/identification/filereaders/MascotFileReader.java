package com.compomics.util.experiment.identification.filereaders;

import com.compomics.mascotdatfile.util.interfaces.FragmentIon;
import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.interfaces.QueryToPeptideMapInf;
import com.compomics.mascotdatfile.util.mascot.PeptideHit;
import com.compomics.mascotdatfile.util.mascot.PeptideHitAnnotation;
import com.compomics.mascotdatfile.util.mascot.ProteinHit;
import com.compomics.mascotdatfile.util.mascot.enumeration.MascotDatfileType;
import com.compomics.mascotdatfile.util.mascot.factory.MascotDatfileFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.FileReader;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 9:45:35 AM
 * This reader will import identifications from a Mascot dat file.
 */
public class MascotFileReader implements FileReader {


    /**
     * The inspected file
     */
    private File inspectedFile;
    /**
     * Instance of the mascotdatfile parser
     */
    private MascotDatfileInf iMascotDatfile;
    /**
     * the PTM factory
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    /**
     * constructor for the mascotFileReader
     */
    public MascotFileReader() {
    }

    /**
     * Constructor for the MascotFilereader
     * @param aFile a file to read
     */
    public MascotFileReader(File aFile) {
        inspectedFile = aFile;
        try {
            iMascotDatfile = MascotDatfileFactory.create(inspectedFile.getCanonicalPath(), MascotDatfileType.INDEX); //getPath might have to be changed into getcanonicalPath
        }
        catch (
                IOException e
                )

        {
            System.exit(1);
        }
    }

    /**
     * get the spectrum file name
     * @return the spectrum file name
     */
    public String getMgfFileName() {
        File temp = new File(iMascotDatfile.getParametersSection().getFile());
        return temp.getName();
    }

    /**
     * getter for the file name
     * @return the file name
     */
    public String getFileName() {
        return iMascotDatfile.getFileName();
    }

    /**
     * a method to get all the spectrum matches
     * @return a set containing all spectrum matches
     */
    public HashSet<SpectrumMatch> getAllSpectrumMatches() {

        HashSet<SpectrumMatch> assignedPeptideHits = new HashSet<SpectrumMatch>();

        QueryToPeptideMapInf lQueryToPeptideMap = iMascotDatfile.getQueryToPeptideMap();
        QueryToPeptideMapInf lDecoyQueryToPeptideMap = iMascotDatfile.getDecoyQueryToPeptideMap();
        for (int i = 0; i < iMascotDatfile.getNumberOfQueries(); i++) {
            Vector<PeptideHit> mascotDecoyPeptideHits = null;
            try {
                mascotDecoyPeptideHits = lDecoyQueryToPeptideMap.getAllPeptideHits(i + 1);
            } catch (Exception e) {
                // Failing would mean that no decoy section was found and the parser crashed, handled later by if (mascotDecoyPeptideHits != null)
            }
            Vector<PeptideHit> mascotPeptideHits = lQueryToPeptideMap.getAllPeptideHits(i + 1);
            Boolean nonDecoyHitFound = false;
            if (mascotPeptideHits != null) {                                        // There might not be an identification for every query
                if (mascotDecoyPeptideHits != null) {
                    if (mascotDecoyPeptideHits.get(0).getIonsScore() <= mascotPeptideHits.get(0).getIonsScore()) {
                        nonDecoyHitFound = true;
                    }
                } else {
                    nonDecoyHitFound = true;
                }
            }
            if (nonDecoyHitFound) {
                boolean singleBestHit = true;
                if (mascotPeptideHits.size() > 1) {
                    if ((mascotPeptideHits.get(0).getExpectancy() == mascotPeptideHits.get(1).getExpectancy()) && (mascotPeptideHits.get(0).getSequence().compareTo(mascotPeptideHits.get(1).getSequence()) != 0)) {
                        singleBestHit = false;
                    }
                }
                if (singleBestHit) {
                    PeptideHit thisPeptideHit = mascotPeptideHits.get(0);
                    SpectrumMatch currentMatch = getSpectrumMatch(thisPeptideHit, i + 1, false);
                    assignedPeptideHits.add(currentMatch);
                }

            } else if (mascotDecoyPeptideHits != null) {
                boolean singleBestHit = true;
                if (mascotDecoyPeptideHits.size() > 1) {
                    if ((mascotDecoyPeptideHits.get(0).getExpectancy() == mascotDecoyPeptideHits.get(1).getExpectancy()) && (mascotDecoyPeptideHits.get(0).getSequence().compareTo(mascotDecoyPeptideHits.get(1).getSequence()) != 0)) {
                        singleBestHit = false;
                    }
                }
                if (singleBestHit) {
                    PeptideHit thisPeptideHit = mascotDecoyPeptideHits.get(0);
                    SpectrumMatch currentMatch = getSpectrumMatch(thisPeptideHit, i + 1, true);
                    assignedPeptideHits.add(currentMatch);
                }
            }
        }

        return assignedPeptideHits;
    }

    /**
     * parses a spectrum match out of a peptideHit
     * @param aPeptideHit   the peptide hit to parse
     * @param query         the corresponding query
     * @param decoySection  is it in the decoy section?
     * @return a spectrum match
     */
    private SpectrumMatch getSpectrumMatch(PeptideHit aPeptideHit, int query, boolean decoySection) {
        boolean c13 = false;
        double deltaMass;
        if (Math.abs(aPeptideHit.getDeltaMass()) > 0.5) {
            deltaMass = 1000000 * Math.abs(aPeptideHit.getDeltaMass() - 1) / aPeptideHit.getPeptideMr();
            c13 = true;
        } else {
            deltaMass = 1000000 * Math.abs(aPeptideHit.getDeltaMass()) / aPeptideHit.getPeptideMr();
        }
        Double measuredMass = aPeptideHit.getPeptideMr() + aPeptideHit.getDeltaMass();
        String measuredCharge = iMascotDatfile.getQuery(query).getChargeString();
        String sign = String.valueOf(measuredCharge.charAt(1));
        Charge charge;
        if (sign.compareTo("+") == 0) {
            charge = new Charge(Charge.PLUS, Integer.valueOf(measuredCharge.substring(0, 1)));
        } else {
            charge = new Charge(Charge.MINUS, Integer.valueOf(measuredCharge.substring(0, 1)));
        }
        ArrayList<ModificationMatch> foundModifications = new ArrayList();
        Modification handledModification;
        PTM correspondingPTM;
        int modificationSite;
        for (int l = 0; l < aPeptideHit.getModifications().length; l++) {
            handledModification = aPeptideHit.getModifications()[l];
            if (handledModification != null) {
                correspondingPTM = ptmFactory.getPTMFromMascotName(handledModification.getShortType());
                if (correspondingPTM != null) {
                    // Modification site not implemented yet
                    foundModifications.add(new ModificationMatch(correspondingPTM, !handledModification.isFixed(), 0));
                }
            }
        }
        Double mascotEValue = aPeptideHit.getExpectancy();

        String spectrumId = iMascotDatfile.getQuery(query).getTitle();
        com.compomics.mascotdatfile.util.mascot.Peak[] kennysPeakList = iMascotDatfile.getQuery(query).getPeakList();
        HashSet<Peak> peakList = new HashSet<Peak>();
        for (com.compomics.mascotdatfile.util.mascot.Peak peak : kennysPeakList) {
            peakList.add(new Peak(peak.getMZ(), peak.getIntensity()));
        }
        MSnSpectrum spectrum = new MSnSpectrum(2, measuredMass, charge, spectrumId, peakList, getMgfFileName(), -1);
        ArrayList<Protein> proteins = new ArrayList();
        for (int j = 0; j < aPeptideHit.getProteinHits().size(); j++) {
            proteins.add(new Protein(((ProteinHit) aPeptideHit.getProteinHits().get(j)).getAccession()));
        }
        boolean reverse = true;
        if (!decoySection) {
            // Necessary if no decoy peptide section was available
            for (int j = 0; j < proteins.size(); j++) {
                if (!proteins.get(j).getAccession().startsWith("REV_") && !proteins.get(j).getAccession().endsWith("_REV") && !proteins.get(j).getAccession().endsWith("_REVERSED")) {
                    reverse = false;
                }
            }
        }

        Peptide thePeptide = new Peptide(aPeptideHit.getSequence(), aPeptideHit.getPeptideMr(), proteins);
        PeptideAssumption currentAssumption = new PeptideAssumption(thePeptide, 1, Advocate.MASCOT, deltaMass, mascotEValue, foundModifications, getFileName(), reverse);
        if (c13) {
            currentAssumption.setC13();
        }
        currentAssumption.setScore(aPeptideHit.getIonsScore());
  //      addAnnotation(currentAssumption, aPeptideHit, query);
        // Secondary hits are not implemented yet
        SpectrumMatch currentMatch = new SpectrumMatch(spectrum, currentAssumption);
        return currentMatch;
    }

    /**
     * add the annotation on a peptide assumption based on the parser informations
     * @param currentMatch  the peptide assumption concerned
     * @param aPeptideHit   the peptideHit associated
     * @param query         the query number
     */
    private void addAnnotation(PeptideAssumption currentMatch, PeptideHit aPeptideHit, int query) {

        PeptideHitAnnotation pha =
                aPeptideHit.getPeptideHitAnnotation(iMascotDatfile.getMasses(), iMascotDatfile.getParametersSection());

        Vector<FragmentIon> ions = pha.getMatchedIonsByMascot(iMascotDatfile.getQuery(query).getPeakList(), aPeptideHit.getPeaksUsedFromIons1());
        IonMatch ionMatch;
        Charge ionCharge;
        for (FragmentIon ion : ions) {
            int ionNumber = ion.getNumber();
            if (ion.isDoubleCharged()) {
                ionCharge = new Charge(Charge.PLUS, 2);
            } else {
                ionCharge = new Charge(Charge.PLUS, 1);
            }
            switch (ion.getID()) {
                case FragmentIon.A_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.A_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.A_H2O_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.AH2O_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.A_H2O_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.AH2O_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.A_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.A_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.A_NH3_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.ANH3_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.A_NH3_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.ANH3_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.B_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.B_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.B_H2O_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.BH2O_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.B_H2O_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.BH2O_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.B_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.B_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.B_NH3_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.BNH3_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.B_NH3_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.BNH3_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.C_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.C_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.C_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.C_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.IMMONIUM:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.IMMONIUM, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.PRECURSOR:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.MH_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.PRECURSOR_LOSS:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.PRECURSOR_LOSS, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.X_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.X_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.X_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.X_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Y_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.Y_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Y_H2O_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.YH2O_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Y_H2O_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.YH2O_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Y_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.Y_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Y_NH3_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.YNH3_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Y_NH3_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.YNH3_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Z_DOUBLE_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.Z_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                case FragmentIon.Z_ION:
                    ionMatch = new IonMatch(new Peak(ion.getMZ(), ion.getIntensity()),
                            new PeptideFragmentIon(PeptideFragmentIon.Z_ION, ionNumber, ionCharge));
                    currentMatch.addAnnotation(ionMatch);
                    break;
                default:
                    // ZH ions are not coded yet
            }
        }
    }

}


