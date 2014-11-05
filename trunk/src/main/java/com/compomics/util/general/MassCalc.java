/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */
package com.compomics.util.general;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.text.StringCharacterIterator;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:41:53 $
 */
/**
 * This class takes care of mass calculation, based on a sequence 
 * in IUPAC format. <br>
 * By default it can handle monoisotopic biochemical element masses
 * and monoisotopic single-letter amino acid masses. <br>
 * The object can be customized with your own mass lists through the
 * use of a properties file, or by directly passing a HashMap.
 * <br><br>
 * <b>Please note:</b> if you decide on your own lists, be sure to
 *	follow the following guidelines:
 * <ul>
 *  <li>
 *    An element (key in properties file or HashMap) can be ONE or TWO
 *    letters. The first ALWAYS has to be UPPERCASE, the optional second 
 *    ALWAYS has to be <i>lowercase</i>.
 *  </li>
 *  <li>
 *    A value has to be a parseable number for a properties file, or
 *    a Double instance for the HashMap. No other formats are accepted!
 *  </li>
 * </ul>
 *
 * @author	Lennart Martens
 */
public class MassCalc {

    // Class specific log4j logger for MassCalc instances.
    static Logger logger = Logger.getLogger(MassCalc.class);

    /**
     * This variable highlights which map was chosen for the element masses.
     */
    private int iChosen = -1;

    /**
     * This hash will contain all the masses for the currently selected
     * element list.
     */
    private HashMap masses = null;

    /**
     * This Vector stores all the HashMaps that can be selected as element
     * lists. <br>
     * Please only access them via the proper index, as defined in static vars
     * on this class.
     */
    private static Vector allMaps = new Vector(2);

    /**
     * An index into an array.
     */
    private static final int ELEMENT = 0;

    /**
     * An index into an array.
     */
    private static final int MULTIPLICITY = 1;

    /**
     * Index for the monoisotopic masses of the biochemically relevant
     * elements.
     */
    public static final int MONOELEMENTS = 0;

    /**
     * Index for the monoisotopic aminoacid masses.
     */
    public static final int MONOAA = 1;

    /**
     * Index for the monoisotopic nucleotide masses.
     */
    public static final int MONONUCLEOTIDES = 2;

    /**
     * Value for the self-defined masses.
     */
    public static final int SELFDEFINED = -1;

    /**
     * Default constructor. The mass list to be used defaults to the
     * monoisotopic masses for biochemically relevant elements.
     */
    public MassCalc() {
        this(MONOELEMENTS);
    }

    /**
     * This constructor allows you to specify an identifier to select a
     * element list to use for calculating a mass. <br>
     * Please use the finalo vriables on this class as identifiers!
     *
     * @param	aMassListIdentifier	int with the identifier for the
     *								elementlist to use.
     */
    public MassCalc(int aMassListIdentifier) {
        if (allMaps.isEmpty()) {
            allMaps.add(MONOELEMENTS, this.loadMassesFromPropFile("MonoElementMasses.properties"));
            allMaps.add(MONOAA, this.loadMassesFromPropFile("MonoAAMasses.properties"));
            allMaps.add(MONONUCLEOTIDES, this.loadMassesFromPropFile("MonoNucleotideMasses.properties"));
        }
        if (aMassListIdentifier > allMaps.size()) {
            throw new IllegalArgumentException("No such elementlist defined (" + aMassListIdentifier + ").\n");
        }
        masses = (HashMap) ((HashMap) allMaps.elementAt(aMassListIdentifier)).clone();
        iChosen = aMassListIdentifier;
    }

    /**
     * This constructor allows the caller to use an elementlist of
     * its own making. Simply passing the filename of the file suffices. <br>
     *
     * @param	aFilename	String with the name of the file to be loaded.
     *						<b>NOTE!</b> this file must be located in the
     *						classpath and mst be a simple properties file!
     */
    public MassCalc(String aFilename) {
        masses = this.loadMassesFromPropFile(aFilename);
    }

    /**
     * This constructor allows the caller to initialize the elementlist
     * with a HashMap of its own design. <br>
     * This HashMap needs be structured in the following way: <br>
     * <ul>
     *  <li>KEY can be one or two letters, the first has to be uppercase,
     *    the second and optional letter has to be lowercase.</li>
     *  <li>VALUE must be a Double value</li>
     * </ul>
     *
     * @param	aElementList	HashMap with the elementlist to use.
     */
    public MassCalc(HashMap aElementList) {
        masses = aElementList;
    }

