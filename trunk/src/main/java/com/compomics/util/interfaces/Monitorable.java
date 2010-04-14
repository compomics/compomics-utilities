/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 16-okt-02
 * Time: 13:38:49
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
 * This interface describes the behaviour for any Object whose progressing state
 * can be monitored.
 *
 * @author Lennart Martens
 */
public interface Monitorable {

    /**
     * This method reports on the total amount steps the task can take before
     * completion.
     *
     * @return  int with the total bytes that can be read from the InputStream.
     */
    public abstract int getMaximum();

    /**
     * This method returns the progress of the task.
     *
     * @return  int with the progress.
     */
    public abstract int monitorProgress();
}
