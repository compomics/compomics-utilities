/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util;

/**
 * All classes that are stored in the backend need a unique identifier,
 * all further classes inherit from this
 * @author dominik.kopczynski
 */
public class IdObject {
    /**
     * unique identifier
     */
    private long id;
    /**
     * modify for storing in the db
     */
    private boolean modified = true;
    
    public IdObject(){}
    
    public void setId(long id){
        this.id = id;
    }
    
    public long getId(){
        return id;
    }
    
    public boolean getModified(){
        return modified;
    }
    
    public void setModified(boolean modified){
        this.modified = modified;
    }
}
