/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.AminoAcid;
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
     * @param tag the identified tag
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
}
