package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import battlecode.common.*;

/**
 * Class for scout robot
 */
public class Scout {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Scout(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            move.move();
            Clock.yield();
        }
    }

    public static void detect() {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo enemy : nearbyEnemies) {
            for (RobotInfo linkedUnits : nearbyEnemies) {

            }
        }
    }
}
