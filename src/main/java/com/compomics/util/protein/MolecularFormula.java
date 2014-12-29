package com.compomics.util.protein;

import com.compomics.util.enumeration.MolecularElement;
import com.compomics.util.interfaces.Sequence;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * This class represents the molecular formula. Basically it count the number of atoms. 
 * Created by IntelliJ IDEA.
 *
 * User: Niklaas
 * Date: 16-Aug-2010
 * Time: 08:25:22
 */
public class MolecularFormula {

    // Class specific log4j logger for MolecularFormula instances.
    Logger logger = Logger.getLogger(MolecularFormula.class);

    /**
     * The hashmap collecting all the MolecularElements
     */
    private HashMap<MolecularElement,Integer> iFormula = new HashMap<MolecularElement,Integer>();

    /**
     * Default constructor
     */
    public MolecularFormula(){

    }

    /**
     * Constructor.
     *
     * @param lSequence the amino acid sequence
     */
    public MolecularFormula(Sequence lSequence){
        HashMap<String, MolecularFormula> iElements = new HashMap<String, MolecularFormula>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("elements.txt")));
            String line;
            String[] lHeaderElements = null;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("#")){
                    //do nothing    
                } else if(line.startsWith("Header")){
                    String lTemp = line.substring(line.indexOf("=") + 1);
                    lHeaderElements = lTemp.split(",");
                } else {
                    String lAa = line.substring(0, line.indexOf("="));
                    String[] lContribution = line.substring(line.indexOf("=") + 1).split(",");

                    MolecularFormula lAaFormula = new MolecularFormula();

                    for(int i = 0; i<lHeaderElements.length; i ++){
                        for (MolecularElement lMolecularElement : MolecularElement.values()) {
                            if(lMolecularElement.toString().equalsIgnoreCase(lHeaderElements[i])){
                                lAaFormula.addElement(lMolecularElement, Integer.valueOf(lContribution[i]));
                            }
                        }
                    }
                    iElements.put(lAa, lAaFormula);
                }
            }
            br.close();
        } catch(Exception e){
            logger.error(e);
        }

        //add the N-terminus
        this.addElement(MolecularElement.H, 2);

        for(int i = 0; i<lSequence.getSequence().length(); i ++){
            String lAa = String.valueOf(lSequence.getSequence().charAt(i));
            MolecularFormula lFormula = iElements.get(lAa);
            this.addMolecularFormula(lFormula);
        }

        //add the C-terminus
        this.addElement(MolecularElement.H, 1);
        this.addElement(MolecularElement.O, 1);
    }

    /**
     * Method to add MolecularElements to this formula
     * @param lMolecularElement The molecular element
     * @param lCount The count
     */
    public void addElement(MolecularElement lMolecularElement, Integer lCount){
        Integer lOldCount = iFormula.get(lMolecularElement);
        if(lOldCount == null){
            lOldCount = lCount;
        } else {
            lOldCount = lOldCount + lCount;
        }
        iFormula.put(lMolecularElement, lOldCount);
    }

    /**
     * Method to add whole formulas to this formula
     * @param lMolecularFormula The MolecularFormula to add
     */
    public void addMolecularFormula(MolecularFormula lMolecularFormula){
        for (MolecularElement lMolecularElement : MolecularElement.values()) {
            this.addElement(lMolecularElement, lMolecularFormula.getElementCount(lMolecularElement));
        }       
    }

    /**
     * Getter for the count of a specific element
     * @param lMolecularElement The MolecularElement to get to count for
     * @return Int with the count
     */
    public int getElementCount(MolecularElement lMolecularElement){
        Integer lCount = iFormula.get(lMolecularElement);
        if(lCount == null){
            return 0;
        }
        return lCount;
    }

    /**
     * To string method
     * @return String
     */
    public String toString(){
        String lResult = "";
        for (MolecularElement lMolecularElement : MolecularElement.values()) {
            int lCount = this.getElementCount(lMolecularElement);
            if(lCount > 0){
                lResult = lResult + lMolecularElement + this.getElementCount(lMolecularElement) + " ";
            }
        }
        return lResult;
    }
}
