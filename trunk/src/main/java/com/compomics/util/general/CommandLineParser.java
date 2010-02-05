/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.general;


import java.util.HashMap;
import java.util.ArrayList;


/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class provides a generic interface for the parsing of 
 * command-line arguments, options and flags. <br />
 * Arguments are 'stand-alone' Strings, options are preceded
 * by '--' without spaces (like in general GNU practice) and
 * flags are indicated by '-' without spaces. <br />
 * If there are options that in turn take arguments,
 * these can be specified via a specific constructor.
 *
 * @author	Lennart Martens
 */
public class CommandLineParser {
	
	/**
	 * The command line all parsing will take place on.
	 */
	private String[] iCommandLine = null;
	
	/**
	 * This boolean indicates whether any arguments were present at all.
	 */
	private boolean iHasArgs = false;

	/**
	 * This String[] will hold all the flags.
	 */
	private String[] iFlags = null;

	/**
	 * This String[] will hold all the options.
	 */
	private String[] iOptions = null;
	
	/**
	 * This String[] will hold all the parameters.
	 */
	private String[] iParams = null;
	
	/**
	 * This String[] will hold all the parameters for those options 
	 * that take them. <br />
	 * Structure is: key->option, value->param for that option. <br />
	 * <i>Please note</i> that this HashMap is only initialized through
	 * the use of the <a href="#optionConstr">constructor</a> which takes 
	 * a String[] with options that take arguments themselves!
	 */
	private HashMap iOptionParams = null;
	
	/**
	 * The constructor requires the caller to provide it with
	 * a command line arguments String[] that will be the 
	 * basis of the parsing.
	 *
	 * @param	aCommandLine	String[] with the command-line arguments.
	 */
	public CommandLineParser(String[] aCommandLine) {
		this(aCommandLine, null);
	}
	
	/**
	 * <a name="optionConstr" />
	 * This constructor requests the command-line String[] as well as
	 * a String[] with a list of options which in turn take
	 * a parameter.
	 *
	 * @param	aCommandLine	String[] with the command-line arguments.
	 * @param	aOptionArgs	String[] with the options that take 
	 *						parameters themselves.
	 */
	public CommandLineParser(String[] aCommandLine, String[] aOptionArgs) {
		if(aCommandLine != null) {
            this.iCommandLine = (String[])aCommandLine.clone();
        }
		// Add all the options that take a parameter to the
		// HashMap.
		iOptionParams = new HashMap();
		// See if the option args [] is not 'null' or empty.
		if(aOptionArgs != null && aOptionArgs.length>0) {
			for(int i=0;i<aOptionArgs.length;i++) {
				iOptionParams.put(aOptionArgs[i], null);
			}
		}
		this.parseCommandLine();
	}
	
	/**
	 * his method returns 'true' if any arguments are present, 'false'
	 * otherwise. <br />
	 * You are well advised to call this method first, before attempting 
	 * to retrieve any parameters, as 'null' might be returned.
	 *
	 * @return	boolean	'true' if arguments are present, 'false' otherwise.
	 */
	public boolean hasArguments() {
		return iHasArgs;
	}
	
	/**
	 * This method will report on all flags that have been found,
	 * or return an empty String[] if none were present.
	 *
	 * @return	String[]	with the flags or an empty array if 
	 *						none were found.
	 */
	public String[] getFlags() {
		return iFlags;
	}
	
	/**
	 * This method will report on all options that have been found,
	 * or return an empty String[] if none were present.
	 *
	 * @return	String[]	with the options or an empty array if 
	 *						none were found.
	 */
	public String[] getOptions() {
		return iOptions;
	}
	
	/**
	 * This method will report on all parameters that have been found,
	 * or return an empty String[] if none were present.
	 *
	 * @return	String[]	with the parameters or an empty array if 
	 *						none were found.
	 */
	public String[] getParameters() {
		return iParams;
	}
	
	/**
	 * This method will report the option parameter for an option
	 * that can take a parameter itself. This option has to be specified in
	 * the <a href="#optionConstr">constructor</a> that allows you to pass
	 * a String[] with all the options that take a parameter themselves. <br />
	 * <b>Note!</b> If no parameter was present, or the option specified 
	 * is not an option flagged as taking a parameter, 'null' is returned!
	 *
	 * @param	aOption	String with the option for which the parameter is 
	 *					to be retrieved.
	 * @return	String	with the parameter, if found, 'null' otherwise.
	 */
	public String getOptionParameter(String aOption) {
		return (String)iOptionParams.get(aOption);
	}

    /**
     * This method test whether the specified flag was set on the commandline.
     *
     * @param aFlag String with the flag to check. Comparison is case-sensitive!
     * @return  boolean 'true' if the flag was set, 'false' otherwise.
     */
    public boolean hasFlag(String aFlag) {
        boolean present = false;
        if(iFlags != null) {
            for(int i = 0; i < iFlags.length; i++) {
                String lFlag = iFlags[i];
                if(lFlag.equals(aFlag)) {
                    present = true;
                    break;
                }
            }
        }
        return present;
    }

	/**
	 * This method will do the actual worjk of parsing the 
	 * command-line arguments into an easily retrievable format.
	 */
	private void parseCommandLine() {
		// Firts see if anything is there at all.
		if((iCommandLine == null) || (iCommandLine.length == 0)) {
			iHasArgs = false;
		} else {
			// Flag that we have args.
			iHasArgs = true;
			// Arguments are present, let's see if we can parse them!
			this.parseFlags(); 
			this.parseOptions();
			this.parseParameters();
		}
		
		// Single point of exit.
		return;
	}
	
