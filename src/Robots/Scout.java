package Robots;

import Helpers.HelperMethods;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Class for scout robot
 */
public class Scout {
    static RobotController rc;
    static HelperMethods helpers;

    public Scout(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
        }
    }
}
