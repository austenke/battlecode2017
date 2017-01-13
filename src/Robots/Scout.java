package Robots;

import Helpers.HelperMethods;
import battlecode.common.*;

/**
 * Class for scout robot
 */
public class Scout {
    static RobotController rc;
    static HelperMethods helpers;
    static Direction goingDirection;

    public Scout(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException {
        goingDirection = HelperMethods.randomDirection();
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            goingDirection = roam(goingDirection);
        }
    }

    public static Direction roam(Direction dir) throws GameActionException {
        return HelperMethods.stayInLocationRange(dir, rc.getLocation(), 30, 1000);
    }

    public static void detect() {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

    }
}
