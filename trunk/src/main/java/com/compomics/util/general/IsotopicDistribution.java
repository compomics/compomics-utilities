package com.compomics.util.general;

import com.compomics.util.enumeration.MolecularElement;
import com.compomics.util.protein.MolecularFormula;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * This class calculates the isotopic distribution based on a molecular formula.
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Aug-2010
 * Time: 09:13:06
 * To change this template use File | Settings | File Templates.
 */
public class IsotopicDistribution {
    // Class specific log4j logger for AASequenceImpl instances.
	Logger logger = Logger.getLogger(IsotopicDistribution.class);

    /**
     * The result of the isotopic distributions calculation. Percentage of the total contribution
     */
    private Vector<Double> iPercTot = null;
    /**
     * The result of the isotopic distributions calculation. Percentage of the contribution compared to the maximum
     */
    private Vector<Double> iPercMax = null;
    /**
     * The molecular formula
     */
    private MolecularFormula iMolecularFormula;


    /**
     * Constructor
     * @param lFormula MolecularFormula
     */
    public IsotopicDistribution(MolecularFormula lFormula){
        this.iMolecularFormula = lFormula;
    }

    /**
     * This method will do the calculations
     */
    public void calculate(){

        Vector<Vector<Double>> lPerc = new Vector<Vector<Double>>();
        Vector<Integer> lNumbers = new Vector<Integer>();
        Vector<Integer> lDaltonDifferences = new Vector<Integer>();
        Vector<BinomialDistributionImpl> lBinomDistributions = new Vector<BinomialDistributionImpl>();
        Vector<IsotopicElement> lElements = IsotopicElement.getAllIsotopicElements(this.getClass(), logger);
        for(int e = 0; e<lElements.size(); e ++){
            IsotopicElement lElmnt = lElements.get(e);
            int lCount = iMolecularFormula.getElementCount(lElmnt.getElement());
            if(lCount > 0){
                lNumbers.add(lCount);
                Vector<Double> lPercElement = new Vector<Double>();
                lPerc.add(lPercElement);
                lBinomDistributions.add(new BinomialDistributionImpl(lCount, lElmnt.getOccurrence()));
                lDaltonDifferences.add(lElmnt.getDaltonDifference());
            }
        }


        for(int i = 0; i<10; i++){
            for(int p = 0; p<lPerc.size(); p ++){
                Vector<Double> lPercElement = lPerc.get(p);
                BinomialDistributionImpl lBinom = lBinomDistributions.get(p);
                int lDaltonDiff = lDaltonDifferences.get(p);
                if(lDaltonDiff > 1){
                    if(i%lDaltonDiff == 0){
                        lPercElement.add(lBinom.probability(i/lDaltonDiff)*lNumbers.get(p));
                    } else {
                        lPercElement.add(0.0);
                    }
                } else {
                    lPercElement.add(lBinom.probability(i)*lNumbers.get(p));
                }

            }
        }

        Vector<Double> lPercTotal = new Vector<Double>();

        for(int i = 0 ; i<lPerc.size(); i ++){
            if(i==0){
                for(int j = 0; j<lPerc.get(i).size(); j ++){
                    lPercTotal.add(lPerc.get(i).get(j));
                }
            } else {
                if(lNumbers.get(i) != 0){
                    Vector<Double> lTempTotal = new Vector<Double>();
                    for(int k = 1; k<=lPercTotal.size(); k ++){
                        double lTempValue = 0.0;
                        for(int l = 0; l<k; l ++){
                            if(lPercTotal.get(l) != 0.0 && lPerc.get(i).get(k - l - 1) != 0.0){
                                lTempValue = lTempValue + ((lPercTotal.get(l))*lPerc.get(i).get(k - l - 1));
                            }
                        }
                        lTempTotal.add(lTempValue);
                    }
                    lPercTotal = lTempTotal;
                }
            }
        }

        double lMax = 0.0;
        for(int k = 0; k<lPercTotal.size(); k ++){
            for(int e = 0; e<lNumbers.size(); e ++){
                lPercTotal.set(k, lPercTotal.get(k)/lNumbers.get(e));
            }
            if(lPercTotal.get(k)>lMax){
                lMax = lPercTotal.get(k);
            }
        }

        iPercTot = lPercTotal;
        iPercMax = new Vector<Double>();
        for(int k = 0; k<iPercTot.size(); k ++){
            iPercMax.add(iPercTot.get(k)/lMax);
        }
    }

    /**
     * This will calculate the isotopic distribution pattern for the given elements. Labeled atoms (13C, Deut, 15N) must not be given since these are always limited to one peak (100% occurrence)
     * and do not contribute to the distribution pattern 
     * @param lC Number of C atoms
     * @param lN Number of N atoms
     * @param lH Number of H atoms
     * @param lO Number of O atoms
     * @param lS Number of S atoms
     */
    public IsotopicDistribution(int lC, int lN, int lH, int lO, int lS){
        iMolecularFormula = new MolecularFormula();
        iMolecularFormula.addElement(MolecularElement.C, lC);
        iMolecularFormula.addElement(MolecularElement.N, lN);
        iMolecularFormula.addElement(MolecularElement.H, lH);
        iMolecularFormula.addElement(MolecularElement.O, lO);
        iMolecularFormula.addElement(MolecularElement.S, lS);

    }

    /**
     * Getter for result of the isotopic distributions calculation. Percentage of the contribution compared to the maximum.
     * @return Array of doubles with %, location in array corresponds with the number of the peak
     */
    public Double[] getPercMax() {
        if(iPercMax == null){
            calculate();
        }
        Double[] lReturn = new Double[iPercMax.size()];
        iPercMax.toArray(lReturn);
        return lReturn;
    }

    /**
     * Getter for result of the isotopic distributions calculation. Percentage of the total contribution.
     * @return Array of doubles with %, location in array corresponds with the number of the peak
     */
    public Double[] getPercTot() {
        if(iPercTot == null){
            calculate();
        }
        Double[] lReturn = new Double[iPercTot.size()];
        iPercTot.toArray(lReturn);
        return lReturn;
    }
}
