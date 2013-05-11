/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.angrypeptide.fun;

/**
 * This class represents a shot
 *
 * @author Marc
 */
public class Shot {
    
    /**
     * The energy of the shot
     */
    private double energy;
    /**
     * The angle of the shot
     */
    private double angle;
    /**
     * The score achieved by this shot
     */
    private double score;
    /**
     * Constructor
     * @param energy the energy of the shoot
     * @param angle the angle of the shoot
     */
    public Shot(double energy, double angle) {
        this.energy = energy;
        this.angle = angle;
    }

    public double getEnergy() {
        return energy;
    }

    public double getAngle() {
        return angle;
    }
    
    /**
     * Returns the distance of the shoot
     * @return 
     */
    public double getDistance() {
        double angleInRadian = 4*Math.PI*angle/360;
        double result = 2*energy/9.81*Math.sin(angleInRadian);
        return result;
    }
    
    /**
     * returns the energy necessary to shoot at a given distance with the given angle
     * @param distance 
     * @param angle the angle in degree (must be between 0 and 90)
     * @return 
     */
    public static double getEnergyForDistance(double distance, double angle) {
        if (angle <= 0 || angle >= 90) {
            throw new IllegalArgumentException("Angle " + angle + " must be between 0 and 90 degrees.");
        }
        double angleInRadian = 4*Math.PI*angle/360;
        double result = distance * 9.81 / (2*Math.sin(angleInRadian));
        return result;
    }
    
    
    /**
     * Returns the minimal energy to reach the distance
     * @param distance 
     * @return 
     */
    public static double getMinEnergyForDistance(double distance) {
        return getEnergyForDistance(distance, 45);
    }
    
    /**
     * Returns a random angle
     * @return 
     */
    public static double getRandomAngle() {
        double randomValue = 90 * Math.random();
        while (randomValue == 0 || randomValue == 90) {
            randomValue = 90 * Math.random();
        }
        return randomValue;
    }
    
    /**
     * Returns a random angle allowing shooting at a given distance given the max energy available.
     * @param distance
     * @param maxEnergy
     * @return 
     */
    public static double getRandomAngleForDistance(double distance, double maxEnergy) {
        Double sin = distance * 9.81 / (2 * maxEnergy);
        if (Math.abs(sin) > 1) {
            throw new IllegalArgumentException("No angle allows shooting at " + distance + " with energy " + maxEnergy);
        }
        Double angleMin = 90 * Math.asin(sin)/Math.PI;
        if (angleMin == Double.NaN || angleMin > 45) {
            throw new IllegalArgumentException("No angle allows shooting at " + distance + " with energy " + maxEnergy);
        } else {
            double random = Math.random();
            double tolerance = 45 - angleMin;
            return angleMin + 2 * random * tolerance;
        }
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
    
    
}
