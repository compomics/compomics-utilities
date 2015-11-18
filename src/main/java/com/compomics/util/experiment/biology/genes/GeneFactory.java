package com.compomics.util.experiment.biology.genes;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.genes.ensembl.EnsemblVersion;
import com.compomics.util.experiment.biology.genes.ensembl.GeneMapping;
import com.compomics.util.experiment.biology.genes.go.GoMapping;
import com.compomics.util.experiment.biology.taxonomy.EnsemblSpecies;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.protein_sequences.FastaIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.preferences.GenePreferences;
import com.compomics.util.protein.Header;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class used to map proteins to gene information.
 *
 * @author Marc Vaudel
 */
public class GeneFactory {

    /**
     * The instance of the factory.
     */
    private static GeneFactory instance = null;

    /**
     * The separator used to separate line contents.
     */
    public final static String separator = "\t";
    /**
     * The folder where gene mapping files are stored.
     */
    private static String GENE_MAPPING_FOLDER = System.getProperty("user.home") + "/.compomics/gene_mappings/";
    /**
     * The subfolder relative to the jar file where gene mapping files are
     * stored in tools.
     */
    private final static String TOOL_GENE_MAPPING_SUBFOLDER = "resources/conf/gene_mappings/";
    /**
     * The name of the Ensembl versions file.
     */
    private static final String ENSEMBL_VERSIONS = "ensembl_versions";
    /**
     * The name of the GO domains file.
     */
    private static final String GO_DOMAINS = "go_domains";
    /**
     * The name of the species file.
     */
    private static final String ENSEMBL_SPECIES_FILENAME = "species";
    /**
     * The suffix to use for files containing gene mappings.
     */
    public final static String GENE_MAPPING_FILE_SUFFIX = "_gene_mappings";
    /**
     * The suffix to use for files containing GO mappings.
     */
    public final static String GO_MAPPING_FILE_SUFFIX = "_go_mappings";
    /**
     * The Ensembl species mapping.
     */
    private EnsemblSpecies ensemblSpecies;
    /**
     * The Ensembl versions for each species.
     */
    private HashMap<String, String> ensemblVersionsMap;

    /**
     * Static method returning the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static GeneFactory getInstance() {
        if (instance == null) {
            instance = new GeneFactory();
        }
        return instance;
    }

    /**
     * Constructor.
     */
    private GeneFactory() {
    }

    /**
     * Initializes the factory.
     *
     * @param jarFilePath the path to the jar file
     *
     * @throws java.io.IOException Exception thrown if an error occurs while
     * reading the species mapping
     */
    public void initialize(String jarFilePath) throws IOException {

        // load the previous ensembl version numbers
        File ensemblVersionsFile = getEnsemblVersionsFile();
        if (ensemblVersionsFile.exists()) {
            loadEnsemblSpeciesVersions(ensemblVersionsFile);
        }

        ensemblSpecies = new EnsemblSpecies();
        File speciesFile = new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + ENSEMBL_SPECIES_FILENAME);
        ensemblSpecies.loadMapping(speciesFile);

