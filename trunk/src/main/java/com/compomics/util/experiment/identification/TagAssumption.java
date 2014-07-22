package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.identification.tags.tagcomponents.MassGap;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.personalization.UrParameter;
import java.util.ArrayList;

/**
 * This class represent a tag assumption made by an identification algorithm
 * based on a sequence tag.
 *
 * @author Marc Vaudel
 */
public class TagAssumption extends SpectrumIdentificationAssumption implements UrParameter {

    /**
     * Serial number for backward compatibility
     */
    static final long serialVersionUID = 8514376202742537298L;
    /**
     * List of mass gaps.
     */
    private Tag tag;

    /**
     * Constructor for en empty assumption.
     */
    public TagAssumption() {
    }

    /**
     * Constructor.
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
     * Returns the tag of this assumption.
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
     * Returns the theoretic mass of the tag, eventually without terminal gaps.
     *
     * @param includeCTermGap if true the C-terminal gap will be added if
     * present
     * @param includeNTermGap if true the N-terminal gap will be added if
     * present
     * @return the theoretic mass of the tag
     */
    public double getTheoreticMass(boolean includeCTermGap, boolean includeNTermGap) {
        return tag.getMass(includeCTermGap, includeNTermGap);
    }

    /**
     * Returns the theoretic mass of the tag, eventually without terminal gaps.
     *
     * @param includeCTermGap if true the C-terminal gap will be added if
     * present
     * @param includeNTermGap if true the N-terminal gap will be added if
     * present
     * @return the theoretic mass of the tag
     */
    public double getTheoreticMz(boolean includeCTermGap, boolean includeNTermGap) {
        return (getTheoreticMass(includeCTermGap, includeNTermGap) + identificationCharge.value * ElementaryIon.proton.getTheoreticMass()) / identificationCharge.value;
    }

    /**
     * Computes the possible tag assumptions which can be obtained from this one
     * by accounting for other charges and isotopes.
     *
     * @param forwardIon indicates whether the tag is based on forward ions (a,
     * b, or c)
     * @param minCharge the minimal precursor charge to consider
     * @param maxCharge the maximal precursor charge to consider
     * @param maxIsotope the maximal isotope number to consider
     *
     * @return the possible tag assumptions which can be obtained from this one
     * by accounting for other charges and isotopes
     */
    public ArrayList<TagAssumption> getPossibleTags(boolean forwardIon, int minCharge, int maxCharge, int maxIsotope) {
        ArrayList<TagAssumption> results = new ArrayList<TagAssumption>();
        double refMz = getTheoreticMz(true, true);
        double refMass = getTheoreticMass();
        int refCharge = identificationCharge.value;
        for (int charge = minCharge; charge <= maxCharge; charge++) {
            for (int isotope = 0; isotope <= maxIsotope; isotope++) {
                if (charge != refCharge || isotope > 0) {
                    double newMass = refMz * charge - charge * ElementaryIon.proton.getTheoreticMass();
                    double deltaMass = newMass - refMass + isotope * Atom.C.getDifferenceToMonoisotopic(1);
                    int index = 0;
                    if (forwardIon) {
                        index = tag.getContent().size() - 1;
                    }
                    TagComponent terminalComponent = tag.getContent().get(index);
                    if ((terminalComponent instanceof MassGap) && terminalComponent.getMass() > -deltaMass) {
                        Tag newTag = new Tag(tag);
                        MassGap terminalGap = (MassGap) newTag.getContent().get(index);
                        terminalGap.setMass(terminalComponent.getMass() + deltaMass);
                        TagAssumption tagAssumption = new TagAssumption(advocate, rank, newTag, new Charge(Charge.PLUS, charge), score);
                        results.add(tagAssumption);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Retunrs a new TagAssumption instance where the tag is a reversed version
     * of this tag.
     *
     * @param yIon indicates whether the tag is based on y ions
     * 
     * @return a new TagAssumption instance where the tag is a reversed version
     * of this tag
     */
    public TagAssumption reverse(boolean yIon) {
        return new TagAssumption(advocate, rank, tag.reverse(yIon), identificationCharge, score);
    }

    @Override
    public String getFamilyName() {
        return "deNovo";
    }

    @Override
    public int getIndex() {
        return 2;
    }

    @Override
    public String toString() {
        return tag.asSequence() + ", " + identificationCharge.getChargeAsFormattedString() + " (" + score + ")";
    }
}
