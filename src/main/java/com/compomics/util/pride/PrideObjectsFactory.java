package com.compomics.util.pride;

import com.compomics.util.io.file.SerializationUtils;
import com.compomics.util.pride.prideobjects.*;
import java.io.*;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * This factory manages the pride objects saved in the user folder.
 *
 * @author Marc Vaudel
 */
public class PrideObjectsFactory {

    /**
     * Instance of the factory.
     */
    private static PrideObjectsFactory instance = null;

    /**
     * Method returning the instance of the factory.
     *
     * @return the instance of the factory
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while serializing the file
     */
    public static PrideObjectsFactory getInstance() throws FileNotFoundException, IOException, ClassNotFoundException {
        if (instance == null) {
            instance = new PrideObjectsFactory();
        }
        return instance;
    }
    /**
     * The folder where PRIDE related info is stored.
     */
    private static String prideFolder = System.getProperty("user.home") + "/.compomics/pride/";
    /**
     * The extension to use when saving objects. By default cus for compomics
     * utilities serialization
     */
    public static final String extension = ".cus";
    /**
     * List of all contacts.
     */
    private static HashMap<String, ContactGroup> contactGroups = new HashMap<String, ContactGroup>();
    /**
     * List of all instruments.
     */
    private static HashMap<String, Instrument> instruments = new HashMap<String, Instrument>();
    /**
     * List of all protocols.
     */
    private static HashMap<String, Protocol> protocols = new HashMap<String, Protocol>();
    /**
     * List of all references.
     */
    private static HashMap<String, ReferenceGroup> references = new HashMap<String, ReferenceGroup>();
    /**
     * List of all samples.
     */
    private static HashMap<String, Sample> samples = new HashMap<String, Sample>();
    /**
     * Utilities to PSI PTM mapping for the default PTMs.
     */
    private static PtmToPrideMap ptmToPrideMap;

    /**
     * Constructor.
     *
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while serializing the file
     */
    private PrideObjectsFactory() throws FileNotFoundException, IOException, ClassNotFoundException {
        loadObjects();
    }

    /**
     * Loads the objects from the pride folder or creates default objects if the
     * folder is not existing.
     *
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while serializing the file
     */
    private void loadObjects() throws FileNotFoundException, IOException, ClassNotFoundException {
        File prideFolderFile = new File(prideFolder);
        if (!prideFolderFile.exists() || prideFolderFile.listFiles().length == 0) {
            createDefaultObjects();
        } else {
            File subFolder = new File(prideFolder, "contactGroups");
            if (!subFolder.exists()) {
                subFolder.mkdir();
            }

            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    try {
                        ContactGroup contactGroup = (ContactGroup) loadObject(file);
                        contactGroups.put(contactGroup.getFileName(), contactGroup);
                    } catch (InvalidClassException e) {
                        file.delete();
                    }
                }
            }

