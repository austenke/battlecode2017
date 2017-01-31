package Robots;

import Helpers.HelperMethods;
import Main.RobotPlayer;
import battlecode.common.*;

import static Helpers.HelperMethods.randomDirection;

/**
 * Class for archon robot.
 */
public class Archon {
    static RobotController rc = RobotPlayer.rc;
    static HelperMethods helpers = RobotPlayer.helpers;

    public Archon() {

    }

    public static void run() throws GameActionException {
        // Our team tank x location
        rc.broadcast(0, -1);

        // Our team tank y location
        rc.broadcast(1, -1);

        // Gardener count
        rc.broadcast(2, -1);

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
                // Gardener cost
                if (rc.getTeamBullets() > 100) {
                    // Generate a random direction
                    Direction dir;

                    MapLocation[] enemyArchs = rc.getInitialArchonLocations(rc.getTeam().opponent());

                    float dirToEnemyArchon = 9999;
                    Direction bestDir = null;

                    for (Direction d : dirList) {
                        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(d, 5), 104, 244, 66);
                        if (rc.canHireGardener(d)) {
                            float dirTo = Math.abs(d.radiansBetween(rc.getLocation().directionTo(enemyArchs[0])));
                            if (dirTo < dirToEnemyArchon) {
                                bestDir = d;
                                dirToEnemyArchon = dirTo;
                            }
                        }
                    }

                    rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(bestDir, 10), 66, 134, 244);

                    dir = bestDir;

                    if (dir == null) {
                        dir = dirList[0];
                        if (dirList.length < 10) {
                            dirList = RobotPlayer.initDirList(20);
                        }
                    }

                    int numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
                    if (rc.getRobotCount() < 10) {
                        if (rc.canHireGardener(dir) && (rc.getRobotCount() == numArchons || rc.getRobotCount() == numArchons + 1 || rc.getRobotCount() == numArchons + 3 || rc.getRobotCount() == numArchons + 5 || rc.getRobotCount() == numArchons + 7)) {
                            rc.hireGardener(dir);
                        }
                    } else if (rc.getRobotCount() % 5 == 4 && rc.canHireGardener(dir)) {
                        rc.hireGardener(dir);
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}