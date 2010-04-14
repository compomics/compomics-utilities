/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

package com.compomics.util.general.servlet;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.http.*;

import com.compomics.util.general.MassCalc;
import com.compomics.util.general.UnknownElementMassException;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:54 $
 */

/**
 * This class implements the MassCalc class as a servlet. <br />
 * It also generates its own submission form upon <i>first contact</i>.
 * 
 * @see com.compomics.util.general.MassCalc
 * @author	Lennart Martens
 */
public class MassCalcServlet extends HttpServlet {
	// Class specific log4j logger for MassCalcServlet instances.
	Logger logger = Logger.getLogger(MassCalcServlet.class);
	
	private static final String SEQUENCE = "SEQUENCE";
	private static final String MASSLISTCHOICE = "MASSLISTCHOICE";
	private static final String MONOBIOCHEM = "MONOBIOCHEM";
	private static final String MONOAA = "MONOAA";
	private static final String SELFLIST = "SELFLIST";
	private static final String ADDSELFLISTBIOCHEM = "ADDSELFLISTBIOCHEM";
	private static final String ADDSELFLISTAA = "ADDSELFLISTAA";
	private static final String SELFDEFINEDLIST = "SELFDEFINEDLIST";
	
	private static final int NO_ERROR = 0;
	private static final int NO_SEQUENCE = 1;
	private static final int NO_SELFDEFINEDLIST = 2;
	private static final int WRONG_SELFDEFINEDLIST = 3;

