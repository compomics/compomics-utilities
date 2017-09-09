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
     * Gets whether an object is a first level object or not i.e. attribute within another object
     * @return first level flag
     */
    public boolean getFirstLevel(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return firstLevel;
    }
    
    
    /**
     * Sets whether an object is a first level object or not i.e. attribute within another object
     * @param firstLevel first level flag 
     */
    public void setFirstLevel(boolean firstLevel){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.firstLevel = firstLevel;
    }
}
