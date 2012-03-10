package com.compomics.util.pride;

import com.compomics.util.pride.prideobjects.Protocol;
import com.compomics.util.pride.prideobjects.Sample;
import com.compomics.util.pride.prideobjects.Instrument;
import com.compomics.util.pride.prideobjects.Reference;
import com.compomics.util.pride.prideobjects.Contact;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This factory manages the pride objects saved in the user folder
 *
 * @author marc
 */
public class PrideObjectsFactory {

    /**
     * Instance of the factory
     */
    private static PrideObjectsFactory instance = null;

    /**
     * Method returning the instance of the factory
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
     * The folder where pride related infos are stored
     */
    public static final String prideFolder = System.getProperty("user.home") + "/.compomics/pride/";
    /**
     * The extension to use when saving objects. By default cus for compomics
     * utilities serialization
     */
    public static final String extension = ".cus";
    /**
     * List of all contacts
     */
    private HashMap<String, Contact> contacts = new HashMap<String, Contact>();
    /**
     * List of all instruments
     */
    private HashMap<String, Instrument> instruments = new HashMap<String, Instrument>();
    /**
     * List of all protocols
     */
    private HashMap<String, Protocol> protocols = new HashMap<String, Protocol>();
    /**
     * List of all references
     */
    private HashMap<String, Reference> references = new HashMap<String, Reference>();
    /**
     * List of all samples
     */
    private HashMap<String, Sample> samples = new HashMap<String, Sample>();
    /**
     * utilities to PSI ptm mapping for the default PTMs
     */
    private PtmToPrideMap ptmToPrideMap;

    /**
     * Constructor
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
     * folder is not existing
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while serializing the file
     */
    private void loadObjects() throws FileNotFoundException, IOException, ClassNotFoundException {
        File prideFolderFile = new File(prideFolder);
        if (!prideFolderFile.exists()) {
            createDefaultObjects();
        } else {
            File subFolder = new File(prideFolder, "contacts");
            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    Contact contact = (Contact) loadObject(file);
                    contacts.put(contact.getFileName(), contact);
                }
            }
            subFolder = new File(prideFolder, "protocols");
            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    Protocol protocol = (Protocol) loadObject(file);
                    protocols.put(protocol.getFileName(), protocol);
                }
            }
            subFolder = new File(prideFolder, "instruments");
            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    Instrument instrument = (Instrument) loadObject(file);
                    instruments.put(instrument.getFileName(), instrument);
                }
            }
            subFolder = new File(prideFolder, "references");
            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    Reference reference = (Reference) loadObject(file);
                    references.put(reference.getFileName(), reference);
                }
            }
            subFolder = new File(prideFolder, "samples");
            for (File file : subFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(extension)) {
                    Sample sample = (Sample) loadObject(file);
                    samples.put(sample.getFileName(), sample);
                }
            }
            File ptmMapFile = new File(prideFolder, PtmToPrideMap.fileName);
            ptmToPrideMap = (PtmToPrideMap) loadObject(ptmMapFile);
        }
    }

    /**
     * creates the default objects
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
        for (Reference reference : Reference.getDefaultReferences()) {
            addReference(reference);
        }
        subFolder = new File(prideFolder, "samples");
        subFolder.mkdir();
        for (Sample sample : Sample.getDefaultSamples()) {
            addSample(sample);
        }
        ptmToPrideMap = new PtmToPrideMap();
    }
    
    /**
     * Adds a contact in the pride objects
     * @param contact       the contact to add
     * @throws IOException  exception thrown whenever an error occurred while saving
     */
    public void addContact(Contact contact) throws IOException {
        contacts.put(contact.getFileName(), contact);
        File subFolder = new File(prideFolder, "contacts");
        saveObject(subFolder, contact);
    }
    
    /**
     * Adds a protocol in the pride objects
     * @param protocol       the protocol to add
     * @throws IOException  exception thrown whenever an error occurred while saving
     */
    public void addProtocol(Protocol protocol) throws IOException {
        protocols.put(protocol.getFileName(), protocol);
        File subFolder = new File(prideFolder, "protocols");
        saveObject(subFolder, protocol);
    }
    
    /**
     * Adds a instrument in the pride objects
     * @param instrument       the instrument to add
     * @throws IOException  exception thrown whenever an error occurred while saving
     */
    public void addInstrument(Instrument instrument) throws IOException {
        instruments.put(instrument.getFileName(), instrument);
        File subFolder = new File(prideFolder, "instruments");
        saveObject(subFolder, instrument);
    }
    
    /**
     * Adds a reference in the pride objects
     * @param reference       the reference to add
     * @throws IOException  exception thrown whenever an error occurred while saving
     */
    public void addReference(Reference reference) throws IOException {
        references.put(reference.getFileName(), reference);
        File subFolder = new File(prideFolder, "references");
        saveObject(subFolder, reference);
    }
    
    /**
     * Adds a contact in the pride objects
     * @param contact       the contact to add
     * @throws IOException  exception thrown whenever an error occurred while saving
     */
    public void addSample(Sample sample) throws IOException {
        samples.put(sample.getFileName(), sample);
        File subFolder = new File(prideFolder, "samples");
        saveObject(subFolder, sample);
    }
    
    /**
     * Sets a new ptm to pride map
     * @param ptmToPrideMap a new ptm to pride map
     */
    public void setPtmToPrideMap(PtmToPrideMap ptmToPrideMap) throws FileNotFoundException, IOException {
        this.ptmToPrideMap = ptmToPrideMap;
        File aFile = new File(prideFolder, PtmToPrideMap.fileName);
        FileOutputStream fos = new FileOutputStream(aFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ptmToPrideMap);
        oos.close();
        bos.close();
        fos.close();
    } 

    /**
     * Loads an object from the given file
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
        FileInputStream fis = new FileInputStream(aFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream in = new ObjectInputStream(bis);
        Object object = in.readObject();
        fis.close();
        bis.close();
        in.close();
        return object;
    }

    /**
     * Saves the object in a file
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
     * Returns the contacts
     * @return the contacts
     */
    public HashMap<String, Contact> getContacts() {
        return contacts;
    }

    /**
     * Returns the instruments
     * @return the instruments
     */
    public HashMap<String, Instrument> getInstruments() {
        return instruments;
    }

    /**
     * Returns the protocols
     * @return the protocols
     */
    public HashMap<String, Protocol> getProtocols() {
        return protocols;
    }

    /**
     * Returns the utilities ptm to pride map
     * @return the utilities ptm to pride map
     */
    public PtmToPrideMap getPtmToPrideMap() {
        return ptmToPrideMap;
    }

    /**
     * Returns the references
     * @return the references
     */
    public HashMap<String, Reference> getReferences() {
        return references;
    }

    /**
     * Returns the samples
     * @return the samples
     */
    public HashMap<String, Sample> getSamples() {
        return samples;
    }
}
