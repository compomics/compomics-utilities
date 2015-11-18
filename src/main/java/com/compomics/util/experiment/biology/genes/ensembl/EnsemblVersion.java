package com.compomics.util.experiment.biology.genes.ensembl;

/**
 * Class for the handling of Ensembl versions.
 *
 * @author Marc Vaudel
 */
public class EnsemblVersion {


    /**
     * Returns the current Ensembl version number. Null if not found.
     *
     * @param ensemblType the Ensembl type, e.g., ensembl or plants
     * 
     * @return the current Ensembl version number
     */
    public static Integer getCurrentEnsemblVersion(String ensemblType) {

        // @TODO: find a less hard coded way of finding the current ensembl versions!!!
        if (ensemblType.equalsIgnoreCase("fungi")
                || ensemblType.equalsIgnoreCase("plants")
                || ensemblType.equalsIgnoreCase("protists")
                || ensemblType.equalsIgnoreCase("metazoa")) {
            return 29;
        } else {
            return 82;
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
     * Returns the name of the Ensembl database for BioMart queries.
     *
     * @param speciesTypeIndex the species type index: 1: fungi, 2: plants, 3:
     * protist, 4: metazoa or 5: default.
     * @return the name of the Ensembl database for BioMart queries
     */
    public static String getEnsemblDbName(int speciesTypeIndex) {

        switch (speciesTypeIndex) {
            case 1:
                return "fungi_mart_" + getCurrentEnsemblVersion("fungi");
            case 2:
                return "plants_mart_" + getCurrentEnsemblVersion("plants");
            case 3:
                return "protists_mart_" + getCurrentEnsemblVersion("protists");
            case 4:
                return "metazoa_mart_" + getCurrentEnsemblVersion("metazoa");
            case 5:
                return "default";
        }

        return "unknown"; // should not happen!!!
    }
}
