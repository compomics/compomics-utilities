package com.compomics.util.experiment;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.Date;
import java.util.HashMap;

/**
 * This class contains project specific parameters.
 * 
 * @author Dominik Kopczynski
 */
public class ProjectParameters extends DbObject {
    private Date creationTime = null;
    private String projectUniqueName = "";
    private HashMap<String, String> stringParameters = new HashMap<>();
    private HashMap<String, Integer> integerParameters = new HashMap<>();
    private HashMap<String, Double> numericParameters = new HashMap<>();
    
    public static final long key = ExperimentObject.asLong("Project_parameters_object");
    
    public ProjectParameters(){
        projectUniqueName = "undefined project";
        creationTime = new Date();
    }
    
    public ProjectParameters(String projectUniqueName){
        this.projectUniqueName = projectUniqueName;
        creationTime = new Date();
    }
    
    public Date getCreationTime(){
        readDBMode();
        return creationTime;
    }
    
    public void setCreationTime(Date creationTime){
        writeDBMode();
        this.creationTime = creationTime;
    }
    
    public String getProjectUniqueName(){
        readDBMode();
        return projectUniqueName;
    }
    
    public void getProjectUniqueName(String projectUniqueName){
        readDBMode();
        this.projectUniqueName = projectUniqueName;
    }
    
    public void setStringParameter(String key, String parameter){
        writeDBMode();
        stringParameters.put(key, parameter);
    }
    
    public String getStringParameter(String key){
        readDBMode();
        return stringParameters.get(key);
    }
    
    public void setIntegerParameter(String key, Integer parameter){
        writeDBMode();
        integerParameters.put(key, parameter);
    }
    
    public int getIntegerParameter(String key){
        readDBMode();
        return integerParameters.get(key);
    }
    
    public void setNumericParameter(String key, Double parameter){
        writeDBMode();
        numericParameters.put(key, parameter);
    }
    
    public double getNumericParameter(String key){
        readDBMode();
        return numericParameters.get(key);
    }
}
