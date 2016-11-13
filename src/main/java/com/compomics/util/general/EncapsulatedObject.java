package com.compomics.util.general;

/**
 * Convenience class allowing the encapsulation of an object.
 *
 * @author Marc Vaudel
 *
 * @param <K> the type of object to encapsulate
 */
public class EncapsulatedObject<K> {
    
    /**
     * The object to encapsulate.
     */
    private K object;
    /**
     * Constructor.
     */
    public EncapsulatedObject() {
        
    }
    /**
     * Constructor.
     * 
     * @param initialValue the initial value
     */
    public EncapsulatedObject(K initialValue) {
        object = initialValue;
    }

    /**
     * Returns the encapsulated object.
     * 
     * @return the encapsulated object
     */
    public K getObject() {
        return object;
    }

    /**
     * Sets the encapsulated object.
     * 
     * @param object the encapsulated object
     */
    public void setObject(K object) {
        this.object = object;
    }
    
}
