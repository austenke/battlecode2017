package Robots;

import Helpers.Economy;
import Helpers.HelperMethods;
import Main.RobotPlayer;
import battlecode.common.*;

import static Helpers.HelperMethods.randomDirection;

/**
 * Class for archon robot.
 */
public class Archon {
    static RobotController rc;
    static HelperMethods helpers;
    static Economy econ;

    public Archon(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.econ = new Economy(rc);

    }

    public static void run() throws GameActionException {
        econ.build();
        // Our team tank x location
        rc.broadcast(0, -1);

        // Our team tank y location
        rc.broadcast(1, -1);

        // Gardener count
        rc.broadcast(2, 0);

        // Tank x location
        rc.broadcast(3, -1);

        // Tank y location
        rc.broadcast(4, -1);

        // Soldier x location
        rc.broadcast(5, -1);

        // Soldier y location
        rc.broadcast(6, -1);

        // Gardener x location
        rc.broadcast(7, -1);

        // Gardener y location
        rc.broadcast(8, -1);

        // Gardener archon dist
        rc.broadcast(9, 6);

        // Scout count
        rc.broadcast(10, 0);

        Direction[] dirList = RobotPlayer.getDirList();

        // The code you want your robot to perform every round should be in this loop
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                econ.build();

                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}