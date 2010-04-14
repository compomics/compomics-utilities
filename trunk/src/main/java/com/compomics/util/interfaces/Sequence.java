/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.interfaces;
import org.apache.log4j.Logger;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This interface defines the default behaviour of any kind of 
 * sequence with a certain mass - be it DNA, portein or something 
 * else.
 *
 * @author	Lennart Martens
 */
public interface Sequence {
	
	/**
	 * This method will set the sequence. <br />
	 * Note that most implementations will also allow you 
	 * to set this via the constructor.
	 *
	 * @param	aSequence	String with the sequence.
	 */
	public void setSequence(String aSequence);
	
	/**
	 * This method will retrieve the sequence.
	 *
	 * @return	String	with the sequence.
	 */
	public String getSequence();
	
	/**
	 * This method will return the mass for the sequence.
	 *
	 * @return	double	with the mass.
	 */
	public double getMass();

    /**
     * This method reports on the length of the current sequence.
     *
     * @return  int with the length of the sequence.
     */
    public int getLength();
}
