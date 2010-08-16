package com.compomics.util.experiment.massspectrometry;

/**
 * Created by IntelliJ IDEA.
 * User: Marc
 * Date: Jun 23, 2010
 * Time: 10:01:29 AM
 * This class will modelize a charge.
 */
public class Charge {

    public final static int PLUS = +1;
    public final static int MINUS = -1;
    public final static int NEUTRAL = 0;

    public int sign;
    public int value;

    public Charge(int sign, int value) {
        this.sign = sign;
        this.value = value;
    }

    public String toString() {
        switch (sign) {
            case PLUS:
                return value + "+";
            case MINUS:
                return value + "-";
            default:
                return "";
        }
    }
}
