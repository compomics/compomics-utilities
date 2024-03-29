package com.compomics.util.general;

import com.compomics.util.enumeration.MolecularElement;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.util.Vector;

/**
 * This class represents the isotopically different element with the occurrence 
 * and the dalton difference between this and the natural variant.
 *
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Aug-2010
 * Time: 09:59:12
 */
public class IsotopicElement {

    // Class specific log4j logger for AASequenceImpl instances.
    Logger logger = LogManager.getLogger(IsotopicElement.class);

    /**
     * The element
     */
    private final MolecularElement iElement;
    /**
     * The dalton difference with the natural form of this element
     */
    private final int iDaltonDifference;
    /**
     * The occurrence of the element
     */
    private final double iOccurrence;

    /**
     * Constructor
     * @param iElement The element
     * @param iDaltonDifference The dalton difference from the natural form of this element
     * @param iOccurrence The occurrence of this element
     */
    private IsotopicElement(MolecularElement iElement, int iDaltonDifference, double iOccurrence) {
        this.iElement = iElement;
        this.iDaltonDifference = iDaltonDifference;
        this.iOccurrence = iOccurrence;
    }

    /**
     * Static method that gives all the isotopicElements from the isotopicElement.txt file
     * @param lClass A class
     * @param lLogger A logger
     * @return a Vector of IsotopicElement 
     */
    public static Vector<IsotopicElement> getAllIsotopicElements(Class lClass, Logger lLogger){
        Vector<IsotopicElement> lResult = new Vector<>();
        //read the isotopicElement.txt file
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(lClass.getClassLoader().getResourceAsStream("isotopicElement.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lEle = line.split(",");
                for (MolecularElement lMolecularElement : MolecularElement.values()) {
                    if(lMolecularElement.toString().equalsIgnoreCase(lEle[0])){
                        lResult.add(new IsotopicElement(lMolecularElement, Integer.valueOf(lEle[1]), Double.valueOf(lEle[2])));
                    }
                }
            }
            br.close();
        } catch(Exception e){
            lLogger.error(e);
        }
        return lResult;
    }

    /**
     * Getter for the MolecularElement
     * @return MolecularElement
     */
    public MolecularElement getElement() {
        return iElement;
    }

    /**
     * Getter for the dalton difference with the natural form of this element
     * @return int
     */
    public int getDaltonDifference() {
        return iDaltonDifference;
    }

    /**
     * Getter for the occurrence of this element
     * @return double
     */
    public double getOccurrence() {
        return iOccurrence;
    }

    /**
     * To string method
     * @return String
     */
    public String toString(){
        return iElement + " Da diff:" + iDaltonDifference + " Occurrence:" + iOccurrence;
    }
}
