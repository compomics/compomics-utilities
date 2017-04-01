package com.compomics.util.experiment.biology.genes.ensembl;

import com.compomics.util.experiment.biology.taxonomy.mappings.EnsemblGenomesSpecies.EnsemblGenomeDivision;

/**
 * Class for the handling of Ensembl versions.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class EnsemblVersion {

    /**
     * Returns the current Ensembl version number. Null if not found.
     *
     * @param ensemblGenomeDivision the Ensembl genome division, null if not
     * Ensembl genome
     *
     * @return the current Ensembl version number
     */
    public static Integer getCurrentEnsemblVersion(EnsemblGenomeDivision ensemblGenomeDivision) {

        // @TODO: find a less hard coded way of finding the current ensembl versions!!!
        if (ensemblGenomeDivision != null) {
            return 34;
        } else {
            return 88;
        }

        // the code below used to work but is not always updated when new ensembl versions are released
//        if (ensemblVersions == null) {
//            ensemblVersions = new HashMap<String, Integer>();
//        }
//        if (!ensemblVersions.containsKey(ensemblType)) {
//
//            try {
//                // get the current Ensembl version
//                URL url = new URL("http://www.biomart.org/biomart/martservice?type=registry");
//
//                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//
//                String inputLine;
//                boolean ensemblVersionFound = false;
//                String ensemblVersionAsText = "?";
//
//                while ((inputLine = in.readLine()) != null && !ensemblVersionFound) {
//                    if (inputLine.indexOf("database=\"" + ensemblType + "_mart_") != -1) {
//                        ensemblVersionAsText = inputLine.substring(inputLine.indexOf("database=\"" + ensemblType + "_mart_") + ("database=\"" + ensemblType + "_mart_").length());
//                        ensemblVersionAsText = ensemblVersionAsText.substring(0, ensemblVersionAsText.indexOf("\""));
//                        ensemblVersionFound = true;
//                    }
//                }
//
//                in.close();
//
//                if (ensemblVersionFound) {
//                    try {
//                        Integer ensemblVersion = new Integer(ensemblVersionAsText);
//                        ensemblVersions.put(ensemblType, ensemblVersion);
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return ensemblVersions.get(ensemblType);
    }

    /**
     * Returns the name of the Ensembl schema for BioMart queries.
     *
     * @param ensemblGenomeDivision the Ensembl genome division
     *
     * @return the name of the Ensembl schema for BioMart queries
     */
    public static String getEnsemblSchemaName(EnsemblGenomeDivision ensemblGenomeDivision) {

        if (ensemblGenomeDivision == null) {
            return "default";
        }
        switch (ensemblGenomeDivision) {
//            case fungi:
//                return "fungi_mart_" + getCurrentEnsemblVersion(ensemblGenomeDivision);
//            case plants:
//                return "plants_mart_" + getCurrentEnsemblVersion(ensemblGenomeDivision);
//            case protists:
//                return "protists_mart_" + getCurrentEnsemblVersion(ensemblGenomeDivision);
//            case metazoa:
//                return "metazoa_mart_" + getCurrentEnsemblVersion(ensemblGenomeDivision);
            case fungi:
                return "fungi_mart";
            case plants:
                return "plants_mart";
            case protists:
                return "protists_mart";
            case metazoa:
                return "metazoa_mart";
            default:
                return "default";
        }
    }
}