    /**
     * This constructor allows the caller to supplement (or to replace
     * elements in) a built-in elementlist with a HashMap of its own
     * design. <br>
     * This HashMap needs be structured in the following way: <br>
     * <ul>
     *  <li>KEY can be one or two letters, the first has to be uppercase,
     *    the second and optional letter has to be lowercase.</li>
     *  <li>VALUE must be a Double value</li>
     * </ul>
     *
     * @param	aMassListIdentifier	int with the identifier for the
     *								built-in elementlist to use.
     * @param	aSupplElementList	HashMap with the supplementary
     *								elementlist to use.
     */
    public MassCalc(int aMassListIdentifier, HashMap aSupplElementList) {
        this(aMassListIdentifier);
        this.masses.putAll(aSupplElementList);
    }

    /**
     * This method attempts to calculate the mass of a chemical formula.
     * It cannot calculate the mass of an element if it is not known to
     * this class (i.e.: if it does not occur in the Properties instance).
     * In that case it will flag an exception.
     *
     * @param	aFormula	String with the chemical formula (or bruto
     *						formula) of the compound in question.
     * @return	double	with the mass of the compound.
     * @exception	UnknownElementMassException	when one of the composing elements'
     *											mass is unknown to the class.
     */
    public double calculateMass(String aFormula) throws UnknownElementMassException {
        // Implemented with a character iterator.
        StringCharacterIterator sci = new StringCharacterIterator(aFormula);

        // The mass we so hungrily crave.
        double mass = 0.0;

        // A HashMap for the Bruto formula.
        HashMap bruto = new HashMap(sci.getEndIndex());

        // Now cycle the iterator.
        char currentChar;
        // Note that iterator advancement is taken care of in the
        // processing methods called form this one!
        while ((currentChar = sci.current()) != StringCharacterIterator.DONE) {
            // For this character, there are only TWO possibilities:
            // either it is a letter, or it is a opening bracket.
            // If it is a letter, we forward to the getElement() method,
            // else we forward it to the getInnerFormula method.
            // The getElement returns an array with two elements: the
            // element and its multiplicity (which can be '1' or more).
            // Indexes are defined in the final variables ELEMENT and
            // MULTIPLICITY.
            // The getInnerFormula just returns a mass.
            if (Character.isLetter(currentChar) || ('_' == currentChar) || ('*' == currentChar)) {
                Object[] result = this.getElement(sci);
                // this method takes care of adding this stuff to the
                // brutoformula.
                // Just to prevent code clutter.
                this.addResultToBrutoFormula((String) result[ELEMENT],
                        ((Integer) result[MULTIPLICITY]).intValue(),
                        bruto);
            } else if (currentChar == '(') {
                mass += this.getInnerFormulaMass(sci);
            } else {
                // This means: no letter and no bracket.
                // It has got to be an error.
                throw new IllegalArgumentException("Formula '" + aFormula
                        + "' could not be parsed due to the following unrecognized character: '" + currentChar + "'!\n");
            }
        }

        // We have a part of the mass (at least, if any inner formulae were
        // present) and the bruto formula for the remainder of the elements.
        // We'll cycle it and add all masses.
        Iterator iter = bruto.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (masses.containsKey(key)) {
                double tempMass = ((Double) masses.get(key)).doubleValue();
                int multiplicity = ((Integer) bruto.get(key)).intValue();
                mass += multiplicity * tempMass;
            } else {
                // Oooops! Unknown element! Flag an UnknownElementMassException
                // and be done with it.
                throw new UnknownElementMassException(key);
            }
        }

        // If the sequence is somehow connected to AAmasses,
        // we should add the mass of H2O!
        MassCalc innerMC = new MassCalc(MassCalc.MONOELEMENTS);
        if (iChosen == MONOAA) {

            //mass += 18.010565;
            mass += innerMC.calculateMass("H2O");
        } else if (iChosen == MONONUCLEOTIDES) {
            // For nucleotides, add hydrogen (for 5') and subtract PO2 (for 3').
            mass += innerMC.calculateMass("H");
            mass -= innerMC.calculateMass("PO2");
        }

