package com.compomics.util.experiment.biology;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Aug 23, 2010
 * Time: 1:44:12 PM
 * This class modelizes an enzyme.
 */
public class Enzyme {


    /*
     * The enzyme id
     */
    private int id;

    /*
     * The enzyme name
     */
    private String name;

    /*
     * The amino-acids before cleavage
     */
    private ArrayList<Character> aminoAcidBefore = new ArrayList<Character>();

    /*
     * The amino-acids after cleavage
     */
    private ArrayList<Character> aminoAcidAfter = new ArrayList<Character>();

    /*
     * The restriction amino-acids before cleavage
     */
    private ArrayList<Character> restrictionBefore = new ArrayList<Character>();

    /*
     * The restriction amino-acids after cleavage
     */
    private ArrayList<Character> restrictionAfter = new ArrayList<Character>();

    /**
     * Get the enzyme name
     *
     * @return          The enzyme name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the enzyme id
     *
     * @return          The enzyme number
     */
    public int getId() {
        return id;
    }

    /**
     * Constructor for an Enzyme
     * @param id                    the enzyme id which should be OMSSA compatible.
     * @param name                  the name of the enzyme
     * @param aminoAcidBefore       the amino-acids which can be found before the cleavage
     * @param restrictionBefore     the amino-acids which should not be found before the cleavage
     * @param aminoAcidAfter        the amino-acids which should be found after the cleavage
     * @param restrictionAfter      the amino-acids which should not be found after the cleavage
     */
    public Enzyme(int id, String name, String aminoAcidBefore, String restrictionBefore, String aminoAcidAfter, String restrictionAfter) {
        this.id = id;
        this.name = name;
        for (char aa : aminoAcidBefore.toCharArray()) {
            this.aminoAcidBefore.add(aa);
        }
        for (char aa : restrictionBefore.toCharArray()) {
            this.restrictionBefore.add(aa);
        }
        for (char aa : aminoAcidAfter.toCharArray()) {
            this.aminoAcidAfter.add(aa);
        }
        for (char aa : restrictionAfter.toCharArray()) {
            this.restrictionAfter.add(aa);
        }
    }

   /**
    * Get the X!Tandem enzyme format
    *
    * @return          The enzyme X!Tandem format as String
    */
    public String getXTandemFormat() {
        String result = "";
        result += "[";
        for (Character aa : aminoAcidBefore) {
            result += aa;
        }
        result += "]";
        if (restrictionBefore.size() > 0) {
            result += "{";
            for (Character aa : restrictionBefore) {
                result += aa;
            }
            result += "}";
        }
        result += "|";
        result += "[";
        for (Character aa : aminoAcidAfter) {
            result += aa;
        }
        result += "]";
        if (restrictionAfter.size() > 0) {
            result += "{";
            for (Character aa : restrictionAfter) {
                result += aa;
            }
            result += "}";
        }
        return result;
    }

}
