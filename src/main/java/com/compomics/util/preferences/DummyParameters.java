package com.compomics.util.preferences;


/**
 * Dummy parameters used to retrieve the parameters type.
 *
 * @author Marc Vaudel
 */
public class DummyParameters implements MarshallableParameter {
    
    /**
     * Name of the type of marshalled parameter.
     */
    private String marshallableParameterType = null;
    

    @Override
    public MarshallableParameter.Type getType() {
        if (marshallableParameterType == null) {
            return null;
        }
        return MarshallableParameter.Type.valueOf(marshallableParameterType);
    }

    @Override
    public void setType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
