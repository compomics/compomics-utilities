package com.compomics.util.experiment.biology;

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
     * The folder containing the enzyme factory.
     */
    private static String SERIALIZATION_FILE_FOLDER = System.getProperty("user.home") + "/.compomics";
    /**
     * The name of the enzyme factory back-up file. The version number follows
     * the one of utilities.
     */
    private static String SERIALIZATION_FILE_NAME = "enzymeFactory-4.8.0.json";

    /**
     * The factory constructor.
     */
    private EnzymeFactory() {
        for (Enzyme enzyme : getDefaultEnzymes()) {
            instance.addEnzyme(enzyme);
        }
    }

    /**
     * Static method to get an instance of the factory.
     *
     * @return the factory instance
     */
    public static EnzymeFactory getInstance() {
        if (instance == null) {
            try {
                File savedFile = new File(SERIALIZATION_FILE_FOLDER, SERIALIZATION_FILE_NAME);
                if (savedFile.exists()) {
                    instance = loadFromFile(savedFile);
                } else {
                    instance = new EnzymeFactory();
                }
            } catch (Exception e) {
                instance = new EnzymeFactory();
            }
        }
        return instance;
    }

    /**
     * Loads an enzyme factory from a file. The file must be an export of the factory in the json format.
     * 
     * @param file the file to load
     * 
     * @return the enzyme factory saved in file
     * 
     * @throws IOException exception thrown whenever an error occurred while loading the file
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
     * @throws IOException exception thrown whenever an error occurred while saving the file
     */
    public static void saveToFile(EnzymeFactory enzymeFactory, File file) throws IOException {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        jsonMarshaller.saveObjectToJson(enzymeFactory, file);
    }

    /**
     * Returns the folder where the factory is saved.
     *
     * @return the folder where the factory is saved
     */
    public static String getSerializationFolder() {
        return SERIALIZATION_FILE_FOLDER;
    }

    /**
     * Sets the folder where the factory is saved.
     *
     * @param serializationFolder the folder where the factory is saved
     */
    public static void setSerializationFolder(String serializationFolder) {
        EnzymeFactory.SERIALIZATION_FILE_FOLDER = serializationFolder;
    }

    /**
     * Get the imported enzymes.
     *
     * @return The enzymes as ArrayList
     */
    public ArrayList<Enzyme> getEnzymes() {
        return new ArrayList<Enzyme>(enzymes.values());
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
     * @param cvTermAccession the accession of the cv term.
     *
     * @return the associated enzyme.
     */
    public Enzyme getUtilitiesEnzyme(String cvTermAccession) {
        for (Enzyme enzyme : enzymes.values()) {
            if (enzyme.getCvTerm().getAccession().equals(cvTermAccession)) {
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
        ArrayList<Enzyme> enzymes = new ArrayList<Enzyme>();

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

        enzyme = new Enzyme("Asp-N Ammonium Bicarbonate");
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
