package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.awt.event.MouseEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class was imported from the Peptizer and MascotDatfile parser, and was
 * developed to display fragmentation information on the modified sequence as
 * inspired by X!Tandem.
 *
 * @author Kenny Helsens
 * @author Lennart Martens
 * @author Harald Barsnes
 */
public class SequenceFragmentationPanel extends JPanel {

    /**
     * A map of the rectangles that have tooltips, i.e., the fragment ion peaks
     * and the PTM highlighting.
     */
    private HashMap<String, Rectangle> tooltipRectangles;
    /**
     * Elementary data for composing the Panel.
     */
    private String[] iSequenceComponents;
    /**
     * The list of fragment ion matches.
     */
    private ArrayList<IonMatch> iIonMatches;
    /**
     * Double array on b-ions for the sequence components. If '0', no
     * corresponding ions were given for the component. Otherwise, a double
     * between [0:1] is stored in the array that is relative with the intensity
     * of the most intense fragment ion.
     */
    private double[] bIons;
    /**
     * Double array on y-ions for the sequence components. If '0', no
     * corresponding ions were given for the component. Otherwise, a double
     * between [0:1] is stored in the array that is relative with the intensity
     * of the most intense fragment ion.
     */
    private double[] yIons;
    /**
     * The font to use.
     */
    private Font iBaseFont = new Font("Monospaced", Font.PLAIN, 14);
    /**
     * The maximum bar height.
     */
    private final double iMaxBarHeight = 40;
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
    private final int iXStart = 10;
    /**
     * This boolean holds whether or not the given sequence is a modified
     * sequence or a normal peptide sequence.
     *
     * Normal: KENNY Modified: NH2-K&lt;Ace&gt;ENNY-COOH
     */
    private boolean isModifiedSequence;
    /**
     * If true the modification are highlighted with a background color.
     */
    private boolean iHighlightModifications;
    /**
     * The modification profile.
     */
    private PtmSettings modificationProfile;
    /**
     * the forward ion type (for instance B ion) as indexed by the
     * PeptideFragmentIon static fields
     */
    private int forwardIon;
    /**
     * the rewind ion type (for instance B ion) as indexed by the
     * PeptideFragmentIon static fields
     */
    private int rewindIon;
    /**
     * Color for the forward ion
     */
    private Color forwardColor;
    /**
     * Color for the rewind ion
     */
    private Color rewindColor;

