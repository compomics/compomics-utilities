package com.compomics.util.experiment.biology.ions;

import com.compomics.util.experiment.biology.Ion;

/**
 * This class models a reporter ion.
 * Ion indexes should be the rounded mass.
 * User: Marc
 * Date: Sep 1, 2010
 * Time: 1:44:59 PM
 */
public class ReporterIon extends Ion {

    /**
     * Reporter ion index
     * iTRAQ 113
     */
    public static final int ITRAQ_113 = 113;
    /**
     * Reporter ion index
     * iTRAQ 114
     */
    public static final int ITRAQ_114 = 114;
    /**
     * Reporter ion index
     * iTRAQ 115
     */
    public static final int ITRAQ_115 = 115;
    /**
     * Reporter ion index
     * iTRAQ 116
     */
    public static final int ITRAQ_116 = 116;
    /**
     * Reporter ion index
     * iTRAQ 117
     */
    public static final int ITRAQ_117 = 117;
    /**
     * Reporter ion index
     * iTRAQ 118
     */
    public static final int ITRAQ_118 = 118;
    /**
     * Reporter ion index
     * iTRAQ 119
     */
    public static final int ITRAQ_119 = 119;
    /**
     * Reporter ion index
     * iTRAQ 121
     */
    public static final int ITRAQ_121 = 121;
    /**
     * Reporter ion index
     * TMT 0
     */
    public static final int TMT0 = 126;
    /**
     * Reporter ion index
     * TMT 1
     */
    public static final int TMT1 = 127;
    /**
     * Reporter ion index
     * TMT 2
     */
    public static final int TMT2 = 128;
    /**
     * Reporter ion index
     * TMT 3
     */
    public static final int TMT3 = 129;
    /**
     * Reporter ion index
     * TMT 4
     */
    public static final int TMT4 = 130;
    /**
     * Reporter ion index
     * TMT 5
     */
    public static final int TMT5 = 131;
    /**
     * ion index according to the static fields or user definition
     */
    private int index;
    /**
     * ion name for user defined ions
     */
    private String name = null;

    /**
     * Constructor for a reporter ion
     * @param index reporter ion index according to the static fields
     */
    public ReporterIon(int index) {
        this.index = index;
        this.familyType = Ion.REPORTER_ION;
        setReferenceMass();
    }

    /**
     * Constructor for a user-defined reporter ion
     * @param index index of the reporter ion (for user-defined ions avoid static fields index)
     * @param name  name of the reporter ion
     * @param mass  theoretic mass of the reporter ion
     */
    public ReporterIon(int index, String name, double mass) {
        this.index = index;
        this.familyType = Ion.REPORTER_ION;
        this.name = name;
        this.theoreticMass = mass;
    }

    /**
     * Getter for the ion type
     * @return the ion index according to the static fields
     */
    public int getIndex() {
        return index;
    }

    /**
     * This method returns the name of the reporter ion
     * @return name of the reporter ion
     */
    public String getName() {
        switch (index) {
            case ITRAQ_114:
                return "iTRAQ 114";
            case ITRAQ_115:
                return "iTRAQ 115";
            case ITRAQ_116:
                return "iTRAQ 116";
            case ITRAQ_117:
                return "iTRAQ 117";
            case ITRAQ_118:
                return "iTRAQ 118";
            case ITRAQ_119:
                return "iTRAQ 119";
            case ITRAQ_121:
                return "iTRAQ 121";
            case TMT0:
                return "TMT 0";
            case TMT1:
                return "TMT 1";
            case TMT2:
                return "TMT 2";
            case TMT3:
                return "TMT 3";
            case TMT4:
                return "TMT 4";
            case TMT5:
                return "TMT 5";
            default:
                if (name != null) {
                    return name;
                }
                return "unknown";
        }
    }

    /**
     * Method to set the mass of the reporter ion
     * @param referenceMass the mass where the reporter ions should be found
     */
    public void setMass(double referenceMass) {
        this.theoreticMass = referenceMass;
    }

    /**
     * This method sets default hard coded reference masses
     */
    private void setReferenceMass() {
        switch (index) {
            case ITRAQ_114:
                this.theoreticMass = 114.1112;
                return;
            case ITRAQ_115:
                this.theoreticMass = 115.1083;
                return;
            case ITRAQ_116:
                this.theoreticMass = 116.1116;
                return;
            case ITRAQ_117:
                this.theoreticMass = 117.1150;
                return;
            case ITRAQ_118:
                this.theoreticMass = 118;
                return;
            case ITRAQ_119:
                this.theoreticMass = 119;
                return;
            case ITRAQ_121:
                this.theoreticMass = 121;
                return;
            case TMT0:
                this.theoreticMass = 126;
                return;
            case TMT1:
                this.theoreticMass = 127;
                return;
            case TMT2:
                this.theoreticMass = 128;
                return;
            case TMT3:
                this.theoreticMass = 129;
                return;
            case TMT4:
                this.theoreticMass = 130;
                return;
            case TMT5:
                this.theoreticMass = 131;
        }
    }
}
