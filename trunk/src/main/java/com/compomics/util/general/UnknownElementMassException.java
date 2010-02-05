/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.general;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements an Exception, thrown when a MassCalc instance
 * is confronted with an element symbol it cannot retrieve in its 
 * element lists.
 *
 * @author	Lennart Martens
 */
public class UnknownElementMassException extends Exception {
	
	/**
	 * The element symbol that was not recognized.
	 */
	private String element = null;
	
	/**
	 * The constructor requires the caller to specify the element
	 * which was not recognized.
	 * 
	 * @param	aElement	String with the symbol of the
	 *						unrecognized element.
	 */
	public UnknownElementMassException(String aElement) {
		super("Unknown mass for element '" + aElement + "'.\n");
		element = aElement;
	}
	
	/**
	 * Simple getter for the element variable.
	 *
	 * @return	String	the symbol for the unknown element.
	 */
	public String getElement() {
		return element;
	}
}