        createDefaultGeneMappingFiles(
                new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + ENSEMBL_VERSIONS),
                new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + GO_DOMAINS),
                new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + ENSEMBL_SPECIES_FILENAME),
                new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + "hsapiens_gene_ensembl_go_mappings"),
                new File(jarFilePath, TOOL_GENE_MAPPING_SUBFOLDER + "hsapiens_gene_ensembl_gene_mappings"),
                true);

        ensemblSpecies = new EnsemblSpecies();
        speciesFile = new File(GENE_MAPPING_FOLDER, ENSEMBL_SPECIES_FILENAME);
        ensemblSpecies.loadMapping(speciesFile);
    }

    /**
     * Returns the gene maps for the fasta file loaded in the factory.
     *
     * @param genePreferences the gene preferences
     * @param waitingHandler waiting handler displaying progress for the
     * download and allowing canceling of the progress.
     *
     * @return the gene maps for the fasta file loaded in the factory
     *
     * @throws java.io.IOException thrown whenever an error occurs while
     * iterating the proteins in the fasta database.
     */
    public GeneMaps getGeneMaps(GenePreferences genePreferences, WaitingHandler waitingHandler) throws IOException {

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        FastaIndex fastaIndex = sequenceFactory.getCurrentFastaIndex();
        HashMap<String, Integer> speciesOccurrence = fastaIndex.getSpecies();
        HashMap<String, GeneMapping> geneMappings = new HashMap<String, GeneMapping>(speciesOccurrence.size());
        HashMap<String, GoMapping> goMappings = new HashMap<String, GoMapping>(speciesOccurrence.size());

        // Download/Update species mapping, put them in maps per species
        for (String species : speciesOccurrence.keySet()) {

            if (!species.equals(SpeciesFactory.unknown)) {

                String ensemblDbName = ensemblSpecies.getDatabaseName(species);

                if (ensemblDbName != null) {
                    File geneMappingFile = getGeneMappingFile(ensemblDbName);
                    File goMappingFile = getGoMappingFile(ensemblDbName);

                    if (genePreferences.getAutoUpdate()) {
                        boolean success = false;
                        try {
                            String speciesType = ensemblSpecies.getSpeciesType(species);
                            if (!geneMappingFile.exists() || !goMappingFile.exists() || newVersionExists(species)) {
                                success = downloadMappings(waitingHandler, speciesType, species);
                            }
                            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                                return null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!success) {
                            waitingHandler.appendReport("Update of Gene information for species " + species + " failed. A previous version will be used if available.", true, true);
                        }
                    }

                    if (geneMappingFile.exists()) {
                        GeneMapping geneMapping = new GeneMapping();
                        try {
                            geneMapping.importFromFile(geneMappingFile, waitingHandler);
                            geneMappings.put(species, geneMapping);
                        } catch (Exception e) {
                            waitingHandler.appendReport("Import of the gene mapping for " + species + " failed. Gene information for this species will not be available.", true, true);
                        }
                    } else {
                        waitingHandler.appendReport("Gene mapping for " + species + " not available. Gene information for this species will not be available.", true, true);
                    }

                    if (goMappingFile.exists()) {
                        GoMapping goMapping = new GoMapping();
                        try {
                            goMapping.laodMappingFromFile(goMappingFile, waitingHandler);
                            goMappings.put(species, goMapping);
                        } catch (Exception e) {
                            waitingHandler.appendReport("Import of the GO mapping for " + species + " failed. GO annotatoin for this species will not be available.", true, true);
                        }
                    } else {
                        waitingHandler.appendReport("GO mapping for " + species + " not available. GO annotatoin for this species will not be available.", true, true);
                    }
                } else {
                    waitingHandler.appendReport(species + " not available in Ensembl. Gene and GO annotatoin for this species will not be available.", true, true);
                }
            }
        }

        // Get the mappings for the proteins in the sequence factory
        GeneMaps geneMaps = new GeneMaps();
        HashMap<String, String> ensemblVersionsUsed = new HashMap<String, String>(ensemblVersionsMap);
        HashMap<String, String> geneNameToEnsemblIdMap = new HashMap<String, String>();
        HashMap<String, String> geneNameToChromosomeMap = new HashMap<String, String>();
        HashMap<String, HashSet<String>> proteinToGoMap = new HashMap<String, HashSet<String>>();
        HashMap<String, HashSet<String>> goToProteinMap = new HashMap<String, HashSet<String>>();
        HashMap<String, String> goNamesMap = new HashMap<String, String>();
        SequenceFactory.HeaderIterator it = sequenceFactory.getHeaderIterator(true);

        while (it.hasNext()) {

            Header header = it.getNext();
            String species = header.getTaxonomy();

            if (species != null) {

                String geneName = header.getGeneName();
                if (geneName != null) {
                    GeneMapping geneMapping = geneMappings.get(species);
                    if (geneMapping != null) {
                        String chromosome = geneMapping.getChromosome(geneName);
                        if (chromosome != null) {
                            geneNameToChromosomeMap.put(geneName, chromosome);
                        }
                        String ensemblId = geneMapping.getEnsemblAccession(geneName);
                        if (ensemblId != null) {
                            geneNameToEnsemblIdMap.put(geneName, ensemblId);
                        }
                    }
                }

                GoMapping goMapping = goMappings.get(species);
                if (goMapping != null) {
                    String accession = header.getAccession();
                    HashSet<String> goTerms = proteinToGoMap.get(accession);
                    if (goTerms == null) {
                        goTerms = new HashSet<String>();
                        proteinToGoMap.put(accession, goTerms);
                    }
                    HashSet<String> newTerms = goMapping.getGoAccessions(accession);
                    if (newTerms != null) {
                        goTerms.addAll(newTerms);
                        for (String goTerm : newTerms) {
                            String goName = goMapping.getTermName(accession);
                            goNamesMap.put(goTerm, goName);

                            HashSet<String> proteins = goToProteinMap.get(goTerm);
                            if (proteins == null) {
                                proteins = new HashSet<String>();
                                goToProteinMap.put(goTerm, proteins);
                            }
                            proteins.add(accession);
                        }
                    }
                }
            }
        }
        geneMaps.setEnsemblVersionsMap(ensemblVersionsUsed);
        geneMaps.setGeneNameToEnsemblIdMap(geneNameToEnsemblIdMap);
        geneMaps.setGeneNameToChromosomeMap(geneNameToChromosomeMap);
        geneMaps.setProteinToGoMap(proteinToGoMap);
        geneMaps.setGoAccessionToProteinMap(goToProteinMap);
        geneMaps.setGoNamesMap(goNamesMap);

        return geneMaps;
    }

    /**
     * Download the gene sequences mappings.
     *
     * @param destinationFile The destination file where to save the gene
     * sequences
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param ensemblSchemaName the Ensembl schema name, e.g., default or
     * plants_mart_18
     * @param selectedSpecies the selected species
     * @param waitingHandler waiting handler displaying progress and allowing
     * canceling the process
     *
     * @return true if downloading went OK
     *
     * @throws MalformedURLException if an MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public boolean downloadGeneSequences(File destinationFile, String ensemblType, String ensemblSchemaName, String selectedSpecies, WaitingHandler waitingHandler) throws MalformedURLException, IOException {

        // Construct data
        String ensemblDbName = ensemblSpecies.getDatabaseName(selectedSpecies);
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"" + ensemblSchemaName + "\" formatter = \"FASTA\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.7\" >"
                + "<Dataset name = \"" + ensemblDbName + "\" interface = \"default\" >"
                + "<Attribute name = \"ensembl_gene_id\" />\n"
                + "<Attribute name = \"coding\" />"
                + "</Dataset>\n"
                + "</Query>"
                + "</Query>";

        String waitingText = "Downloading gene sequences. Please Wait...";
        return queryEnsembl(requestXml, waitingText, destinationFile, ensemblType, waitingHandler);
    }

    /**
     * Download the GO mappings.
     *
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param ensemblSchemaName the Ensembl schema name, e.g., default or
     * plants_mart_18
     * @param selectedSpecies the selected species
     * @param swissProtMapping if true, use the uniprot_swissprot_accession
     * parameter, if false use the uniprot_sptrembl parameter
     * @param waitingHandler waiting handler displaying progress and allowing
     * canceling the process
     *
     * @return true if downloading went OK
     *
     * @throws MalformedURLException if an MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public boolean downloadGoMappings(String ensemblType, String ensemblSchemaName, String selectedSpecies, boolean swissProtMapping, WaitingHandler waitingHandler) throws MalformedURLException, IOException {

        String accessionMapping;
        String ensemblDbName = ensemblSpecies.getDatabaseName(selectedSpecies);

        if (swissProtMapping) {
            if (ensemblType.equalsIgnoreCase("ensembl")) {
                accessionMapping = "\"uniprot_swissprot\"";
            } else {
                accessionMapping = "\"uniprot_swissprot_accession\"";
            }
        } else {
            accessionMapping = "\"uniprot_sptrembl\"";
        }

        // Construct data
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"" + ensemblSchemaName + "\" formatter = \"TSV\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.7\" >"
                + "<Dataset name = \"" + ensemblDbName + "\" interface = \"default\" >"
                + "<Attribute name = " + accessionMapping + " />";

        if (ensemblType.equalsIgnoreCase("ensembl")) {
            requestXml += "<Attribute name = \"goslim_goa_accession\" />"
                    + "<Attribute name = \"goslim_goa_description\" />";
        } else {
            requestXml += "<Attribute name = \"go_accession\" />"
                    + "<Attribute name = \"go_name_1006\" />";
        }

        requestXml += "</Dataset>"
                + "</Query>";

        // @TODO: have to check if goslim_goa_accession and goslim_goa_description is available
        File tempFile = getGoMappingFile(ensemblDbName);

        String waitingText = "Downloading GO Mappings. Please Wait...";
        return queryEnsembl(requestXml, waitingText, tempFile, ensemblType, waitingHandler);
    }

    /**
     * Sends an XML query to Ensembl and writes the result in a text file.
     *
     * @param requestXml the XML request
     * @param destinationFile the file where to save the results
     * @param ensemblType the Ensembl type, e.g., default or plants
     *
     * @return true if downloading went OK
     *
     * @throws MalformedURLException if an MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public boolean queryEnsembl(String requestXml, File destinationFile, String ensemblType) throws MalformedURLException, IOException {
        return queryEnsembl(requestXml, destinationFile, ensemblType, null);
    }

    /**
     * Sends an XML query to Ensembl and writes the result in a text file.
     *
     * @param requestXml the XML request
     * @param destinationFile the file where to save the results
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param waitingHandler waiting handler displaying progress and allowing
     * canceling the process
     *
     * @return true if downloading went OK
     *
     * @throws MalformedURLException if an MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public boolean queryEnsembl(String requestXml, File destinationFile, String ensemblType, WaitingHandler waitingHandler) throws MalformedURLException, IOException {
        return queryEnsembl(requestXml, null, destinationFile, ensemblType, waitingHandler);
    }

    /**
     * Sends an XML query to Ensembl and writes the result in a text file.
     *
     * @param requestXml the XML request
     * @param destinationFile the file where to save the results
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param waitingHandler waiting handler displaying progress and allowing
     * canceling the process
     * @param waitingText the text to write in case a progress dialog is used
     *
     * @return true if downloading went OK
     *
     * @throws MalformedURLException if an MalformedURLException occurs
     * @throws IOException if an IOException occurs
     */
    public boolean queryEnsembl(String requestXml, String waitingText, File destinationFile, String ensemblType, WaitingHandler waitingHandler) throws MalformedURLException, IOException {

        if (waitingHandler != null && waitingHandler instanceof ProgressDialogX && waitingText == null) {
            waitingText = "Downloading from Ensembl. Please wait...";
        }
        boolean success = true;

        int lastThousand = 0;

        if (waitingHandler == null || !waitingHandler.isRunCanceled()) {

            // Send data
            URL url = getEnsemblUrl(ensemblType);

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            try {
                wr.write(requestXml);
                wr.flush();

                if (waitingHandler == null || !waitingHandler.isRunCanceled()) {

                    if (waitingHandler != null) {
                        waitingHandler.setWaitingText(waitingText);
                    } else {
                        System.out.println(waitingText);
                    }

                    int counter = 0;

                    boolean fileCreated = destinationFile.createNewFile();

                    if (fileCreated || destinationFile.exists()) {

                        // Get the response
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        try {
                            FileWriter w = new FileWriter(destinationFile);
                            try {
                                BufferedWriter bw = new BufferedWriter(w);

                                try {
                                    String rowLine = br.readLine();

                                    if (rowLine != null && rowLine.startsWith("Query ERROR")) {
                                        if (rowLine.lastIndexOf("Attribute goslim_goa_accession NOT FOUND") != -1) {
                                            success = false;
                                        } else if (rowLine.lastIndexOf("Attribute uniprot_swissprot_accession NOT FOUND") != -1) {
                                            success = false;
                                        } else {
                                            throw new IllegalArgumentException("Query error: " + rowLine);
                                        }
                                    } else {
                                        while (rowLine != null && success) {
                                            if (waitingHandler != null) {
                                                if (waitingHandler.isRunCanceled()) {
                                                    break;
                                                }
                                                if (waitingHandler instanceof ProgressDialogX) {
                                                    waitingHandler.setWaitingText(waitingText + " (" + counter++ + " rows downloaded)");
                                                }
                                            } else {
                                                int thousand = ++counter / 10000;
                                                if (thousand > lastThousand) {
                                                    System.out.println(waitingText + " (" + counter + " rows downloaded)");
                                                    lastThousand = thousand;
                                                }
                                            }
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
                        if (waitingHandler != null) {
                            waitingHandler.setRunCanceled();
                        }
                        throw new IllegalArgumentException("The mapping file could not be created.");
                    }
                }
            } finally {
                wr.close();
            }
        }

        return success;
    }

    /**
     * Download the gene mappings.
     *
     * @param ensemblType the Ensembl type, e.g., default or plants
     * @param ensemblSchemaName the Ensembl schema name, e.g., default or
     * plants_mart_18
     * @param selectedSpecies the selected species
     * @param ensemblVersion the Ensembl version
     * @param waitingHandler the waiting handler
     *
     * @throws MalformedURLException if an MalformedURLException occurs
     * @throws IOException if an IOException occurs
     * @throws IllegalArgumentException if an IllegalArgumentException occurs
     */
    public void downloadGeneMappings(String ensemblType, String ensemblSchemaName, String selectedSpecies, String ensemblVersion,
            WaitingHandler waitingHandler) throws MalformedURLException, IOException, IllegalArgumentException {

        // fix needed to support both default and custom ensembl species
        String externalReference;
        if (ensemblSchemaName.equalsIgnoreCase("default")) {
            externalReference = "<Attribute name = \"external_gene_name\" />";
        } else {
            externalReference = "<Attribute name = \"external_gene_id\" />";
        }

        // Construct data
        String requestXml = "query=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE Query>"
                + "<Query  virtualSchemaName = \"" + ensemblSchemaName + "\" formatter = \"TSV\" header = \"0\" uniqueRows = \"1\" count = \"\" datasetConfigVersion = \"0.7\" >"
                + "<Dataset name = \"" + selectedSpecies + "\" interface = \"default\" >"
                + "<Attribute name = \"ensembl_gene_id\" />"
                + externalReference
                + "<Attribute name = \"chromosome_name\" />"
                + "</Dataset>"
                + "</Query>";

        // @TODO: use the queryEnsembl method here as well?
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

                    String ensemblDbName = ensemblSpecies.getDatabaseName(selectedSpecies);
                    File tempFile = getGeneMappingFile(ensemblDbName);
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
                                            if (waitingHandler instanceof ProgressDialogX) {
                                                waitingHandler.setWaitingText("Downloading Gene Mappings. Please Wait... (" + counter++ + " rows downloaded)");
                                            }
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
     * Returns the path to the folder containing the gene mapping files.
     *
     * @return the gene mapping folder
     */
    public static File getGeneMappingFolder() {
        return new File(GENE_MAPPING_FOLDER);
    }

    /**
     * Sets the folder where gene mappings are saved.
     *
     * @param geneMappingFolder the folder where gene mappings are saved
     */
    public static void setGeneMappingFolder(String geneMappingFolder) {
        GENE_MAPPING_FOLDER = geneMappingFolder;
    }

    /**
     * Copies teh given gene mapping files to the gene mappings folder. If newer
     * versions of the mapping exists they will be overwritten according to
     * updateEqualVersion.
     *
     * @param aEnsemblVersionsFile the Ensembl versions file
     * @param aGoDomainsFile the GO domains file
     * @param aSpeciesFile the species file
     * @param aDefaultSpeciesGoMappingsFile the default species GO mappings file
     * @param aDefaultSpeciesGeneMappingFile the default species gene mappings
     * file
     * @param updateEqualVersion if true, the version is updated with equal
     * version numbers, false, only update if the new version is newer
     */
    public void createDefaultGeneMappingFiles(File aEnsemblVersionsFile, File aGoDomainsFile, File aSpeciesFile,
            File aDefaultSpeciesGoMappingsFile, File aDefaultSpeciesGeneMappingFile, boolean updateEqualVersion) {

        if (!getGeneMappingFolder().exists()) {
            boolean folderCreated = getGeneMappingFolder().mkdirs();

            if (!folderCreated) {
                throw new IllegalArgumentException("Could not create the gene mapping folder.");
            }
        }

        File speciesFile = getSpeciesFile();
        File ensemblVersionsFile = getEnsemblVersionsFile();
        File goDomainsFile = getGoDomainsFile();
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
                    throw new IllegalArgumentException("Could not create the Ensembl versions file.");
                }

                Util.copyFile(aEnsemblVersionsFile, ensemblVersionsFile);

            } else {

                // file exists, just update the human ensembl version
                // read the "new" human Ensembl versions number
                Integer humanEnsemblVersionNew = getEnsemblVersionFromFile(aEnsemblVersionsFile, "hsapiens_gene_ensembl");

                if (humanEnsemblVersionNew != null) {

                    Integer humanEnsemblVersionOld = getEnsemblVersionFromFile(ensemblVersionsFile, "hsapiens_gene_ensembl");
                    if (humanEnsemblVersionOld == null
                            || humanEnsemblVersionOld.equals(humanEnsemblVersionNew) && updateEqualVersion
                            || humanEnsemblVersionOld < humanEnsemblVersionNew) {
                        updateHumanEnsembl = true;
                        updateEnsemblVersion("Human (Homo sapiens)", "Ensembl " + humanEnsemblVersionNew);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not create or update the Ensembl versions file.");
        }

        try {
            if (!goDomainsFile.exists()) {
                boolean fileCreated = goDomainsFile.createNewFile();
                if (!fileCreated) {
                    throw new IllegalArgumentException("Could not create the GO domains file.");
                }
            }
            Util.copyFile(aGoDomainsFile, goDomainsFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not create the GO domains file.");
        }

        if (updateHumanEnsembl) {

            try {
                if (!defaultSpeciesGoMappingsFile.exists()) {
                    boolean fileCreated = defaultSpeciesGoMappingsFile.createNewFile();
                    if (!fileCreated) {
                        throw new IllegalArgumentException("Could not create the default species GO mapping file.");
                    }
                }
                Util.copyFile(aDefaultSpeciesGoMappingsFile, defaultSpeciesGoMappingsFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Could not create the default species GO mapping file.");
            }

            try {
                if (!defaultSpeciesGeneMappingFile.exists()) {
                    boolean fileCreated = defaultSpeciesGeneMappingFile.createNewFile();
                    if (!fileCreated) {
                        throw new IllegalArgumentException("Could not create the default species gene mapping file.");
                    }
                }
                Util.copyFile(aDefaultSpeciesGeneMappingFile, defaultSpeciesGeneMappingFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Could not create the default species gene mapping file.");
            }
        }
    }

    /**
     * Update the Ensembl version for the given species in the local map and in
     * the Ensembl versions file.
     *
     * @param selectedSpecies the database name of the species to update, e.g.,
     * hsapiens_gene_ensembl
     * @param ensemblVersion the new Ensembl version
     *
     * @throws IOException if an IOException occurs
     */
    public void updateEnsemblVersion(String selectedSpecies, String ensemblVersion) throws IOException {

        if (ensemblVersionsMap == null) {
            ensemblVersionsMap = new HashMap<String, String>();
        }

        String databaseName = ensemblSpecies.getDatabaseName(selectedSpecies);
        if (databaseName == null) {
            throw new IllegalArgumentException("Ensembl database not found for species " + selectedSpecies + ".");
        }
        ensemblVersionsMap.put(databaseName, ensemblVersion);

        FileWriter w = new FileWriter(getEnsemblVersionsFile());
        try {
            BufferedWriter bw = new BufferedWriter(w);
            try {

                for (String key : ensemblVersionsMap.keySet()) {
                    bw.write(key + separator + ensemblVersionsMap.get(key));
                    bw.newLine();
                }

            } finally {
                bw.close();
            }
        } finally {
            w.close();
        }
    }

    /**
     * Gets the Ensembl version of a given species from a file.
     *
     * @param ensemblVersionsFile the Ensembl versions file
     * @param species the species of interest
     *
     * @return the Ensembl version
     *
     * @throws IOException thrown whenever an error occurred while reading the
     * file
     */
    public Integer getEnsemblVersionFromFile(File ensemblVersionsFile, String species) throws IOException {
        Integer version = null;
        FileReader r = new FileReader(ensemblVersionsFile);
        try {
            BufferedReader br = new BufferedReader(r);
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] splittedLine = line.split(separator);
                    String speciesAtLine = splittedLine[0];
                    if (speciesAtLine.equals(species)) {
                        String[] ensemblVersionSplit = splittedLine[1].split(" ");
                        version = new Integer(ensemblVersionSplit[1]);
                    }
                }
            } finally {
                br.close();
            }
        } finally {
            r.close();
        }
        return version;
    }

    /**
     * Loads the given Ensembl species file.
     *
     * @param ensemblVersionsFile the Ensembl species file to load
     * @throws FileNotFoundException if an FileNotFoundException occurs
     * @throws IOException if an IOException occurs
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

    /**
     * Try to download the gene and GO mappings for the currently selected
     * species.
     *
     * @param waitingHandler the waiting handler
     * @param currentEnsemblSpeciesType the species type
     * @param selectedSpecies the species
     *
     * @return true if the download was successful
     *
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading the mapping files
     */
    public boolean downloadMappings(WaitingHandler waitingHandler, String currentEnsemblSpeciesType, String selectedSpecies) throws IOException {

        if (waitingHandler.isReport()) {
            waitingHandler.appendReport("Downloading GO and gene mappings.", true, true);
        }

        String selectedDb = ensemblSpecies.getDatabaseName(selectedSpecies);
        String ensemblType = "ensembl";

        int speciesTypeIndex = 5;

        if (currentEnsemblSpeciesType.equalsIgnoreCase("fungi")) {
            speciesTypeIndex = 1;
            ensemblType = "fungi";
        } else if (currentEnsemblSpeciesType.equalsIgnoreCase("plants")) {
            speciesTypeIndex = 2;
            ensemblType = "plants";
        } else if (currentEnsemblSpeciesType.equalsIgnoreCase("protists")) {
            speciesTypeIndex = 3;
            ensemblType = "protists";
        } else if (currentEnsemblSpeciesType.equalsIgnoreCase("metazoa")) {
            speciesTypeIndex = 4;
            ensemblType = "metazoa";
        }

        if (!waitingHandler.isRunCanceled()) {

            boolean goMappingsDownloaded = downloadGoMappings(ensemblType, EnsemblVersion.getEnsemblDbName(speciesTypeIndex), selectedDb, true, waitingHandler);

            // swiss prot mapping not found, try trembl
            if (!goMappingsDownloaded) {
                goMappingsDownloaded = downloadGoMappings(ensemblType, EnsemblVersion.getEnsemblDbName(speciesTypeIndex), selectedDb, false, waitingHandler);
            }

            if (!goMappingsDownloaded) {
                waitingHandler.appendReport("Gene ontology mappings not available. Downloading gene mappings only.", true, true);
            } else {
                waitingHandler.setWaitingText("GO Mappings Downloaded.");
                if (waitingHandler.isReport()) {
                    waitingHandler.appendReport("GO mappings downloaded.", true, true);
                }
            }
        }
        if (!waitingHandler.isRunCanceled()) {
            downloadGeneMappings(ensemblType, EnsemblVersion.getEnsemblDbName(speciesTypeIndex), selectedDb,
                    EnsemblVersion.getCurrentEnsemblVersion(ensemblType).toString(), waitingHandler);

            if (!waitingHandler.isRunCanceled()) {
                waitingHandler.setWaitingText("Gene Mappings Downloaded.");
                if (waitingHandler.isReport()) {
                    waitingHandler.appendReport("Gene mappings downloaded.", true, true);
                }
            }
        }

        boolean canceled = waitingHandler.isRunCanceled();
        return !canceled;
    }

    /**
     * Returns the gene mapping file.
     *
     * @param ensemblDatabaseName the species name
     *
     * @return the gene mapping file
     */
    public static File getGeneMappingFile(String ensemblDatabaseName) {
        return new File(getGeneMappingFolder(), ensemblDatabaseName + GENE_MAPPING_FILE_SUFFIX);
    }

    /**
     * Returns the GO mapping file.
     *
     * @param ensemblDatabaseName the species name
     * @return the GO mapping file
     */
    public static File getGoMappingFile(String ensemblDatabaseName) {
        return new File(getGeneMappingFolder(), ensemblDatabaseName + GO_MAPPING_FILE_SUFFIX);
    }

    /**
     * Returns the Ensembl version file.
     *
     * @return the Ensembl version file
     */
    public static File getEnsemblVersionsFile() {
        return new File(getGeneMappingFolder(), ENSEMBL_VERSIONS);
    }

    /**
     * Returns the species file.
     *
     * @return the species file
     */
    public static File getSpeciesFile() {
        return new File(getGeneMappingFolder(), ENSEMBL_SPECIES_FILENAME);
    }

    public static File getGoDomainsFile() {
        return new File(getGeneMappingFolder(), GO_DOMAINS);
    }

    /**
     * Returns the Ensembl species.
     *
     * @return the Ensembl species
     */
    public EnsemblSpecies getEnsemblSpecies() {
        return ensemblSpecies;
    }

    /**
     * Returns the Ensembl version for a given species.
     *
     * @param speciesName the species of interest
     *
     * @return the Ensembl version for a given species.
     */
    public String getEnsemblVersion(String speciesName) {
        String selectedDb = ensemblSpecies.getDatabaseName(speciesName);
        if (ensemblVersionsMap == null) {
            return null;
        }
        return ensemblVersionsMap.get(selectedDb);

    }

    /**
     * Returns true if a newer version of the species mapping exists in Ensembl.
     *
     * @param speciesName the name of the species of interest
     *
     * @return rue if a newer version of the species mapping exists in Ensemble
     */
    public boolean newVersionExists(String speciesName) {

        String ensemblType = ensemblSpecies.getSpeciesType(speciesName);
        Integer latestEnsemblVersion = EnsemblVersion.getCurrentEnsemblVersion(ensemblType);
        String currentEnsemblVersionAsString = getEnsemblVersion(speciesName);

        if (currentEnsemblVersionAsString != null) {

            currentEnsemblVersionAsString = currentEnsemblVersionAsString.substring(currentEnsemblVersionAsString.indexOf(" ") + 1);
            Integer currentEnsemblVersion;

            try {
                currentEnsemblVersion = new Integer(currentEnsemblVersionAsString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                currentEnsemblVersion = latestEnsemblVersion;
            }

            return currentEnsemblVersion < latestEnsemblVersion;
        }

        return true;
    }

}
