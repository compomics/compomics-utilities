/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-feb-03
 * Time: 18:19:45
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
 * This interface describes the behaviour for a class that can pick up results.
 *
 * @author Lennart Martens
 */
public interface PickUp {

    /**
     * This method should be called by the notifier when appropriate.
     *
     * @param   aObject Object with the data that should be sent.
     */
    public void sendIncoming(Object aObject);
}
