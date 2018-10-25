package com.compomics.util.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import org.apache.commons.math.util.FastMath;

/**
 * Functions operating with BigDecimal objects.
 *
 * @author Marc Vaudel
 */
public class BigFunctions {

    /**
     * Empty default constructor
     */
    public BigFunctions() {
    }

    /**
     * Cache for the base used for the log.
     */
    private static double logBase = 0;
    /**
     * Cache for the logarithm value of the base used for the log.
     */
    private static BigDecimal logBaseValue;
    /**
     * Cache for factorials.
     */
    private static final HashMap<BigInteger, BigInteger> factorialsCache = new HashMap<BigInteger, BigInteger>(1000);

    /**
     * Returns n! as BigInteger.
     *
     * @param n a given BigInteger
     *
     * @return the corresponding factorial
     */
    public static BigInteger factorial(BigInteger n) {
        if (n.compareTo(BigInteger.ZERO) == -1) {
            throw new ArithmeticException("Attempting to calculate the factorial of a negative number.");
        }
        if (n.compareTo(BigInteger.ONE) != 1) {
            return BigInteger.ONE;
        } else {
            if (n.compareTo(new BigInteger("21")) == -1) {
                return new BigInteger(BasicMathFunctions.factorial(n.intValue()) + "");
            }
            if (n.compareTo(BigMathUtils.thousand.toBigInteger()) == -1) {
                BigInteger result = factorialsCache.get(n);
                if (result == null) {
                    result = estimateFactorial(n);
                }
                return result;
            }
            BigInteger nMinusOne = factorial(n.subtract(BigInteger.ONE));
            return nMinusOne.multiply(n);
        }
    }

    /**
     * Estimates the factorial in a synchronous method as part of the factorial
     * method.
     *
     * @param n a given BigInteger
     *
     * @return the corresponding factorial
     */
    private static synchronized BigInteger estimateFactorial(BigInteger n) {
        BigInteger result = factorialsCache.get(n);
        if (result == null) {
            result = factorial(n.subtract(BigInteger.ONE)).multiply(n);
            factorialsCache.put(n, result);
        }
        return result;
    }

    /**
     * Returns n!/k! as BigInteger.
     *
     * @param n a given BigInteger
     * @param k a given BigInteger
     *
     * @return the corresponding factorial
     */
    public static BigInteger factorial(BigInteger n, BigInteger k) {
        if (n.compareTo(k) == -1) {
            throw new ArithmeticException("n < k in n!/k!.");
        }
        if (n.compareTo(k) == 0) {
            return BigInteger.ONE;
        } else {
            return factorial(n.subtract(BigInteger.ONE), k).multiply(n);
        }
    }

    /**
     * Returns the number of k-combinations in a set of n elements as a big
     * decimal.
     *
     * @param k the number of k-combinations
     * @param n the number of elements
     *
     * @return the number of k-combinations in a set of n elements
     */
    public static BigInteger getCombination(BigInteger k, BigInteger n) {
        if (k.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ONE;
        } else if (k.compareTo(n) == -1) {
            BigInteger numerator = factorial(n, k);
            BigInteger denominator = factorial(n.subtract(k));
            BigInteger result = numerator.divide(denominator);
            return result;
        } else if (k.compareTo(n) == 0) {
            return BigInteger.ONE;
        } else {
            throw new IllegalArgumentException("n>k in combination.");
        }
    }

    /**
     * Returns the natural logarithm of a big decimal. FastMath method is used
     * when possible. Results are not rounded.
     *
     * @param bigDecimal the big decimal to estimate the log on
     * @param mathContext the math context to use for the calculation
     *
     * @return the log of a big decimal
     */
    public static BigDecimal ln(BigDecimal bigDecimal, MathContext mathContext) {
        if (bigDecimal.compareTo(BigDecimal.ZERO) != 1) {
            throw new IllegalArgumentException("Attempting to estimate the log of 0.");
        } else if (bigDecimal.compareTo(BigDecimal.ONE) == 0) {
            // log(1)=0
            return BigDecimal.ZERO;
        } else if (bigDecimal.compareTo(BigMathUtils.E) == 0) {
            // log(1)=0
            return BigDecimal.ZERO;
        }

        int precision = mathContext.getPrecision();
        boolean inRange = false;
        double deltaInf = FastMath.pow(10, -precision - 1); // one order of magnitude as margin
        if (precision < 300) {
            double deltaSup = FastMath.pow(10, -precision + 1);
            // try to find the range where FastMath methods can be used
            if (bigDecimal.compareTo(BigDecimal.ONE) == 1) {
                BigDecimal maxValue = new BigDecimal(Double.MAX_VALUE * (1 - deltaSup));
                if (bigDecimal.compareTo(maxValue) == -1) {
                    inRange = true;
                }
            } else {
                BigDecimal minValue = new BigDecimal(Double.MIN_NORMAL * (1 + deltaSup));
                if (bigDecimal.compareTo(minValue) == 1) {
                    inRange = true;
                }
            }
        }
        if (inRange) {
            double doubleValue = bigDecimal.doubleValue();
            double log = FastMath.log(doubleValue);
            double resolution = Math.abs(FastMath.pow(2, -60) / log);
            if (resolution < deltaInf) {
                return new BigDecimal(log);
            }
        }
        return lnBD(bigDecimal, mathContext);
    }

