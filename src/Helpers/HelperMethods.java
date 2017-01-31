package Helpers;

import battlecode.common.*;

import java.util.Random;

/**
 * Class for common helper methods
 */
public class HelperMethods {
    static RobotController rc;
    static Random rand;

    public HelperMethods(RobotController rc) {
        rand = new Random(rc.getID());
        this.rc = rc;
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return new Direction((float)HelperMethods.randomNum() * 2 * (float)Math.PI);
    }

    public static float randomNum() {
        return rand.nextFloat();
    }
}