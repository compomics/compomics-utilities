package com.compomics.util.experiment.biology.enzymes;

import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.pride.CvTerm;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

/**
 * This factory will load enzymes from an XML file and provide them on demand as
 * a standard class.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class EnzymeFactory {

    /**
     * The imported enzymes.
     */
    private HashMap<String, Enzyme> enzymes = null;
    /**
     * The instance of the factory.
     */
    private static EnzymeFactory instance = null;
    /**
     * The alphabetically sorted enzyme names.
     */
    private static ArrayList<String> sortedEnzymeNames = null;
    /**
     * The folder containing the enzymes factory.
     */
    private static String SERIALIZATION_FILE_FOLDER = System.getProperty("user.home") + "/.compomics";
    /**
     * The name of the enzymes factory back-up file. The version number follows
     * the one of utilities.
     */
    private static final String SERIALIZATION_FILE_NAME = "enzymeFactory-5.0.4-beta.json";

    /**
     * The factory constructor.
     */
    private EnzymeFactory() {
        enzymes = new HashMap<>();
    }

    /**
     * Static method to get an instance of the factory. Attempts to load the
     * factory from the file set in the path preferences. If any exception
     * occurs it is ignored silently and defaults are used.
     *
     * @return the factory instance
     */
    public static EnzymeFactory getInstance() {
        if (instance == null) {
            try {
                File savedFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
                instance = loadFromFile(savedFile);
                sortedEnzymeNames = null;
            } catch (Exception e) {
                setDefaultEnzymes();
                sortedEnzymeNames = null;
            }
        }
        return instance;
    }

    /**
     * Saves the factory in the user folder.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the modificationFactory
     */
    public void saveFactory() throws IOException {
        File factoryFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
        if (!factoryFile.getParentFile().exists()) {
            factoryFile.getParentFile().mkdir();
        }
        saveToFile(instance, factoryFile);
    }

    /**
     * Sets the instance to only contain the default enzymes.
     */
    public static void setDefaultEnzymes() {
        instance = new EnzymeFactory();
        for (Enzyme enzyme : getDefaultEnzymes()) {
            instance.addEnzyme(enzyme);
        }
        sortedEnzymeNames = null;
    }

    /**
     * Loads an enzyme factory from a file. The file must be an export of the
     * factory in the json format.
     *
     * @param file the file to load
     *
     * @return the enzyme factory saved in file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the file
     */
    public static EnzymeFactory loadFromFile(File file) throws IOException {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        EnzymeFactory result = (EnzymeFactory) jsonMarshaller.fromJson(EnzymeFactory.class, file);
        sortedEnzymeNames = null;
        return result;
    }

    /**
     * Saves en enzyme factory to a file.
     *
     * @param enzymeFactory the enzyme factory to save
     * @param file the file where to save
     *
     * @throws IOException exception thrown whenever an error occurred while
     * saving the file
     */
    public static void saveToFile(EnzymeFactory enzymeFactory, File file) throws IOException {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        jsonMarshaller.saveObjectToJson(enzymeFactory, file);
    }

    /**
     * Returns the folder where to save the factory.
     *
     * @return the folder where to save the factory
     */
    public static String getSerializationFolder() {
        return SERIALIZATION_FILE_FOLDER;
    }

    /**
     * Sets the folder where to save the factory. Warning: this overwrites
     * SERIALIZATION_FILE_FOLDER.
     *
     * @param serializationFilePath the folder where to save the factory
     */
    public static void setSerializationFolder(String serializationFilePath) {
        SERIALIZATION_FILE_FOLDER = serializationFilePath;
    }

    /**
     * Get the imported enzymes.
     *
     * @return The enzymes as ArrayList
     */
    public ArrayList<Enzyme> getEnzymes() {
        return new ArrayList<>(enzymes.values());
    }

    /**
     * Get the sorted list of enzyme names.
     *
     * @return the enzyme names as a sorted ArrayList
     */
    public ArrayList<String> getSortedEnzymeNames() {

        if (sortedEnzymeNames != null) {
            return sortedEnzymeNames;
        }

        sortedEnzymeNames = new ArrayList<>();

        for (Enzyme tempEnzyme : enzymes.values()) {
            sortedEnzymeNames.add(tempEnzyme.getName());
        }

        Collections.sort(sortedEnzymeNames);

        return sortedEnzymeNames;
    }

    /**
     * Returns the enzyme corresponding to the given name. Null if not found.
     *
     * @param enzymeName the name of the desired enzyme
     * @return the corresponding enzyme
     */
    public Enzyme getEnzyme(String enzymeName) {
        return enzymes.get(enzymeName);
    }

    /**
     * Adds an enzyme in the factory.
     *
     * @param enzyme the new enzyme to add
     */
    public void addEnzyme(Enzyme enzyme) {
        enzymes.put(enzyme.getName(), enzyme);
        sortedEnzymeNames = null;
    }

    /**
     * Removes an enzyme from the mapping.
     *
     * @param enzymeName the name of the enzyme to remove.
     */
    public void removeEnzyme(String enzymeName) {
        enzymes.remove(enzymeName);
        sortedEnzymeNames = null;
    }

    /**
     * Indicates whether an enzyme is loaded in the factory.
     *
     * @param enzyme the name of the enzyme
     * @return a boolean indicating whether an enzyme is loaded in the factory
     */
    public boolean enzymeLoaded(String enzyme) {
        return enzymes.containsKey(enzyme);
    }

    /**
     * Returns the enzyme associated to the given cvTerm. Null if not found.
     *
     * @param cvTermAccession the accession of the cv term
     *
     * @return the associated enzyme.
     */
    public Enzyme getUtilitiesEnzyme(String cvTermAccession) {
        for (Enzyme enzyme : enzymes.values()) {
            if (enzyme.getCvTerm() != null
                    && enzyme.getCvTerm().getAccession().equals(cvTermAccession)) {
                return enzyme;
            }
        }
        return null;
    }

    /**
     * Returns a list of default enzymes.
     *
     * @return a list of default enzymes
     */
    private static ArrayList<Enzyme> getDefaultEnzymes() {

        // NOTE: enzyme names cannot contain comma as this is used by
        // some of the search engines to separate multiple enzymes!
        ArrayList<Enzyme> enzymes = new ArrayList<>();

        Enzyme enzyme = new Enzyme("Trypsin");
        enzyme.addAminoAcidBefore('R');
        enzyme.addAminoAcidBefore('K');
        enzyme.addRestrictionAfter('P');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001251", "Trypsin", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Trypsin (no P rule)");
        enzyme.addAminoAcidBefore('R');
        enzyme.addAminoAcidBefore('K');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001313", "Trypsin/P", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Arg-C");
        enzyme.addAminoAcidBefore('R');
        enzyme.addRestrictionAfter('P');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001303", "Arg-C", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Arg-C (no P rule)");
        enzyme.addAminoAcidBefore('R');
        enzymes.add(enzyme);

        enzyme = new Enzyme("Arg-N");
        enzyme.addAminoAcidAfter('R');
        enzymes.add(enzyme);

        enzyme = new Enzyme("Glu-C");
        enzyme.addAminoAcidBefore('E');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001917", "glutamyl endopeptidase", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Lys-C");
        enzyme.addAminoAcidBefore('K');
        enzyme.addRestrictionAfter('P');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001309", "Lys-C", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Lys-C (no P rule)");
        enzyme.addAminoAcidBefore('K');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001310", "Lys-C/P", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Lys-N");
        enzyme.addAminoAcidAfter('K');
        enzymes.add(enzyme);

        enzyme = new Enzyme("Asp-N");
        enzyme.addAminoAcidAfter('D');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001304", "Asp-N", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Asp-N (ambic)");
        enzyme.addAminoAcidAfter('D');
        enzyme.addAminoAcidAfter('E');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001305", "Asp-N_ambic", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Chymotrypsin");
        enzyme.addAminoAcidBefore('F');
        enzyme.addAminoAcidBefore('Y');
        enzyme.addAminoAcidBefore('W');
        enzyme.addAminoAcidBefore('L');
        enzyme.addRestrictionAfter('P');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001306", "Chymotrypsin", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Chymotrypsin (no P rule)");
        enzyme.addAminoAcidBefore('F');
        enzyme.addAminoAcidBefore('Y');
        enzyme.addAminoAcidBefore('W');
        enzyme.addAminoAcidBefore('L');
        enzymes.add(enzyme);

        enzyme = new Enzyme("Pepsin A");
        enzyme.addAminoAcidBefore('F');
        enzyme.addAminoAcidBefore('L');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001311", "Pepsin A", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("CNBr");
        enzyme.addAminoAcidBefore('M');
        enzyme.setCvTerm(new CvTerm("PSI-MS", "MS:1001307", "CNBr", null));
        enzymes.add(enzyme);

        enzyme = new Enzyme("Thermolysin");
        enzyme.addAminoAcidAfter('A');
        enzyme.addAminoAcidAfter('F');
        enzyme.addAminoAcidAfter('I');
        enzyme.addAminoAcidAfter('L');
        enzyme.addAminoAcidAfter('M');
        enzyme.addAminoAcidAfter('V');
        enzymes.add(enzyme);

        enzyme = new Enzyme("LysargiNase");
        enzyme.addAminoAcidAfter('R');
        enzyme.addAminoAcidAfter('K');
        enzymes.add(enzyme);

        return enzymes;
    }
}
