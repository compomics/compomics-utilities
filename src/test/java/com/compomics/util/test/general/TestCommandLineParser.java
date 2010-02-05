/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.test.general;


import junit.framework.*;
import com.compomics.util.general.CommandLineParser;


/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class is the test for the CommandLineParser class.
 *
 * @see com.compomics.util.general.CommandLineParser
 * @author	Lennart Martens.
 */
public class TestCommandLineParser extends TestCase {
	
	public TestCommandLineParser() {
		this("The test for the CommandLineParser class.");
	}
	
	public TestCommandLineParser(String aName) {
		super(aName);
	}
	
	/**
	 * This method test the constructors and their abilitity to parse
	 * without error.
	 */
	public void testInitialization() {
		// Test all 'not present cases'.
		CommandLineParser clp = new CommandLineParser(null);
		Assert.assertTrue("With just a 'null'", !clp.hasArguments());
		clp = new CommandLineParser(new String[]{});
		Assert.assertTrue("With just an empty String[]", !clp.hasArguments());
		clp = new CommandLineParser(null, null);
		Assert.assertTrue("Args 'null', OptionArgs 'null'.", !clp.hasArguments());
		clp = new CommandLineParser(new String[]{}, null);
		Assert.assertTrue("Args empty, OptionArgs 'null'.", !clp.hasArguments());
		clp = new CommandLineParser(null, new String[]{});
		Assert.assertTrue("Args 'null', OptionArgs empty.", !clp.hasArguments());
		clp = new CommandLineParser(null, new String[]{"Test"});
		Assert.assertTrue("Args 'null', OptionArgs filled.", !clp.hasArguments());
		clp = new CommandLineParser(new String[]{}, new String[]{});
		Assert.assertTrue("Args empty, OptionArgs empty.", !clp.hasArguments());
		clp = new CommandLineParser(new String[]{}, new String[]{"Test"});
		Assert.assertTrue("Args empty, OptionArgs filled.", !clp.hasArguments());
		
		// Test four 'present' cases.
		clp = new CommandLineParser(new String[]{"Test"});
		Assert.assertTrue(clp.hasArguments());
		clp = new CommandLineParser(new String[]{"Test"}, null);
		Assert.assertTrue(clp.hasArguments());
		clp = new CommandLineParser(new String[]{"Test"}, new String[]{});
		Assert.assertTrue(clp.hasArguments());
		clp = new CommandLineParser(new String[]{"Test"}, new String[]{"Test2"});
		Assert.assertTrue(clp.hasArguments());
	}
	
	/**
	 * This method test the parsing of flags.
	 */
	public void testFlagParsing() {
		// The flags.
		String flag1 = "k";
		String flag2 = "l";
		String flag3 = "f";
		String flag4 = "g";
		
		String[] args = new String[] {"Parameter", "--Option1", "--Option2", "-klf", "-g"};
		CommandLineParser clp = new CommandLineParser(args);
		// Check whether any arguments are in fact found.
		Assert.assertTrue(clp.hasArguments());
		String[] result = clp.getFlags();
		// See if it is not 'null' or empty.
		Assert.assertTrue(result != null);
		Assert.assertTrue(result.length>0);

        // Check the 'hasFlag method'.
        Assert.assertTrue(clp.hasFlag(flag1));
        Assert.assertTrue(clp.hasFlag(flag2));
        Assert.assertTrue(clp.hasFlag(flag3));
        Assert.assertTrue(clp.hasFlag(flag4));
        Assert.assertFalse(clp.hasFlag("x"));

		// Four flags present, see if all are found.
		Assert.assertEquals("Incorrect number of flags found!", 4, result.length);
		// Cycle each and check.
		boolean allFound = true;
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		boolean found4 = false;
		for(int i=0;i<result.length;i++) {
			// Logic here is: all must be present just ONCE,
			// and no extra can be present!
			if(!found1 && result[i].equals(flag1)) {
				found1 = true;
			} else if(!found2 && result[i].equals(flag2)) {
				found2 = true;
			} else if(!found3 && result[i].equals(flag3)) {
				found3 = true;
			} else if(!found4 && result[i].equals(flag4)) {
				found4 = true;
			} else {
				// Entry not found in list!
				// This means a flag has been devised from thin air!
				allFound = false;
			}
		}
		Assert.assertTrue(allFound && found1 && found2 && found3 && found4);
		
		// Test the empty String[] for the absence of flags.
		clp = new CommandLineParser(new String[]{"--Option1", "--Option2", "Param1", "Param2"});
		Assert.assertTrue(clp.hasArguments());
		result = clp.getFlags();
		Assert.assertTrue(result != null);
        Assert.assertFalse(clp.hasFlag("k"));
		Assert.assertEquals(0, result.length);
	}
	
