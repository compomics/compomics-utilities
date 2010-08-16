package com.compomics.util.experiment.identification;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 22, 2010
 * Time: 1:55:22 PM
 * The advocate of a hit can be a search engine, a rescoring algorithm, etc.
 */
public interface Advocate extends Serializable {

    // search engines ids

    public static final int MASCOT = 0;
    public static final int OMSSA = 1;
    public static final int XTANDEM = 2;
    public static final int PEPTIZER = 3;


    public String getName();

    public int getId();
}
