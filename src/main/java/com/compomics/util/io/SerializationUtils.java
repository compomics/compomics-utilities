/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.io;

import java.io.*;

/**
 * This class implements convenience methods for serialization and deserialization
 *
 * @author Marc
 */
public class SerializationUtils {
    
    /**
     * Writes an object to the destination file
     * @param object the object
     * @param destinationFile the destination file
     * @throws IOException exception thrown whenever an error occurred while writing the file
     */
    public static void writeObject(Object object, File destinationFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(destinationFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
        bos.close();
        fos.close();
    }
    
    /**
     * Reads an object from a serialized file
     * @param serializedFile the serialized file
     * @return the object
     * @throws IOException exception thrown whenever an error occurred while reading the file
     * @throws ClassNotFoundException exception thrown whenever an unknown class is found
     */
    public static Object readObject(File serializedFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(serializedFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream in = new ObjectInputStream(bis);
        Object object = in.readObject();
        in.close();
        fis.close();
        bis.close();
        return object;
    }
    
}
