package com.compomics.util.db.object;


import java.io.Serializable;
import org.zoodb.api.impl.ZooPC;

/**
 * All classes that are stored in the backend need a unique identifier,
 * all further classes inherit from this class.
 * 
 * @author dominik.kopczynski
 */
public class DbObject extends ZooPC implements Serializable {
    
    private static final long serialVersionUID = -7906158551970915613l;
    
    /**
     * unique identifier
     */
    private long id;
    /**
     * Indicates if the object is already stored in the db
     */
    private boolean storedInDB = false;
    /** 
     * flag if object is a first level object or not
     */
    private boolean firstLevel = false;
    
    public DbObject(){}
    
    /**
     * Returns the id of the object.
     * 
     * @return the id of the object
     */
    public long getId(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return id;
    }
    
    /**
     * Sets the id of the object.
     * 
     * @param id the id of the object
     */
    public void setId(long id){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.id = id;
    }
    
    /**
     * Indicates whether the object is a first level object.
     * 
     * @return a boolean indicating whether the object is a first level object
     */
    public boolean getFirstLevel(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return firstLevel;
    }
    
    /**
     * Sets whether the object is a first level object.
     * 
     * @param firstLevel a boolean indicating whether the object is a first level object
     */
    public void setFirstLevel(boolean firstLevel){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.firstLevel = firstLevel;
    }
    
    /**
     * Indicates whether the object is stored in the database.
     * 
     * @return a boolean indicating whether the object is stored in the database
     */
    public boolean getStoredInDB(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return storedInDB;
    }
    
    /**
     * Sets whether the object is stored in the database.
     * 
     * @param storedInDB a boolean indicating whether the object is stored in the database
     */
    public void setStoredInDB(boolean storedInDB){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.storedInDB = storedInDB;
    }
}