        // This should be it.
        return mass;
    }

    /**
     * This method adds the element with given multiplicity to the
     * HashMap. the element as the key, multiplicity as the value.
     * If the element was already present as a key, the multiplicity
     * is added to the existing multiplicity.
     *
     * @param	aElement	String with the element symbol.
     * @param	aMultiplicity	int with the multiplicity of the element.
     * @param	aBruto	HashMap with the bruto formula to add the element and
     *					multiplicity to.
     */
    private void addResultToBrutoFormula(String aElement, int aMultiplicity, HashMap aBruto) {
        if (aBruto.containsKey(aElement)) {
            // Add the multiplicity to the existing value.
            int tempValue = ((Integer) aBruto.get(aElement)).intValue();
            tempValue += aMultiplicity;
            aBruto.put(aElement, Integer.valueOf(tempValue));
        } else {
            // Not yet there, simply insert it.
            aBruto.put(aElement, Integer.valueOf(aMultiplicity));
        }
    }

    /**
     * This method will read an element symbol and it's multiplicity from an
     * SCI.
     *
     * @param	aSCI	StringCharacterIterator to read from.
     * @return	Object[]	with the element behind index ELEMENT and the
     *						multiplicity behind index MULTIPLICITY.
     */
    private Object[] getElement(StringCharacterIterator aSCI) {
        Object[] result = new Object[2];

        // Okay, we'll need to find out the element name.
        // It can consist of one or two letters, the second
        // being lowercase if present.
        String element = Character.toString(aSCI.current());
        int multiplicity = 1;

        // First of all, check whether there IS a next (the element
        // could well be the last in line, in which case multiplicity is 1
        // and we're done!
        char next = aSCI.next();
        if (next == StringCharacterIterator.DONE) {
            // We don't do anything else here.
        } else {
            // Check if the next char is a lowercase letter.
            if (Character.isLetter(next) && Character.isLowerCase(next)) {
                // Add the second char to the element String and
                // move the position one step further.
                element += Character.toString(next);
                next = aSCI.next();
            } else if (next == '<') {
                // It's the start of a modification tag.
                // Let's grab it and add it!
                element += this.isolateInnerPartString(aSCI, '<', '>', true);
            }

            // Now we can check multiplicity.
            // This is only necessary if the next char is a number, else
            // we'll just set it to '1'.
            if ((next != StringCharacterIterator.DONE) && Character.isDigit(next)) {
                multiplicity = this.getMultiplicity(aSCI);
            } else {
                // Just set to one.
                multiplicity = 1;
            }
        }

        // Voila.
        result[ELEMENT] = element;
        result[MULTIPLICITY] = Integer.valueOf(multiplicity);

        return result;
    }

    /**
     * This method attempts to read a multiplicity starting from the current
     * position in the SCI parameter. The position will be moved such that
     * calling next on the iterator results in getting the first non-numerical
     * character to follow the multiplicity.
     *
     * @param	aSCI	StringCharacterIterator to read from.
     * @return	int	the multiplicity.
     */
    private int getMultiplicity(StringCharacterIterator aSCI) {
        int mp = 0;

        // If the current char is not a number, multiplicity is simply
        // '1'.
        if (!Character.isDigit(aSCI.current())) {
            mp = 1;
        } else {
            // The current char is the first of the number.
            String number = Character.toString(aSCI.current());

            // Fence-post.
            char next = aSCI.next();
            // Get all digits constructing the number.
            while ((next != StringCharacterIterator.DONE) && Character.isDigit(next)) {

                // Add it to the number.
                number += Character.toString(next);

                // Increment.
                next = aSCI.next();
            }

            // Convert the number into an int.
            mp = Integer.parseInt(number);
        }

        // Voila.
        return mp;
    }

    /**
     * This method will isolate and calculate the mass for the inner formula
     * presented here, starting from the current position in the aSCI.
     * This means that a call to current yields an opening bracket.
     * The position of the SCI will be moved to the closing bracket of
     * the inner formula.
     *
     * @param	aSCI	StringCharacterIterator with the inner formula.
     * @return	double	with the mass of the inner formula.
     */
    private double getInnerFormulaMass(StringCharacterIterator aSCI) throws UnknownElementMassException {

        int multiplicity;

        // Isolate inner formula String.
        String inner = this.isolateInnerPartString(aSCI, '(', ')', false);

        // Calculate mass.
        double mass = this.calculateMass(inner);

        // Get multiplicity.
        multiplicity = this.getMultiplicity(aSCI);

        // Calculate result.
        mass *= multiplicity;

        return mass;
    }

    /**
     * This method will isolate an inner part, if the SCI is
     * currently positioned on the opening token of that inner
     * part. <br />
     * It also allows for nested inner parts!
     *
     * @param	aSCI	StringCharacterIterator from which to read the
     *					inner formula.
     * @param	aOpener	char with the opening token for the inner part.
     * @param	aCloser	char with the closing token for the inner part.
     * @param	aKeepTokens	boolean that indicates whether the tokens
     *						should be included in the return String.
     * @return	String	with the inner formula.
     */
    private String isolateInnerPartString(StringCharacterIterator aSCI, char aOpener, char aCloser, boolean aKeepTokens) {
        // The String which we'll return.
        String innerFormula = "";

        // We will count tokens...
        int tokenCount = 1;

        // Current character is opening token and can and will be ignored.
        char next = aSCI.next();
        // This position is also the starting position for our String to be.
        int startPosition = aSCI.getIndex();
        // This one is derived from the logic within the loop below.
        int endPosition = -1;

        // Now to count tokens. Opening token adds 1 to the counter,
        // closing token subtracts 1. If the counter reaches zero, we've
        // found the end of our innerFormula.
        while (tokenCount > 0) {

            if (next == aOpener) {
                // Opening token, add one to counter.
                tokenCount++;
            } else if (next == aCloser) {
                // Closing token, subtract one from counter.
                tokenCount--;
            }

            // Advance one character.
            next = aSCI.next();
        }

        // Okay, end found. We'll have to retrieve it's position, 'though.
        // It's position is NOT the current, since that is BEYOND the last
        // closing token. It is not one before that, since that would be
        // the closing token itself.
        // We need the position 2 before the current.
        // BTW: this int is to reset to the current position when we're done.
        int imPosition = aSCI.getIndex();
        endPosition = imPosition - 2;

        // Construct the inner formula from the characters starting at
        // 'startPosition' and ending with 'endPosition'. Note that both are
        // inclusive.
        for (int i = startPosition; i <= endPosition; i++) {
            // Set the index for the char to retrieve.
            aSCI.setIndex(i);
            // Get the current char and append it to the String.
            innerFormula += Character.valueOf(aSCI.current());
        }

        // Reset the index on the SCI to the correct endposition (which is
        // just after the last closing bracket, btw).
        // We've stored that position in 'imPosition'.
        aSCI.setIndex(imPosition);

        // All done.
        return ((aKeepTokens ? Character.valueOf(aOpener) : "")
                + innerFormula + (aKeepTokens ? Character.toString(aCloser) : ""));
    }

    /**
     * This method loads a properties file and creates a HashMap
     * from this file where elements are keys and values are the
     * masses for the elements. <br />
     * It is assumed (and thus <i>necessary</i>) that these files are located
     * <b>in the classpath</b>.
     *
     * @param	aFilename	String with the name of the file.
     * @return	HashMap	with the key-value pairs (element - mass).
     */
    private HashMap loadMassesFromPropFile(String aFilename) {
        Properties tMasses = new Properties();
        HashMap lMasses = new HashMap();
        try {
            // Load the monoisotopic masses for elements file.
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(aFilename);
            if (is == null) {
                throw new IOException();
            }
            // Initialize the properties.
            tMasses.load(is);
            // Transform the values from Strings into doubles.
            Iterator iter = tMasses.keySet().iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                lMasses.put(o, new Double((String) tMasses.get(o)));
            }

        } catch (IOException ioe) {
            logger.error("\n**********************\nUnable to load file '" + aFilename + "' from the classpath.");
            logger.error("All mass calculations based on these masses will throw Exceptions!\n**********************\n");
        }

        return lMasses;
    }

    // For easy access.
    /**
     * The main method can be used for command-line usage of this class.
     * The parameters should be (a) chemical (or bruto) formula(e) to
     * calculate the mass for.
     *
     * @param	args String[]	at least one chemical formula should be specified,
     *						up to as much as you can cramp into a single
     *						command-line.
     */
    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            logger.error("\nUsage: MassCalc [-a|n] <formula1> [<formula2> ...]\n");
        } else {
            int start = 0;
            int elementlist = MassCalc.MONOELEMENTS;
            if (args[0].equals("-a")) {
                start = 1;
                elementlist = MassCalc.MONOAA;
                if (args.length < 2) {
                    logger.error("\nUsage: MassCalc [-a] <formula1> [<formula2> ...]\n");
                }
            } else if (args[0].equals("-n")) {
                start = 1;
                elementlist = MassCalc.MONONUCLEOTIDES;
                if (args.length < 2) {
                    logger.error("\nUsage: MassCalc [-n] <formula1> [<formula2> ...]\n");
                }
            }
            MassCalc mc = new MassCalc(elementlist);

            try {
                for (int i = start; i < args.length; i++) {
                    logger.info("\nMass for '" + args[i] + "': " + mc.calculateMass(args[i]) + ".");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