	/**
	 * This method searches for flags, and if present, adds them to the
	 * 'flags' String[]. <br />
	 * Flags are demarcated by '-' and no separation space. More may be 
	 * put behind a single '-'.
	 */
	private void parseFlags() {
		
		// Tempstorage for the flags.
		ArrayList tempFlags = new ArrayList();
		
		// Get all Strings from the String[] that start with '-'.
		// Note that we should make sure to avoid taking the Strings 
		// starting with '--' !
		for(int i=0;i<iCommandLine.length;i++) {
			if(iCommandLine[i].startsWith("-") && !iCommandLine[i].startsWith("--")) {
				String temp = iCommandLine[i];
				// Get rid of the leading '-'.
				temp = temp.substring(1);
				// See if multiple flags are contained on this line.
				if(temp.length() > 1) {
					// Split it up in seperate letters, add each
					// of these to the array.
					for(int j=0;j<temp.length();j++) {
						tempFlags.add(temp.substring(j, j+1));
					}
				} else {
					// Just add it.
					tempFlags.add(temp);
				}
			}
		}
		
		// Now to transform our List into an array and set it.
		iFlags = new String[tempFlags.size()];
		tempFlags.toArray(iFlags);
	}
	
	/**
	 * This method searches for options, and if present, adds them to the
	 * 'options' String[]. <br />
	 * Options are demarcated by '--' (akin to GNU notation) and no 
	 * separation space. Some options may take an additional parameter
	 * themselves. If so, this has to be flagged by using the 
	 * <a href="#optionConstr">constructor</a> that takes an additional 
	 * String[] with the names of the options that take an additional
	 * argument.
	 */
	private void parseOptions() {
		// We need something dynamic for a short while.
		ArrayList tempOptions = new ArrayList();
        ArrayList afterArray = new ArrayList();
        for(int i = 0; i < iCommandLine.length; i++) {
            String s = iCommandLine[i];
            afterArray.add(s);
        }
		// Find all options.
		// If one of these happens to take an argument,
		// (i.e.: is a key in the iOptionParams Hashmap) 
		// retrieve that parameter (it is the next element
		// in the args array) and add it as a value to
		// that key in the HashMap.
		for(int i=0;i<iCommandLine.length;i++) {
			if(iCommandLine[i].startsWith("--")) {
				// Found one!
				// Strip it of the '--'.
				String temp = iCommandLine[i].substring(2);
				// See if it takes a parameter.
				if(iOptionParams.containsKey(temp)) {
                    // First of all, remove this option from the
                    // afterarray.
                    afterArray.remove(iCommandLine[i]);
					// Okay, next element in the String has to
					// be the one we're looking for.
                    StringBuffer param = new StringBuffer();
                    // Remove this next element too.
                    afterArray.remove(iCommandLine[i+1]);
                    // See if it is a compound option (meaning it is encased in
                    // quotes).
                    if(iCommandLine[i+1].startsWith("\"") && !(iCommandLine[i+1].endsWith("\""))) {
                        param.append(iCommandLine[i+1].substring(1));
                        int counter = 1;
                        do {
                            counter++;
                            param.append(" " + iCommandLine[i+counter]);
                            afterArray.remove(iCommandLine[i+counter]);
                        } while(!iCommandLine[i+counter].endsWith("\""));
                        param.deleteCharAt(param.length()-1);
                    } else if(iCommandLine[i+1].startsWith("\"") && iCommandLine[i+1].endsWith("\"")) {
                        param.append(iCommandLine[i+1].substring(1, iCommandLine[i+1].length()-1));
                    } else {
                        param.append(iCommandLine[i+1]);
                    }
					iOptionParams.put(temp, param.toString());
				}
				
				// Add it to the temp list.
				tempOptions.add(temp);
			}
		}
		
		// Convert the list into a String[] and store it.
		iOptions = new String[tempOptions.size()];
		tempOptions.toArray(iOptions);

        // Overwrite the commandline array with the 'purified' one.
        iCommandLine = new String[afterArray.size()];
        afterArray.toArray(iCommandLine);
	}
	
	/**
	 * This method searches for parameters, and if present, adds them to the
	 * 'parameters' String[]. <br />
	 * Parameters are identifiable through their lack of demarcation. <br />
	 * Note that we should take care NOT to include parameters for options!
	 * There is a special check for that in the identifying loop!
	 */
	private void parseParameters() {
		// Temp list storage.
		ArrayList tempParams = new ArrayList();
		
		// Find all params, ignore the params for options!
		for(int i=0;i<iCommandLine.length;i++) {
			// Luckily, '--' also starts with '-' ;-).
			if(!iCommandLine[i].startsWith("-")) {
				// Okay, it is POTENTIALLY a parameter.
				// First it has to survive the simple test against it
				// being a parameter for an option.
				// So we see if there is a previous argument,
				// if there is, whether it starts with '--', and
				// if it does, whether it is present in the
				// iOptionParams HashMap as a key.
				String temp = iCommandLine[i];
				// If the current item has index of 0, there is no previous, 
				// therefor it cannot be an optionparam.
				if((i>0) && (iCommandLine[i-1].startsWith("--")) && (iOptionParams.containsKey(iCommandLine[i-1].substring(2)))) {
					// This is an option parameter.
					// leave it alone.
				} else {
					tempParams.add(temp);
				}
			}
		}
		
		// Convert to array and be done with it.
		iParams = new String[tempParams.size()];
		tempParams.toArray(iParams);
	}
}
