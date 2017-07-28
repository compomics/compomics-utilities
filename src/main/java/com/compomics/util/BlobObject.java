/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util;

import com.compomics.util.db.ObjectsDB;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author dominik.kopczynski
 */
public class BlobObject extends IdObject {
    private byte[] blob;
    public Object getBlob;
    
    public BlobObject(Object object) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
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
        blob = bos.toByteArray();
    }
    
    public BlobObject(){
    }
    
    public BlobObject(byte[] blob){
        this.blob = blob;
    }
    
    public void setBlob(byte[] blob){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.blob = blob;
    }
    
    public byte[] getBlob(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return blob;
    }
    
    public Object unBlob() throws IOException, ClassNotFoundException{
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        Object object;
        ByteArrayInputStream bais = new ByteArrayInputStream(blob);
        BufferedInputStream bis = new BufferedInputStream(bais);
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
        return object;
    }
}
