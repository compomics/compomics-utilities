package com.compomics.util.experiment.identification.modification.mapping;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.AndromedaParameters;
import com.compomics.util.parameters.identification.tool_specific.OmssaParameters;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Functions mapping search engine modifications to utilities by name.
 *
 * @author Marc Vaudel
 */
public class ModificationNameMapper {

    /**
     * The mass tolerance to be used to match modifications from search engines
     * and expected modifications. 0.01 by default, the mass resolution in
     * X!Tandem result files.
     */
    public static final double MOD_MASS_TOLERANCE = 0.01;

    public static HashMap<Integer, HashSet<String>> getPossibleModificationNames(
            Peptide peptide,
            ModificationMatch modificationMatch,
            IdfileReader idfileReader,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        switch (idfileReader.getClass().getSimpleName()) {
            case "MascotIdidfileReader":
            case "XTandemIdfileReader":
            case "MsAmandaIdfileReader":
            case "MzIdentMLIdfileReader":
            case "PepxmlIdfileReader":
            case "TideIdfileReader":
                return getPossibleModificationNamesByMass(
                        peptide,
                        modificationMatch,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );

            case "DirecTagIdfileReader":
            case "NovorIdfileReader":
            case "OnyaseIdfileReader":
                return getPossibleModificationNamesByName(
                        peptide,
                        modificationMatch,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );

            case "OMSSAIdfileReader":
                return getPossibleModificationNamesOmssa(
                        peptide,
                        modificationMatch,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );

            case "AndromedaIdfileReader":
                return getPossibleModificationNamesAndromeda(
                        peptide,
                        modificationMatch,
                        searchParameters,
                        modificationSequenceMatchingParameters,
                        sequenceProvider,
                        modificationProvider
                );

            default:

                throw new IllegalArgumentException("Modification mapping not implemented for file reader " + idfileReader.getClass().getSimpleName()+ ".");

        }
    }

    public static HashMap<Integer, HashSet<String>> getPossibleModificationNamesByMass(
            Peptide peptide,
            ModificationMatch modificationMatch,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        String searchEngineModificationName = modificationMatch.getModification();

        try {

            double modMass = Double.parseDouble(
                    searchEngineModificationName.substring(
                            0,
                            searchEngineModificationName.indexOf('@')
                    )
            );
            return ModificationUtils.getExpectedModifications(
                    modMass,
                    searchParameters.getModificationParameters(),
                    peptide,
                    MOD_MASS_TOLERANCE,
                    sequenceProvider,
                    modificationSequenceMatchingParameters,
                    searchParameters
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Impossible to parse \'" + searchEngineModificationName + "\' as a modification. Expected \'mass@position\'.",
                    e
            );
        }
    }

    public static HashMap<Integer, HashSet<String>> getPossibleModificationNamesByName(
            Peptide peptide,
            ModificationMatch modificationMatch,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        String searchEngineModificationName = modificationMatch.getModification();

        Modification modification = modificationProvider.getModification(searchEngineModificationName);

        if (modification == null) {

            throw new IllegalArgumentException("Modification not recognized : " + searchEngineModificationName + ".");

        }
        return ModificationUtils.getExpectedModifications(
                modification.getMass(),
                searchParameters.getModificationParameters(),
                peptide,
                MOD_MASS_TOLERANCE,
                sequenceProvider,
                modificationSequenceMatchingParameters,
                searchParameters
        );
    }

    public static HashMap<Integer, HashSet<String>> getPossibleModificationNamesOmssa(
            Peptide peptide,
            ModificationMatch modificationMatch,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        String searchEngineModificationName = modificationMatch.getModification();

        OmssaParameters omssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());

        if (!omssaParameters.hasModificationIndexes()) {

            throw new IllegalArgumentException("OMSSA modification indexes not set in the search parameters.");

        }

        int omssaIndex;

        try {

            omssaIndex = Integer.parseInt(searchEngineModificationName);

        } catch (Exception e) {

            throw new IllegalArgumentException("Impossible to parse OMSSA modification index " + searchEngineModificationName + ".");

        }

        String omssaName = omssaParameters.getModificationName(omssaIndex);

        if (omssaName == null) {

            throw new IllegalArgumentException(
                    "Impossible to find OMSSA modification of index "
                    + omssaIndex + "."
            );

        }

        Modification modification = modificationProvider.getModification(omssaName);

        return ModificationUtils.getExpectedModifications(
                modification.getMass(),
                searchParameters.getModificationParameters(),
                peptide,
                MOD_MASS_TOLERANCE,
                sequenceProvider,
                modificationSequenceMatchingParameters,
                searchParameters
        );

    }

    public static HashMap<Integer, HashSet<String>> getPossibleModificationNamesAndromeda(
            Peptide peptide,
            ModificationMatch modificationMatch,
            SearchParameters searchParameters,
            SequenceMatchingParameters modificationSequenceMatchingParameters,
            SequenceProvider sequenceProvider,
            ModificationProvider modificationProvider
    ) {

        String searchEngineModificationName = modificationMatch.getModification();

        AndromedaParameters andromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());

        if (!andromedaParameters.hasModificationIndexes()) {

            throw new IllegalArgumentException("Andromeda modification indexes not set in the search parameters.");

        }

        int andromedaIndex;

        try {

            andromedaIndex = Integer.parseInt(searchEngineModificationName);

        } catch (Exception e) {

            throw new IllegalArgumentException("Impossible to parse Andromeda modification index " + searchEngineModificationName + ".");

        }

        String andromedaName = andromedaParameters.getModificationName(andromedaIndex);

        if (andromedaName == null) {

            throw new IllegalArgumentException(
                    "Impossible to find Andromeda modification of index " + andromedaIndex + "."
            );
        }

        Modification modification = modificationProvider.getModification(andromedaName);
        
        return ModificationUtils.getExpectedModifications(
                modification.getMass(),
                searchParameters.getModificationParameters(),
                peptide,
                MOD_MASS_TOLERANCE,
                sequenceProvider,
                modificationSequenceMatchingParameters,
                searchParameters
        );

    }

}
