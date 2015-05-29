package com.compomics.util.math.roc;

import com.compomics.util.math.statistics.Distribution;
import com.compomics.util.math.statistics.ROC;
import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.commons.math.MathException;

/**
 * This class can be used to draw roc curves from experimental data.
 *
 * @author Marc Vaudel
 */
public class DistributionRoc implements ROC {

    /**
     * The control distribution.
     */
    private final Distribution distributionControl;
    /**
     * The patient distribution.
     */
    private final Distribution distributionPatient;

    /**
     * Constructor. The patient distribution should be higher (to the right)
     * than the control distribution.
     *
     * @param distributionControl the control distribution
     * @param distributionPatient the patient distribution
     */
    public DistributionRoc(Distribution distributionControl, Distribution distributionPatient) {
        this.distributionControl = distributionControl;
        this.distributionPatient = distributionPatient;
    }

    @Override
    public double getValueAt(double specificity, MathContext mathContext) throws MathException {
        double x = distributionPatient.getValueAtCumulativeProbability(specificity, mathContext).doubleValue();
        return distributionControl.getCumulativeProbabilityAt(x, mathContext).doubleValue();
    }

    @Override
    public double getSpecificityAt(double sensitivity, MathContext mathContext) throws MathException {
        double x = distributionControl.getValueAtCumulativeProbability(sensitivity, mathContext).doubleValue();
        return distributionPatient.getCumulativeProbabilityAt(x, mathContext).doubleValue();
    }

    @Override
    public double[][] getxYValues(MathContext mathContext) throws MathException {
        double[][] result = new double[101][2];
        for (int i = 0; i <= 100; i++) {
            double x = ((double) i) / 100;
            double y = getValueAt(x, mathContext);
            result[i][0] = x;
            result[i][1] = y;
        }
        return result;
    }

    @Override
    public double getAuc(MathContext mathContext) throws MathException {
        int nBins = 1000;
        double binSize = 1.0 / nBins;
        double halfBin = binSize / 2;
        double auc = 0.0;
        for (int i = 0; i < nBins; i++) {
            double x = i * binSize + halfBin;
            double y = getValueAt(x, mathContext);
            auc += y;
        }
        auc *= binSize;
        return auc;
    }
}
