/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 31-jan-2005
 * Time: 17:53:30
 */
package com.compomics.util.interfaces;

/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/02 10:40:14 $
 */

/**
 * This interface describes the behaviour for a class that wraps a table element.
 *
 * @author Lennart Martens
 * @version $Id: TableValueWrapper.java,v 1.1 2005/02/02 10:40:14 lennart Exp $
 */
public interface TableValueWrapper {

    /**
     * This method returns the Object that this instance wraps around.
     *
     * @return  Object with the original instance to be put on the table.
     */
    public abstract Object getValue();
}
