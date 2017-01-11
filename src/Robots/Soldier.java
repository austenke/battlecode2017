package Robots;

import battlecode.common.*;
import Helpers.HelperMethods;
import java.awt.*;

/**
 * Class for soldier robot.
 */
public class Soldier {
    static RobotController rc;
    static HelperMethods helpers;

    public Soldier(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() {
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);

                // See if there are any nearby friendly robots
                RobotInfo[] friendlyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

                // If there are some...
                if (enemyRobots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(enemyRobots[0].location));
                    }
                    if (enemyRobots[0].getLocation().isWithinDistance(rc.getLocation(), rc.getType().sensorRadius - 1)) {
                        helpers.tryMove(rc.getLocation().directionTo(enemyRobots[0].getLocation()).opposite());
                    } else {
                        helpers.tryMove(rc.getLocation().directionTo(enemyRobots[0].getLocation()));
                    }
                }
                else {
                    // Move randomly
                    helpers.tryMove(helpers.randomDirection());
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
