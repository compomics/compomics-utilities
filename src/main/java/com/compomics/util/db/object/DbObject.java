package com.compomics.util.db.object;


import org.zoodb.api.impl.ZooPC;

/**
 * All classes that are stored in the backend need a unique identifier,
 * all further classes inherit from this class.
 * 
 * @author Dominik Kopczynski
 */
public class DbObject extends ZooPC {
    
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
        
        readDBMode();
        
        return id;
    
    }
    
    /**
     * Sets the id of the object.
     * 
     * @param id the id of the object
     */
    public void setId(long id){
        
        writeDBMode();
        
        this.id = id;
    
    }
    
    
    /**
     * Gets whether an object is a first level object or not i.e. attribute within another object
     * @return first level flag
     */
    public boolean getFirstLevel(){
        
        readDBMode();
        
        return firstLevel;
    
    }
    
    
    /**
     * Sets whether an object is a first level object or not i.e. attribute within another object
     * @param firstLevel first level flag 
     */
    public void setFirstLevel(boolean firstLevel){
        
        readDBMode();
        
        this.firstLevel = firstLevel;
    
    }
    
    /**
     * Sets the ZooDB to read mode
     */
    public void readDBMode(){
        try {
            ObjectsDB.increaseRWCounter();
            zooActivateRead();
        }
        finally {
            ObjectsDB.decreaseRWCounter();
        }
    }
    
    
    
    /**
     * Sets the ZooDB to write mode
     */
    public void writeDBMode(){
        try {
            ObjectsDB.increaseRWCounter();
            zooActivateWrite();
        }
        finally {
            ObjectsDB.decreaseRWCounter();
        }
    }
            
}
