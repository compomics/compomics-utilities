package com.compomics.util.gui.protein;

import com.compomics.util.Util;
import java.awt.event.MouseEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A panel for displaying modification profiles. (Based on the SequenceFragmentationPanel.)
 *
 * @author Harald Barsnes
 * @author Kenny Helsens
 * @author Lennart Martens
 */
public class SequenceModificationPanel extends JPanel {

    /**
     * A map of the rectangles used to draw each profile peak. This map is 
     * later used for the tooltip for each peak.
     */
    private HashMap<String, Rectangle> fragmentIonRectangles;
    /**
     * Elementary data for composing the Panel.
     */
    private String[] iSequenceComponents;
    /**
     * The list of modification profiles.
     */
    private ArrayList<ModificationProfile> profiles;
    /**
     * The font to use.
     */
    private Font iBaseFont = new Font("Monospaced", Font.PLAIN, 14);
    /**
     * The maximum bar height.
     */
    private final double iMaxBarHeight = 20;
    /**
     * The width of the bars.
     */
    private final int iBarWidth = 3;
    /**
     * The horizontal space.
     */
    private final int iHorizontalSpace = 3;
    /**
     * The x-axis start position.
     */
    private final int iXStart = 30;
    /**
     * The y-axis start position.
     */
    private final int iYStart = 10;
    /**
     * This boolean holds whether or not the given sequence is a modified 
     * sequence or a normal peptide sequence.
     *
     * Normal: KENNY
     * Modified: NH2-K<Ace>ENNY-COOH
     */
    private boolean isModifiedSequence;

    /**
     * Creates a new SequenceFragmentationPanel.
     *
     * @param aSequence                  String with the Modified Sequence of a peptide identification.
     * @param profiles                   ArrayList with the modification profiles.
     * @param boolModifiedSequence       boolean describing the sequence. This constructor can be used to enter a ModifiedSequence or a normal sequence.
     * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public SequenceModificationPanel(String aSequence, ArrayList<ModificationProfile> profiles, boolean boolModifiedSequence) throws HeadlessException {
        super();
        isModifiedSequence = boolModifiedSequence;
        iSequenceComponents = parseSequenceIntoComponents(aSequence);
        this.profiles = profiles;
        this.setPreferredSize(new Dimension(estimateWidth(), estimateHeight()));
        this.setMaximumSize(new Dimension(estimateWidth(), estimateHeight()));

        fragmentIonRectangles = new HashMap<String, Rectangle>();

        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseMoved(MouseEvent me) {
                mouseMovedHandler(me);
            }
        });
    }

    /**
     * Paints the SequenceModificationPanel.
     *
     * Based on the given ModifiedSequence Components and Modification profile, a visualisation 
     * is drawn on a Graphics object showing the profile above the sequence.
     *
     * @param g the specified Graphics window
     * @see java.awt.Component#update(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        // Set the base font, monospaced!
        g2.setFont(iBaseFont);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Drawing offsets.
        int yLocation = new Double(iMaxBarHeight).intValue() + iYStart;
        int xLocation = iXStart;

        int lFontHeight = g2.getFontMetrics().getHeight();
        Double lMidStringHeight = yLocation - lFontHeight * 0.2;
        Double aboveSequenceHeight = yLocation - lFontHeight * 0.5;
        Double belowSequenceHeight = yLocation + lFontHeight * 0.15;

        
        // find max a score
        double maxAScore = 0;
        
        for (int i = 0; i < iSequenceComponents.length; i++) {
            for (int j = 0; j < profiles.size(); j++) {
             
                ModificationProfile currentModificationProfile = profiles.get(j);
                
                if (maxAScore < currentModificationProfile.getProfile()[i][ModificationProfile.A_SCORE_ROW_INDEX]) {
                    maxAScore = currentModificationProfile.getProfile()[i][ModificationProfile.A_SCORE_ROW_INDEX];
                }
            }
        }
        
        
        for (int i = 0; i < iSequenceComponents.length; i++) {
            
            // reset base color to black.
            g2.setColor(Color.black);

            // Draw this amino acid.
            g2.setColor(Color.black);
            g2.drawString(iSequenceComponents[i], xLocation, yLocation);

            int tempXLocation = xLocation;

            // special case for modifications on the first reidue
            if (i == 0) {
                xLocation += g2.getFontMetrics().stringWidth(iSequenceComponents[i]) - g2.getFontMetrics().stringWidth("X");
            }
            
            // draw bars below the sequence
            for (int j = 0; j < profiles.size(); j++) {

                ModificationProfile currentModificationProfile = profiles.get(j);

                if (currentModificationProfile.getProfile()[i][ModificationProfile.A_SCORE_ROW_INDEX] > 0) {
                    g2.setColor(currentModificationProfile.getColor());

                    int lBarHeight = (new Double((currentModificationProfile.getProfile()[i][ModificationProfile.A_SCORE_ROW_INDEX] / maxAScore) * iMaxBarHeight).intValue());
                    if (lBarHeight < 5) {
                        lBarHeight = 7;
                    }

                    int barStart = belowSequenceHeight.intValue() + 1;

                    Rectangle tempRectangle = new Rectangle(xLocation+1, barStart, g2.getFontMetrics().stringWidth("X")-2, lBarHeight);

                    g2.fill(tempRectangle);
                    fragmentIonRectangles.put(currentModificationProfile.getPtmName() + " (" + (i + 1) + ")"
                            + " [a-score: " + Util.roundDouble(currentModificationProfile.getProfile()[i][ModificationProfile.A_SCORE_ROW_INDEX], 2) + "]",
                            tempRectangle);

                    g2.setColor(Color.black);
                }
            }

            // draw bars above the sequence
            for (int j = 0; j < profiles.size(); j++) {

                ModificationProfile currentModificationProfile = profiles.get(j);

                if (currentModificationProfile.getProfile()[i][ModificationProfile.DELTA_SCORE_ROW_INDEX] > 0) {
                    g2.setColor(currentModificationProfile.getColor());

                    int lBarHeight = (new Double((currentModificationProfile.getProfile()[i][ModificationProfile.DELTA_SCORE_ROW_INDEX] / 100) * iMaxBarHeight).intValue());
                    if (lBarHeight < 5) {
                        lBarHeight = 7;
                    }

                    int barStart = aboveSequenceHeight.intValue() - 2 - lBarHeight;
                    Rectangle tempRectangle = new Rectangle(xLocation+1, barStart, g2.getFontMetrics().stringWidth("X")-2, lBarHeight);
                    g2.fill(tempRectangle);
                    fragmentIonRectangles.put(currentModificationProfile.getPtmName() + " (" + (i + 1) + ")"
                            + " [d-score: " + Util.roundDouble(currentModificationProfile.getProfile()[i][ModificationProfile.DELTA_SCORE_ROW_INDEX], 2) + "]",
                            tempRectangle);

                    g2.setColor(Color.black);
                }
            }

            xLocation = tempXLocation;

            // Move the XLocation forwards with the component's length and the horizontal spacer..
            xLocation = xLocation + g2.getFontMetrics().stringWidth(iSequenceComponents[i]) + iHorizontalSpace;

            // Move the XLocation forwards with the component's length and the horizontal spacer..
            xLocation = xLocation + iBarWidth + iHorizontalSpace;
        }

        this.setPreferredSize(new Dimension(xLocation, 200));
    }

    /**
     * This method can parse a modified sequence String into a String[] with different components.
     *
     * @param aSequence String with the Modified sequence of a peptideHit.
     * @return the modified sequence of the peptidehit in a String[].
     *         Example:
     *         The peptide Ace-K<AceD3>ENNYR-COOH will return a String[] with
     *         [0]Ace-K<AceD3>
     *         [1]E
     *         [2]N
     *         [3]N
     *         [4]Y
     *         [5]R-COOH
     */
    private String[] parseSequenceIntoComponents(String aSequence) {

        String[] result;

        if (isModifiedSequence) {

            // Given sequence is a ModifiedSequence!
            ArrayList parts = new ArrayList();
            String temp = aSequence;
            int start = 0;

            if (temp.startsWith("#")) {
                int nterm = temp.indexOf("#", start + 1);
                start = temp.indexOf("-", nterm);
            } else {
                start = temp.indexOf("-");
            }

            start++;
            String part = temp.substring(0, start).trim();
            temp = temp.substring(start).trim();
            int endIndex = 1;

            if (temp.charAt(endIndex) == '<') {
                endIndex++;
                while (temp.charAt(endIndex) != '>') {
                    endIndex++;
                }
                endIndex++;
            }

            part += temp.substring(0, endIndex);
            temp = temp.substring(endIndex);
            parts.add(part);

            while (temp.length() > 0) {
                start = 0;
                endIndex = 1;
                if (temp.charAt(start + endIndex) == '<') {
                    endIndex++;
                    while (temp.charAt(start + endIndex) != '>') {
                        endIndex++;
                    }
                    endIndex++;
                }

                if (temp.charAt(start + endIndex) == '-') {
                    endIndex = temp.length();
                }

                part = temp.substring(0, endIndex);
                temp = temp.substring(endIndex);
                parts.add(part);
            }

            result = new String[parts.size()];
            parts.toArray(result);

        } else {
            // Given sequence is a flat sequence!
            result = new String[aSequence.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = Character.toString(aSequence.charAt(i));
            }
        }

        return result;
    }

