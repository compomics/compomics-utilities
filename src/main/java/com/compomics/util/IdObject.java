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
     * table for keeping current database scheme
     */
    private String table;
    
    public IdObject(){};
    
    public void setId(long id){
        this.id = id;
    }
    
    public long getId(){
        return id;
    }
    
    public void setTable(String table){
        this.table = table;
    }
    
    public String getTable(){
        return table;
    }
}
