package com.compomics.util.io.file;

import java.io.*;

/**
 * This class implements convenience methods for serialization and
 * deserialization.
 *
 * @author Marc Vaudel
 */
public class SerializationUtils {

    /**
     * Writes an object to the destination file.
     *
     * @param object the object
     * @param destinationFile the destination file
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     */
    public static void writeObject(Object object, File destinationFile) throws IOException {

        FileOutputStream fos = new FileOutputStream(destinationFile.getAbsoluteFile());
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                try {
                    oos.writeObject(object);
                } finally {
                    oos.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            fos.close();
        }
    }

    /**
     * Reads an object from a serialized file.
     *
     * @param serializedFile the serialized file
     * @return the object
     * @throws IOException exception thrown whenever an error occurred while
     * reading the file
     * @throws ClassNotFoundException exception thrown whenever an unknown class
     * is found
     */
    public static Object readObject(File serializedFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(serializedFile);
        try {
            BufferedInputStream bis = new BufferedInputStream(fis);
            try {
                ObjectInputStream in = new ObjectInputStream(bis);
                try {
                    Object object = in.readObject();
                    return object;
                } finally {
                    in.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            fis.close();
        }
    }
}
