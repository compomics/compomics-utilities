/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 8-okt-02
 * Time: 18:06:48
 */
package com.compomics.util.io;
import org.apache.log4j.Logger;


import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.DualEnzyme;
import com.compomics.util.protein.RegExEnzyme;

import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.io.*;


/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/18 11:39:11 $
 */

/**
 * This class will load Enzyme properties from a Mascot
 * (<a href="http://www.matrixscience.com" target="_blank">www.matrixscience.com</a>)
 * formatted text file.
 *
 * @author Lennart Martens
 */
public class MascotEnzymeReader {

    // Class specific log4j logger for MascotEnzymeReader instances.
    Logger logger = Logger.getLogger(MascotEnzymeReader.class);

    /**
     * This HashMap will hold all the Enzyme entries we've found in the file.
     * The enzyme name doubles as the key.
     */
    private HashMap iEnzymes = null;

    /**
     * The constructor requires that you specify the file from which to load
     * the enzyme information.
     * Information is immediately loaded upon construction.
     *
     * @param   aEnzymeFile String with the filename of the mascot enzyme file.
     * @exception   IOException when the enzyme file could not be read.
     */
    public MascotEnzymeReader(String aEnzymeFile) throws IOException {
        this(new FileInputStream(aEnzymeFile));
    }

    /**
     * The constructor allows you specify to specify an inputstream from which to load
     * the enzyme information. <b>Note that the stream is closed after loading!!!</b>
     * Information is immediately loaded upon construction.
     *
     * @param   aEnzymeStream InputStream to the mascot enzyme file.
     * @exception   IOException when the enzyme file could not be read.
     */
    public MascotEnzymeReader(InputStream aEnzymeStream) throws IOException {
        try {
            InputStream in = aEnzymeStream;
            BufferedReader lBr = new BufferedReader(new InputStreamReader(in));
            iEnzymes = this.readAllEntries(lBr);
            lBr.close();
            in.close();
        } catch(IOException ioe) {
            throw new IOException("Unable to load Mascot enzyme file from stream: " + ioe.getMessage());
        }
    }

    /**
     * This method loads all entries from the Mascot enzymefile associated with the
     * BufferedReader
     *
     * @param   aBr BufferedReader to read the enzymefile from.
     * @return  HashMap with all the entries, and with the name for each entry
     *                  doubling as the key for that entry.
     * @exception   IOException when the reading goes wrong.
     */
    private HashMap readAllEntries(BufferedReader aBr) throws IOException {
        HashMap entries = new HashMap();

        String line = aBr.readLine();

        // The constituent parts of an enzyme.
        String title = null;
        String cleavage = null;
        String restrict = null;
        String position = null;

        while(line != null) {
            line = line.trim();
            if(line.equals("*") || line.equals("")) {
                if(line.equals("*") && cleavage != null && position != null) {
                    if(title.toLowerCase().startsWith("dual")) {
                        // Process dual enzyme here.
                        // The cleavables are separated in N-terms and C-terms by the 'X' character.
                        StringTokenizer st = new StringTokenizer(cleavage.toUpperCase(), "X");
                        if(st.countTokens() != 2) {
                            String error = null;
                            if(st.countTokens() > 2) {
                                error = " more than one ";
                            } else {
                                error = "out the ";
                            }
                            logger.error("Dual enzyme detected (title starts with 'dual', regardless of case) but with" + error
                                    + "'X' separator between N-terminal cleavables and C-terminal cleavables.\nTreating it as a regular enzyme.");
                            entries.put(title, new Enzyme(title, cleavage, restrict, position));
                        } else {
                            String ntermCleavage = st.nextToken().trim();
                            String ctermCleavage = st.nextToken().trim();
                            entries.put(title, new DualEnzyme(title, ntermCleavage, ctermCleavage, restrict, position));
                        }
                    } else if(title.toLowerCase().startsWith("regex")) {
                        // first check if we can compile the regular expression
                        // @ToDo: if we throw a checked exception here, it will have percussions throughout the system!
                        Pattern.compile(cleavage, Pattern.CASE_INSENSITIVE);
                        entries.put(title, new RegExEnzyme(title, cleavage, restrict, position));

                    } else {
                        // Process regular enzyme here.
                        entries.put(title, new Enzyme(title, cleavage, restrict, position));
                    }
                    title = null;
                    cleavage = null;
                    restrict = null;
                    position = null;
                }
                line = aBr.readLine();
            } else {
                String insensitive = line.toUpperCase();
                if(insensitive.indexOf("TITLE") >= 0) {
                    int start = line.indexOf(":") + 1;
                    title = line.substring(start).trim();
                } else if(insensitive.indexOf("CLEAVAGE") >= 0) {
                    int start = line.indexOf(":") + 1;
                    cleavage = line.substring(start).trim();
                } else if(insensitive.indexOf("RESTRICT") >= 0) {
                    int start = line.indexOf(":") + 1;
                    restrict = line.substring(start).trim();
                } else if(insensitive.indexOf("CTERM") >= 0) {
                    position = "Cterm";
                } else if(insensitive.indexOf("NTERM") >= 0) {
                    position = "Nterm";
                }
                // Advance a line.
                line =aBr.readLine();
            }
        }

        return entries;
    }

    /**
     * This method reports on all the known names for enzymes in this reader.
     *
     * @return  String[]    with all the names.
     */
    public String[] getEnzymeNames() {
        Set s = this.iEnzymes.keySet();
        int liSize = s.size();
        String[] result = new String[liSize];
        s.toArray(result);
        return result;
    }

    /**
     * This method will return a <b>copy of</b> an Enzyme instance
     * for the given name, or 'null' if the enzyme was not found in the current list.
     *
     * @param   aTitle  String with the title (name) of the Enzyme to retrieve.
     * @return  Enzyme  when the specified enzyme was found, 'null' otherwise!
     */
    public Enzyme getEnzyme(String aTitle) {
        Enzyme e = (Enzyme)this.iEnzymes.get(aTitle);
        Enzyme result = null;

        if(e != null) {
            result = (Enzyme)e.clone();
        }

        return result;
    }
}
