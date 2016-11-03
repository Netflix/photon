package com.netflix.imflibrary.utils;

/**
 * Created by svenkatrav on 10/26/16.
 */
public class Fraction {
    private final Integer numerator;
    private final Integer denominator;

    /**
     * Instantiates a new Fraction.
     *
     * @param numerator Numerator value of the Fraction
     * @param denominator Denominator value of the Fraction
     */
    public Fraction(Integer numerator, Integer denominator)
    {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Instantiates a new Fraction.
     *
     * @param numerator Numerator value of the Fraction
     */
    public Fraction(Integer numerator)
    {
        this.numerator = numerator;
        this.denominator = 1;
    }

    /**
     *  Returns a fraction holding the value represented by the string argument.
     * @param s the string to be parsed
     * @return Returns a fraction holding the value represented by the string argument
     */
    public static Fraction valueOf(String s)
    {
        try {
            String values[] = s.split("(\\s|/)");
            if (values.length == 2) {
                return new Fraction(Integer.valueOf(values[0]), Integer.valueOf(values[1]));
            }
        }
        catch(Exception e) {
            return null;
        }
        return null;

    }

    /**
     * Getter for the numerator of the rational
     *
     * @return the numerator
     */
    public Integer getNumerator()
    {
        return this.numerator;
    }

    /**
     * Getter for the denominator of the rational
     *
     * @return the denominator
     */
    public Integer getDenominator()
    {
        return this.denominator;
    }

    /**
     * A method to compare 2 rationals, returns true if the rationals match or false if they do not
     * Note : If the object that was passed in is not an instance of rational this method will return
     * false
     * @param other the object that this rational object should be compared with
     * @return result of comparing this rational object with the object that was passed in
     */
    public boolean equals(Object other)
    {
        if (other == null  || !(other instanceof Fraction))
        {
            return false;
        }

        Fraction otherObject = (Fraction)other;
        return (this.numerator.equals(otherObject.getNumerator()) &&
                this.denominator.equals(otherObject.getDenominator()));
    }

    /**
     * A method that returns the sum of hashes corresponding to the numerator and denominator of this rational
     * @return the sum of hashes corresponding to the numerator and denominator of this rational
     */
    public int hashCode()
    {
        Integer hash = 1;
        hash = hash * 31 + this.numerator;
        hash = hash * 31 + this.denominator;
        return hash;
    }

    /**
     * A method that returns a string representation of a Rational object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return String.format("%d/%d", this.numerator, this.denominator);
    }}
