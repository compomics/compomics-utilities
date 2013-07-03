package com.compomics.util.preferences;

import com.compomics.util.Util;
import com.compomics.util.experiment.annotation.gene.GeneFactory;
import com.compomics.util.experiment.annotation.go.GOFactory;
import com.compomics.util.gui.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Contains methods for downloading gene and GO mappings.
 *
 * @author Harald Barsnes
 */
public class GenePreferences implements Serializable {

    /**
     * The serial number for serialization compatibility.
     */
    static final long serialVersionUID = -1286840382594446279L;
    /**
     * The folder where gene mapping related info is stored.
     */
    public static final String GENE_MAPPING_FOLDER = System.getProperty("user.home") + "/.compomics/gene_mappings/";
    /**
     * The suffix to use for files containing gene mappings.
     */
    public final static String GENE_MAPPING_FILE_SUFFIX = "_gene_mappings";
    /**
     * The suffix to use for files containing GO mappings.
     */
    public final static String GO_MAPPING_FILE_SUFFIX = "_go_mappings";
    /**
     * The current species. Used for the gene mappings.
     */
    private String currentSpecies = null;
    /**
     * The current species type. Used for the gene mappings.
     */
    private String currentSpeciesType = null;
    /**
     * The GO domain map. e.g., key: GO term: GO:0007568, element:
     * biological_process.
     */
    private HashMap<String, String> goDomainMap;
    /**
     * The species map, key: latin name, element: ensembl database name, e.g.,
     * key: Homo sapiens, element: hsapiens_gene_ensembl.
     *
     * @deprecated use
     */
    private HashMap<String, String> speciesMap;
    /**
     * The species map. Main key: Ensembl type, e.g., Vertebrates or Plants.
     * Next level: key: latin name, element: ensembl database name, e.g., key:
     * Homo sapiens, element: hsapiens_gene_ensembl.
     */
    private HashMap<String, HashMap<String, String>> allSpeciesMap;
    /**
     * The Ensembl versions for the downloaded species.
     */
    private HashMap<String, String> ensemblVersionsMap;
    /**
     * The list of species.
     *
     * @deprecated use allSpecies instead
     */
    private ArrayList<String> availableSpecies;
    /**
     * Map of all the species.
     */
    private HashMap<String, ArrayList<String>> allSpecies;
    /**
     * Old vector of species.
     *
     * @deprecated
     */
    private Vector<String> species;

    /**
     * Create a new GenePreferences object.
     */
    public GenePreferences() {
    }

    /**
     * Creates new gene preferences based on a GenePreferences object.
     *
     * @param genePreferences
     */
    public GenePreferences(GenePreferences genePreferences) {
        if (genePreferences.getGoDomainMap() != null) {
            goDomainMap = new HashMap<String, String>();
            goDomainMap.putAll(genePreferences.getGoDomainMap());
        }
        if (genePreferences.getEnsemblVersionsMap() != null) {
            ensemblVersionsMap = new HashMap<String, String>();
            ensemblVersionsMap.putAll(genePreferences.getEnsemblVersionsMap());
        }
        if (genePreferences.getSpecies() != null || genePreferences.getSpeciesMap() != null) {
            allSpecies = getAllSpecies();
        }
        if (genePreferences.getCurrentSpecies() != null) {
            currentSpecies = genePreferences.getCurrentSpecies();
        }
    }

    /**
     * Return the protein evidence type as text.
     *
     * @param type the type of evidence
     * @return the protein evidence type as text
     */
    public static String getProteinEvidencAsString(Integer type) {

        switch (type) {
            case 1:
                return "Protein";
            case 2:
                return "Transcript";
            case 3:
                return "Homology";
            case 4:
                return "Predicted";
            case 5:
                return "Uncertain";
            default:
                return null;
        }
    }

