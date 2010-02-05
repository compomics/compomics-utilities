/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 1-jul-2004
 * Time: 15:08:17
 */
package com.compomics.util.protein;


import java.io.*;
import java.sql.*;
import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class can be used to generate a Modification instance from a code or title. <br />
 * Modification information is loaded from files or database, as specified in the two available
 * constructors for this Factory.
 *
 * @author Lennart Martens
 */
public class ModificationFactory {

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String RDBMS = "RDBMS";

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String RDBDRIVER = "RDBDRIVER";

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String TABLE = "TABLE";

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String USER = "USER";

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String PASSWORD = "PASSWORD";

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String MODFILE = "MODFILE";

    /**
     * Constant for a key in the ModificationFactory.properties file.
     */
    private static final String CODEFILE = "CODEFILE";

    /**
     * This HashMap holds will hold all the data necessary to create a specific modification.
     */
    private static HashMap allMods = null;

    /**
     * This HashMap maps a modification code to a modification title.
     */
    private static HashMap codeToTitle = null;

    /**
     * This HashMap maps a modification title to a modification code.
     */
    private static HashMap titleToCode = null;

    /**
     * This boolean is set to true when the factory is initialized.
     */
    private static boolean iInitialized = false;


    /**
     * This Factory is fully static.
     */
    private ModificationFactory(){}

    /**
     * This method will return a Modification instance from a title.
     *
     * @param aTitle  String with the title for a modification.
     * @param aLocation int with the location for the modification.
     * @return  Modification    with the corresponding modification, or 'null' when the
     *                          modification was not found.
     */
    public static com.compomics.util.interfaces.Modification getModification(String aTitle, int aLocation) {
        com.compomics.util.interfaces.Modification mod = null;
        // See if the factory has been initialized.
        checkInit();
        // Find the title.
        if(allMods.containsKey(aTitle)) {
            ModificationTemplate template = (ModificationTemplate)allMods.get(aTitle);
            mod = new ModificationImplementation(template, aLocation);
        }
        return mod;
    }

    /**
     * This method takes a code and a residue (the residue for the N-terminus is NTERMINUS and for the
     * C-terminus CTERMINUS!) and converts this into a Modification instance if possible.
     *
     * @param aCode String with the code for the modification
     * @param aResidue  String with the residue carrying the modification (the residue for the N-terminus
     *                  is defined in the constant NTERMINUS and for the C-terminus in the constant CTERMINUS!).
     * @param aLocation int with the location for the modification.
     * @return  Modification    with the corresponding modification, or 'null' when the
     *                          modification was not found.
     */
    public static com.compomics.util.interfaces.Modification getModification(String aCode, String aResidue, int aLocation) {
        com.compomics.util.interfaces.Modification mod = null;
        // See if the factory has been initialized.
        checkInit();
        // Try and find the key without forgetting about possible duplicates.
        Iterator it = codeToTitle.keySet().iterator();
        while(it.hasNext()) {
            String code = (String)it.next();
            if(code.startsWith(aCode) && ( (code.length()-aCode.length())==0 || (code.length()-aCode.length())==1) ) {
                String title = (String)codeToTitle.get(code);
                ModificationTemplate template = (ModificationTemplate)allMods.get(title);
                // If the allDetails Object[] is 'null', we have encountered a code that maps to a title
                // which is NOT present in the 'allMods' HashMap, and therefore is probably not present in the
                // 'modifications.txt'.
                if(template == null) {
                    continue;
                }
                Collection residues = template.getResidues();
                if(residues.contains(aResidue)) {
                    if(mod == null) {
                        mod = new ModificationImplementation(template, aLocation);
                    }
                }
            }
        }

        return mod;
    }


