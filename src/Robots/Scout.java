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
            try {
                MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam().opponent());
                MapLocation myArchon = archons[0];

                for (MapLocation archonLoc : archons) {
                    if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                        myArchon = archonLoc;
                    }
                }

                move.stayInLocationRange(myArchon, 10, 30);
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
}
