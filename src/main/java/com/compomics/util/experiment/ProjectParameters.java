/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment;

import com.compomics.util.IdObject;
import java.util.Date;
import java.util.HashMap;

/**
 * All project specific parameters are supposed to be stored here
 * @author Dominik Kopczynski
 */
public class ProjectParameters extends IdObject {
    private Date creationTime = null;
    private String projectUniqueName = "";
    private HashMap<String, String> stringParameters = new HashMap<String, String>();
    private HashMap<String, Integer> integerParameters = new HashMap<String, Integer>();
    private HashMap<String, Double> numericParameters = new HashMap<String, Double>();
    
    public static String nameForDatabase = "Project_parameters_object";
    
    public ProjectParameters(){
        projectUniqueName = "undefined project";
        creationTime = new Date();
    }
    
    public ProjectParameters(String projectUniqueName){
        this.projectUniqueName = projectUniqueName;
        creationTime = new Date();
    }
    
    public Date getCreationTime(){
        zooActivateRead();
        return creationTime;
    }
    
    public void setCreationTime(Date creationTime){
        zooActivateWrite();
        this.creationTime = creationTime;
    }
    
    public String getProjectUniqueName(){
        zooActivateRead();
        return projectUniqueName;
    }
    
    public void getProjectUniqueName(String projectUniqueName){
        zooActivateRead();
        this.projectUniqueName = projectUniqueName;
    }
    
    public void setStringParameter(String key, String parameter){
        zooActivateWrite();
        stringParameters.put(key, parameter);
    }
    
    public String getStringParameter(String key){
        zooActivateRead();
        return stringParameters.get(key);
    }
    
    public void setIntegerParameter(String key, Integer parameter){
        zooActivateWrite();
        integerParameters.put(key, parameter);
    }
    
    public int getIntegerParameter(String key){
        zooActivateRead();
        return integerParameters.get(key);
    }
    
    public void setNumericParameter(String key, Double parameter){
        zooActivateWrite();
        numericParameters.put(key, parameter);
    }
    
    public double getNumericParameter(String key){
        zooActivateRead();
        return numericParameters.get(key);
    }
}
