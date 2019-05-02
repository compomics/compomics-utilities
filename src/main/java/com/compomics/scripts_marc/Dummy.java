package com.compomics.scripts_marc;

/**
 *
 * @author Marc Vaudel
 */
public class Dummy {
    
    public synchronized void delay(long delay) throws InterruptedException {
        
        this.wait(delay);
        
    }

}
