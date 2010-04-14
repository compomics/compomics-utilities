/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 24-dec-02
 * Time: 16:30:42
 */
package com.compomics.util.db;
import org.apache.log4j.Logger;

import java.sql.Types;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class converts column types into Strings representing
 * corresponding Java types.
 *
 * @author Lennart Martens
 */
public class ColumnTypeConverter {
	// Class specific log4j logger for ColumnTypeConverter instances.
	Logger logger = Logger.getLogger(ColumnTypeConverter.class);

    /**
     * This method converts coded SQL columntypes into
     * the corresponding Java objects (whose names are given
     * int the String[] elements).
     *
     * @param   aTypes  int[] with the coded SQL column types.
     * @return  String[]    with the names of the Java types.
     */
    public static String[] convertTypes(int[] aTypes, int[] aSizes) {
        String[] types = new String[aTypes.length];

        for(int i=0;i<aTypes.length;i++) {
            String temp = null;
            switch(aTypes[i]) {
                case Types.ARRAY:
                    temp = "byte[]";
                    break;
                case Types.BIGINT:
                    temp = "long";
                    break;
                case Types.BINARY:
                    temp = "byte[]";
                    break;
                case Types.BIT:
                    temp = "boolean";
                    break;
                case Types.BLOB:
                    temp = "byte[]";
                    break;
                case Types.CHAR:
                    if(aSizes[i] == 1) {
                        temp = "char";
                    } else {
                        temp = "String";
                    }
                    break;
                case Types.CLOB:
                    temp = "String";
                    break;
                case Types.DATE:
                    temp = "java.sql.Timestamp";
                    break;
                case Types.DECIMAL:
                    temp = "Number";
                    break;
                case Types.DISTINCT:
                    temp = "byte[]";
                    break;
                case Types.DOUBLE:
                    temp = "double";
                    break;
                case Types.FLOAT:
                    temp = "double";
                    break;
                case Types.INTEGER:
                    temp = "long";
                    break;
                case Types.JAVA_OBJECT:
                    temp = "Object";
                    break;
                case Types.LONGVARBINARY:
                    temp = "byte[]";
                    break;
                case Types.LONGVARCHAR:
                    temp = "String";
                    break;
                case Types.NULL:
                    temp = "null";
                    break;
                case Types.NUMERIC:
                    temp = "Number";
                    break;
                case Types.OTHER:
                    temp = "Object";
                    break;
                case Types.REAL:
                    temp = "double";
                    break;
                case Types.REF:
                    temp = "String";
                    break;
                case Types.SMALLINT:
                    temp = "int";
                    break;
                case Types.STRUCT:
                    temp = "Object";
                    break;
                case Types.TIME:
                    temp = "java.sql.Time";
                    break;
                case Types.TIMESTAMP:
                    temp = "java.sql.Timestamp";
                    break;
                case Types.TINYINT:
                    temp = "int";
                    break;
                case Types.VARBINARY:
                    temp = "byte[]";
                    break;
                case Types.VARCHAR:
                    temp = "String";
                    break;
                default :
                    break;
            }
            types[i] = temp;
        }

        return types;
    }
}
