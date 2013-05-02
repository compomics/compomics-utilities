package com.compomics.util.io;

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
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(destinationFile);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
            bos.close();
            fos.close();
        } catch (IOException e) {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
            throw e;
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
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(serializedFile);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);
            Object object = in.readObject();
            in.close();
            fis.close();
            bis.close();
            return object;
        } catch (IOException e) {
            if (in != null) {
                in.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (bis != null) {
                bis.close();
            }
            throw e;
        } catch (ClassNotFoundException e) {
            if (in != null) {
                in.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (bis != null) {
                bis.close();
            }
            throw e;
        }
    }
}
