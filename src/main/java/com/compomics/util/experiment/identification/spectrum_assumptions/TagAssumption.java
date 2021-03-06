package com.compomics.util.experiment.identification.spectrum_assumptions;

import com.compomics.util.experiment.biology.atoms.Atom;
import com.compomics.util.experiment.biology.ions.Charge;
import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.amino_acid_tags.MassGap;
import java.util.ArrayList;

/**
 * This class represent a tag assumption made by an identification algorithm
 * based on a sequence tag.
 *
 * @author Marc Vaudel
 */
public class TagAssumption extends SpectrumIdentificationAssumption {

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
     * Constructor.Note: if PsmScores.scoreRoundingDecimal is not null the
     * scored will be floored accordingly.
     *
     * @param advocate the advocate supporting this assumption
     * @param rank the rank of the assumption
     * @param tag the identified tag
     * @param identificationCharge the identified charge
     * @param rawScore the raw score, i.e. the untransformed score given by the
     * search engine
     * @param score the (potentially transformed) score, typically a search
     * engine e-value (whether the score is ascending or descending can be known
     * from the SearchEngine class)
     */
    public TagAssumption(int advocate, int rank, Tag tag, int identificationCharge, double rawScore, double score) {
        this.advocate = advocate;
        this.rank = rank;
        this.tag = tag;
        this.identificationCharge = identificationCharge;
        this.rawScore = rawScore;
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

    public void setTag(Tag tag) {

        this.tag = tag;
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

        return (getTheoreticMass(includeCTermGap, includeNTermGap) + identificationCharge * ElementaryIon.proton.getTheoreticMass()) / identificationCharge;
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

        ArrayList<TagAssumption> results = new ArrayList<>();
        double refMz = getTheoreticMz(true, true);
        double refMass = getTheoreticMass();
        int refCharge = identificationCharge;

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
                        TagAssumption tagAssumption = new TagAssumption(advocate, rank, newTag, charge, rawScore, score);
                        results.add(tagAssumption);

                    }
                }
            }
        }

        return results;

    }

    /**
     * Returns a new TagAssumption instance where the tag is a reversed version
     * of this tag.
     *
     * @param yIon indicates whether the tag is based on y ions
     *
     * @return a new TagAssumption instance where the tag is a reversed version
     * of this tag
     */
    public TagAssumption reverse(boolean yIon) {

        return new TagAssumption(advocate, rank, tag.reverse(yIon), identificationCharge, rawScore, score);
    }

    @Override
    public String toString() {

        return tag.asSequence() + ", " + Charge.toString(identificationCharge) + " (" + score + ")";
    }
}