    /**
     * Creates a new SequenceFragmentationPanel.
     *
     * @deprecated use the panel with ion selection instead
     * @param aSequence String with the Modified Sequence of an peptide
     * identification.
     * @param aIonMatches ArrayList with Fragmentation ion matches.
     * @param boolModifiedSequence boolean describing the sequence. This
     * constructor can be used to enter a ModifiedSequence or a normal sequence.
     * @param aHighlightModifications boolean decides whether the modification
     * are highlighted by adding a star above the modified residue instead if
     * displaying the PTM short name
     * @param modificationProfile the modification profile
     * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public SequenceFragmentationPanel(String aSequence, ArrayList<IonMatch> aIonMatches, boolean boolModifiedSequence,
            boolean aHighlightModifications, PtmSettings modificationProfile) throws HeadlessException {
        this(aSequence, aIonMatches, boolModifiedSequence, aHighlightModifications, modificationProfile, PeptideFragmentIon.B_ION, PeptideFragmentIon.Y_ION);
    }

    /**
     * Creates a new SequenceFragmentationPanel working with B and Y ions.
     *
     * @param aSequence String with the Modified Sequence of an peptide
     * identification.
     * @param aIonMatches ArrayList with Fragmentation ion matches.
     * @param boolModifiedSequence boolean describing the sequence. This
     * constructor can be used to enter a ModifiedSequence or a normal sequence.
     * @param aHighlightModifications boolean decides whether the modification
     * are highlighted by adding a star above the modified residue instead if
     * displaying the PTM short name
     * @param modificationProfile the modification profile
     * @param forwardIon the forward ion type (for instance B ion) as indexed by
     * the PeptideFragmentIon static fields
     * @param rewindIon the rewind ion type (for instance Y ion) as indexed by
     * the PeptideFragmentIon static fields
     * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public SequenceFragmentationPanel(String aSequence, ArrayList<IonMatch> aIonMatches, boolean boolModifiedSequence,
            boolean aHighlightModifications, PtmSettings modificationProfile, int forwardIon, int rewindIon) throws HeadlessException {
        super();

        this.forwardIon = forwardIon;
        forwardColor = SpectrumPanel.determineFragmentIonColor(Ion.getGenericIon(Ion.IonType.PEPTIDE_FRAGMENT_ION, forwardIon), false);
        this.rewindIon = rewindIon;
        rewindColor = SpectrumPanel.determineFragmentIonColor(Ion.getGenericIon(Ion.IonType.PEPTIDE_FRAGMENT_ION, rewindIon), false);

        this.modificationProfile = modificationProfile;
        isModifiedSequence = boolModifiedSequence;
        iSequenceComponents = parseSequenceIntoComponents(aSequence);
        iIonMatches = aIonMatches;
        iHighlightModifications = aHighlightModifications;

        this.normalizeMatchedIons();
        this.setPreferredSize(new Dimension(estimateWidth(), estimateHeight()));

        tooltipRectangles = new HashMap<String, Rectangle>();

        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseMoved(MouseEvent me) {
                mouseMovedHandler(me);
            }
        });
    }
    
    /**
     * Creates a new SequenceFragmentationPanel working with B and Y ions.
     *
     * @param taggedModifiedSequence the tagged modified peptide sequence
     * @param aIonMatches ArrayList with Fragmentation ion matches.
     * @param aHighlightModifications boolean decides whether the modification
     * are highlighted by adding a star above the modified residue instead if
     * displaying the PTM short name
     * @param modificationProfile the modification profile
     * @param forwardIon the forward ion type (for instance B ion) as indexed by
     * the PeptideFragmentIon static fields
     * @param rewindIon the rewind ion type (for instance Y ion) as indexed by
     * the PeptideFragmentIon static fields
     * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public SequenceFragmentationPanel(String taggedModifiedSequence, ArrayList<IonMatch> aIonMatches, 
            boolean aHighlightModifications, PtmSettings modificationProfile, int forwardIon, int rewindIon) throws HeadlessException {
        super();

        this.forwardIon = forwardIon;
        forwardColor = SpectrumPanel.determineFragmentIonColor(Ion.getGenericIon(Ion.IonType.PEPTIDE_FRAGMENT_ION, forwardIon), false);
        this.rewindIon = rewindIon;
        rewindColor = SpectrumPanel.determineFragmentIonColor(Ion.getGenericIon(Ion.IonType.PEPTIDE_FRAGMENT_ION, rewindIon), false);

        this.modificationProfile = modificationProfile;
        isModifiedSequence = true;
        iSequenceComponents = parseSequenceIntoComponents(taggedModifiedSequence);
        iIonMatches = aIonMatches;
        iHighlightModifications = aHighlightModifications;

        this.normalizeMatchedIons();
        this.setPreferredSize(new Dimension(estimateWidth(), estimateHeight()));

        tooltipRectangles = new HashMap<String, Rectangle>();

        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseMoved(MouseEvent me) {
                mouseMovedHandler(me);
            }
        });
    }

    /**
     * Paints the SequenceFragmentationPanel.
     *
     * Based on the given ModifiedSequence Components and Fragmentions, a
     * visualization (inspired by X!Tandem) is drawn on a Graphics object. Next
     * to every possible fragmentation site of the peptide a bar is drawn whether
     * b or y ions were found originating from this fragmentation side.
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
        int yLocation = ((int) iMaxBarHeight) + iXStart;
        int xLocation = iXStart;

        int lFontHeight = g2.getFontMetrics().getHeight();
        Double lMidStringHeight = yLocation - lFontHeight * 0.2;

        for (int i = 0; i < iSequenceComponents.length; i++) {
            // reset base color to black.
            g2.setColor(Color.black);

            /**
             * A. Draw the component. --------------------
             */
            String residue = iSequenceComponents[i];
            String modification = "";

            // check if it's a modified sequence
            boolean modified = residue.contains("<");

            // remove the modification from the residue
            if (modified && iHighlightModifications) {
                modification = residue.substring(residue.indexOf("<"), residue.lastIndexOf(">") + 1);
                residue = residue.substring(0, residue.indexOf("<")) + residue.substring(residue.lastIndexOf(">") + 1);
            }

