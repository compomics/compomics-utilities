package com.compomics.util.preferences;

import com.compomics.util.Util;
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
     * The species separator used in the species comboboxes.
     */
    public final static String SPECIES_SEPARATOR = "------------------------------------------------------------";
    /**
     * The text to use to tell the user to please select a species in the list.
     */
    public final static String SELECT_SPECIES_TAG = "-- Select Species --";
    /**
     * The text to use for no species selected.
     */
    public final static String NO_SPECIES_TAG = "-- (no selection) --";
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
     * The GO domain map. e.g., key: GO term: GO:0007568, element:
     * biological_process.
     */
    private HashMap<String, String> goDomainMap;
    /**
     * The species map, key: latin name, element: ensembl database name, e.g.,
     * key: Homo sapiens, element: hsapiens_gene_ensembl.
     */
    private HashMap<String, String> speciesMap;
    /**
     * The Ensembl versions for the downloaded species.
     */
    private HashMap<String, String> ensemblVersionsMap;
    /**
     * The list of species.
     */
    private Vector<String> species;

    /**
     * Create a new GenePreferences object.
     */
    public GenePreferences() {
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
     * @param selectedSpecies
     * @param ensemblVersion
     * @param waitingHandler
     * @return true if the download was ok
     * @throws MalformedURLException
     * @throws IOException
     */
    public boolean downloadGoMappings(String selectedSpecies, String ensemblVersion, WaitingHandler waitingHandler) throws MalformedURLException, IOException {

        // Construct data
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"default\" formatter = \"TSV\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.6\" >"
                + "<Dataset name = \"" + selectedSpecies + "\" interface = \"default\" >"
                + "<Attribute name = \"uniprot_swissprot_accession\" />"
                + "<Attribute name = \"goslim_goa_accession\" />"
                + "<Attribute name = \"goslim_goa_description\" />"
                + "</Dataset>"
                + "</Query>";

        if (!waitingHandler.isRunCanceled()) {

            // Send data
            URL url = new URL("http://www.biomart.org/biomart/martservice/result");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(requestXml);
            wr.flush();

            if (!waitingHandler.isRunCanceled()) {

                // Get the response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                waitingHandler.setWaitingText("Downloading GO Mappings. Please Wait...");

                int counter = 0;

                File tempFile = new File(getGeneMappingFolder(), selectedSpecies + GO_MAPPING_FILE_SUFFIX);
                boolean fileCreated = tempFile.createNewFile();

                if (fileCreated) {

                    FileWriter w = new FileWriter(tempFile);
                    BufferedWriter bw = new BufferedWriter(w);

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

                    bw.close();
                    w.close();
                    wr.close();
                    br.close();

                    if (!waitingHandler.isRunCanceled()) {
                        updateEnsemblVersion(selectedSpecies, "Ensembl " + ensemblVersion);
                    }
                } else {
                    waitingHandler.setRunCanceled();
                    throw new IllegalArgumentException("The mapping file could not be created.");
                }

                return !waitingHandler.isRunCanceled();

            } else {
                wr.close();
                return false;
            }
        }

        return false;
    }

    /**
     * Download the gene mappings.
     *
     * @param selectedSpecies
     * @param waitingHandler
     * @return true if the download was ok
     * @throws MalformedURLException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public boolean downloadGeneMappings(String selectedSpecies, WaitingHandler waitingHandler) throws MalformedURLException, IOException, IllegalArgumentException {

        // Construct data
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"default\" formatter = \"TSV\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.6\" >"
                + "<Dataset name = \"" + selectedSpecies + "\" interface = \"default\" >"
                + "<Attribute name = \"ensembl_gene_id\" />"
                + "<Attribute name = \"external_gene_id\" />"
                + "<Attribute name = \"chromosome_name\" />"
                + "</Dataset>"
                + "</Query>";

        if (!waitingHandler.isRunCanceled()) {

            // Send data
            URL url = new URL("http://www.biomart.org/biomart/martservice/result");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(requestXml);
            wr.flush();

            if (!waitingHandler.isRunCanceled()) {

                // Get the response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                waitingHandler.setWaitingText("Downloading Gene Mappings. Please Wait...");

                int counter = 0;

                File tempFile = new File(getGeneMappingFolder(), selectedSpecies + GENE_MAPPING_FILE_SUFFIX);
                boolean fileCreated = tempFile.createNewFile();

                if (fileCreated) {

                    FileWriter w = new FileWriter(tempFile);
                    BufferedWriter bw = new BufferedWriter(w);

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

                    bw.close();
                    w.close();
                    wr.close();
                    br.close();

                } else {
                    waitingHandler.setRunCanceled();
                    throw new IllegalArgumentException("The mapping file could not be created.");
                }

                return !waitingHandler.isRunCanceled();
            } else {
                wr.close();
                return false;
            }
        }

        return false;
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
     * Insert the default gene mappings files. If the files already exists these
     * will be kept and not overwritten.
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

        if (!speciesFile.exists()) {
            try {
                boolean fileCreated = speciesFile.createNewFile();

                if (!fileCreated) {
                    throw new IllegalArgumentException("Could not create the species file!");
                }

                Util.copyFile(aSpeciesFile, speciesFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Could not create the species file!");
            }
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

                // read the "new" human Ensembl versions number
                Integer humanEnsemblVersionNew = null;
                FileReader r = new FileReader(aEnsemblVersionsFile);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                if (line != null) {
                    StringTokenizer tok = new StringTokenizer(line);
                    tok.nextToken(); // species
                    tok.nextToken(); // ensembl
                    humanEnsemblVersionNew = new Integer(tok.nextToken());
                }

                br.close();
                r.close();


                if (humanEnsemblVersionNew != null) {

                    // find the old human Ensembl versions number
                    r = new FileReader(ensemblVersionsFile);
                    br = new BufferedReader(r);

                    line = br.readLine();

                    while (line != null && !updateHumanEnsembl) {

                        StringTokenizer tok = new StringTokenizer(line);
                        String tempSpecies = tok.nextToken(); // species
                        tok.nextToken(); // ensembl
                        Integer humanEnsemblVersionOld = new Integer(tok.nextToken());

                        if (tempSpecies.equalsIgnoreCase("hsapiens_gene_ensembl")) {
                            if (humanEnsemblVersionOld < humanEnsemblVersionNew) {
                                updateHumanEnsembl = true;
                            }
                        }

                        line = br.readLine();
                    }

                    br.close();
                    r.close();

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

        if (!goDomainsFile.exists()) {
            try {
                boolean fileCreated = goDomainsFile.createNewFile();

                if (!fileCreated) {
                    throw new IllegalArgumentException("Could not create the GO domains file!");
                }

                Util.copyFile(aGoDomainsFile, goDomainsFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Could not create the GO domains file!");
            }
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
            species = new Vector<String>();
            speciesMap = new HashMap<String, String>();
            ensemblVersionsMap = new HashMap<String, String>();

            if (!goDomainsFile.exists()) {
                throw new IllegalArgumentException("GO domains file \"" + goDomainsFile.getName() + "\" not found!\n"
                        + "Continuing without GO domains.");
            } else {

                // read the GO domains
                FileReader r = new FileReader(goDomainsFile);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                while (line != null) {
                    String[] elements = line.split("\\t");
                    goDomainMap.put(elements[0], elements[1]);
                    line = br.readLine();
                }

                br.close();
                r.close();
            }

            if (ensemblVersionsFile.exists()) {

                // read the Ensembl versions
                loadEnsemblSpeciesVersions(ensemblVersionsFile);
            }


            if (!speciesFile.exists()) {
                throw new IllegalArgumentException("GO species file \"" + speciesFile.getName() + "\" not found!\n"
                        + "GO Analysis Canceled.");
            } else {

                // read the species list
                FileReader r = new FileReader(speciesFile);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                species.add(SELECT_SPECIES_TAG);
                species.add(SPECIES_SEPARATOR);

                while (line != null) {
                    String[] elements = line.split("\\t");
                    speciesMap.put(elements[0].trim(), elements[1].trim());

                    if (species.size() == 5) {
                        species.add(SPECIES_SEPARATOR);
                    }

                    if (ensemblVersionsMap.containsKey(elements[1].trim())) {
                        species.add(elements[0].trim() + " [" + ensemblVersionsMap.get(elements[1].trim()) + "]");
                    } else {
                        species.add(elements[0].trim() + " [N/A]");
                    }

                    line = br.readLine();
                }

                species.add(SPECIES_SEPARATOR);
                species.add(NO_SPECIES_TAG);

                br.close();
                r.close();
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
     * Returns the species map. Key: latin name, element: ensembl database name,
     * e.g., key: Homo sapiens, element: hsapiens.
     *
     * @return the speciesMap
     */
    public HashMap<String, String> getSpeciesMap() {
        return speciesMap;
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
     * Return the species list. NB: also contains species separators.
     *
     * @return the species
     */
    public Vector<String> getSpecies() {
        return species;
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
        BufferedWriter bw = new BufferedWriter(w);

        if (ensemblVersionsMap == null) {
            ensemblVersionsMap = new HashMap<String, String>();
        }

        ensemblVersionsMap.put(selectedSpecies, ensemblVersion);

        Iterator<String> iterator = ensemblVersionsMap.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            bw.write(key + "\t" + ensemblVersionsMap.get(key) + System.getProperty("line.separator"));
        }

        bw.close();
        w.close();
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
        BufferedReader br = new BufferedReader(r);
        
        ensemblVersionsMap = new HashMap<String, String>();

        String line = br.readLine();

        while (line != null) {
            String[] elements = line.split("\\t");
            ensemblVersionsMap.put(elements[0], elements[1]);
            line = br.readLine();
        }

        br.close();
        r.close();
    }
}
