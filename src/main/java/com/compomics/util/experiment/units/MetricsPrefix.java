/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.experiment.units;

import java.io.Serializable;

/**
 * Enumeration of the metrics prefixes.
 *
 * @author Marc Vaudel
 */
public enum MetricsPrefix implements Serializable {

    yotta("yotta", "Y", 24),
    zetta("zetta", "Z", 21),
    exa("exa", "E", 18),
    peta("peta", "P", 15),
    tera("tera", "T", 12),
    giga("giga", "G", 9),
    mega("mega", "M", 6),
    kilo("kilo", "k", 3),
    hecto("hecto", "h", 2),
    deca("deca", "da", 1),
    deci("deci", "d", -1),
    centi("centi", "c", -2),
    milli("milli", "m", -3),
    micro("micro", "μ", -6),
    nano("nano", "n", -9),
    pico("pico", "p", -12),
    femto("femto", "f", -15),
    atto("atto", "a", -18),
    zepto("zepto", "z", -21),
    yocto("yocto", "y", -24);

    /**
     * The power of 10 to use
     */
    public final int power;
    /**
     * The prefix in full letters
     */
    public final String prefix;
    /**
     * The prefix symbol
     */
    public final String symbol;

    /**
     * Constructor.
     *
     * @param prefix The prefix in full letters
     * @param symbol The prefix symbol
     * @param power The power of 10 to use
     */
    private MetricsPrefix(String prefix, String symbol, int power) {
        this.prefix = prefix;
        this.symbol = symbol;
        this.power = power;
    }

}