    /**
     * Returns the log of a big decimal. No FastMath method is used, see ln().
     * Results are not rounded.
     *
     * @param bigDecimal the big decimal to estimate the log on
     * @param mathContext the math context to use for the calculation
     *
     * @return the log of a big decimal
     */
    public static BigDecimal lnBD(BigDecimal bigDecimal, MathContext mathContext) {

        if (bigDecimal.compareTo(BigDecimal.ONE) == -1) {
            // ln(x) = -ln(1/x)
            return lnBD(BigDecimal.ONE.divide(bigDecimal, mathContext), mathContext).negate();
        } else if (bigDecimal.compareTo(BigDecimal.TEN) == 1) {
            // ln(x) = 10 + ln(x/10)
            int k = -bigDecimal.scale();
            BigDecimal reducedDecimal = bigDecimal.movePointLeft(k);
            while (reducedDecimal.compareTo(BigDecimal.TEN) == 1) {
                reducedDecimal = reducedDecimal.movePointLeft(1);
                k++;
            }
            MathContext lnMathContext = new MathContext(mathContext.getPrecision() + 1, mathContext.getRoundingMode());
            BigDecimal reducedLog = lnBD(reducedDecimal, lnMathContext);
            int precisionIncrease = ((int) FastMath.log10((double) k)) + 1;
            int lnTenPrecisionNeeded = mathContext.getPrecision() + precisionIncrease;
            MathContext tenMathContext = new MathContext(lnTenPrecisionNeeded, mathContext.getRoundingMode());
            return BigMathUtils.getLn10(tenMathContext).multiply(new BigDecimal(k)).add(reducedLog);
        } else if (bigDecimal.compareTo(BigMathUtils.E) == 1) {
            //ln(x) = 1 + ln(x/e)
            MathContext lnMathContext = new MathContext(mathContext.getPrecision() + 1, mathContext.getRoundingMode());
            BigDecimal logByE = lnBD(bigDecimal.divide(BigMathUtils.E, lnMathContext), lnMathContext);
            return BigDecimal.ONE.add(logByE);
        }

        int precision = mathContext.getPrecision();
        MathContext tailorMathContext = new MathContext(precision + 2, mathContext.getRoundingMode());
        // tailor development
        BigDecimal tailorDevelopment = BigDecimal.ZERO;
        BigDecimal x = (bigDecimal.subtract(BigDecimal.ONE)).divide(bigDecimal.add(BigDecimal.ONE), mathContext),
                xK = x,
                x2 = x.multiply(x),
                limit = new BigDecimal(BigInteger.ONE, precision),
                tailorFactor = BigDecimal.ONE;
        tailorDevelopment = tailorDevelopment.add(x);
        int n = 1;
        if (tailorFactor.compareTo(limit) == -1) {
            throw new IllegalArgumentException("Method not implemented for precision " + precision + ".");
        }
        while (tailorFactor.abs().compareTo(limit) != -1) {
            n += 2;
            xK = xK.multiply(x2);
            tailorFactor = xK.divide(new BigDecimal(n), tailorMathContext);
            tailorDevelopment = tailorDevelopment.add(tailorFactor);
        }
        tailorDevelopment = tailorDevelopment.multiply(BigMathUtils.two);
        return tailorDevelopment;
    }

    /**
     * Returns the log of the input in the desired base. See ln method. Results
     * are not rounded.
     *
     * @param input the input
     * @param base the log base
     * @param mathContext the math context to use for the calculation
     *
     * @return the log value of the input in the desired base.
     */
    public static BigDecimal log(BigDecimal input, double base, MathContext mathContext) {
        if (base <= 0) {
            throw new IllegalArgumentException("Attempting to comupute logarithm of base " + base + ".");
        } else if (base != logBase) {
            logBase = base;
            logBaseValue = ln(new BigDecimal(base), mathContext);
        }
        return ln(input, mathContext).divide(logBaseValue, mathContext);
    }

