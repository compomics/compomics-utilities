package com.compomics.util.preferences;

/**
 * Interface for a parameter which can be marshalled as .par file.
 *
 * @author Marc Vaudel
 */
public interface MarshallableParameter {

    /**
     * Enum of the type of parameter which can be encountered in a .par file.
     */
    public static enum Type {
        search_parameters, identification_parameters;
    }
    
    /**
     * Returns the type of marshalled parameter from an unmarshalled object. The type must be written in the file. Null if not a MarshallableParameter.
     * 
     * @return the type of marshalled parameter from an unmarshalled object
     */
    public Type getType();
    
    
}