    /**
     * Returns a String representation of the modifications.
     *
     * @return  String with a String representation of the modification.
     */
    public static String modificationsToString() {
        StringBuffer sb = new StringBuffer();
        // See if the factory has been initialized.
        checkInit();
        // Cycle all keys.
        Set keyset =  allMods.keySet();
        String[] keys = new String[allMods.size()];
        keyset.toArray(keys);
        Arrays.sort(keys);
        for(int i = 0; i < keys.length; i++) {
            String title = keys[i];
            ModificationTemplate template = (ModificationTemplate)allMods.get(title);
            boolean hidden = template.isArtifact();
            sb.append("Title:" + title + "\n");
            if(hidden) {
                sb.append("Hidden\n");
            }
            keyset = (Set)template.getResidues();
            String[] massKeys = new String[keyset.size()];
            keyset.toArray(massKeys);
            Arrays.sort(massKeys);
            for(int j = 0; j < massKeys.length; j++) {
                String residue = massKeys[j];
                if(residue.equals(com.compomics.util.interfaces.Modification.NTERMINUS)) {
                    sb.append("Nterm:");
                } else if(residue.equals(com.compomics.util.interfaces.Modification.CTERMINUS)) {
                    sb.append("Cterm:");
                } else {
                    sb.append("Residues:" + residue);
                }
                sb.append(" " + template.getMonoisotopicMassDelta(residue) + " " + template.getAverageMassDelta(residue) + "\n");
            }
            sb.append("*\n");
        }

        return sb.toString();
    }

    /**
     * Returns a String representation of the modification title to code mappings.
     *
     * @return  String with a String representation of the code tot title mappings.
     */
    public static String modificationConversionToString() {
        StringBuffer result = new StringBuffer();
        // See if the factory has been initialized.
        checkInit();
        // Compose the String.
        Set keySet = titleToCode.keySet();
        String[] titles = new String[keySet.size()];
        keySet.toArray(titles);
        Arrays.sort(titles);
        for(int i = 0; i < titles.length; i++) {
            String lTitle = titles[i];
            // Only append those mappings that have a title linked to a
            // real ModificationTemplate.
            if(allMods.containsKey(lTitle)) {
                result.append(lTitle + "=" + titleToCode.get(lTitle) + "\n");
            }
        }

        return result.toString();
    }

    /**
     * This method can be used to refresh all data from the data store indicated in the ModificationFactory.properties
     * file.
     */
    public static void reLoadAllData() {
        iInitialized = false;
        checkInit();
    }

    /**
     * This method returns all modification titles known to the Factory.
     *
     * @return  String[] with all the modification titles.
     */
    public static String[] getAllModificationTitles() {
        checkInit();
        String[] result = null;
        Set titles = allMods.keySet();
        result = new String[titles.size()];
        titles.toArray(result);

        return result;
    }

