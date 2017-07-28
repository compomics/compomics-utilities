package com.compomics.util.experiment.personalization;

import com.compomics.util.db.ObjectsDB;
import com.compomics.util.IdObject;
import java.util.HashMap;

/**
 * This abstract class provides customization facilities. Tool dependent
 * parameters can be added to classes extending this class.
 *
 * @author Marc Vaudel
 */
public abstract class ExperimentObject extends IdObject {

    /**
     * The version UID for Serialization/Deserialization compatibility.
     */
    static final long serialVersionUID = 1929697552061121072L;
    /**
     * Map containing user refinement parameters.
     */
    private HashMap<String, UrParameter> urParams = null;

    
    
    /**
     * Adds a user refinement parameter.
     *
     * @param parameter The parameter
     */
    public void addUrParam(UrParameter parameter) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        if (urParams == null) {
            createParamsMap();
        }
        urParams.put(parameter.getParameterKey(), parameter);
    }
    
    /**
     * Removes a user parameter from the user parameters map.
     * 
     * @param paramterKey the key of the parameter
     */
    public void removeUrParam(String paramterKey) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        if (urParams != null) {
            urParams.remove(paramterKey);
        }
    }
    
    /**
     * Creates the parameters map unless done by another thread already.
     */
    private synchronized void createParamsMap() {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        if (urParams == null) {
            urParams = new HashMap<String, UrParameter>(1);
        }
    }

    /**
     * Returns the refinement parameter of the same type than the one provided. Null if not found.
     *
     * @param parameter the desired parameter
     * @return the value stored. Null if not found.
     */
    public UrParameter getUrParam(UrParameter parameter) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (urParams == null) {
            return null;
        }
        return urParams.get(parameter.getParameterKey());
    }
    
    /**
     * Clears the loaded parameters.
     */
    public void clearParametersMap() {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        urParams = null;
    }
    
    public void setUrParams(HashMap<String, UrParameter> urParams){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.urParams = urParams;
    }
    
    public HashMap<String, UrParameter> getUrParams(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return urParams;
    }
}
