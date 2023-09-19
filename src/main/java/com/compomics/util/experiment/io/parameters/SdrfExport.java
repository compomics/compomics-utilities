package com.compomics.util.experiment.io.parameters;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import static com.compomics.util.experiment.biology.modifications.ModificationType.modaa;
import static com.compomics.util.experiment.biology.modifications.ModificationType.modcaa_peptide;
import static com.compomics.util.experiment.biology.modifications.ModificationType.modcaa_protein;
import static com.compomics.util.experiment.biology.modifications.ModificationType.modnaa_peptide;
import com.compomics.util.io.flat.SimpleFileWriter;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.pride.CvTerm;
import java.io.File;
import java.util.ArrayList;

/**
 * This class exports a draft of sdrf file.
 *
 * @author Marc Vaudel
 */
public class SdrfExport {

    public static void writeSdrf(
            File sdrfFile,
            SearchParameters searchParameters,
            ArrayList<String> msFileNames,
            ModificationProvider modificationProvider
    ) {

        ArrayList<String> modificationsFields = new ArrayList<>();

        for (String modName : searchParameters.getModificationParameters().getFixedModifications()) {

            Modification modification = modificationProvider.getModification(modName);

            modificationsFields.add(getModificationString(modification, true));

        }

        for (String modName : searchParameters.getModificationParameters().getAllNotFixedModifications()) {

            Modification modification = modificationProvider.getModification(modName);

            modificationsFields.add(getModificationString(modification, false));

        }

        ArrayList<String> enzymesFields = new ArrayList<>();

        if (searchParameters.getDigestionParameters().hasEnzymes()) {

            for (Enzyme enzyme : searchParameters.getDigestionParameters().getEnzymes()) {

                enzymesFields.add(getEnzymeString(enzyme));

            }
        }

        String precursorMassTolerance = searchParameters.isPrecursorAccuracyTypePpm()
                ? searchParameters.getPrecursorAccuracy() + " ppm"
                : searchParameters.getPrecursorAccuracy() + " Da";

        String fragmentMassTolerance = searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.PPM
                ? searchParameters.getPrecursorAccuracy() + " ppm"
                : searchParameters.getPrecursorAccuracy() + " Da";

        StringBuilder header = new StringBuilder();
        header.append("comment[data file]");

        StringBuilder lineSuffixBuilder = new StringBuilder();

        for (String field : modificationsFields) {

            header.append("\t").append("comment[modification parameters]");
            lineSuffixBuilder.append("\t").append(field);

        }

        for (String field : enzymesFields) {

            header.append("\t").append("comment[cleavage agent details]");
            lineSuffixBuilder.append("\t").append(field);

        }

        header.append("\t").append("comment[precursor mass tolerance]");
        lineSuffixBuilder.append("\t").append(precursorMassTolerance);

        header.append("\t").append("comment[fragment mass tolerance]");
        lineSuffixBuilder.append("\t").append(fragmentMassTolerance);

        String lineSuffix = lineSuffixBuilder.toString();

        try (SimpleFileWriter writer = new SimpleFileWriter(sdrfFile, false)) {

            writer.writeLine(header.toString());

            for (String fileName : msFileNames) {

                StringBuilder line = new StringBuilder();
                line.append(fileName).append(lineSuffix);

                writer.writeLine(line.toString());

            }
        }
    }

    private static String getEnzymeString(Enzyme enzyme) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("NT=").append(enzyme.getName()).append(';');
        stringBuilder.append("AC=").append(enzyme.getCvTerm().getAccession()).append(';');

        // Regular exception for cleavage site?
        return stringBuilder.toString();

    }

    private static String getModificationString(Modification modification, boolean fixed) {

        StringBuilder stringBuilder = new StringBuilder();

        boolean found = false;

        CvTerm cvTerm = modification.getUnimodCvTerm();

        if (cvTerm != null) {

            stringBuilder.append("NT=").append(cvTerm.getName()).append(';');
            stringBuilder.append("AC=").append(cvTerm.getAccession()).append(';');
            found = true;

        } else {

            cvTerm = modification.getPsiModCvTerm();

            if (cvTerm != null) {

                stringBuilder.append("NT=").append(cvTerm.getName()).append(';');
                stringBuilder.append("AC=").append(cvTerm.getAccession()).append(';');
                found = true;

            }
        }

        if (!found) {

            stringBuilder.append("NT=").append(modification.getName()).append(';');

        }

        // Add atom chain?
        if (fixed) {

            stringBuilder.append("MT=Fixed;");

        } else {

            stringBuilder.append("MT=Variable;");

        }

        stringBuilder.append("PP=").append(getModificationPosition(modification)).append(';');

        stringBuilder.append("MM=").append(modification.getMass()).append(';');

        AminoAcidPattern aminoAcidPattern = modification.getPattern();

        if (aminoAcidPattern != null) {

            stringBuilder.append("TS=").append(aminoAcidPattern.getPrositeFormat()).append(';');

        }

        return stringBuilder.toString();

    }

    private static String getModificationPosition(Modification modification) {

        switch (modification.getModificationType()) {

            case modc_peptide:
            case modcaa_peptide:
                return "Any C-term";
            case modc_protein:
            case modcaa_protein:
                return "Protein C-term";
            case modn_peptide:
            case modnaa_peptide:
                return "Any N-term";
            case modn_protein:
            case modnaa_protein:
                return "Protein N-term";
            default:
                return "Anywhere";
        }

    }

}