            // if modified, highlight the modification if highlighting is selected
            if (modified) {

                Color color = modificationProfile.getColor(modification); // not that this mapping works on the short name while the proper mapping is on the long name...
                if (color == null) {
                    color = forwardColor;
                }
                g2.setColor(color);

                String ptmName = modification.substring(1, modification.length() - 1); // remove the start and end tags

                if (i == 0) {
                    String nTerminal = residue.substring(0, residue.length() - 1);
                    Rectangle tempRectangle = new Rectangle(xLocation - 1 + g2.getFontMetrics().stringWidth(nTerminal), yLocation - (g2.getFontMetrics().getHeight() / 2) - 1,
                            g2.getFontMetrics().stringWidth(residue.substring(residue.length() - 1)) + 2, (g2.getFontMetrics().getHeight() / 2) + 4);
                    g2.fill(tempRectangle);
                    tooltipRectangles.put("<html>" + ptmName + " (" + (i + 1) + ")</html>", tempRectangle);
                    g2.setColor(Color.BLACK);
                    g2.drawString(nTerminal, xLocation, yLocation);
                    g2.setColor(Color.WHITE);
                    g2.drawString(residue.substring(residue.length() - 1), xLocation + g2.getFontMetrics().stringWidth(nTerminal), yLocation);
                    g2.setColor(Color.BLACK);
                } else if (i == iSequenceComponents.length - 1) {
                    Rectangle tempRectangle = new Rectangle(xLocation - 1, yLocation - (g2.getFontMetrics().getHeight() / 2) - 1,
                            g2.getFontMetrics().stringWidth(residue.substring(0, 1)) + 2, (g2.getFontMetrics().getHeight() / 2) + 4);
                    g2.fill(tempRectangle);
                    tooltipRectangles.put("<html>" + ptmName + " (" + (i + 1) + ")</html>", tempRectangle);
                    g2.setColor(Color.WHITE);
                    g2.drawString(residue.substring(0, 1), xLocation, yLocation);
                    g2.setColor(Color.BLACK);
                    g2.drawString(residue.substring(1), xLocation + g2.getFontMetrics().stringWidth(residue.substring(0, 1)), yLocation);
                } else {
                    Rectangle tempRectangle = new Rectangle(xLocation - 1, yLocation - (g2.getFontMetrics().getHeight() / 2) - 1,
                            g2.getFontMetrics().stringWidth(residue) + 2, (g2.getFontMetrics().getHeight() / 2) + 4);
                    g2.fill(tempRectangle);
                    tooltipRectangles.put("<html>" + ptmName + " (" + (i + 1) + ")</html>", tempRectangle);
                    g2.setColor(Color.WHITE);
                    g2.drawString(residue, xLocation, yLocation);
                    g2.setColor(Color.BLACK);
                }
            } else {
                // Draw this component.
                g2.drawString(residue, xLocation, yLocation);
            }

            // Move the XLocation forwards with the component's length and the horizontal spacer..
            xLocation += g2.getFontMetrics().stringWidth(residue) + iHorizontalSpace;

            /**
             * B. Draw the bars. --------------------
             */
            int lBarHeight;
            // bIon Bar
            if (i <= bIons.length - 1) {
                if (bIons[i] != 0) {
                    lBarHeight = (int) (bIons[i] * iMaxBarHeight);
                    if (lBarHeight < 5) {
                        lBarHeight = 7;
                    }
                    g2.setColor(forwardColor);
                    Rectangle tempRectangle = new Rectangle(xLocation, lMidStringHeight.intValue() + 1, iBarWidth, lBarHeight);
                    g2.fill(tempRectangle);

                    tooltipRectangles.put("<html>" + PeptideFragmentIon.getSubTypeAsString(forwardIon) + "<sub>" + (i + 1) + "</sub></html>", tempRectangle);
                }
            }

            // yIon Bar
            if (i <= yIons.length - 1) {
                if (yIons[yIons.length - (i + 1)] != 0) {
                    lBarHeight = (int) (yIons[yIons.length - (i + 1)] * iMaxBarHeight);
                    if (lBarHeight < 5) {
                        lBarHeight = 7;
                    }
                    g2.setColor(rewindColor);
                    // y bar height and y-axis start are somewhat different for yIons.
                    int yBarStart = lMidStringHeight.intValue() - 1 - lBarHeight;
                    Rectangle tempRectangle = new Rectangle(xLocation, yBarStart, iBarWidth, lBarHeight);
                    g2.fill(tempRectangle);

                    tooltipRectangles.put("<html>" + PeptideFragmentIon.getSubTypeAsString(rewindIon) + "<sub>" + (yIons.length - i) + "</sub></html>", tempRectangle);
                }
            }

