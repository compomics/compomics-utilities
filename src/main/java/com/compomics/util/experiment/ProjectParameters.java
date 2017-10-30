package com.compomics.util.experiment;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.Date;
import java.util.HashMap;

/**
 * All project specific parameters are supposed to be stored here
 * @author Dominik Kopczynski
 */
public class ProjectParameters extends DbObject {
    private Date creationTime = null;
    private String projectUniqueName = "";
    private HashMap<String, String> stringParameters = new HashMap<>();
    private HashMap<String, Integer> integerParameters = new HashMap<>();
    private HashMap<String, Double> numericParameters = new HashMap<>();
    
    public static final String nameForDatabase = "Project_parameters_object";
    
    public ProjectParameters(){
        projectUniqueName = "undefined project";
        creationTime = new Date();
    }
    
    public ProjectParameters(String projectUniqueName){
        this.projectUniqueName = projectUniqueName;
        creationTime = new Date();
    }
    
    public Date getCreationTime(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return creationTime;
    }
    
    public void setCreationTime(Date creationTime){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.creationTime = creationTime;
    }
    
    public String getProjectUniqueName(){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return projectUniqueName;
    }
    
    public void getProjectUniqueName(String projectUniqueName){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        this.projectUniqueName = projectUniqueName;
    }
    
    public void setStringParameter(String key, String parameter){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        stringParameters.put(key, parameter);
    }
    
    public String getStringParameter(String key){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return stringParameters.get(key);
    }
    
    public void setIntegerParameter(String key, Integer parameter){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        integerParameters.put(key, parameter);
    }
    
    public int getIntegerParameter(String key){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return integerParameters.get(key);
    }
    
    public void setNumericParameter(String key, Double parameter){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        numericParameters.put(key, parameter);
    }
    
    public double getNumericParameter(String key){
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return numericParameters.get(key);
    }
    
    /**
     * Returns the key to use when sorting the project parameters to the database.
     * 
     * @return the key to use when sorting the project parameters to the database
     */
    public static long getKey() {
        
        return ExperimentObject.asLong(nameForDatabase);
        
    }
}
