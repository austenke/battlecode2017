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
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    System.out.println("Sensed robots");
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                        System.out.println("-----firing-----");
                    }
                    else {
                        if (robots[0].getLocation().isWithinDistance(rc.getLocation(), rc.getType().sensorRadius - 3)) {
                            helpers.tryMove(rc.getLocation().directionTo(robots[0].getLocation()).opposite());
                        } else {
                            helpers.tryMove(rc.getLocation().directionTo(robots[0].getLocation()));
                        }
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