    /**
     * Download the GO mappings.
     *
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param ensemblSchemaName the Ensembl schema name, e.g., default or
     * plants_mart_18
     * @param selectedSpecies
     * @param ensemblVersion
     * @param waitingHandler
     * @throws MalformedURLException
     * @throws IOException
     */
    public void downloadGoMappings(String ensemblType, String ensemblSchemaName, String selectedSpecies, String ensemblVersion, WaitingHandler waitingHandler) throws MalformedURLException, IOException {

        // Construct data
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"" + ensemblSchemaName + "\" formatter = \"TSV\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.7\" >"
                + "<Dataset name = \"" + selectedSpecies + "\" interface = \"default\" >"
                + "<Attribute name = \"uniprot_swissprot_accession\" />"
                //+ "<Attribute name = \"uniprot_sptrembl\" />" // @TODO: not yet supported... how to handle old files?
                + "<Attribute name = \"goslim_goa_accession\" />"
                + "<Attribute name = \"goslim_goa_description\" />"
                + "</Dataset>"
                + "</Query>";

        // @TODO: have to check if goslim_goa_accession and goslim_goa_description is available

        if (!waitingHandler.isRunCanceled()) {

            // Send data
            URL url = getEnsemblUrl(ensemblType);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            try {
                wr.write(requestXml);
                wr.flush();

                if (!waitingHandler.isRunCanceled()) {

                    waitingHandler.setWaitingText("Downloading GO Mappings. Please Wait...");

                    int counter = 0;

                    File tempFile = new File(getGeneMappingFolder(), selectedSpecies + GO_MAPPING_FILE_SUFFIX);
                    boolean fileCreated = tempFile.createNewFile();

                    if (fileCreated) {

                        // Get the response
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        try {
                            FileWriter w = new FileWriter(tempFile);
                            try {
                                BufferedWriter bw = new BufferedWriter(w);

                                try {
                                    String rowLine = br.readLine();

                                    if (rowLine != null && rowLine.startsWith("Query ERROR")) {
                                        throw new IllegalArgumentException("Query error: " + rowLine);
                                    } else {
                                        while (rowLine != null && !waitingHandler.isRunCanceled()) {
                                            waitingHandler.setWaitingText("Downloading GO Mappings. Please Wait... (" + counter++ + " rows downloaded)");
                                            bw.write(rowLine + System.getProperty("line.separator"));
                                            rowLine = br.readLine();
                                        }
                                    }

                                } finally {
                                    bw.close();
                                }
                            } finally {
                                w.close();
                            }
                        } finally {
                            br.close();
                        }

                        if (!waitingHandler.isRunCanceled()) {
                            updateEnsemblVersion(selectedSpecies, "Ensembl " + ensemblVersion);
                        }
                    } else {
                        waitingHandler.setRunCanceled();
                        throw new IllegalArgumentException("The mapping file could not be created.");
                    }
                }
            } finally {
                wr.close();
            }
        }
    }

    /**
     * Download the gene mappings.
     *
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param ensemblSchemaName the Ensembl schema name, e.g., default or
     * plants_mart_18
     * @param selectedSpecies
     * @param waitingHandler
     * @throws MalformedURLException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void downloadGeneMappings(String ensemblType, String ensemblSchemaName, String selectedSpecies, WaitingHandler waitingHandler) throws MalformedURLException, IOException, IllegalArgumentException {

        // Construct data
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"" + ensemblSchemaName + "\" formatter = \"TSV\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.7\" >"
                + "<Dataset name = \"" + selectedSpecies + "\" interface = \"default\" >"
                + "<Attribute name = \"ensembl_gene_id\" />"
                + "<Attribute name = \"external_gene_id\" />"
                + "<Attribute name = \"chromosome_name\" />"
                + "</Dataset>"
                + "</Query>";

        if (!waitingHandler.isRunCanceled()) {

            // Send data
            URL url = getEnsemblUrl(ensemblType);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            try {

                wr.write(requestXml);
                wr.flush();

                if (!waitingHandler.isRunCanceled()) {


                    waitingHandler.setWaitingText("Downloading Gene Mappings. Please Wait...");

                    int counter = 0;

                    File tempFile = new File(getGeneMappingFolder(), selectedSpecies + GENE_MAPPING_FILE_SUFFIX);
                    boolean fileCreated = tempFile.createNewFile();

                    if (fileCreated) {

                        // Get the response
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        try {
                            FileWriter w = new FileWriter(tempFile);
                            try {
                                BufferedWriter bw = new BufferedWriter(w);
                                try {

                                    String rowLine = br.readLine();

                                    if (rowLine != null && rowLine.startsWith("Query ERROR")) {
                                        throw new IllegalArgumentException("Query error on line: " + rowLine);
                                    } else {
                                        while (rowLine != null && !waitingHandler.isRunCanceled()) {
                                            waitingHandler.setWaitingText("Downloading Gene Mappings. Please Wait... (" + counter++ + " rows downloaded)");
                                            bw.write(rowLine + System.getProperty("line.separator"));
                                            rowLine = br.readLine();
                                        }
                                    }

                                } finally {
                                    bw.close();
                                }
                            } finally {
                                w.close();
                            }
                        } finally {
                            br.close();
                        }
                    } else {
                        waitingHandler.setRunCanceled();
                        throw new IllegalArgumentException("The mapping file could not be created.");
                    }
                }
            } finally {
                wr.close();
            }
        }
    }

    /**
     * Returns the path to the folder containing the gene mapping files.
     *
     * @return the gene mapping folder
     */
    public File getGeneMappingFolder() {
        return new File(GENE_MAPPING_FOLDER);
    }

