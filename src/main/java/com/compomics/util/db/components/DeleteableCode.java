/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-dec-02
 * Time: 13:25:54
 */
package com.compomics.util.db.components;
import org.apache.log4j.Logger;

import com.compomics.util.db.DBMetaData;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class genereates the code to make a DBAccessor class an implementation of the Deleteable interface.
 *
 * @author Lennart Martens
 */
public class DeleteableCode {

    /**
     * Empty default constructor
     */
    public DeleteableCode() {
    }

    // Class specific log4j logger for DeleteableCode instances.
    Logger logger = Logger.getLogger(DeleteableCode.class);

    /**
     * This String will hold the generated code for this component.
     */
    private String iCode = null;

    /**
     * This constructor will generate the code to make the DBAccessor an implementation
     * of the Deleteable interface, based on the specified metadata.
     *
     * @param   aMeta   DBMetaData with the metadata to generate the code for.
     */
    public DeleteableCode(DBMetaData aMeta) {
        // First gather the needed information.
        String table = aMeta.getTableName();
        String[] pkCols = aMeta.getPrimaryKeyColumns();

        // Generate the query for the delete.
        StringBuffer query = new StringBuffer("\"DELETE FROM " + table + " WHERE ");
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
                "\t/**\n\t * This method allows the caller to delete the data represented by this\n\t * object in a persistent store.\n");
        lsb.append("\t *\n\t * @param   aConn Connection to the persitent store.\n\t */\n");
        lsb.append("\tpublic int delete(Connection aConn) throws SQLException {\n");
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
        lsb.append("\t\tint result = lStat.executeUpdate();\n");
        lsb.append("\t\tlStat.close();\n");
        lsb.append("\t\treturn result;\n");
        lsb.append("\t}\n");

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
