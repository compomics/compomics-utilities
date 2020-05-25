package com.compomics.util.db.object;

import java.io.Serializable;


//import org.zoodb.api.impl.ZooPC;

/**
 * All classes that are stored in the backend need a unique identifier,
 * all further classes inherit from this class.
 * 
 * @author Dominik Kopczynski
 */
public class DbObject implements Serializable {
    
    /**
     * Unique identifier.
     */
    private long id;
    /** 
     * Flag if object is a first level object or not.
     */
    private boolean firstLevel = false;
    
    /**
     * Constructor.
     */
    public DbObject(){}
    
    /**
     * Returns the id of the object.
     * 
     * @return the id of the object
     */
    public long getId(){
        
        //readDBMode();
        
        return id;
    
    }
    
    
    /**
     * Sets the id of the object.
     * 
     * @param id the id of the object
     */
    public void setId(long id){
        
        //writeDBMode();
        
        this.id = id;
    
    }
    
    
    /**
     * Sets the ZooDB to read mode
     */
    public void readDBMode(){
        /*
        try {
            ObjectsDB.increaseRWCounter();
            zooActivateRead();
        }
        finally {
            ObjectsDB.decreaseRWCounter();
        }
        */
    }
    
    
    
    /**
     * Sets the ZooDB to write mode
     */
    public void writeDBMode(){
        /*
        try {
            ObjectsDB.increaseRWCounter();
            zooActivateWrite();
        }
        finally {
            ObjectsDB.decreaseRWCounter();
        }
        */
    }
            
}
