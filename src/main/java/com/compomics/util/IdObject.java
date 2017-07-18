/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util;


import org.zoodb.api.impl.ZooPC;

/**
 * All classes that are stored in the backend need a unique identifier,
 * all further classes inherit from this
 * @author dominik.kopczynski
 */
public class IdObject extends ZooPC {
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
        zooActivateWrite();
        this.id = id;
    }
    
    public long getId(){
        zooActivateRead();
        return id;
    }
    
    public boolean getModified(){
        zooActivateRead();
        return modified;
    }
    
    public void setModified(boolean modified){
        zooActivateWrite();
        this.modified = modified;
    }
}