    /**
     * Try to load the specified file from an absolute name or, if that fails, the classpath.
     * If the file is found, it is parsed and the in-memory code to title mappings are initialized.
     *
     * @param   aCodesFile   String with the filename for the file to load the code tot title mappings from.
     *                      This name can be an absolute filename, or the name of a file in the classpath.
     */
    private static void loadCodesFromFile(String aCodesFile) {
        codeToTitle = new HashMap();
        titleToCode = new HashMap();
        try {
            // Get an InputStream to the text file.
            InputStream in = null;
            // Try an absolute pathname.
            File temp = new File(aCodesFile);
            if(!temp.exists()) {
                // In getting here
                in = ModificationFactory.class.getClassLoader().getResourceAsStream(aCodesFile);
                if (in == null) {
                    throw new IOException("Unable to load '" + aCodesFile + "' as an absolute path as well as from the classpath! Please check the filename!!");
                }
            } else {
                in = new FileInputStream(temp);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            // The HashMap that holds all codes that have duplicates.
            HashMap duplicateCodes = new HashMap();
            while((line= br.readLine()) != null) {
                line = line.trim();
                // Skip comment & blank lines.
                if(line.startsWith("#") || line.startsWith("!") || line.equals("")) {
                    continue;
                } else {
                    int location = line.indexOf("=");
                    String title = line.substring(0, location).trim();
                    String code = line.substring(location+1).trim();
                    Object previous = titleToCode.put(title, code);
                    // Now first see if the code has already had duplicates.
                    if(duplicateCodes.containsKey(code)) {
                        int count = ((Integer)duplicateCodes.get(code)).intValue();
                        count++;
                        codeToTitle.put(code + count, title);
                        duplicateCodes.put(code, new Integer(count));
                    } else {
                        previous = codeToTitle.put(code, title);
                        // Here conflicts can arise.
                        if(previous != null) {
                            duplicateCodes.put(code, new Integer(1));
                            codeToTitle.put(code + "0", previous);
                            codeToTitle.put(code + "1", title);
                            codeToTitle.remove(code);
                        }
                    }
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Try to load the specified file from an absolute name or, if that fails, the classpath.
     * If the file is found, it is parsed and the in-memory modification table is initialized.
     *
     * @param   aModsFile   String with the filename for the file to load the modifications from.
     *                      This name can be an absolute filename, or the name of a file in the classpath.
     */
    private static void loadModificationsFromFile(String aModsFile) {
        allMods = new HashMap();
        try {
            // Get an InputStream to the text file.
            InputStream in = null;
            // First see if we can load it from an absolute path.
            File temp = new File(aModsFile);
            if(!temp.exists()) {
                // In getting here
                in = ModificationFactory.class.getClassLoader().getResourceAsStream(aModsFile);
                if (in == null) {
                    throw new IOException("Unable to load '" + aModsFile + "' as an absolute path as well as from the classpath! Please check the filename!!");
                }
            } else {
                in = new FileInputStream(temp);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line= br.readLine()) != null) {
                // Trim the line.
                line = line.trim();
                // Parse the 'modifications.txt' file.
                if(line.startsWith("Title:")) {
                    // Read the title.
                    String title = line.substring(6);
                    // Read the next line.
                    line = br.readLine().trim();
                    boolean hidden = false;
                    HashMap massDeltas = new HashMap();
                    if(line.equals("Hidden")) {
                        hidden = true;
                        line = br.readLine();
                    }
                    if(line.startsWith("Nterm:")) {
                        String residue = com.compomics.util.interfaces.Modification.NTERMINUS;
                        massDeltas.put(residue, parseMonoAndAverageMassDelta(line.substring(6)));
                    } else if(line.startsWith("Cterm:")) {
                        String residue = com.compomics.util.interfaces.Modification.CTERMINUS;
                        massDeltas.put(residue, parseMonoAndAverageMassDelta(line.substring(6)));
                    } else if(line.startsWith("Residues")) {
                        while(line.startsWith("Residues")) {
                            int colon = line.indexOf(":");
                            int first = line.indexOf(" ");
                            String residue = line.substring(colon+1, first).trim();
                            massDeltas.put(residue, parseMonoAndAverageMassDelta(line.substring(first+1)));
                            line = br.readLine();
                        }
                    }
                    // We should add the modification.
                    allMods.put(title, new ModificationTemplate(title, (String)titleToCode.get(title), massDeltas, hidden));
                }
            }
            br.close();
            in.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method will take a String of the form 'xxx.xxx xxx.xxxx' and return the
     * two space-separated decimal numbers in an array of doubles. <br />
     * Specifically for the ModificationFactory class, the first element in the array
     * is the monoisotopic mass, the second is the average mass delta.
     *
     * @param aData String in the form of 'xxx.xxxxx xxx.xxxxxx' to parse the two doubles from.
     * @return  double[] with the two numbers in the String (0:MONO, 1:AVG)
     */
    private static double[] parseMonoAndAverageMassDelta(String aData) {
        int spaceLocation = aData.indexOf(" ");
        double mono = Double.parseDouble(aData.substring(0, spaceLocation).trim());
        double avg = Double.parseDouble(aData.substring(spaceLocation).trim());
        return new double[] {mono, avg};
    }

    /**
     * This method loads and initiliazes the modification maps in memory.
     *
     * @param   aModsFile  String with the filename for the file to load the modifications from.
     *                              This name can be an absolute filename, or the name of a file in the classpath.
     * @param   aCodesFile  String with the filename for the file to load the code to title mappings from.
     *                              This name can be an absolute filename, or the name of a file in the classpath.
     */
    private static void loadAllFromFiles(String aModsFile, String aCodesFile) {
        loadCodesFromFile(aCodesFile);

        loadModificationsFromFile(aModsFile);
    }

    /**
     * This method loads all modification from the specified RDBMS system. It expects the following columns to be present in the
     * specified table (please read the information below for a detailed description of the massdeltas columns):
     *  <ul>
     *   <li>title [read as String]</li>
     *   <li>code [read as String]</li>
     *   <li>artifact [read as boolean]</li>
     *   <li>monoisotopicmassdeltas [read as String, with the following formatting: (RESIDUE1)_xxx.yyyy;(RESIDUE2)_aaa.bbb]</li>
     *   <li>averagemassdeltas [read as String, with the following formatting: (RESIDUE1)_xxx.yyyy;(RESIDUE2)_aaa.bbb]</li>
     *  </ul>
     *
     * Some detailed information about the massdeltas columns could be useful. <br />
     * These fields contain data that represents which residue will suffer which mass delta for the specific modification.
     * These mass deltas can be measured both monoisotopically and averaged over the isotopes. In order to map these fields
     * correctly it is necessary to have a correspondence between the residues mentioned in the monoisotopic and average rows.
     * If a certain residue is only present in one of these columns, there will be trouble!
     *
     * @param aDB   String with the jdbc connection String.
     * @param aDriver   String with the fully qualified classname of the DB driver
     * @param aTable    String with the table to perform the query on.
     * @param aConnectionProps  Properties with all properties for the connection set.
     * @throws IOException  wraps all exceptions that can occur here (mostly SQLExceptions, obviously).
     */
    private static void loadAllFromRDBMS(String aDB, String aDriver, String aTable, Properties aConnectionProps) throws IOException {
        // Try to get a connection.
        Connection conn = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            // First get a hold of the driver.
            Driver driver = null;
            try {
                driver = (Driver)Class.forName(aDriver).newInstance();
            } catch(ClassNotFoundException cnfe) {
                throw new SQLException("Unable to load driver '" + aDriver + "'! Are you sure it is in the classpath?");
            } catch(IllegalAccessException iae) {
                throw new SQLException("The driver '" + aDriver + "' does not seem to have an accessible public constructor!");
            } catch(InstantiationException ie) {
                throw new SQLException("The driver '" + aDriver + "' does not seem to have an accessible public constructor!");
            }
            if(driver != null) {
                // Okay, driver loaded! Let's get connected.
                conn = driver.connect(aDB, aConnectionProps);
                // Connection made, construct query.
                // No PreparedStatement used as this query will not be executed frequently and as such
                // will only clutter the preparedstatement cache of the driver.
                stat = conn.createStatement();
                // Execute the SQL.
                rs = stat.executeQuery("select title, code, artifact, monoisotopicmassdeltas, averagemassdeltas from " + aTable);
                // HashMap used for tracking duplicate codes.
                HashMap duplicateCodes = new HashMap();
                allMods = new HashMap();
                titleToCode = new HashMap();
                codeToTitle = new HashMap();
                // Cycle the results.
                while(rs.next()) {
                    // In JDBC: column numbers start from '1'.
                    String title = rs.getString(1).trim();
                    String code = rs.getString(2);
                    boolean artifact = rs.getBoolean(3);
                    String monoDeltas = rs.getString(4).trim();
                    String avgDeltas = rs.getString(5).trim();

                    // Parse the mono and avg mass deltas Strings.
                    HashMap massDeltas = parseDeltasFromRDBMSStrings(monoDeltas, avgDeltas);

                    // Now to construct a Modification objects and add it to the map.
                    allMods.put(title, new ModificationTemplate(title, code, massDeltas, artifact));

                    // We mustn't forget the 'title to code' and 'code to title' mappings!
                    // Title to code is quite easy:
                    if(code != null) {
                        // Note that we check whether the title is unique!
                        Object found = titleToCode.put(title, code);
                        if(found != null) {
                            System.err.println("Duplicate title for modification: " + found);
                        }
                        // The inverse mapping is somewhat more difficult, since code need not be unique.
                        // If the code is already present, we need to do some complex stuff.
                        // Now first see if the code has already had duplicates.
                        if(duplicateCodes.containsKey(code)) {
                            int count = ((Integer)duplicateCodes.get(code)).intValue();
                            count++;
                            codeToTitle.put(code + count, title);
                            duplicateCodes.put(code, new Integer(count));
                        } else {
                            Object previous = codeToTitle.put(code, title);
                            // Here conflicts can arise.
                            if(previous != null) {
                                duplicateCodes.put(code, new Integer(1));
                                codeToTitle.put(code + "0", previous);
                                codeToTitle.put(code + "1", title);
                                codeToTitle.remove(code);
                            } else {
                            }
                        }
                    }
                }
                // Closing everything down is done in the 'finally' section.
            } else {
                // That's odd: no exception and yet no driver either!
                throw new SQLException("Driver '" + aDriver + "' was not loaded correctly! Unfortunately, no further details are known.");
            }
        } catch(SQLException sqle) {
            throw new IOException(sqle.getMessage());
        } finally {
            // Wrap things up nicely.
            if(rs != null) {
                try {
                    rs.close();
                } catch(Exception e) {
                    // Too late to worry about that now.
                }
            }
            if(stat != null) {
                try {
                    stat.close();
                } catch(Exception e) {
                    // Too late to worry about that now.
                }
            }
            if(conn != null) {
                try {
                    conn.close();
                } catch(Exception e) {
                    // Too late to worry about that now.
                }
            }
        }
    }

    /**
     * This method can parse a HashMap with monoisotopic and average mass deltas from the
     * Strings as stored in the database. <br />
     * Formatting of these Strings should be: <br />
     *  [residue1]_xx.yyyy;[residue2]_aa.bbbb <br />
     *  eg.: S_165.7654;T_153.9965 <br />
     * <b>Please note</b> that every residue present in the monoisotopic list should also be present in the
     * average list!
     *
     * @param aMonoDeltas   String with the monoistopic mass deltas, formatted as specified above.
     * @param aAvgDeltas String with the average mass deltas, formatted as specified above.
     * @return  HashMap with the mass delta mappings.
     * @throws IOException  when the parsing failed.
     */
    private static HashMap parseDeltasFromRDBMSStrings(String aMonoDeltas, String aAvgDeltas) throws IOException {
        HashMap mappings = new HashMap();
        // First the monoisotopic bits.
        // Start by splitting up the semicolon-delimited parts.
        StringTokenizer st = new StringTokenizer(aMonoDeltas, ";");
        // This arraylist is used as a check on whether each residue, specified in the monoisotopic section,
        // is also present in the average section. We add all mono residues here, and later subtract those from the
        // average. If the size of this list is greater than 0 at the end, there was at leats one residue present
        // in the mono part that was NOT in the average part, and we even know which one!
        ArrayList keys = new ArrayList(10);
        while(st.hasMoreTokens()) {
            // This should yield something of the form [residueX]_xxx.yyyy.
            String resMassCombo = st.nextToken();
            // Find the first underscore starting from the end (this way, one can use underscores in the
            // code, which is NOT recommended, by the way).
            int location = resMassCombo.lastIndexOf("_");
            if(location < 0) {
                throw new IOException("The content of the monoisotopicmassdeltas row could not be parsed from (a String + '_' + a double) since the '_' is missing!");
            }
            String residue = resMassCombo.substring(0, location);
            String stringMonoValue = resMassCombo.substring(location+1);
            double mono = 0.0;
            try {
                mono = Double.parseDouble(stringMonoValue);
            } catch(NumberFormatException nfe) {
                throw new IOException("The content of the monoisotopicmassdeltas row could not be parsed from (a String + '_' + a double)!");
            }
            mappings.put(residue, new Double(mono));
            keys.add(residue);
        }

        // Okay, we now have all mappings for the monoisotopic stuff.
        // The average stuff should hold the same number of mappings.
        st = new StringTokenizer(aAvgDeltas, ";");
        while(st.hasMoreTokens()) {
            // This should yield something of the form [residueX]_xxx.yyyy.
            String resMassCombo = st.nextToken();
            // Find the first underscore starting from the end (this way, one can use underscores in the
            // code, which is NOT recommended, by the way).
            int location = resMassCombo.lastIndexOf("_");
            if(location < 0) {
                throw new IOException("The content of the averagemassdeltas row could not be parsed from (a String + '_' + a double) since the '_' is missing!");
            }
            String residue = resMassCombo.substring(0, location);
            String stringAvgValue = resMassCombo.substring(location+1);
            double avg = 0.0;
            try {
                avg = Double.parseDouble(stringAvgValue);
            } catch(NumberFormatException nfe) {
                throw new IOException("The content of the averagemassdeltas row could not be parsed from (a String + '_' + a double)!");
            }
            // See if this mapping is already there (it should be, if it's not, we throw an exception).
            Object temp = mappings.get(residue);
            if(temp == null) {
                throw new IOException("Residue '" + residue + "' was only present in the average mass delta mappings and NOT in the monoisotopic ones!");
            }
            // Okay, temp is not 'null', so cast it, mold it, group with the average and store it again as a a double[].
            double mono = ((Double)temp).doubleValue();
            mappings.put(residue, new double[]{mono, avg});
            keys.remove(residue);
        }

        // Check our keys arraylist.
        if(keys.size() > 0) {
            // This is not good.
            // Generate a nice report, though.
            Iterator iter = keys.iterator();
            StringBuffer residues = new StringBuffer();
            while(iter.hasNext()) {
                String residue = (String)iter.next();
                residues.append(residue + " ");
            }
            throw new IOException("The following residues all had a monoisotopic mass delta mapping, yet no average mass dleta mapping: " + residues.toString() + "!");
        }
        // Finis!
        return mappings;
    }

    /**
     * This method checks the 'iInitialized' boolean. When it is not set, it will
     * initialize the factory based on the input method set in the 'ModificationFactory.properties file'. <br />
     * Note that RDBMS has precedence over file.
     */
    private static void checkInit() {
        // See if we are initialized. If not, do the following:
        //  1. Find the properties file.
        //  2. Determine which resource (files or RDBMS) to use for retrieving data.
        //  3. Call the appropriate retrieve methods.
        //  4. If all goes well, set iInitialized to 'true' and be done with it.
        if(!iInitialized) {
            InputStream is = ModificationFactory.class.getClassLoader().getResourceAsStream("ModificationFactory.properties");
            if(is == null) {
                loadAllFromFiles("modifications.txt", "modificationConversion.txt");
            } else {
                // Okay, load the Properties.
                Properties props = new Properties();
                try {
                    props.load(is);
                    // Okay, let's see what we should do.
                    // If we find the RDBMS tag, we go with that, else try to find the MODFILE tag.
                    if(props.containsKey(RDBMS)) {
                        // Handle RDBMS.
                        String db = props.getProperty(RDBMS);
                        String driver = props.getProperty(RDBDRIVER);
                        String table = props.getProperty(TABLE);
                        String user = props.getProperty(USER);
                        String password = props.getProperty(PASSWORD);
                        // Start doing validations.
                        // We need db, driver and table info. User and password are optional,
                        // yet if either one is present, the other must be present as well.
                        if(db == null || db.trim().equals("")) {
                            throw new IOException(RDBMS + " key defined  in 'ModificationFactory.properties', yet its value was 'null' or empty String!");
                        } else if(driver == null || driver.trim().equals("")) {
                            throw new IOException(RDBMS + " key defined  in 'ModificationFactory.properties', yet mandatory " + RDBDRIVER + " 'null' or empty String!");
                        } else if(table == null || table.trim().equals("")) {
                            throw new IOException(RDBMS + " key defined  in 'ModificationFactory.properties', yet mandatory " + TABLE + " 'null' or empty String!");
                        }
                        // Now do the user and password.
                        Properties dbProps = new Properties();
                        if(user != null && !user.trim().equals("")) {
                            if(password == null || password.trim().equals("")) {
                                throw new IOException(USER + " key defined  in 'ModificationFactory.properties', yet mandatory " + PASSWORD + " 'null' or empty String!");
                            } else {
                                // Some DB drivers like 'user', some like 'username'.
                                dbProps.put("user", user.trim());
                                dbProps.put("username", user.trim());
                                dbProps.put("password", password.trim());
                            }
                        } else if(password != null && !password.trim().equals("")) {
                            throw new IOException(PASSWORD + " key defined  in 'ModificationFactory.properties', yet mandatory " + USER + " 'null' or empty String!");
                        }
                        // Okay, let 'er roll.
                        loadAllFromRDBMS(db, driver, table, dbProps);
                    } else if(props.containsKey(MODFILE)) {
                        // Handle file-input.
                        String mods = props.getProperty(MODFILE);
                        String codes = props.getProperty(CODEFILE);
                        // Check if anything sensible is contained in these.
                        if(mods == null || mods.trim().equals("")) {
                            throw new IOException("No " + RDBMS + " key defined in 'ModificationFactory.properties' and " + MODFILE + " was 'null' or empty String!");
                        } else if(codes == null || codes.trim().equals("")) {
                            throw new IOException("No " + RDBMS + " key defined in 'ModificationFactory.properties' and mandatory " + CODEFILE + " was 'null' or empty String!");
                        } else {
                            // Okay, passed the test. Go get 'em!
                            loadAllFromFiles(mods, codes);
                        }
                    }
                    iInitialized = true;
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