	/**
	 * This method test the parsing of options.
	 */
	public void testOptionParsing() {
		String option1 = "option1";
		String option2 = "option2";
		String option3 = "option3";
		
		// First without option parameters.
		CommandLineParser clp = new CommandLineParser(new String[]{"parameter", "-f", "-klg"});
		// See if there are arguments.
		Assert.assertTrue(clp.hasArguments());
		// See if there are options.
		String[] result = clp.getOptions();
		// See if the result is not 'null' but empty.
		Assert.assertTrue(result != null);
		Assert.assertTrue(result.length == 0);
		
		// Now with only 'plain' options parameters.
		clp = new CommandLineParser(new String[]{"parameter", "-f", "--"+option3, "-klg", "--"+option1, "--"+option2});
		// See if there are arguments.
		Assert.assertTrue(clp.hasArguments());
		// See if there are options.
		result = clp.getOptions();
		// See if the result is not 'null' or empty.
		Assert.assertTrue(result != null);
		Assert.assertTrue(result.length > 0);
		// See if the count of options are correct.
		Assert.assertEquals(3, result.length);
		
		// Cycle each and check.
		boolean allFound = true;
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for(int i=0;i<result.length;i++) {
			// Logic here is: all must be present just ONCE,
			// and no extra can be present!
			if(!found1 && result[i].equals(option1)) {
				found1 = true;
			} else if(!found2 && result[i].equals(option2)) {
				found2 = true;
			} else if(!found3 && result[i].equals(option3)) {
				found3 = true;
			} else {
				// Entry not found in list!
				// This means a flag has been devised from thin air!
				allFound = false;
			}
		}
		Assert.assertTrue(allFound && found1 && found2 && found3);
		
		// Now to check for options that carry parameters.
		String op1Param = "Option 1 parameter.";
		clp = new CommandLineParser(new String[] {"parameter", "-f", "--"+option3, "-klg", "--"+option1, op1Param, "--"+option2},
									new String[] {option1});
		// Okay, check for presence of option1 in the list.
		result = clp.getOptions();
		boolean found = false;
		for(int i=0;i<result.length;i++) {
			if(result[i].equals(option1)) {
				found = true;
				// No need to continue.
				break;
			}
		}
		Assert.assertTrue(found);
		// Now to see if the param is present.
		String param = clp.getOptionParameter(option1);
		Assert.assertEquals(op1Param, param);
		// Next, see if the 'null' behaviour is correct for all the others.
		Assert.assertTrue(clp.getOptionParameter(option2) == null);
		Assert.assertTrue(clp.getOptionParameter(option3) == null);
	}

	/**
	 * This method test the parsing of parameters.
	 */
	public void testParameterParsing() {
		String param1 = "Param1";
		String param2 = "Param1";
		
		// Check for correct behaviour if no params.
		CommandLineParser clp = new CommandLineParser(new String[]{"-klf", "--option1"});
		Assert.assertTrue(clp.getParameters().length == 0);
		// Check for correct behaviour if no params but option params.
		clp = new CommandLineParser(new String[]{"-klf", "--option1", "option1parameter"},
													  new String[]{"option1"});
		Assert.assertTrue(clp.getParameters().length == 0);
		
		// Simple param passing.
		clp = new CommandLineParser(new String[]{param1});
		// See if args are detected.
		Assert.assertTrue(clp.hasArguments());
		// We should have non-null, non-zerolength params array.
		String[] result = clp.getParameters();
		Assert.assertTrue(result != null);
		Assert.assertTrue(result.length > 0);
		// See how many params are found.
		Assert.assertTrue(result.length == 1);
		// Check the param.
		Assert.assertEquals(param1, result[0]);
		
		// Multiple params.
		clp = new CommandLineParser(new String[]{param1, param2});
		result = clp.getParameters();
		// See how many params are found.
		Assert.assertTrue(result.length == 2);
		// Check the params.
		Assert.assertEquals(param1, result[0]);
		Assert.assertEquals(param2, result[1]);
		
		// With interference.
		clp = new CommandLineParser(new String[]{"--Option1", param1, "-klf", "-g", param2, "--Option2"});
		result = clp.getParameters();
		// See how many params are found.
		Assert.assertTrue(result.length == 2);
		// Check the params.
		Assert.assertEquals(param1, result[0]);
		Assert.assertEquals(param2, result[1]);
		
		// With interference & option parameters.
		clp = new CommandLineParser(new String[]{"--Option1", "optionparam1", param1, "-klf", "-g", param2, "--Option2"},
									new String[]{"Option1"});
		result = clp.getParameters();
		// See how many params are found.
		Assert.assertTrue(result.length == 2);
		// Check the params.
		Assert.assertEquals(param1, result[0]);
		Assert.assertEquals(param2, result[1]);
	}

    /**
     * This method test compoound parameter parsing.
     */
    public void testCompoundParameterParsing() {
        String compound = "This is a compound option parameter!";
        String standard = "standardOptionParameter";
        String strangeCompoundParam = "StrangeCompoundParam.";
        String regParam1 = "regularParameter1";
        String regParam2 = "regularParameter2";

        CommandLineParser clp = new CommandLineParser(new String[]{"-klf", "--strangeCompound", "\""+strangeCompoundParam+"\"", "--compound", "\"This", "is", "a", "compound", "option", "parameter!\"", "--standard", standard, regParam1, regParam2}, new String[]{"compound", "standard", "strangeCompound"});

        // The regular parameters.
        Assert.assertEquals("Incorrect number of parameters found with compound option parameter!", 2, clp.getParameters().length);
        Assert.assertEquals(regParam1, clp.getParameters()[0]);
        Assert.assertEquals(regParam2, clp.getParameters()[1]);
        // The flags.
        Assert.assertEquals("Incorrect number of flags found with compound option parameter!", 3, clp.getFlags().length);
        Assert.assertEquals("k", clp.getFlags()[0]);
        Assert.assertEquals("l", clp.getFlags()[1]);
        Assert.assertEquals("f", clp.getFlags()[2]);
        // And the option parameters.
        Assert.assertEquals("Incorrect number of option parameters found with compound option parameter!", 3, clp.getOptions().length);
        Assert.assertEquals("strangeCompound", clp.getOptions()[0]);
        Assert.assertEquals("compound", clp.getOptions()[1]);
        Assert.assertEquals("standard", clp.getOptions()[2]);
        Assert.assertEquals(strangeCompoundParam, clp.getOptionParameter("strangeCompound"));
        Assert.assertEquals(standard, clp.getOptionParameter("standard"));
        Assert.assertEquals(compound, clp.getOptionParameter("compound"));
    }
}
