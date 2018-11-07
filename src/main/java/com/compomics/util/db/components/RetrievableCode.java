/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 29-dec-02
 * Time: 14:24:22
 */
package com.compomics.util.db.components;
import org.apache.log4j.Logger;

import com.compomics.util.db.DBMetaData;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2009/07/23 10:59:18 $
 */

/**
 * This class generates the code to make a DBAccessor an implementation of the Retrieveable
 * interface.
 *
 * @author Lennart Martens
 */
public class RetrievableCode {

    /**
     * Empty default constructor
     */
    public RetrievableCode() {
    }

    // Class specific log4j logger for RetrievableCode instances.
    Logger logger = Logger.getLogger(RetrievableCode.class);

    /**
     * This String will hold the generated code.
     */
    private String iCode = null;

    /**
     * This constructor allows the generation of the code for the implementation of
     * the Retrievable interface for a DBAccessor class, based on the specified metadata.
     *
     * @param   aMeta   DBMetaData  with the metadata to generate the code for.
     */
    public RetrievableCode(DBMetaData aMeta) {
        // First get the primary key columns.
        String[] pkCols = aMeta.getPrimaryKeyColumns();
        // Next get all column names and types.
        String[] names = aMeta.getColumnNames();
        String[] types = aMeta.getConvertedColumnTypes();
        // The tablename.
        String table = aMeta.getTableName();

        // The 'select' query.
        StringBuffer query = new StringBuffer("\"SELECT * FROM " + table + " WHERE ");
        for(int i = 0; i < pkCols.length; i++) {
            String lCol = pkCols[i];
            // First one does not have leading 'AND'.
            if(i == 0) {
                query.append(lCol + " = ?");
            } else {
                query.append(" AND " + lCol + " = ?");
            }
        }
        query.append("\"");

        // Now generate the code.
        StringBuffer lsb = new StringBuffer(
                "\t/**\n\t * This method allows the caller to read data for this\n\t * object from a persistent store based on the specified keys.\n");
        lsb.append("\t *\n\t * @param   aConn Connection to the persitent store.\n\t */\n");
        lsb.append("\tpublic void retrieve(Connection aConn, HashMap aKeys) throws SQLException {\n");
        lsb.append("\t\t// First check to see whether all PK fields are present.\n");
        for(int i=0;i<pkCols.length;i++) {
            lsb.append("\t\tif(!aKeys.containsKey(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ")) {\n");
            lsb.append("\t\t\tthrow new IllegalArgumentException(\"Primary key field '" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase())
                    + "' is missing in HashMap!\");\n");
            lsb.append("\t\t} else {\n");
            String lCol = pkCols[i];
            String name = "i" + lCol.substring(0,1).toUpperCase() + lCol.substring(1).toLowerCase();
            lsb.append("\t\t\t" + name);
            // Check the type. If it is Object or array, a cast suffices. If it is a primitive, we'll have to accommodate.
            String type = aMeta.getConvertedColumnType(i);
            if(Character.isLowerCase(type.charAt(0)) && !type.endsWith("[]")) {
                // Primitive. Let's accommodate.
                if(type.equals("int")) {
                    lsb.append(" = ((Integer)aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ")).intValue();\n");
                } else if(type.equals("long")) {
                    lsb.append(" = ((Long)aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ")).longValue();\n");
                } else if(type.equals("double")) {
                    lsb.append(" = ((Double)aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ")).doubleValue();\n");
                } else if(type.equals("char")) {
                    lsb.append(" = ((Character)aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ")).charValue();\n");
                } else if(type.equals("boolean")) {
                    lsb.append(" = ((Boolean)aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ")).booleanValue();\n");
                } else {
                    // Do an object cast as well. Better then nothing...
                    lsb.append(" = (" + aMeta.getConvertedColumnType(i) + ")aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ");\n");
                }
            } else {
                // Object type, cast directly.
                lsb.append(" = (" + aMeta.getConvertedColumnType(i) + ")aKeys.get(" + (Character.isDigit(pkCols[i].charAt(0))?"i"+pkCols[i].toUpperCase():pkCols[i].toUpperCase()) + ");\n");
            }
            lsb.append("\t\t}\n");
        }
        lsb.append("\t\t// In getting here, we probably have all we need to continue. So let's...\n");
        lsb.append("\t\tPreparedStatement lStat = aConn.prepareStatement(" + query.toString() + ");\n");
        for(int i = 0; i < pkCols.length; i++) {
            String lCol = pkCols[i];
            String name = "i" + lCol.substring(0,1).toUpperCase() + lCol.substring(1).toLowerCase();
            String type = aMeta.getConvertedColumnType(lCol);
            // See if we have an Object type, or something else.
            if(Character.isUpperCase(type.charAt(0)) || type.indexOf(".") >= 0) {
                lsb.append("\t\tlStat.setObject(" + (i+1) + ", " + name + ");\n");
            } else {
                if(type.equals("byte[]")) {
                    // A Byte[] is set using an inputstream.
                    lsb.append("\t\tByteArrayInputStream bais" + i + " = new ByteArrayInputStream(" + name + ");\n");
                    lsb.append("\t\tlStat.setBinaryStream(" + (i+1) + ", bais" + i + ", " + name + ".length);\n");
                } else if(type.equals("int")) {
                    lsb.append("\t\tlStat.setInt(" + (i+1) + ", " + name + ");\n");
                } else if(type.equals("long")) {
                    lsb.append("\t\tlStat.setLong(" + (i+1) + ", " + name + ");\n");
                } else if(type.equals("double")) {
                    lsb.append("\t\tlStat.setDouble(" + (i+1) + ", " + name + ");\n");
                } else if(type.equals("char")) {
                    lsb.append("\t\tlStat.setObject(" + (i+1) + ", new Character(" + name + "));\n");
                } else if(type.equals("boolean")) {
                    lsb.append("\t\tlStat.setBoolean(" + (i+1) + ", " + name + ");\n");
                }
            }
        }
        lsb.append("\t\tResultSet lRS = lStat.executeQuery();\n");
        lsb.append("\t\tint hits = 0;\n");
        lsb.append("\t\twhile(lRS.next()) {\n");
        lsb.append("\t\t\thits++;\n");
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            String varName = "i" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            String type = types[i];
            // Object types are easy.
            // Others need some specific treatment.
            if(Character.isUpperCase(type.charAt(0)) || type.indexOf(".") >= 0) {
                lsb.append("\t\t\t" + varName + " = (" + type + ")lRS.getObject(\"" + name + "\");\n");
            } else {
                if(type.equals("byte[]")) {
                    lsb.append("\t\t\tInputStream is" + i + " = lRS.getBinaryStream(\"" + name + "\");\n");
                    lsb.append("\t\t\tVector bytes" + i + " = new Vector();\n");
                    lsb.append("\t\t\tint reading = -1;\n");
                    lsb.append("\t\t\ttry {\n");
                    lsb.append("\t\t\t\twhile((reading = is" + i + ".read()) != -1) {\n");
                    lsb.append("\t\t\t\t\tbytes" + i + ".add(new Byte((byte)reading));\n");
                    lsb.append("\t\t\t\t}\n");
                    lsb.append("\t\t\t\tis" + i + ".close();\n");
                    lsb.append("\t\t\t} catch(IOException ioe) {\n");
                    lsb.append("\t\t\t\tbytes" + i + " = new Vector();\n");
                    lsb.append("\t\t\t}\n");
                    lsb.append("\t\t\treading = bytes" + i + ".size();\n");
                    lsb.append("\t\t\t" + varName + " = new byte[reading];\n");
                    lsb.append("\t\t\tfor(int i=0;i<reading;i++) {\n");
                    lsb.append("\t\t\t\t" + varName + "[i] = ((Byte)bytes" + i + ".get(i)).byteValue();\n");
                    lsb.append("\t\t\t}\n");
                } else if(type.equals("int")) {
                    lsb.append("\t\t\t" + varName + " = lRS.getInt(\"" + name + "\");\n");
                } else if(type.equals("long")) {
                    lsb.append("\t\t\t" + varName + " = lRS.getLong(\"" + name + "\");\n");
                } else if(type.equals("double")) {
                    lsb.append("\t\t\t" + varName + " = lRS.getDouble(\"" + name + "\");\n");
                } else if(type.equals("char")) {
                    lsb.append("\t\t\tString temp" + varName + " = lRS.getString(\"" + name + "\");\n");
                    lsb.append("\t\t\tif(temp" + varName + " != null) {\n");
                    lsb.append("\t\t\t\t" + varName + " = temp" + varName + ".charAt(0);\n");
                    lsb.append("\t\t\t} else {\n");
                    lsb.append("\t\t\t\t" + varName + " = ' ';\n");
                    lsb.append("\t\t\t}\n");
                } else if(type.equals("boolean")) {
                    lsb.append("\t\t\t" + varName + " = lRS.getBoolean(\"" + name + "\");\n");
                }
            }
        }
        lsb.append("\t\t}\n");
        lsb.append("\t\tlRS.close();\n");
        lsb.append("\t\tlStat.close();\n");
        lsb.append("\t\tif(hits>1) {\n");
        lsb.append("\t\t\tthrow new SQLException(\"More than one hit found for the specified primary keys in the '"
                + table + "' table! Object is initialized to last row returned.\");\n");
        lsb.append("\t\t} else if(hits == 0) {\n");
        lsb.append("\t\t\tthrow new SQLException(\"No hits found for the specified primary keys in the '"
                + table + "' table! Object is not initialized correctly!\");\n");
        lsb.append("\t\t}\n");
        lsb.append("\t}\n");


        // Add a simple static 'basic select' getter.
        lsb.append("\t/**\n\t * This method allows the caller to obtain a basic select for this table.\n");
        lsb.append("\t *\n");
        lsb.append("\t * @return   String with the basic select statement for this table.\n");
        lsb.append("\t */\n");
        lsb.append("\tpublic static String getBasicSelect(){\n");
        lsb.append("\t\treturn \"select * from " + table + "\";\n");
        lsb.append("\t}\n\n");


        // Add another static convenience method that retrieves all the entries in the DB for this table.
        String tableAsClassnameStart = table.substring(0, 1).toUpperCase() + table.substring(1).toLowerCase();

        lsb.append("\t/**\n\t * This method allows the caller to obtain all rows for this\n\t * table from a persistent store.\n");
        lsb.append("\t *\n\t * @param   aConn Connection to the persitent store.\n");
        lsb.append("\t * @return   ArrayList<" + tableAsClassnameStart + "TableAccessor>   with all entries for this table.\n\t */\n");
        lsb.append("\tpublic static ArrayList<" + tableAsClassnameStart + "TableAccessor> retrieveAllEntries(Connection aConn) throws SQLException {\n");
        lsb.append("\t\tArrayList<" + tableAsClassnameStart + "TableAccessor>  entities = new ArrayList<" + tableAsClassnameStart + "TableAccessor>();\n");
        lsb.append("\t\tStatement stat = aConn.createStatement();\n");
        lsb.append("\t\tResultSet rs = stat.executeQuery(getBasicSelect());\n");
        lsb.append("\t\twhile(rs.next()) {\n");
        lsb.append("\t\t\tentities.add(new " + tableAsClassnameStart + "TableAccessor(rs));\n");
        lsb.append("\t\t}\n");
        lsb.append("\t\trs.close();\n");
        lsb.append("\t\tstat.close();\n");
        lsb.append("\t\treturn entities;\n");
        lsb.append("\t}\n\n");

        this.iCode = lsb.toString();
    }

    /**
     * This method will report on the generated code.
     *
     * @return  String  with the generated code.
     */
    public String toString() {
        return "\n" + iCode + "\n";
    }
}