    /**
     * Returns the value of the exponential of the given BigDecimal using the
     * given MathContext. When possible, the FastMath method is used, and no
     * rounding is performed. Results are not rounded.
     *
     * @param bigDecimal the big decimal
     * @param mathContext the math context
     *
     * @return the value of the exponential of the given BigDecimal using the
     * given MathContext
     */
    public static BigDecimal exp(BigDecimal bigDecimal, MathContext mathContext) {
        if (bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
            // exp(0)=1
            return BigDecimal.ONE;
        } else if (bigDecimal.compareTo(BigDecimal.ONE) == 0) {
            // exp(1)=e
            return BigMathUtils.E;
        }

        int precision = mathContext.getPrecision();
        boolean inRange = false;
        double deltaInf = FastMath.pow(10, -precision - 1);
        if (precision < 300) {
            double deltaSup = FastMath.pow(10, -precision + 1);
            // try to find the range where FastMath methods can be used
            if (bigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                double maxValue = FastMath.log(Double.MAX_VALUE * (1 - deltaSup));
                BigDecimal maxValueBD = new BigDecimal(maxValue);
                if (bigDecimal.compareTo(maxValueBD) == -1) {
                    inRange = true;
                }
            } else {
                double minValue = FastMath.log(Double.MIN_NORMAL * (1 + deltaSup));
                BigDecimal minValueBD = new BigDecimal(minValue);
                if (bigDecimal.compareTo(minValueBD) == 1) {
                    inRange = true;
                }
            }
        }
        if (inRange) {
            double doubleValue = bigDecimal.doubleValue();
            double exp = FastMath.exp(doubleValue);
            double resolution = Math.abs(FastMath.pow(2, -60) / exp);
            if (resolution < deltaInf) {
                return new BigDecimal(exp);
            }
        }
        return expBD(bigDecimal, mathContext);
    }

    /**
     * Returns the estimated maximal value exp can be calculated on according to
     * the mathContext. Attempting to calculate exp on higher values (or lower
     * than -value) will most likely overflow the capacity of a BigDecimal.
     * Results are not rounded.
     *
     * @param mathContext the math context to use for the calculation
     *
     * @return the maximal value exp can be calculated on
     */
    public static BigDecimal getMaxExp(MathContext mathContext) {
        return BigMathUtils.getLn10(mathContext).multiply(new BigDecimal(Integer.MAX_VALUE));
    }

    /**
     * Returns the value of the exponential of the given BigDecimal using the
     * given MathContext. FastMath method is not used, see exp(). Results are
     * not rounded. The result is of precision p=p0-x.log(e) where p0 is the
     * precision of the math context and log is the log 10. First order, no
     * guarantee.
     *
     * @param x the big decimal
     * @param mathContext the math context
     *
     * @return the value of the exponential of the given BigDecimal using the
     * given MathContext
     */
    public static BigDecimal expBD(BigDecimal x, MathContext mathContext) {
        int precision = mathContext.getPrecision();
        if (x.compareTo(BigDecimal.ZERO) == -1) {
            // exp(-x)=1/exp(x)
            return BigDecimal.ONE.divide(expBD(x.negate(), mathContext), mathContext);
        } else if (x.compareTo(BigDecimal.ONE) == 1) {
            // exp(-x)=exp(x/(2^n))^(2^n)
            BigDecimal powerTwo = BigDecimal.ONE;
            int k;
            for (k = 1; k < 30 && powerTwo.compareTo(x) == -1; k *= 2) {
                powerTwo = powerTwo.multiply(BigMathUtils.two);
            }
            MathContext subExpMathContext = new MathContext(precision + 1, mathContext.getRoundingMode());
            BigDecimal reduced = x.divide(powerTwo, subExpMathContext);
            BigDecimal subExp = expBD(reduced, subExpMathContext);
            BigDecimal result = subExp.pow(k);
            return result;
        } else {
            MathContext tailorMathContext = new MathContext(precision + 2, mathContext.getRoundingMode());
            // tailor development
            BigDecimal tailorDevelopment = BigDecimal.ONE,
                    xK = BigDecimal.ONE,
                    tailorFactor = BigDecimal.ONE,
                    limit = new BigDecimal(BigInteger.ONE, precision),
                    factorial = BigDecimal.ONE;
            int n = 0;
            if (tailorFactor.compareTo(limit) == -1) {
                throw new IllegalArgumentException("Method not implemented for precision " + precision + ".");
            }
            while (tailorFactor.abs().compareTo(limit) != -1) {
                n++;
                factorial = factorial.multiply(new BigDecimal(n));
                xK = xK.multiply(x);
                tailorFactor = xK.divide(factorial, tailorMathContext);
                tailorDevelopment = tailorDevelopment.add(tailorFactor);
            }
            return tailorDevelopment;
        }
    }

    /**
     * Returns the first big decimal power the second using the given math
     * context. Results are not rounded. The precision of the result is
     * p=p0-x2.ln(x1).log(e)-log(ln(x1)+x2) where p0 is the precision of the
     * math context and log is the log 10. First order, no guarantee. If x1 is
     * exact, e.g. in 10^x2, p=p0-x2.ln(x1).log(e)-log(ln(x1)) where p0 is the
     * precision of the math context and log is the log 10. First order, no
     * guarantee.
     *
     * @param x1 the first big decimal
     * @param x2 the second big decimal
     * @param mathContext the math context
     *
     * @return the first big decimal power the second using the given math
     * context
     */
    public static BigDecimal pow(BigDecimal x1, BigDecimal x2, MathContext mathContext) {
        BigDecimal lnBigDecimal1 = ln(x1, mathContext);
        BigDecimal x = lnBigDecimal1.multiply(x2);
        return exp(x, mathContext);
    }
}
