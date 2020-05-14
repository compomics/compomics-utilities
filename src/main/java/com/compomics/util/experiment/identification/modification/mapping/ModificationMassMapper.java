package com.compomics.util.experiment.identification.modification.mapping;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.identification.Advocate;
import static com.compomics.util.experiment.identification.modification.mapping.ModificationNameMapper.MOD_MASS_TOLERANCE;
import static com.compomics.util.experiment.identification.modification.mapping.ModificationNameMapper.getPossibleModificationNamesByMass;
import static com.compomics.util.experiment.identification.modification.mapping.ModificationNameMapper.getPossibleModificationNamesByName;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.AndromedaParameters;
import com.compomics.util.parameters.identification.tool_specific.OmssaParameters;
import static com.compomics.util.experiment.identification.modification.mapping.ModificationNameMapper.getPossibleModificationNamesAndromeda;
import static com.compomics.util.experiment.identification.modification.mapping.ModificationNameMapper.getPossibleModificationNamesOmssa;

/**
 * Function inferring the mass of a modification based on the search engine used.
 *
 * @author Marc Vaudel
 */
public class ModificationMassMapper {


    /**
     * Returns the mass indicated by the identification algorithm for the given
     * modification.
     *
     * @param searchEngineModificationName The name according to the identification file reader.
     * @param idfileReader The identification file reader.
     * @param searchParameters The search parameters.
     * @param modificationProvider The modification provider to use.
     *
     * @return The mass of the modification.
     */
    public static double getMass(
            String searchEngineModificationName,
            IdfileReader idfileReader,
            SearchParameters searchParameters,
            ModificationProvider modificationProvider
    ) {

        switch (idfileReader.getClass().getSimpleName()) {
            case "MascotIdidfileReader":
            case "XTandemIdfileReader":
            case "MsAmandaIdfileReader":
            case "MzIdentMLIdfileReader":
            case "PepxmlIdfileReader":
            case "TideIdfileReader":
                return getMassByMass(
                        searchEngineModificationName
                );

            case "DirecTagIdfileReader":
            case "NovorIdfileReader":
            case "OnyaseIdfileReader":
                return getMassByName(
                        searchEngineModificationName, 
                        modificationProvider
                );

            case "OMSSAIdfileReader":
                return getMassOmssa(
                        searchEngineModificationName, 
                        modificationProvider, 
                        searchParameters
                );

            case "AndromedaIdfileReader":
                return getMassAndromeda(
                        searchEngineModificationName, 
                        modificationProvider, 
                        searchParameters
                );

            default:

                throw new IllegalArgumentException("Modification mapping not implemented for file reader " + idfileReader.getClass().getSimpleName() + ".");

        }
    }
    public static double getMassByMass(
            String searchEngineModificationName
    ) {

        try {

            return Double.parseDouble(
                    searchEngineModificationName.substring(
                            0,
                            searchEngineModificationName.indexOf('@')
                    )
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Impossible to parse \'" + searchEngineModificationName + "\' as a modification. Expected \'mass@position\'.",
                    e
            );
        }
    }
    
    public static double getMassByName(
            String searchEngineModificationName,
            ModificationProvider modificationProvider
    ) {

        Modification modification = modificationProvider.getModification(searchEngineModificationName);

        if (modification == null) {

            throw new IllegalArgumentException("Modification not recognized : " + searchEngineModificationName + ".");

        }
        
        return modification.getMass();
    }
    
    public static double getMassOmssa(
            String searchEngineModificationName,
            ModificationProvider modificationProvider,
            SearchParameters searchParameters
    ) {

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
        
        return modification.getMass();
        
    }
    
    public static double getMassAndromeda(
            String searchEngineModificationName,
            ModificationProvider modificationProvider,
            SearchParameters searchParameters
    ) {

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
        
        return modification.getMass();
    }
    
}