            // Move the XLocation forwards with the component's length and the horizontal spacer..
            xLocation = xLocation + iBarWidth + iHorizontalSpace;
        }

        this.setPreferredSize(new Dimension(xLocation, 200));
    }

    /**
     * This method can parse a modified sequence String into a String[] with
     * different components. Primitive analog to getModifiedSequenceComponents()
     * on a peptidehit.
     *
     * @param aSequence String with the Modified sequence of a peptideHit.
     * @return the modified sequence of the peptidehit in a String[]. Example:
     * The peptide Ace-K&lt;AceD3&gt;ENNYR-COOH will return a String[] with
     * [0]Ace-K&lt;AceD3&gt; [1]E [2]N [3]N [4]Y [5]R-COOH
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

        ArrayList<String> unmodifiedString = new ArrayList<String>();

        // remove the ptms, e.g., <oxidation>, as these will not be shown anyway
        for (String residue : iSequenceComponents) {
            // check if it's a modified sequence
            boolean modified = residue.contains("<");

            // remove the modification from the residue
            if (modified) {
                residue = residue.substring(0, residue.indexOf("<")) + residue.substring(residue.lastIndexOf(">") + 1);
            }

            unmodifiedString.add(residue);
        }

        for (String temp : unmodifiedString) {
            // Move X for a text component.
            lEstimateX += this.getFontMetrics(iBaseFont).stringWidth(temp) + iHorizontalSpace;
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
     * Build the normalized intensity indexes for the parts of the modified
     * sequence that were covered by fragment ions.
     */
    private void normalizeMatchedIons() {

        // Create Y and B boolean arrays.
        bIons = new double[iSequenceComponents.length - 1];
        yIons = new double[iSequenceComponents.length - 1];

        // Dig up the most intense matched ion.
        double lMaxIntensity = 0.0;
        for (IonMatch lMatch : iIonMatches) {
            if (lMaxIntensity < lMatch.peak.intensity) {
                lMaxIntensity = lMatch.peak.intensity;
            }
        }

        for (IonMatch lMatch : iIonMatches) {
            if (lMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                double lRatio = lMatch.peak.intensity / lMaxIntensity;
                PeptideFragmentIon lFragmentIon = (PeptideFragmentIon) lMatch.ion;
                if (lFragmentIon.getSubType() == rewindIon) {
                    // If array unit is not '0', another ion for this fragmentation site is already found.
                    if (yIons[lFragmentIon.getNumber() - 1] != 0) {
                        // We want to save the most intense.
                        if (yIons[lFragmentIon.getNumber() - 1] > lRatio) {
                            // Reset lRatio to the most intense.
                            lRatio = yIons[lFragmentIon.getNumber() - 1];
                        }
                    }
                    yIons[lFragmentIon.getNumber() - 1] = lRatio;
                } else if (lFragmentIon.getSubType() == forwardIon) {
                    if (bIons[lFragmentIon.getNumber() - 1] != 0) {
                        if (bIons[lFragmentIon.getNumber() - 1] > lRatio) {
                            lRatio = bIons[lFragmentIon.getNumber() - 1];
                        }
                    }
                    bIons[lFragmentIon.getNumber() - 1] = lRatio;
                }
            }
        }
    }

    /**
     * Set the Sequence for the SequenceFragmentationPanel.
     *
     * @param lSequence String with peptide sequence.
     * @param boolModifiedSequence Boolean whether lSequence is a Modified
     * Sequence "NH2-K&lt;Ace&gt;ENNY-COOH" or a Flat Sequence "KENNY".
     */
    public void setSequence(String lSequence, boolean boolModifiedSequence) {
        isModifiedSequence = boolModifiedSequence;
        iSequenceComponents = parseSequenceIntoComponents(lSequence);
    }

    /**
     * Set the ArrayList with FragmentIon matches. The double[] indexing b and y
     * ion intensities will be recalculated.
     *
     * @param lIonMatches ArrayList
     */
    public void setIonMatches(ArrayList lIonMatches) {
        iIonMatches = lIonMatches;
        normalizeMatchedIons();
    }

    /**
     * If the mouse hovers over one of the fragment ion peaks the tooltip is set
     * to the fragment ion type and number. And if hovering over a modified
     * residue the modification name is shown. If not the tooltip is set to
     * null.
     */
    private void mouseMovedHandler(MouseEvent me) {

        String tooltip = null;

        Iterator<String> rectangles = tooltipRectangles.keySet().iterator();

        boolean matchFound = false;

        // iterate the rectangles and look for matches
        while (rectangles.hasNext() && !matchFound) {

            String key = rectangles.next();

            if (tooltipRectangles.get(key).contains(me.getPoint())) {
                tooltip = key;
                matchFound = true;
            }
        }

        this.setToolTipText(tooltip);
    }
}
