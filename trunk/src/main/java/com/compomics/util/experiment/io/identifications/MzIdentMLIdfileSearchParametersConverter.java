package com.compomics.util.experiment.io.identifications;

import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.Enzyme;
import uk.ac.ebi.jmzidml.model.mzidml.ParamList;
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
     * Constructor.
     *
     * @param mzIdentMLFile the mzIdentML file
     * @param searchParameters the search parameters object to save to
     * @param species the current species
     * @param waitingHandler the waiting handler
     * @return the extracted search parameters
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if a IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public static String getSearchParameters(File mzIdentMLFile, SearchParameters searchParameters, String species, WaitingHandler waitingHandler)
            throws FileNotFoundException, IOException, ClassNotFoundException {

        String parametersReport = "<br><b><u>Extracted Search Parameters</u></b><br>";

        // unmarshal the mzid file
        MzIdentMLUnmarshaller unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile);
        //MzIdentMLUnmarshaller unmarshaller = new MzIdentMLUnmarshaller(mzIdentMLFile, true); // @TODO: figure out when to use in memory processing

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            mzIdentMLFile = null;
            unmarshaller = null;
            //unmarshaller.close(); // @TODO: close method is missing?
            return null;
        }

        // get the spectrum identification protocol
        SpectrumIdentificationProtocol spectrumIdentificationProtocol = unmarshaller.unmarshal(SpectrumIdentificationProtocol.class);

        // get the fragment ion tolerance and type
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

        parametersReport += "<br><b>Fragment Ion Mass Tolerance:</b> ";
        if (fragmentMinTolerance != null && fragmentMaxTolerance != null) {

            Double fragmentTolerance;

            if (Math.abs(fragmentMinTolerance) - Math.abs(fragmentMaxTolerance) < 0.0000001) {
                fragmentTolerance = Math.abs(fragmentMinTolerance);
            } else {
                fragmentTolerance = Math.max(Math.abs(fragmentMinTolerance), Math.abs(fragmentMaxTolerance));
            }

            searchParameters.setPrecursorAccuracy(fragmentTolerance);
            if (fragmentToleranceTypeIsPpm) {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.PPM);
                parametersReport += fragmentTolerance + " ppm";
            } else {
                searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
                parametersReport += fragmentTolerance + " Da";
            }
        } else {
            parametersReport += searchParameters.getFragmentIonAccuracy() + " Da (default)"; // @TODO: what about accuracy in ppm
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

        parametersReport += "<br><b>Precursor Ion Mass Tolerance:</b> ";
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
                parametersReport += precursorTolerance + " ppm";
            } else {
                searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                parametersReport += precursorTolerance + " Da";
            }
        } else {
            parametersReport += searchParameters.getPrecursorAccuracy() + " ppm (default)"; // @TODO: what about accuracy in Dalton
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

        parametersReport += "<br><br><b>Enzyme:</b> ";

        if (enzyme != null) {
            com.compomics.util.experiment.biology.Enzyme utilitiesEnzyme = EnzymeFactory.getUtilitiesEnzyme(enzyme); // @TODO: replace by use of cv terms
            if (utilitiesEnzyme != null) {
                searchParameters.setEnzyme(utilitiesEnzyme);
                parametersReport += utilitiesEnzyme.getName() + "<br>";
            } else {
                searchParameters.setEnzyme(EnzymeFactory.getInstance().getEnzyme("Trypsin"));
                parametersReport += "Trypsin (assumed)<br>";
            }
        } else {
            searchParameters.setEnzyme(EnzymeFactory.getInstance().getEnzyme("Trypsin"));
            parametersReport += "Trypsin (assumed)<br>";
        }

        parametersReport += "<b>Maximum Missed Cleavages:</b> ";
        if (maxMissedCleavages != null) {
            searchParameters.setnMissedCleavages(maxMissedCleavages);
            parametersReport += maxMissedCleavages;
        } else {
            parametersReport += searchParameters.getnMissedCleavages() + " (default)";
        }

        // set the min/max precursor charge
        parametersReport += "<br><br><b>Min Precusor Charge:</b> ";
        parametersReport += searchParameters.getMinChargeSearched().value + " (default)";

        parametersReport += "<br><b>Max Precusor Charge:</b> ";
        parametersReport += searchParameters.getMaxChargeSearched().value + " (default)";

        // taxonomy and species
        parametersReport += "<br><br><b>Species:</b> ";
        if (species == null || species.length() == 0) {
            parametersReport += "unknown";
        } else {
            parametersReport += species;
        }

        // get the modifications
//        ModificationProfile modificationProfile = new ModificationProfile();
//        
//        ModificationParams modifications = spectrumIdentificationProtocol.getModificationParams();
//
//        for (SearchModification tempMod : modifications.getSearchModification()) {
//            if (!tempMod.getCvParam().isEmpty()) {
//
//                CvParam cvParam = tempMod.getCvParam().get(0); // example: <cvParam cvRef="UNIMOD" accession="UNIMOD:4" name="Carbamidomethyl" value="57.021464"/>
//
//                // @TODO: convert to utilities ptms!                
////                if (tempMod.isFixedMod()) {
////                    modificationProfile.addFixedModification(null);
////                } else {
////                    modificationProfile.addVariableModification(null);
////                }
//            }
//        }
//        searchParameters.setModificationProfile(modificationProfile);
        // get the database
//        DataCollection dataCollection = unmarshaller.unmarshal(DataCollection.class);
//        List<SearchDatabase> databases = dataCollection.getInputs().getSearchDatabase();
//        String databaseLocation = null;
//
//        if (!databases.isEmpty()) {
//            databaseLocation = databases.get(0).getLocation();
//            searchParameters.setFastaFile(new File(databaseLocation));
//        }
        // close file
        mzIdentMLFile = null;
        unmarshaller = null;
        //unmarshaller.close(); // @TODO: close method is missing?

        if (waitingHandler != null && waitingHandler.isRunCanceled()) {
            return null;
        }

        return parametersReport;
    }
}
