package com.compomics.util.db.object.objects;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.db.object.ObjectsDB;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class used to store entire objects in the database as a blob.
 *
 * @author dominik.kopczynski
 */
public class BlobObject extends DbObject {
    
    /**
     * Byte representation of the blob.
     */
    private byte[] blob;
    /**
     * Object representation of the blob.
     */
    public Object getBlob;
    
    /**
     * Constructor.
     * 
     * @param object the object represented in this blob.
     * 
     * @throws IOException exception thrown whenever an error occurred while getting the byte representation of the object
     */
    public BlobObject(Object object) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(object);
            }
        } finally {
            bos.close();
        }
        blob = bos.toByteArray();
    }
    
    /**
     * Constructor.
     */
    public BlobObject(){
    }
    
    /**
     * Constructor.
     * 
     * @param blob the byte representation of the object.
     */
    public BlobObject(byte[] blob){
        this.blob = blob;
    }
    
    /**
     * Sets the byte representation of the object.
     * 
     * @param blob the blob as byte array
     */
    public void setBlob(byte[] blob){
        writeDBMode();
        this.blob = blob;
    }
    
    /**
     * Returns the byte representation of the object.
     * 
     * @return the byte representation of the object
     */
    public byte[] getBlob(){
        readDBMode();
        return blob;
    }
    
    /**
     * Returns the object represented by this blob.
     * 
     * @return the object represented by this blob
     * 
     * @throws IOException exception thrown whenever an error occurred while reading the object from its byte representation
     */
    public Object unBlob() throws IOException {
        
        readDBMode();
        
        Object object;
        ByteArrayInputStream bais = new ByteArrayInputStream(blob);
        
        try (BufferedInputStream bis = new BufferedInputStream(bais); ObjectInputStream in = new ObjectInputStream(bis)) {
            
            object = in.readObject();
        
        } catch (ClassNotFoundException e) {
            
            throw new RuntimeException(e);
            
        }
        
        return object;
    }
}