    /**
     * Returns the current species.
     *
     * @return the currentSpecies
     */
    public String getCurrentSpecies() {
        return currentSpecies;
    }

    /**
     * Set the current species.
     *
     * @param currentSpecies the currentSpecies to set
     */
    public void setCurrentSpecies(String currentSpecies) {
        this.currentSpecies = currentSpecies;
    }

    /**
     * Returns the current species type.
     *
     * @return the currentSpeciesType
     */
    public String getCurrentSpeciesType() {
        return currentSpeciesType;
    }

    /**
     * Set the current species type.
     *
     * @param currentSpeciesType the currentSpeciesType to set
     */
    public void setCurrentSpeciesType(String currentSpeciesType) {
        this.currentSpeciesType = currentSpeciesType;
    }

    /**
     * Insert the default gene mappings files. If newer versions of the mapping
     * exists they will not be overwritten.
     *
     * @param aEnsemblVersionsFile
     * @param aGoDomainsFile
     * @param aSpeciesFile
     * @param aDefaultSpeciesGoMappingsFile
     * @param aDefaultSpeciesGeneMappingFile
     */
    public void createDefaultGeneMappingFiles(File aEnsemblVersionsFile, File aGoDomainsFile, File aSpeciesFile, File aDefaultSpeciesGoMappingsFile, File aDefaultSpeciesGeneMappingFile) {

        if (!getGeneMappingFolder().exists()) {
            boolean folderCreated = getGeneMappingFolder().mkdir();

            if (!folderCreated) {
                throw new IllegalArgumentException("Could not create the gene mapping folder!");
            }
        }

        File speciesFile = new File(getGeneMappingFolder(), "species");
        File ensemblVersionsFile = new File(getGeneMappingFolder(), "ensembl_versions");
        File goDomainsFile = new File(getGeneMappingFolder(), "go_domains");
        File defaultSpeciesGoMappingsFile = new File(getGeneMappingFolder(), aDefaultSpeciesGoMappingsFile.getName());
        File defaultSpeciesGeneMappingFile = new File(getGeneMappingFolder(), aDefaultSpeciesGeneMappingFile.getName());

        try {
            if (!speciesFile.exists()) {
                boolean fileCreated = speciesFile.createNewFile();
                if (!fileCreated) {
                    throw new IllegalArgumentException("Could not create the species file!");
                }
            }
            Util.copyFile(aSpeciesFile, speciesFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not create the species file!");
        }

        boolean updateHumanEnsembl = false;

        try {
            if (!ensemblVersionsFile.exists()) {

                updateHumanEnsembl = true;

                boolean fileCreated = ensemblVersionsFile.createNewFile();

                if (!fileCreated) {
                    throw new IllegalArgumentException("Could not create the Ensembl versions file!");
                }

                Util.copyFile(aEnsemblVersionsFile, ensemblVersionsFile);

            } else {
                // file exists, just update the human ensembl version

                String line;
                // read the "new" human Ensembl versions number
                Integer humanEnsemblVersionNew = null;
                FileReader r = new FileReader(aEnsemblVersionsFile);
                try {
                    BufferedReader br = new BufferedReader(r);
                    try {
                        if ((line = br.readLine()) != null) {
                            StringTokenizer tok = new StringTokenizer(line);
                            tok.nextToken(); // species
                            tok.nextToken(); // ensembl
                            humanEnsemblVersionNew = new Integer(tok.nextToken());
                        }
                    } finally {
                        br.close();
                    }
                } finally {
                    r.close();
                }


                if (humanEnsemblVersionNew != null) {

                    // find the old human Ensembl versions number
                    r = new FileReader(ensemblVersionsFile);
                    try {
                        BufferedReader br = new BufferedReader(r);
                        try {
                            while ((line = br.readLine()) != null && !updateHumanEnsembl) {

                                StringTokenizer tok = new StringTokenizer(line);
                                String tempSpecies = tok.nextToken(); // species
                                tok.nextToken(); // ensembl
                                Integer humanEnsemblVersionOld = new Integer(tok.nextToken());

                                if (tempSpecies.equalsIgnoreCase("hsapiens_gene_ensembl")) {
                                    if (humanEnsemblVersionOld < humanEnsemblVersionNew) {
                                        updateHumanEnsembl = true;
                                    }
                                }
                            }

                        } finally {
                            br.close();
                        }
                    } finally {
                        r.close();
                    }

                    // load the previous ensembl version numbers
                    loadEnsemblSpeciesVersions(ensemblVersionsFile);

                    // resave the ensembl human version numbers
                    if (updateHumanEnsembl) {
                        updateEnsemblVersion("hsapiens_gene_ensembl", "Ensembl " + humanEnsemblVersionNew);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not create or update the Ensembl versions file!");
        }

        try {
            if (!goDomainsFile.exists()) {
                boolean fileCreated = goDomainsFile.createNewFile();
                if (!fileCreated) {
                    throw new IllegalArgumentException("Could not create the GO domains file!");
                }
            }
            Util.copyFile(aGoDomainsFile, goDomainsFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not create the GO domains file!");
        }

        if (updateHumanEnsembl) {

            try {
                if (!defaultSpeciesGoMappingsFile.exists()) {
                    boolean fileCreated = defaultSpeciesGoMappingsFile.createNewFile();
                    if (!fileCreated) {
                        throw new IllegalArgumentException("Could not create the default species GO mapping file!");
                    }
                }
                Util.copyFile(aDefaultSpeciesGoMappingsFile, defaultSpeciesGoMappingsFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Could not create the default species GO mapping file!");
            }

            try {
                if (!defaultSpeciesGeneMappingFile.exists()) {
                    boolean fileCreated = defaultSpeciesGeneMappingFile.createNewFile();
                    if (!fileCreated) {
                        throw new IllegalArgumentException("Could not create the default species gene mapping file!");
                    }
                }
                Util.copyFile(aDefaultSpeciesGeneMappingFile, defaultSpeciesGeneMappingFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Could not create the default species gene mapping file!");
            }
        }
    }

    /**
     * Load the mapping files.
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void loadSpeciesAndGoDomains() throws IOException, IllegalArgumentException {

        try {
            if (!getGeneMappingFolder().exists()) {
                throw new IllegalArgumentException("Could not create the gene mapping folder!");
            }

            File speciesFile = new File(getGeneMappingFolder(), "species");
            File ensemblVersionsFile = new File(getGeneMappingFolder(), "ensembl_versions");
            File goDomainsFile = new File(getGeneMappingFolder(), "go_domains");

            if (!speciesFile.exists()) {
                throw new IllegalArgumentException("Could not create the species file!");
            }

            if (!ensemblVersionsFile.exists()) {
                throw new IllegalArgumentException("Could not create the Ensembl versions file!");
            }

            if (!goDomainsFile.exists()) {
                throw new IllegalArgumentException("Could not create the GO domains file!");
            }

            goDomainMap = new HashMap<String, String>();
            allSpecies = new HashMap<String, ArrayList<String>>();
            allSpeciesMap = new HashMap<String, HashMap<String, String>>();
            ensemblVersionsMap = new HashMap<String, String>();

            if (!goDomainsFile.exists()) {
                throw new IllegalArgumentException("GO domains file \"" + goDomainsFile.getName() + "\" not found!\n"
                        + "Continuing without GO domains.");
            } else {

                // read the GO domains
                FileReader r = new FileReader(goDomainsFile);
                try {
                    BufferedReader br = new BufferedReader(r);
                    try {

                        String line;

                        while ((line = br.readLine()) != null) {
                            String[] elements = line.split("\\t");
                            goDomainMap.put(elements[0], elements[1]);
                        }

                    } finally {
                        br.close();
                    }
                } finally {
                    r.close();
                }
            }

            if (ensemblVersionsFile.exists()) {
                // read the Ensembl versions
                loadEnsemblSpeciesVersions(ensemblVersionsFile);
            }

            if (!speciesFile.exists()) {
                throw new IllegalArgumentException("Species file \"" + speciesFile.getName() + "\" not found!\n"
                        + "GO Analysis Canceled.");
            } else {

                // read the species list
                FileReader r = new FileReader(speciesFile);
                try {
                    BufferedReader br = new BufferedReader(r);
                    try {

                        String line = br.readLine();
                        String currentSpeciesType = line.substring(1);
                        ArrayList<String> tempSpeciesList = new ArrayList<String>();

                        while ((line = br.readLine()) != null) {

                            if (line.trim().length() > 0) {

                                if (line.startsWith(">")) {
                                    // add the species to the map
                                    allSpecies.put(currentSpeciesType, tempSpeciesList);

                                    // reset for the next species
                                    currentSpeciesType = line.substring(1);
                                    tempSpeciesList = new ArrayList<String>();
                                } else {

                                    String[] elements = line.split("\\t");
                                    String tempSpecies = elements[0].trim();

                                    if (!allSpeciesMap.containsKey(currentSpeciesType)) {
                                        allSpeciesMap.put(currentSpeciesType, new HashMap<String, String>());
                                    }

                                    allSpeciesMap.get(currentSpeciesType).put(tempSpecies, elements[1].trim());
                                    tempSpeciesList.add(tempSpecies);
                                }
                            }
                        }

                        // add the last species type
                        if (!tempSpeciesList.isEmpty()) {
                            allSpecies.put(currentSpeciesType, tempSpeciesList);
                        }

                    } finally {
                        br.close();
                    }
                } finally {
                    r.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("An error occured when loading the species and GO domain file.\nGO Analysis Canceled.");
        }
    }

    /**
     * Returns the GO domain map, e.g., key: GO term: GO:0007568, element:
     * biological_process.
     *
     * @return the goDomainMap
     */
    public HashMap<String, String> getGoDomainMap() {
        return goDomainMap;
    }

    /**
     * Returns the species map. Key: latin name, element: Ensembl database name,
     * e.g., key: Homo sapiens, element: hsapiens.
     *
     * @return the speciesMap
     * @deprecated use getAllSpeciesMap instead
     */
    public HashMap<String, String> getSpeciesMap() {
        return speciesMap;
    }

    /**
     * Returns the species map. Main key: Ensembl type, e.g., Vertebrates or
     * Plants. Next level: key: latin name, element: ensembl database name,
     * e.g., key: Homo sapiens, element: hsapiens_gene_ensembl.
     *
     * @return the speciesMap
     */
    public HashMap<String, HashMap<String, String>> getAllSpeciesMap() {
        return allSpeciesMap;
    }

    /**
     * Returns the Ensembl database name corresponding to a species name
     * according to the speciesMap. Null if not found.
     *
     * @param speciesName the species name as available in the species list
     * @return the Ensembl database name
     * @deprecated use the one with the Ensembl type parameter instead
     */
    public String getEnsemblDatabaseName(String speciesName) {
        return speciesMap.get(speciesName);
    }

    /**
     * Returns the Ensembl database name corresponding to a species name
     * according to the speciesMap. Null if not found.
     *
     * @param ensemblType the Ensembl type, e.g., Vertebrates or Plants
     * @param speciesName the species name as available in the species list
     * @return the Ensembl database name
     */
    public String getEnsemblDatabaseName(String ensemblType, String speciesName) {
        return allSpeciesMap.get(ensemblType).get(speciesName);
    }

    /**
     * Returns the Ensembl versions map.
     *
     * @return the ensemblVersionsMap
     */
    public HashMap<String, String> getEnsemblVersionsMap() {
        return ensemblVersionsMap;
    }

    /**
     * Returns the Ensembl version corresponding to the given Ensembl database
     * according to the ensemblVersionsMap. Null if not found.
     *
     * @param ensemblDatabase the Ensembl database
     * @return the Ensembl version
     */
    public String getEnsemblVersion(String ensemblDatabase) {
        return ensemblVersionsMap.get(ensemblDatabase);
    }

    /**
     * Returns the Ensembl version for the given species name. Null if not
     * found.
     *
     * @param speciesName the species name as available in the species list
     * @return the Ensembl version
     * @deprecated use getEnsemblSpeciesVersion(String ensemblType, String
     * speciesName) instead
     */
    public String getEnsemblSpeciesVersion(String speciesName) {
        String ensemblDB = getEnsemblDatabaseName(speciesName);
        if (ensemblDB != null) {
            return getEnsemblVersion(ensemblDB);
        }
        return null;
    }

    /**
     * Returns the Ensembl version for the given species name. Null if not
     * found.
     *
     * @param ensemblType the Ensembl type, e.g., Vertebrates or Plants
     * @param speciesName the species name as available in the species list
     * @return the Ensembl version
     */
    public String getEnsemblSpeciesVersion(String ensemblType, String speciesName) {
        String ensemblDB = getEnsemblDatabaseName(ensemblType, speciesName);
        if (ensemblDB != null) {
            return getEnsemblVersion(ensemblDB);
        }
        return null;
    }

    /**
     * Return the species list. NB: also contains species separators.
     *
     * @return the species
     * @deprecated use getAllSpecies instead
     */
    public ArrayList<String> getSpecies() {
        if (availableSpecies == null && species != null) {
            availableSpecies = new ArrayList<String>();
            availableSpecies.addAll(species);
        }
        return availableSpecies;
    }

    /**
     * Return the species lists.
     *
     * @return the species
     */
    public HashMap<String, ArrayList<String>> getAllSpecies() {
        if (species != null) {
            allSpecies = new HashMap<String, ArrayList<String>>();
            ArrayList<String> temp = new ArrayList<String>();
            temp.addAll(species);
            allSpecies.put("Vertebrates", temp);
        }
        if (availableSpecies != null) {
            allSpecies = new HashMap<String, ArrayList<String>>();
            allSpecies.put("Vertebrates", availableSpecies);
        }
        return allSpecies;
    }

    /**
     * Update the Ensembl version for the given species.
     *
     * @param selectedSpecies the database name of the species to update, e.g.,
     * hsapiens_gene_ensembl
     * @param ensemblVersion the new Ensembl version
     * @throws IOException
     */
    public void updateEnsemblVersion(String selectedSpecies, String ensemblVersion) throws IOException {

        FileWriter w = new FileWriter(new File(getGeneMappingFolder(), "ensembl_versions"));
        try {
            BufferedWriter bw = new BufferedWriter(w);
            try {

                if (ensemblVersionsMap == null) {
                    ensemblVersionsMap = new HashMap<String, String>();
                }

                ensemblVersionsMap.put(selectedSpecies, ensemblVersion);

                Iterator<String> iterator = ensemblVersionsMap.keySet().iterator();

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    bw.write(key + "\t" + ensemblVersionsMap.get(key) + System.getProperty("line.separator"));
                }

            } finally {
                bw.close();
            }
        } finally {
            w.close();
        }
    }

    /**
     * Loads the given Ensembl species file.
     *
     * @param ensemblVersionsFile the Ensembl species file to load
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void loadEnsemblSpeciesVersions(File ensemblVersionsFile) throws FileNotFoundException, IOException {

        // load the existing ensembl version numbers
        FileReader r = new FileReader(ensemblVersionsFile);
        try {
            BufferedReader br = new BufferedReader(r);
            try {

                ensemblVersionsMap = new HashMap<String, String>();
                String line = br.readLine();

                while (line != null) {
                    String[] elements = line.split("\\t");
                    ensemblVersionsMap.put(elements[0], elements[1]);
                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        } finally {
            r.close();
        }
    }

    /**
     * Imports the gene mappings.
     *
     * @param jarFilePath the jar file path
     * @param waitingHandler the waiting handler
     * @return a boolean indicating whether the loading was successful
     */
    public boolean loadGeneMappings(String jarFilePath, WaitingHandler waitingHandler) {

        //@TODO: we might want to split this method?

        boolean success = true;
        try {
            createDefaultGeneMappingFiles(
                    new File(jarFilePath, "resources/conf/gene_ontology/ensembl_versions"),
                    new File(jarFilePath, "resources/conf/gene_ontology/go_domains"),
                    new File(jarFilePath, "resources/conf/gene_ontology/species"),
                    new File(jarFilePath, "resources/conf/gene_ontology/hsapiens_gene_ensembl_go_mappings"),
                    new File(jarFilePath, "resources/conf/gene_ontology/hsapiens_gene_ensembl_gene_mappings"));
            loadSpeciesAndGoDomains();
        } catch (IOException e) {
            if (waitingHandler.isReport()) {
                waitingHandler.appendReport("An error occurred while attempting to create the gene preferences.", true, true);
            }
            e.printStackTrace();
            success = false;
        }

        if (getCurrentSpecies() != null && getCurrentSpeciesType() != null && getAllSpeciesMap() != null && new File(getGeneMappingFolder(),
                getAllSpeciesMap().get(getCurrentSpeciesType()).get(getCurrentSpecies()) + GenePreferences.GENE_MAPPING_FILE_SUFFIX).exists()) {
            try {
                GeneFactory geneFactory = GeneFactory.getInstance();
                geneFactory.initialize(new File(getGeneMappingFolder(),
                        getAllSpeciesMap().get(getCurrentSpeciesType()).get(getCurrentSpecies()) + GenePreferences.GENE_MAPPING_FILE_SUFFIX), null);
            } catch (Exception e) {
                if (waitingHandler.isReport()) {
                    waitingHandler.appendReport("Unable to load the gene mapping file.", true, true);
                }
                e.printStackTrace();
                success = false;
            }
        }

        if (getCurrentSpecies() != null && getCurrentSpeciesType() != null && getAllSpeciesMap() != null && new File(getGeneMappingFolder(),
                getAllSpeciesMap().get(getCurrentSpeciesType()).get(getCurrentSpecies()) + GenePreferences.GO_MAPPING_FILE_SUFFIX).exists()) {
            try {
                GOFactory goFactory = GOFactory.getInstance();
                goFactory.initialize(new File(getGeneMappingFolder(),
                        getAllSpeciesMap().get(getCurrentSpeciesType()).get(getCurrentSpecies()) + GenePreferences.GO_MAPPING_FILE_SUFFIX), null);
            } catch (Exception e) {
                if (waitingHandler.isReport()) {
                    waitingHandler.appendReport("Unable to load the gene ontology mapping file.", true, true);
                }
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    /**
     * Returns the Ensembl URL for the given Ensembl (sub-)version.
     *
     * @param ensemblType the Ensembl type, e.g., fungi or plants
     * @return the Ensembl URL
     * @throws MalformedURLException
     */
    private URL getEnsemblUrl(String ensemblType) throws MalformedURLException {
        if (ensemblType.equalsIgnoreCase("fungi")) {
            return new URL("http://fungi.ensembl.org/biomart/martservice/result");
        } else if (ensemblType.equalsIgnoreCase("plants")) {
            return new URL("http://plants.ensembl.org/biomart/martservice/result");
        } else if (ensemblType.equalsIgnoreCase("protists")) {
            return new URL("http://protists.ensembl.org/biomart/martservice/result");
        } else if (ensemblType.equalsIgnoreCase("metazoa")) {
            return new URL("http://metazoa.ensembl.org/biomart/martservice/result");
        } else {
            return new URL("http://www.biomart.org/biomart/martservice/result");
        }
    }
}
