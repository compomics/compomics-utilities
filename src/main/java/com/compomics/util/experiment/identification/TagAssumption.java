/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.personalization.ExperimentObject;
import java.util.ArrayList;

/**
 * This class represent a tag assumption made by an identification algorithm based on a sequence tag.
 *
 * @author Marc
 */
public class TagAssumption extends SpectrumIdentificationAssumption {
    
    /**
     * list of mass gaps
     */
    private Tag tag;
    /**
     * Constructor
     * 
     * @param advocate the advocate supporting this assumption
     * @param rank the rank of the assumption
     * @param tag the identified tag 
     * @param identificationCharge the identified charge
     * @param score the score
     */
    public TagAssumption(int advocate, int rank, Tag tag, Charge identificationCharge, double score) {
        this.advocate = advocate;
        this.rank = rank;
        this.tag = tag;
        this.identificationCharge = identificationCharge;
        this.score = score;
    }

    /**
     * Returns the tag of this assumption
     * 
     * @return the tag of this assumption
     */
    public Tag getTag() {
        return tag;
    }
    
    @Override
    public double getTheoreticMass() {
        return tag.getMass();
    }
    
    /**
     * Returns the theoretic mass of the tag, eventually without terminal gaps
     * 
     * @param includeCTermGap if true the C-terminal gap will be added if present
     * @param includeNTermGap if true the N-terminal gap will be added if present
     * 
     * @return the theoretic mass of the tag
     */
    public double getTheoreticMass(boolean includeCTermGap, boolean includeNTermGap) {
        return tag.getMass(includeCTermGap, includeNTermGap);
    }
    
    /**
     * Returns the theoretic mass of the tag, eventually without terminal gaps
     * 
     * @param includeCTermGap if true the C-terminal gap will be added if present
     * @param includeNTermGap if true the N-terminal gap will be added if present
     * 
     * @return the theoretic mass of the tag
     */
    public double getTheoreticMz(boolean includeCTermGap, boolean includeNTermGap) {
        return (getTheoreticMass(includeCTermGap, includeNTermGap) + ElementaryIon.proton.getTheoreticMass() * (identificationCharge.value - 1)) / identificationCharge.value;
    }
}
