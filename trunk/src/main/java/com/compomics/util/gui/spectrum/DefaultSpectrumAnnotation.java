/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-jun-2004
 * Time: 13:04:56
 */
package com.compomics.util.gui.spectrum;
import org.apache.log4j.Logger;

import com.compomics.util.gui.interfaces.SpectrumAnnotation;

import java.awt.*;

/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2007/10/22 10:09:02 $
 */

/**
 * This class provideds a default implementation of the SpectrumAnnotation interface.
 * 
 * @author Lennart Martens
 * @version $Id: DefaultSpectrumAnnotation.java,v 1.1 2007/10/22 10:09:02 lennart Exp $
 */
public class DefaultSpectrumAnnotation implements SpectrumAnnotation {
	// Class specific log4j logger for DefaultSpectrumAnnotation instances.
	Logger logger = Logger.getLogger(DefaultSpectrumAnnotation.class);

    private Color iColor = null;

    private double iErrorMargin = 0.0;

    private double iMZ = 0.0;

    private String iLabel = null;

    public DefaultSpectrumAnnotation(double aMZ, double aErrorMargin, Color aColor, String aLabel) {
        this.iMZ = aMZ;
        this.iErrorMargin = aErrorMargin;
        this.iColor = aColor;
        this.iLabel = aLabel;
    }

    /**
     * This method returns the color for the annotation.
     *
     * @return Color with the color for the annotation.
     */
    public Color getColor() {
        return iColor;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This method returns the allowed error margin (both sides)
     * for the M/Z of the annotation (eg., 0.1 means an allowed
     * interval of [M/Z-0.1, M/Z+0.1].
     *
     * @return double  with the error margin.
     */
    public double getErrorMargin() {
        return iErrorMargin;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This method returns the label for the annotation.
     *
     * @return String with the label to display (above the M/Z)
     *         for this annotation.
     */
    public String getLabel() {
        return iLabel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This method returns the M/Z of the feature to annotate.
     *
     * @return double with the M/Z.
     */
    public double getMZ() {
        return iMZ;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
