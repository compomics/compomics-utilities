package com.compomics.util.experiment.io.identifications.idfilereaders;

import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.ModificationProfile;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.DataCollection;
import uk.ac.ebi.jmzidml.model.mzidml.Enzyme;
import uk.ac.ebi.jmzidml.model.mzidml.ModificationParams;
import uk.ac.ebi.jmzidml.model.mzidml.ParamList;
import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase;
import uk.ac.ebi.jmzidml.model.mzidml.SearchModification;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol;
import uk.ac.ebi.jmzidml.model.mzidml.Tolerance;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;

/**
 * Reads search parameters from a mzIdentML result files. (Work in progress...)
 *
 * @author Harald Barsnes
 */
public class MzIdentMLIdfileSearchParametersConverter extends ExperimentObject {

    /**
     * The enzyme factory.
     */
    private static EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();

    /**
     * Main class for testing purposes only.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            File lEnzymeFile = new File("C:\\Users\\hba041\\My_Applications\\peptide-shaker\\resources\\conf\\peptideshaker_enzymes.xml");
            enzymeFactory.importEnzymes(lEnzymeFile);
            MzIdentMLIdfileSearchParametersConverter.getSearchParameters(
                    new File("C:\\Users\\hba041\\Desktop\\yasset\\total-spectra-mascot.mzid"),
                    //new File("C:\\Users\\hba041\\Desktop\\yasset\\total-spectra-myrimatch.mzid"),
                    //new File("C:\\Users\\hba041\\Desktop\\test\\mzIdentML\\PeptideShaker_example_export.mzid"), 
                    new File("C:\\Users\\hba041\\Desktop\\test\\mzIdentML\\test.parameters"), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     *
     * @param mzIdentMLFile
     * @param searchParametersFile the file to save the search parameters to
     * @param waitingHandler
     * @return the extracted search parameters
     * @throws FileNotFoundException
     * @throws IOException
     * @throws java.lang.ClassNotFoundException
     */
    public static SearchParameters getSearchParameters(File mzIdentMLFile, File searchParametersFile, WaitingHandler waitingHandler) throws FileNotFoundException, IOException, ClassNotFoundException {

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setParametersFile(searchParametersFile);

        // unmarshal the mzid file
        MzIdentMLUnmarshaller unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile);
        //MzIdentMLUnmarshaller unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile, true); // @TODO: figure out when to use in memory processing

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            mzIdentMLFile = null;
            unmarshaller = null;
            //unmarshaller.close(); // @TODO: close method is missing?
            return null;
        }

        // get the modifications
        ModificationProfile modificationProfile = new ModificationProfile();
        SpectrumIdentificationProtocol spectrumIdentificationProtocol = unmarshaller.unmarshal(SpectrumIdentificationProtocol.class);
        ModificationParams modifications = spectrumIdentificationProtocol.getModificationParams();

        for (SearchModification tempMod : modifications.getSearchModification()) {
            if (!tempMod.getCvParam().isEmpty()) {

                CvParam cvParam = tempMod.getCvParam().get(0); // example: <cvParam cvRef="UNIMOD" accession="UNIMOD:4" name="Carbamidomethyl" value="57.021464"/>

                // @TODO: convert to utilities ptms!                
//                if (tempMod.isFixedMod()) {
//                    modificationProfile.addFixedModification(null);
//                } else {
//                    modificationProfile.addVariableModification(null);
//                }
            }
        }
        searchParameters.setModificationProfile(modificationProfile);

        // get the database
        DataCollection dataCollection = unmarshaller.unmarshal(DataCollection.class);
        List<SearchDatabase> databases = dataCollection.getInputs().getSearchDatabase();
        String databaseLocation = null;

        if (!databases.isEmpty()) {
            databaseLocation = databases.get(0).getLocation();
            searchParameters.setFastaFile(new File(databaseLocation));
        }

        // get the enzym
        String enzyme = null;
        Integer maxMissedCleavages = null;
        List<Enzyme> enzymes = spectrumIdentificationProtocol.getEnzymes().getEnzyme();
        if (!enzymes.isEmpty()) {
            ParamList paramList = enzymes.get(0).getEnzymeName();

            if (!paramList.getParamGroup().isEmpty()) {
                enzyme = paramList.getParamGroup().get(0).getName();
            }

            if (enzymes.get(0).getMissedCleavages() != null) {
                maxMissedCleavages = enzymes.get(0).getMissedCleavages();
            }
        }
        if (enzyme != null) {
            com.compomics.util.experiment.biology.Enzyme utilitiesEnzyme = EnzymeFactory.getUtilitiesEnzyme(enzyme); // @TODO: replace by use of cv terms
            if (utilitiesEnzyme != null) {
                searchParameters.setEnzyme(utilitiesEnzyme);
            }
        }
        if (maxMissedCleavages != null) {
            searchParameters.setnMissedCleavages(maxMissedCleavages);
        }

        // get the precursor tolerance and type
        Double precursorMinTolerance = null;
        Double precursorMaxTolerance = null;
        Boolean precursorToleranceTypeIsPpm = true;
        Tolerance tempPrecursorTolerance = spectrumIdentificationProtocol.getParentTolerance();

        for (CvParam cvParam : tempPrecursorTolerance.getCvParam()) {
            if (cvParam.getAccession().equalsIgnoreCase("MS:1001412")) {
                precursorMaxTolerance = Double.valueOf(cvParam.getValue());
                precursorToleranceTypeIsPpm = cvParam.getUnitAccession().equalsIgnoreCase("UO:0000169");
            } else if (cvParam.getAccession().equalsIgnoreCase("MS:1001413")) {
                precursorMinTolerance = Double.valueOf(cvParam.getValue());
                precursorToleranceTypeIsPpm = cvParam.getUnitAccession().equalsIgnoreCase("UO:0000169");
            }
        }

        if (precursorMinTolerance != null && precursorMaxTolerance != null) {

            Double precursorTolerance;

            if (Math.abs(precursorMinTolerance) - Math.abs(precursorMaxTolerance) < 0.0000001) {
                precursorTolerance = Math.abs(precursorMinTolerance);
            } else {
                precursorTolerance = Math.max(Math.abs(precursorMinTolerance), Math.abs(precursorMaxTolerance));
            }

            searchParameters.setPrecursorAccuracy(precursorTolerance);
            if (precursorToleranceTypeIsPpm) {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
            } else {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
            }
        }

        // get the precursor tolerance and type
        Double fragmentMinTolerance = null;
        Double fragmentMaxTolerance = null;
        Boolean fragmentToleranceTypeIsPpm = false;
        Tolerance tempFragmentTolerance = spectrumIdentificationProtocol.getFragmentTolerance();

        for (CvParam cvParam : tempFragmentTolerance.getCvParam()) {
            if (cvParam.getAccession().equalsIgnoreCase("MS:1001412")) {
                fragmentMaxTolerance = Double.valueOf(cvParam.getValue());
                fragmentToleranceTypeIsPpm = cvParam.getUnitAccession().equalsIgnoreCase("UO:0000169");
            } else if (cvParam.getAccession().equalsIgnoreCase("MS:1001413")) {
                fragmentMinTolerance = Double.valueOf(cvParam.getValue());
                fragmentToleranceTypeIsPpm = cvParam.getUnitAccession().equalsIgnoreCase("UO:0000169");
            }
        }
        if (fragmentMinTolerance != null && fragmentMaxTolerance != null) {

            Double fragmentTolerance;

            if (Math.abs(precursorMinTolerance) - Math.abs(fragmentMaxTolerance) < 0.0000001) {
                fragmentTolerance = Math.abs(precursorMinTolerance);
            } else {
                fragmentTolerance = Math.max(Math.abs(precursorMinTolerance), Math.abs(fragmentMaxTolerance));
            }

            searchParameters.setPrecursorAccuracy(fragmentTolerance);
            if (fragmentToleranceTypeIsPpm) {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
            } else {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            }
        }

        // close file
        mzIdentMLFile = null;
        unmarshaller = null;
        //unmarshaller.close(); // @TODO: close method is missing?

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return null;
        }

        // save the parameters to file
        if (searchParametersFile != null) {
            SearchParameters.saveIdentificationParameters(searchParameters, searchParametersFile);
        }

        return searchParameters;
    }
}
