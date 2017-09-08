package com.compomics.util.experiment.biology.enzymes;

import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.pride.CvTerm;

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * This factory will provide the implemented enzymes.
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
     * The file where the factory is saved.
     */
    private static String SERIALIZATION_FILE = null;

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
        return getInstance(null);
    }

    /**
     * Static method to get an instance of the factory. Attempts to load the
     * factory from the given file. If the file is null, attempts to load from
     * the file set in the path preferences. If any exception occurs it is
     * ignored silently and defaults are used.
     *
     * @param enzymeFile the file to load the factory from
     *
     * @return the factory instance
     */
    public static EnzymeFactory getInstance(File enzymeFile) {
        if (instance == null) {
            try {
                if (enzymeFile == null && getSerializationFile() != null) {
                    enzymeFile = new File(getSerializationFile());
                }
                if (enzymeFile != null && enzymeFile.exists()) {
                    instance = loadFromFile(enzymeFile);
                } else {
                    instance = getDefault();
                }
            } catch (Exception e) {
                instance = getDefault();
            }
        }
        return instance;
    }

    /**
     * Returns an instance containing only the default enzymes.
     *
     * @return an instance containing only the default enzymes
     */
    public static EnzymeFactory getDefault() {
        EnzymeFactory enzymeFactory = new EnzymeFactory();
        for (Enzyme enzyme : getDefaultEnzymes()) {
            enzymeFactory.addEnzyme(enzyme);
        }
        return enzymeFactory;
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
     * Returns the file where to save the factory.
     *
     * @return the file where to save the factory
     */
    public static String getSerializationFile() {
        return SERIALIZATION_FILE;
    }

    /**
     * Sets the file where to save the factory. Warning: this overwrites
     * SERIALIZATION_FILE_FOLDER.
     *
     * @param serializationFilePath the file where to save the factory
     */
    public static void setSerializationFile(String serializationFilePath) {
        SERIALIZATION_FILE = serializationFilePath;
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
    }

    /**
     * Removes an enzyme from the mapping.
     *
     * @param enzymeName the name of the enzyme to remove.
     */
    public void removeEnzyme(String enzymeName) {
        enzymes.remove(enzymeName);
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
            if (enzyme.getCvTerm() != null && enzyme.getCvTerm().getAccession().equals(cvTermAccession)) {
                return enzyme;
            }
        }
        return null;
    }

    /**
     * Creates the MS Amanda enzyme settings file corresponding to the enzymes
     * loaded in the factory to the given file.
     *
     * @param file the file
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public void writeMsAmandaEnzymeFile(File file) throws IOException {

        // @TODO: not yet in use... (and not properly tested)
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        String toWrite = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
        bw.write(toWrite);
        bw.newLine();

        bw.write("<enzymes>");
        bw.newLine();

        for (Enzyme enzyme : getEnzymes()) {

            bw.write("  <enzyme>");
            bw.newLine();

            bw.write("    <name>" + enzyme.getName() + "</name>");
            bw.newLine();

            String cleavageSite = "";
            String inhibitors = "";
            String position;

            if (!enzyme.getAminoAcidBefore().isEmpty()) {
                position = "after";
                for (Character aminoAcid : enzyme.getAminoAcidBefore()) {
                    cleavageSite += aminoAcid;
                }
                for (Character aminoAcid : enzyme.getRestrictionAfter()) {
                    inhibitors += aminoAcid;
                }
            } else {
                position = "before";
                for (Character aminoAcid : enzyme.getAminoAcidAfter()) {
                    cleavageSite += aminoAcid;
                }
                for (Character aminoAcid : enzyme.getRestrictionBefore()) {
                    inhibitors += aminoAcid;
                }
            }

            bw.write("    <cleavage_sites>" + cleavageSite + "</cleavage_sites>");
            bw.newLine();

            if (!inhibitors.isEmpty()) {
                bw.write("    <inhibitors>" + inhibitors + "</inhibitors>");
                bw.newLine();
            }

            bw.write("    <position>" + position + "</position>");
            bw.newLine();

            bw.write("  </enzyme>");
            bw.newLine();
        }

        bw.write("</enzymes>");

        bw.flush();
        bw.close();
    }

    /**
     * Returns a list of default enzymes.
     *
     * @return a list of default enzymes
     */
    private static ArrayList<Enzyme> getDefaultEnzymes() {
        
        // note that enzyme names cannot contain comma as this is used by
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