    /**
     * Returns an estimation of the width.
     *
     * @return an estimation of the width
     */
    private int estimateWidth() {
        int lEstimateX = iXStart;

        for (int i = 0; i < iSequenceComponents.length; i++) {
            // Move X for a text component.
            lEstimateX += this.getFontMetrics(iBaseFont).stringWidth(iSequenceComponents[i]) + iHorizontalSpace;
            // Move the XLocation forwards with the component's length and the horizontal spacer.
            lEstimateX += iBarWidth + iHorizontalSpace;
        }

        lEstimateX += iXStart;
        return lEstimateX;
    }

    /**
     * Returns an estimation of the height.
     *
     * @return an estimation of the height
     */
    private int estimateHeight() {
        int lEstimateY = 0;
        lEstimateY += 2 * iXStart;
        lEstimateY += 1.8 * iMaxBarHeight;
        return lEstimateY;
    }

    /**
     * Set the Sequence for the SequenceFragmentationPanel.
     *
     * @param lSequence            String with peptide sequence.
     * @param boolModifiedSequence Boolean whether lSequence is a Modified Sequence
     *                             "NH2-K<Ace>ENNY-COOH" or a Flat Sequence "KENNY".
     */
    public void setSequence(String lSequence, boolean boolModifiedSequence) {
        isModifiedSequence = boolModifiedSequence;
        iSequenceComponents = parseSequenceIntoComponents(lSequence);
    }

    /**
     * If the mouse hovers over one of the fragment ion peaks the tooltip is 
     * set to the fragment ion type and number. If not the tooltip is set 
     * to null.
     */
    private void mouseMovedHandler(MouseEvent me) {

        String tooltip = null;

        Iterator<String> ions = fragmentIonRectangles.keySet().iterator();

        boolean matchFound = false;

        // iterate the peak rectangles and look for matches
        while (ions.hasNext() && !matchFound) {

            String key = ions.next();

            if (fragmentIonRectangles.get(key).contains(me.getPoint())) {
                tooltip = key;
                matchFound = true;
            }
        }

        this.setToolTipText(tooltip);
    }
}
