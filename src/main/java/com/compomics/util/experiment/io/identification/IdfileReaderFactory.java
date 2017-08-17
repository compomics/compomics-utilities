package com.compomics.util.experiment.io.identification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This factory will provide the appropriate identification file reader for each
 * type of file. Null when the format is not supported.
 *
 * @author Marc Vaudel
 */
public class IdfileReaderFactory {

    /**
     * Class specific log4j logger for Enzyme instances.
     */
    static Logger logger = Logger.getLogger(IdfileReaderFactory.class);
    /**
     * The factory instance.
     */
    private static IdfileReaderFactory singleton = null;
    /**
     * The list of registered IdfileReaders.
     */
    private static HashMap<String, Class> idFileReaders = new HashMap<String, Class>();

    /**
     * Static initializer block that checks for registered IdfileReaders through
     * the Java service loader
     */
    static {
        ServiceLoader<IdfileReader> ifdrServiceLoader = ServiceLoader.load(IdfileReader.class);
        Iterator<IdfileReader> idfrIterator = ifdrServiceLoader.iterator();

        while (idfrIterator.hasNext()) {
            IdfileReader idfileReader = idfrIterator.next();
            logger.info("Found IdfileReader '" + idfileReader.getClass().getCanonicalName() + "' in Java service loader.");
            IdfileReaderFactory.registerIdFileReader(idfileReader.getClass(), idfileReader.getExtension());
        }
    }

    /**
     * The factory constructor.
     */
    private IdfileReaderFactory() {
    }

    /**
     * A static method to retrieve the instance of the factory.
     *
     * @return the factory instance
     */
    public static IdfileReaderFactory getInstance() {
        if (singleton == null) {
            singleton = new IdfileReaderFactory();
        }
        return singleton;
    }

    /**
     * This method registers a new IdfileReader Class, and the file extension it
     * can read from. Note that the collection of IdfileReaders is keyed by this
     * extension, and similar to the java.util.HashMap syntax, a Class is
     * therefore returned if the extension provided already had an associated
     * Class.
     *
     * @param aReader Class of the IdfileReader to register.
     * @param aExtension String with the extension of the file that this
     * IdfileReader implementation can read.
     * @return Class with the Class that was already previously registered for
     * this extension, or 'null' if the extension was not yet registered at all.
     */
    public static Class registerIdFileReader(Class aReader, String aExtension) {
        Class result = null;
        // See if we have the right type of class!
        if (IdfileReader.class.isAssignableFrom(aReader)) {
            // Now verify the presence of a correct constructor!
            try {
                aReader.getConstructor(File.class);
                result = idFileReaders.put(aExtension, aReader);
                logger.info("Registered IdfileReader implementation '" + aReader.getCanonicalName() + "' for extension '" + aExtension + "'.");
                if (result != null) {
                    logger.warn("Overwrite occurred for extension '" + aExtension + "'; replaced old IdfileReader '" + result.getCanonicalName() + "' with new IdfileReader '" + aReader.getCanonicalName() + "'!");
                }
            } catch (NoSuchMethodException nsme) {
                logger.warn("Unable to find required constructor with single java.io.File parameter in IdfileReader implementation '" + aReader.getCanonicalName() + "'! IdfileReader is ignored!");
                nsme.printStackTrace();
            }
        } else {
            logger.warn("Was expecting an implementation of '" + IdfileReader.class.getCanonicalName() + "', but got class '" + aReader.getCanonicalName() + "' instead! Ignoring IdfileReader!");
        }
        return result;
    }

    /**
     * This method returns the proper identification file reader depending on
     * the format of the provided file. It is very important to close the file
     * reader after creation.
     *
     * @param aFile the file to parse
     * @return an adapted file reader
     * 
     * @throws SAXException if a SAXException occurs
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     * @throws OutOfMemoryError thrown if the parser runs out of memory
     */
    public IdfileReader getFileReader(File aFile) throws SAXException, FileNotFoundException, IOException, OutOfMemoryError {

        // @TODO: create parsers using waiting handlers and indexed files.
        // The return value, defaulting to null.
        IdfileReader result = null;

        // Get file name of the idfile to process.
        String name = aFile.getName().toLowerCase();

        // Iterator registered IdfileReaders, see who likes this file. First come, first served.
        // @TODO: May want to make this more sophisticated, possibly like the DBLoaders in DBToolkit, 
        //        that get the actual file to read some lines prior to making up their mind; thus constitutes 
        //        an actual format check rather than an extension check.
        Iterator<String> extensions = idFileReaders.keySet().iterator();

        while (extensions.hasNext()) {
            String key = extensions.next();
            String extension = key.toLowerCase();
            if (name.endsWith(extension)) {
                Class idfileReaderClass = idFileReaders.get(key);
                try {
                    result = (IdfileReader) idfileReaderClass.getConstructor(File.class).newInstance(aFile);
                    break;
                } catch (NoSuchMethodException nsme) {
                    logger.error("Unable to find required constructor with single java.io.File parameter in IdfileReader implementation '"
                            + idfileReaderClass.getCanonicalName() + "', matching query extension '" + extension + "'!", nsme);
                    nsme.printStackTrace();
                } catch (IllegalAccessException iae) {
                    logger.error("Required public constructor with single java.io.File parameter in IdfileReader implementation '"
                            + idfileReaderClass.getCanonicalName() + "', matching query extension '" + extension + "' has incorrect access modifier!", iae);
                    iae.printStackTrace();
                } catch (InvocationTargetException ite) {
                    if (ite.getCause() instanceof OutOfMemoryError) {
                        ite.printStackTrace();
                        throw (OutOfMemoryError) ite.getCause();
                    } else {
                        logger.error("Required constructor with single java.io.File parameter in IdfileReader implementation '"
                                + idfileReaderClass.getCanonicalName() + "', matching query extension '" + extension + "' threw an exception!", ite);
                        ite.printStackTrace();
                    }
                } catch (InstantiationException ie) {
                    logger.error("Required constructor with single java.io.File parameter in IdfileReader implementation '"
                            + idfileReaderClass.getCanonicalName() + "', matching query extension '" + extension + "' inaccessible; probably abstract class?!", ie);
                    ie.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}