	private static final int ONLY_SELF = 1;
	private static final int ADD_SELF = 2;

	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		this.doPost(req, res);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		try {
			// First we see whether a parameter is present.
			// If this is the case, we should generate the submission form.
			if(req.getParameterNames().hasMoreElements()) {
				// Okay, we've got parameters, see if they're correct.
				Enumeration en = req.getParameterNames();
				HashMap params = new HashMap(3);
				while(en.hasMoreElements()) {
					String key = (String)en.nextElement();
					params.put(key, req.getParameter(key));
				}
				
				// For the self-defined list.
				int selfDefined = 0;
				HashMap selfList = null;
				
				// Checking for a sequence...
				if(params.get(SEQUENCE) == null || ((String)params.get(SEQUENCE)).trim().equals("")) {
					// Re-generate form with error message.
					this.generateSubmissionForm(res, NO_SEQUENCE);
					return;
				} else if(params.get(MASSLISTCHOICE).equals(SELFLIST)) {
					// Flag that we've got a self defined list to use exclusively.
					selfDefined = ONLY_SELF;
					// See if there is a list, and see if it can be parsed.
					if((params.get(SELFDEFINEDLIST) == null) || (((String)params.get(SELFDEFINEDLIST)).trim().equals(""))) {
						// Re-generate form with error message.
						this.generateSubmissionForm(res, NO_SELFDEFINEDLIST);
						return;
					} else {
						// There is a list, see if we can parse it.
						String list = (String)params.get(SELFDEFINEDLIST);
						try {
							selfList = this.parseSelfList(list);
						} catch(Exception e) {
							this.generateSubmissionForm(res, WRONG_SELFDEFINEDLIST);
						}
					}
				} else if(	params.get(MASSLISTCHOICE).equals(ADDSELFLISTBIOCHEM) || 
							params.get(MASSLISTCHOICE).equals(ADDSELFLISTAA)) {
								// Flag that we've got a self defined list to use 
								// additively.
								selfDefined = ADD_SELF;
								// There can be a list, see if we can parse it.
								String list = (String)params.get(SELFDEFINEDLIST);
								try {
									selfList = this.parseSelfList(list);
								} catch(Exception e) {
									this.generateSubmissionForm(res, WRONG_SELFDEFINEDLIST);
								}
				}
				
				// Okay, in getting here, we should be able to compute!
				String sequence = (String)params.get(SEQUENCE);
				int massList = 0;
				if(selfDefined == ONLY_SELF) {
					massList = -1;
				} else {
					String mlChoice = (String)params.get(MASSLISTCHOICE);
					mlChoice = mlChoice.trim();
					if(mlChoice.equals(MONOBIOCHEM) || mlChoice.equals(ADDSELFLISTBIOCHEM)) {
						massList = MassCalc.MONOELEMENTS;
					} else if(mlChoice.equals(MONOAA) || mlChoice.equals(ADDSELFLISTAA)) {
						massList = MassCalc.MONOAA;
					}
				}
				
				double mass = 0.0;
				MassCalc mc = null;
				// Bigger than zero: use built-in list, 
				// but possibly additively use self-defined list.
				// Else: use only self-defined list.
				if(massList>=0) {
					if(selfDefined == ADD_SELF) {
						// Additive selflist.
						mc = new MassCalc(massList, selfList);
					} else {
						// Plain old built-in type.
						mc = new MassCalc(massList);
					}
				} else {
					// Use only self defined list.
					mc = new MassCalc(selfList);
				}
				try {
					mass = mc.calculateMass(sequence);
					this.writeOutput(res, sequence, mass);
				} catch(UnknownElementMassException uem) {
					this.handleError(res, sequence, uem);
				}
			} else {
				// No parameters found. So generate the form.
				this.generateSubmissionForm(res, NO_ERROR);
			}
		}catch(IOException ioe) {
		}
	}
	
	private void generateSubmissionForm(HttpServletResponse res, int error) throws IOException {
		PrintWriter out = res.getWriter();
		out.println("<html>");
		out.println("	<head><title>Generic Mass Calculator (L. Martens)</title></head>");
		out.println("	<body>");
		out.println("		<h1>Generic mass calculator <i>(by Lennart Martens)</i></h1>");
		// Check for error mesages to be displayed.
		if(error != NO_ERROR) {
			this.printErrorMessage(out, error);
		}
		out.println("		<br /><hr /><br />");
		out.println("		<form method=\"POST\" action=\"http://beo04.rug.ac.be/utilities/masscalc\">");
		out.println("			<table>");
		out.println("				<tr>");
		out.println("					<td align=\"center\">");
		out.println("						<h3>Paste your sequence here:</h3>");
		out.println("					</td>");
		out.println("					<td>");
		out.println("						<h3>Paste your own masslist here (key=value)</h3>");
		out.println("					</td>");
		out.println("				</tr>");
		out.println("				<tr>");
		out.println("					<td align=\"center\" valign=\"top\">");
		out.println("						<textarea name=\"" + SEQUENCE + "\" rows=\"6\" cols=\"40\"></textarea>");
		out.println("					</td>");
		out.println("					<td align=\"center\" valign=\"top\" rowspan=\"2\">");
		out.println("						<textarea name=\"" + SELFDEFINEDLIST + "\" rows=\"12\" cols=\"30\"></textarea>");
		out.println("					</td>");
		out.println("				</tr>");
		out.println("				<tr>");
		out.println("					<td>");
		out.println("						<br />");
		out.println("						<input type=\"radio\" name=\"" + MASSLISTCHOICE + "\" value=\"" + MONOBIOCHEM + "\" checked/> <b>Monoisotopic biochemical elements</b>");
		out.println("						<br />");
		out.println("						<input type=\"radio\" name=\"" + MASSLISTCHOICE + "\" value=\"" + MONOAA      + "\" /> <b>Monoisotopic amino acids</b>");
		out.println("						<br />");
		out.println("						<input type=\"radio\" name=\"" + MASSLISTCHOICE + "\" value=\"" + SELFLIST    + "\" /> <b>Self-defined list</b>");
		out.println("						<br />");
		out.println("						<input type=\"radio\" name=\"" + MASSLISTCHOICE + "\" value=\"" + ADDSELFLISTBIOCHEM + "\" /> <b>Add/override self-defined elements to biochem list</b>");
		out.println("						<br />");
		out.println("						<input type=\"radio\" name=\"" + MASSLISTCHOICE + "\" value=\"" + ADDSELFLISTAA + "\" /> <b>Add/override self-defined elements to AA list</b>");
		out.println("						<br />");
		out.println("					</td>");
		out.println("				</tr>");
		out.println("			</table>");
		out.println("			<br /><br /><br /><br />");
		out.println("			<input type=\"submit\" value=\"Calculate!\" />");
		out.println("			<br />");
		out.println("			<input type=\"reset\" value=\"Reset form.\" />");
		out.println("		</form>");
		out.println("</body>");
		out.println("</html>");
		out.flush();
		out.close();
	}
	
	private void printErrorMessage(PrintWriter out, int aErrorCode) {
		switch(aErrorCode) {
			case NO_ERROR:
				break;
			case NO_SEQUENCE:
				out.println("		<font color=\"red\"><h1>You need to enter a sequence!</h1></font>");
				break;
			case NO_SELFDEFINEDLIST:
				out.println("		<font color=\"red\"><h1>If you select the 'Self defined list' choice, you need to specify a list in the text area on the right!</h1></font>");
				out.println("		<br /><br />");
				break;
			case WRONG_SELFDEFINEDLIST:
				out.println("		<font color=\"red\"><h1>Your self-defined list cannot be parsed! It should be formatted in KEY=VALUE pairs, KEY being a symbol of maximum two characters, the first character must be uppercase, the (optional) second character lowercase, and VALUE should hold a number. Only one KEY=VALUE pair per line!!!</h1></font>");
				out.println("		<br /><br />");
				break;
		}
	}
	
	private void writeOutput(HttpServletResponse aRes, String aSequence, double aMass) throws IOException {
		PrintWriter out = aRes.getWriter();
		out.println("<html>");
		out.println("	<head><title>Mass for " + aSequence + "</title></head>");
		out.println("	<body>");
		out.println("		<h1>Mass calculation results</h1>");
		out.println("		<hr /><br /><br /><br />");
		out.println("		<h3>Mass was: <font color=\"blue\">" + aMass + "</font> Da.</h3>");
		out.println("		<br /><br />");
		out.println("		<h4>Sequence was:<h4>");
		out.println("		<font color=\"green\">" + aSequence + "</font>");
		out.println("	</body>");
		out.println("</html>");
		out.flush();
		out.close();
	}
	
	private void handleError(HttpServletResponse aRes, String aSequence, UnknownElementMassException aUem) throws IOException {
		PrintWriter out = aRes.getWriter();
		out.println("<html>");
		out.println("	<head><title>Unknown element '" + aUem.getElement() +"' encountered!</title></head>");
		out.println("	<body>");
		out.println("		<font color=\"red\"><h1>Unknown element '" + aUem.getElement() +"' encountered!</h1></font>");
		out.println("		Did you type the case correctly according to IUPAC standards? <br />");
		out.println("		Did you type amino acid one letter code and in UPPERCASE?");
		out.println("		<br /><br /><hr /><br /><br />");
		aUem.printStackTrace(out);
		out.println("	</body>");
		out.println("</html>");
		out.flush();
		out.close();
	}
	
	/**
	 * This method attempts to parse a String into a HashMap.
	 *
	 * @param	aList	String with the list to be parsed.
	 * @return	HashMap	if all goes well!
	 * @exception	Exception	if anything at all goes wrong!
	 */
	private HashMap parseSelfList(String aList) throws Exception{
		HashMap toReturn = new HashMap();
		aList = aList.trim();
		StringTokenizer st = new StringTokenizer(aList, " =_:\n");
		while(st.hasMoreTokens()) {
			String key = st.nextToken();
			
			Double value = new Double(st.nextToken());
			toReturn.put(key, value);
		}
		
		return toReturn;
	}
}
