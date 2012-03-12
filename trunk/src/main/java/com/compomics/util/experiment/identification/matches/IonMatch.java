package com.compomics.util.experiment.identification.matches;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon.PeptideFragmentIonType;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.pride.CvTerm;

/**
 * This class will model the assignment of a peak to a theoretical ion.
 * 
 * @author Marc Vaudel
 */
public class IonMatch extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = 5753142782728884464L;
    /**
     * The matched peak
     */
    public Peak peak;
    /**
     * The matching ion
     */
    public Ion ion;
    /**
     * The supposed charge of the ion
     */
    public Charge charge;

    /**
     * Constructor for an ion peak.
     *
     * @param aPeak     the matched peak
     * @param anIon     the corresponding type of ion
     * @param aCharge   the charge of the ion
     */
    public IonMatch(Peak aPeak, Ion anIon, Charge aCharge) {
        peak = aPeak;
        ion = anIon;
        charge = aCharge;
    }

    /**
     * Get the matching error.
     *
     * @deprecated replaced by getAbsoluteError() and getRelativeError()
     * @return the matching error
     */
    public double getError() {
        return peak.mz - ((ion.theoreticMass + charge.value * Atom.H.mass) / charge.value);
    }
    
    /**
     * Get the absolute matching error in Da.
     *
     * @return the absolute matching error
     */
    public double getAbsoluteError() {
        return peak.mz - ((ion.theoreticMass + charge.value * Atom.H.mass) / charge.value);
    }
    
    /**
     * Get the relative matching error in ppm.
     *
     * @return the relative matching error
     */
    public double getRelativeError() { 
        return ((peak.mz - ((ion.theoreticMass + charge.value * Atom.H.mass) / charge.value)) 
                / ((ion.theoreticMass + charge.value * Atom.H.mass) / charge.value)) * 1000000;
    }

    /**
     * Returns the annotation to use for the given ion match as a String.
     *
     * Format: ion type + [ion number] + [charge] + [neutral loss]
     *
     * @return the annotation to use for the given ion match
     */
    public String getPeakAnnotation() {
        return getPeakAnnotation(false);
    }
    
    /**
     * Returns the annotation to use for the given ion match as a String.
     *
     * Format: ion type + [ion number] + [charge] + [neutral loss]
     *
     * @param html  if true, returns the annotation as HTML with subscripts tags
     * @return      the annotation to use for the given ion match
     */
    public String getPeakAnnotation(boolean html) {
        
        if (ion instanceof PeptideFragmentIon) {
            PeptideFragmentIon fragmentIon = ((PeptideFragmentIon) ion);

            String annotation = fragmentIon.getIonType();
            
            if (html) {
                annotation = "<html>" + annotation;
            }

            // add fragment ion number
            if (fragmentIon.getType() != PeptideFragmentIonType.IMMONIUM
                    && fragmentIon.getType() != PeptideFragmentIonType.PRECURSOR_ION) {
                if (html) {
                    annotation += "<sub>" + fragmentIon.getNumber() + "</sub>";
                } else  {
                    annotation += fragmentIon.getNumber();
                }
            }

            // add charge
            annotation += charge.getChargeAsFormattedString();
            
            // add any neutral losses
            if (html) {
                String neutralLoss = fragmentIon.getNeutralLoss();
                
                for (int i=0; i<neutralLoss.length(); i++) {
                    if (Character.isDigit(neutralLoss.charAt(i))) {
                        annotation += "<sub>" + neutralLoss.charAt(i) + "</sub>";
                    } else {
                        annotation += neutralLoss.charAt(i);
                    }
                }
            } else {
                annotation += fragmentIon.getNeutralLoss();
            }

            if (html) {
                annotation = annotation + "</html>";
            }
            
            return annotation;
        } else {
            return null;
        }
    }
    
    /**
     * Returns the pride CV term for the ion match m/z
     * @return the pride CV term for the ion match m/z
     */
    public CvTerm getMZPrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000188", "product ion m/z", peak.mz + "");
    }
    
    /**
     * Returns the pride CV term for the ion match intensity
     * @return the pride CV term for the ion match intensity
     */
    public CvTerm getIntensityPrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000189", "product ion intensity", peak.intensity + "");
    }
    
    /**
     * Returns the pride CV term for the ion match error
     * @return the pride CV term for the ion match error
     */
    public CvTerm getIonMassErrorPrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000190", "product ion mass error", getAbsoluteError() + "");
    }
    
    /**
     * Returns the pride CV term for the ion match charge
     * @return the pride CV term for the ion match charge
     */
    public CvTerm getChargePrideCvTerm() {
        return new CvTerm("PRIDE", "PRIDE:0000204", "product ion charge", charge.value + "");
    }
}