            subFolder = new File(prideFolder, "protocols");
            if (!subFolder.exists()) {
                subFolder.mkdir();
            }

            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    try {
                        Protocol protocol = (Protocol) loadObject(file);
                        protocols.put(protocol.getFileName(), protocol);
                    } catch (InvalidClassException e) {
                        file.delete();
                    }
                }
            }

            subFolder = new File(prideFolder, "instruments");
            if (!subFolder.exists()) {
                subFolder.mkdir();
            }

            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    try {
                        Instrument instrument = (Instrument) loadObject(file);
                        instruments.put(instrument.getFileName(), instrument);
                    } catch (InvalidClassException e) {
                        file.delete();
                    }
                }
            }

            subFolder = new File(prideFolder, "referenceGroups");
            if (!subFolder.exists()) {
                subFolder.mkdir();
            }

            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    try {
                        ReferenceGroup referenceGroup = (ReferenceGroup) loadObject(file);
                        references.put(referenceGroup.getFileName(), referenceGroup);
                    } catch (InvalidClassException e) {
                        file.delete();
                    }
                }
            }

            subFolder = new File(prideFolder, "samples");
            if (!subFolder.exists()) {
                subFolder.mkdir();
            }

            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    try {
                        Sample sample = (Sample) loadObject(file);
                        samples.put(sample.getFileName(), sample);
                    } catch (InvalidClassException e) {
                        file.delete();
                    }
                }
            }

            File ptmMapFile = new File(prideFolder, PtmToPrideMap.fileName);

            try {
                // check if the file exists, if not create it
                if (!ptmMapFile.exists()) {
                    ptmToPrideMap = new PtmToPrideMap();
                    setPtmToPrideMap(ptmToPrideMap);
                }

                ptmToPrideMap = (PtmToPrideMap) loadObject(ptmMapFile);

                // corrupt file, reset the mappings
                if (ptmToPrideMap == null) {
                    System.out.println("Error: Corrupt PRIDE PTM mapping file. Resetting the mappings.");
                    ptmMapFile.delete();
                    ptmToPrideMap = new PtmToPrideMap();
                    setPtmToPrideMap(ptmToPrideMap);
                }

            } catch (InvalidClassException e) {
                e.printStackTrace();
                ptmMapFile.delete();
                ptmToPrideMap = new PtmToPrideMap();
                setPtmToPrideMap(ptmToPrideMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ptmToPrideMap = new PtmToPrideMap();
                setPtmToPrideMap(ptmToPrideMap);
            } catch (IOException e) {
                e.printStackTrace();
                ptmMapFile.delete();
                ptmToPrideMap = new PtmToPrideMap();
                setPtmToPrideMap(ptmToPrideMap);
            }
        }
    }

    /**
     * Creates the default objects.
     *
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while serializing the file
     */
    private void createDefaultObjects() throws FileNotFoundException, IOException, ClassNotFoundException {
        File prideFolderFile = new File(prideFolder);
        prideFolderFile.mkdirs();
        File subFolder = new File(prideFolder, "contacts");
        subFolder.mkdir();
        subFolder = new File(prideFolder, "contactGroups");
        subFolder.mkdir();
        subFolder = new File(prideFolder, "protocols");
        subFolder.mkdir();
        for (Protocol protocol : Protocol.getDefaultProtocols()) {
            addProtocol(protocol);
        }
        subFolder = new File(prideFolder, "instruments");
        subFolder.mkdir();
        for (Instrument defaultInstrument : Instrument.getDefaultInstruments()) {
            addInstrument(defaultInstrument);
        }
        subFolder = new File(prideFolder, "references");
        subFolder.mkdir();
        subFolder = new File(prideFolder, "referenceGroups");
        subFolder.mkdir();
        for (ReferenceGroup reference : ReferenceGroup.getDefaultReferences()) {
            addReferenceGroup(reference);
        }
        subFolder = new File(prideFolder, "samples");
        subFolder.mkdir();
        for (Sample sample : Sample.getDefaultSamples()) {
            addSample(sample);
        }
        ptmToPrideMap = new PtmToPrideMap();
    }

    /**
     * Adds a contact group in the PRIDE objects.
     *
     * @param contactGroup the contact group to add
     * @throws IOException exception thrown whenever an error occurred while
     * saving
     */
    public void addContactGroup(ContactGroup contactGroup) throws IOException {
        contactGroups.put(contactGroup.getFileName(), contactGroup);
        File subFolder = new File(prideFolder, "contactGroups");
        saveObject(subFolder, contactGroup);
    }

    /**
     * Delete the given contact group.
     *
     * @param contactGroup the group to delete
     */
    public void deleteContactGroup(ContactGroup contactGroup) {
        File subFolder = new File(prideFolder, "contactGroups");
        deleteObject(subFolder, contactGroup.getFileName());
        contactGroups.remove(contactGroup.getFileName());
    }

    /**
     * Adds a protocol in the PRIDE objects.
     *
     * @param protocol the protocol to add
     * @throws IOException exception thrown whenever an error occurred while
     * saving
     */
    public void addProtocol(Protocol protocol) throws IOException {
        protocols.put(protocol.getFileName(), protocol);
        File subFolder = new File(prideFolder, "protocols");
        saveObject(subFolder, protocol);
    }

    /**
     * Delete the given protocol.
     *
     * @param protocol the protocol to delete
     */
    public void deleteProtocol(Protocol protocol) {
        File subFolder = new File(prideFolder, "protocols");
        deleteObject(subFolder, protocol.getFileName());
        protocols.remove(protocol.getFileName());
    }

    /**
     * Adds a instrument in the PRIDE objects.
     *
     * @param instrument the instrument to add
     * @throws IOException exception thrown whenever an error occurred while
     * saving
     */
    public void addInstrument(Instrument instrument) throws IOException {
        instruments.put(instrument.getFileName(), instrument);
        File subFolder = new File(prideFolder, "instruments");
        saveObject(subFolder, instrument);
    }

    /**
     * Delete the given instrument.
     *
     * @param instrument the instrument to delete
     */
    public void deleteInstrument(Instrument instrument) {
        File subFolder = new File(prideFolder, "instruments");
        deleteObject(subFolder, instrument.getFileName());
        instruments.remove(instrument.getFileName());
    }

    /**
     * Adds a reference group in the PRIDE objects.
     *
     * @param referenceGroup the reference group to add
     * @throws IOException exception thrown whenever an error occurred while
     * saving
     */
    public void addReferenceGroup(ReferenceGroup referenceGroup) throws IOException {
        references.put(referenceGroup.getFileName(), referenceGroup);
        File subFolder = new File(prideFolder, "referenceGroups");
        saveObject(subFolder, referenceGroup);
    }

    /**
     * Delete the given reference group.
     *
     * @param referenceGroup the reference group to delete
     */
    public void deleteReferenceGroup(ReferenceGroup referenceGroup) {
        File subFolder = new File(prideFolder, "referenceGroups");
        deleteObject(subFolder, referenceGroup.getFileName());
        references.remove(referenceGroup.getFileName());
    }

    /**
     * Adds a sample in the PRIDE objects.
     *
     * @param sample the sample to add
     * @throws IOException exception thrown whenever an error occurred while
     * saving
     */
    public void addSample(Sample sample) throws IOException {
        samples.put(sample.getFileName(), sample);
        File subFolder = new File(prideFolder, "samples");
        saveObject(subFolder, sample);
    }

    /**
     * Delete the given sample.
     *
     * @param sample the sample to delete
     */
    public void deleteSample(Sample sample) {
        File subFolder = new File(prideFolder, "samples");
        deleteObject(subFolder, sample.getFileName());
        samples.remove(sample.getFileName());
    }

    /**
     * Sets a new PTM to PRIDE map.
     *
     * @param ptmToPrideMap a new PTM to pride map
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public void setPtmToPrideMap(PtmToPrideMap ptmToPrideMap) throws FileNotFoundException, IOException {
        if (ptmToPrideMap == null) {
            throw new IllegalArgumentException("Attempting to overwrite the PTM to PRIDE mapping with a null object.");
        }
        PrideObjectsFactory.ptmToPrideMap = ptmToPrideMap;
        File aFile = new File(prideFolder, PtmToPrideMap.fileName);
        SerializationUtils.writeObject(ptmToPrideMap, aFile);
    }

    /**
     * Loads an object from the given file.
     *
     * @param aFile the file of interest
     * @return the serialized object
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the file
     */
    private Object loadObject(File aFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        Object object = null;
        FileInputStream fis = new FileInputStream(aFile);
        try {
            BufferedInputStream bis = new BufferedInputStream(fis);
            try {
                ObjectInputStream in = new ObjectInputStream(bis);
                try {
                    object = in.readObject();
                } finally {
                    in.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            fis.close();
        }
        return object;
    }

    /**
     * Saves the object in a file.
     *
     * @param destinationFolder the folder where to save the object
     * @param object the object to save in a file
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while serializing the file
     */
    private void saveObject(File destinationFolder, PrideObject object) throws IOException {
        File aFile = new File(destinationFolder, object.getFileName() + extension);
        FileOutputStream fos = new FileOutputStream(aFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
        bos.close();
        fos.close();
    }

    /**
     * Deletes the given file.
     *
     * @param folder the folder where the file is located
     * @param fileName the name of the file to delete
     */
    private void deleteObject(File folder, String aFileName) {

        String fileName = aFileName + extension;

        if (new File(folder, fileName).exists()) {
            boolean deleted = new File(folder, fileName).delete();
            if (!deleted) {
                JOptionPane.showMessageDialog(null, "Failed to delete the file \'" + new File(folder, fileName).getAbsolutePath() + "\'.\n"
                        + "Please delete the file manually.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Returns the contact groups.
     *
     * @return the contact groups
     */
    public HashMap<String, ContactGroup> getContactGroups() {
        return contactGroups;
    }

    /**
     * Returns the instruments.
     *
     * @return the instruments
     */
    public HashMap<String, Instrument> getInstruments() {
        return instruments;
    }

    /**
     * Returns the protocols.
     *
     * @return the protocols
     */
    public HashMap<String, Protocol> getProtocols() {
        return protocols;
    }

    /**
     * Returns the utilities PTM to pride map.
     *
     * @return the utilities PTM to pride map
     */
    public PtmToPrideMap getPtmToPrideMap() {
        return ptmToPrideMap;
    }

    /**
     * Returns the reference groups.
     *
     * @return the reference groups
     */
    public HashMap<String, ReferenceGroup> getReferenceGroups() {
        return references;
    }

    /**
     * Returns the samples.
     *
     * @return the samples
     */
    public HashMap<String, Sample> getSamples() {
        return samples;
    }

    /**
     * Returns the folder where pride annotation information should be saved.
     *
     * @return the folder where pride annotation information should be saved
     */
    public static String getPrideFolder() {
        return prideFolder;
    }

    /**
     * Sets the folder where pride annotation information should be saved.
     *
     * @param prideFolder the folder where pride annotation information should
     * be saved
     */
    public static void setPrideFolder(String prideFolder) {
        PrideObjectsFactory.prideFolder = prideFolder;
    }
}
