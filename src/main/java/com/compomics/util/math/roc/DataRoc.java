package com.compomics.util.math.roc;

import com.compomics.util.math.statistics.ROC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math.MathException;

/**
 * This class can be used to draw roc curves from experimental data.
 *
 * @author Marc Vauel
 */
public class DataRoc implements ROC {

    /**
     * Empty default constructor
     */
    public DataRoc() {
    }

    /**
     * The x values of the ROC points.
     */
    private ArrayList<Double> xValues;

    /**
     * The y values of the ROC points.
     */
    private ArrayList<Double> yValues;
    /**
     * The method to use to interpolate between points.
     */
    private RocInterpolation rocInterpolation;

    /**
     * Enum listing the possible ways of interpolating points on the ROC.
     */
    public static enum RocInterpolation {

        /**
         * Returns the maximum value of the two surrounding points.
         */
        maximum,
        /**
         * Returns a linear interpolation of the two surrounding points.
         */
        linear,
        /**
         * Returns the minimal value of the two surrounding points.
         */
        minimum;
    }

    /**
     * Constructor.
     *
     * @param controlValues the control values
     * @param patientValues the patient values
     * @param rocInterpolation the method to use to interpolate between points
     */
    public DataRoc(ArrayList<Double> controlValues, ArrayList<Double> patientValues, RocInterpolation rocInterpolation) {
        if (controlValues == null || controlValues.isEmpty()) {
            throw new IllegalArgumentException("No control values given for ROC curve creation.");
        }
        if (patientValues == null || patientValues.isEmpty()) {
            throw new IllegalArgumentException("No patient values given for ROC curve creation.");
        }
        this.rocInterpolation = rocInterpolation;
        // values map: value -> {# control, # patients}
        HashMap<Double, int[]> valuesMap = new HashMap<>();
        int nControls = 0;
        for (Double value : controlValues) {
            int[] n = valuesMap.get(value);
            if (n == null) {
                n = new int[]{1, 0};
                valuesMap.put(value, n);
            } else {
                n[0] = n[0] + 1;
            }
            nControls++;
        }
        int nPatients = 0;
        for (Double value : patientValues) {
            int[] n = valuesMap.get(value);
            if (n == null) {
                n = new int[]{0, 1};
                valuesMap.put(value, n);
            } else {
                n[1] = n[1] + 1;
            }
            nPatients++;
        }
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        ArrayList<Double> values = new ArrayList<>(valuesMap.keySet());
        Collections.sort(values);
        int patientCpt = 0;
        int controlCpt = 0;
        for (Double value : values) {
            int[] counts = valuesMap.get(value);
            if (counts[1] > 0) {
                double x = ((double) patientCpt) / nPatients;
                xValues.add(x);
                double y = ((double) controlCpt) / nControls;
                yValues.add(y);
            }
            patientCpt += counts[1];
            controlCpt += counts[0];
        }
        xValues.add(1.0);
        yValues.add(1.0);
    }

    @Override
    public double getValueAt(double specificity) throws MathException {
        Double xBefore = null;
        Double yBefore = null;
        Double xAfter = null;
        Double yAfter = null;
        int i = 0;
        for (double xValue : xValues) {
            if (xBefore == null || xValue < specificity && xValue > xBefore) {
                xBefore = xValue;
                yBefore = yValues.get(i);
            } else if (xValue > specificity || i == xValues.size() - 1) {
                xAfter = xValue;
                yAfter = yValues.get(i);
                break;
            }
            i++;
        }
        if (specificity == xBefore || rocInterpolation == RocInterpolation.minimum) {
            return yBefore;
        } else if (specificity == xAfter || rocInterpolation == RocInterpolation.maximum) {
            return yAfter;
        } else {
            double y = yBefore + (specificity - xBefore) / (xAfter - xBefore) * (yAfter - yBefore);
            return y;
        }
    }

    @Override
    public double getSpecificityAt(double sensitivity) throws MathException {
        Double xBefore = null;
        Double yBefore = null;
        Double xAfter = null;
        Double yAfter = null;
        int i = 0;
        for (double yValue : yValues) {
            if (xBefore == null || yValue < sensitivity && yValue > yBefore) {
                yBefore = yValue;
                xBefore = xValues.get(i);
            } else if (yValue > sensitivity || i == yValues.size() - 1) {
                yAfter = yValue;
                xAfter = xValues.get(i);
                break;
            }
            i++;
        }
        if (sensitivity == yBefore || rocInterpolation == RocInterpolation.minimum) {
            return xBefore;
        } else if (sensitivity == yAfter || rocInterpolation == RocInterpolation.maximum) {
            return xAfter;
        } else {
            double x = xBefore + (sensitivity - yBefore) / (yAfter - yBefore) * (xAfter - xBefore);
            return x;
        }
    }

    @Override
    public double[][] getxYValues() throws MathException {
        double[][] result = new double[xValues.size()][2];
        int i = 0;
        for (double xValue : xValues) {
            result[i][0] = xValue;
            result[i][1] = yValues.get(i);
            i++;
        }
        return result;
    }

    @Override
    public double getAuc() throws MathException {
        double auc = 0;
        for (int i = 0; i < xValues.size() - 1; i++) {
            double xAfter = xValues.get(i + 1);
            double xBefore = xValues.get(i);
            if (rocInterpolation == RocInterpolation.minimum) {
                auc += yValues.get(i) * (xAfter - xBefore);
            } else if (rocInterpolation == RocInterpolation.maximum) {
                auc += yValues.get(i + 1) * (xAfter - xBefore);
            } else if (rocInterpolation == RocInterpolation.linear) {
                double yBefore = yValues.get(i);
                double yAfter = yValues.get(i + 1);
                auc += ((yAfter + yBefore) / 2) * (xAfter - xBefore);
            } else {
                throw new UnsupportedOperationException("No AUC calculation implemented for ROC interpolation " + rocInterpolation + ".");
            }
        }
        return auc;
    }
}
