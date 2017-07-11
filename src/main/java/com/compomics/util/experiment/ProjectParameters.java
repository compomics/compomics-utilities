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
    Date creationTime = new Date();
    String projectUniqueName = "";
    HashMap<String, String> stringParameters = new HashMap<String, String>();
    HashMap<String, Integer> integerParameters = new HashMap<String, Integer>();
    HashMap<String, Double> numericParameters = new HashMap<String, Double>();
    
    public ProjectParameters(){}
    
    public Date getCreationTime(){
        return creationTime;
    }
    
    public void setCreationTime(Date creationTime){
        this.creationTime = creationTime;
    }
    
    public String getProjectUniqueName(){
        return projectUniqueName;
    }
    
    public void getProjectUniqueName(String projectUniqueName){
        this.projectUniqueName = projectUniqueName;
    }
    
    public void setStringParameter(String key, String parameter){
        stringParameters.put(key, parameter);
    }
    
    public String getStringParameter(String key){
        return stringParameters.get(key);
    }
    
    public void setIntegerParameter(String key, Integer parameter){
        integerParameters.put(key, parameter);
    }
    
    public int getIntegerParameter(String key){
        return integerParameters.get(key);
    }
    
    public void setNumericParameter(String key, Double parameter){
        numericParameters.put(key, parameter);
    }
    
    public double getNumericParameter(String key){
        return numericParameters.get(key);
    }
}
