package Robots;

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

    public Archon(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction[] dirList = RobotPlayer.getDirList();
                Direction dir = dirList[0];
                for(Direction d : dirList){
                    if(rc.canBuildRobot(RobotType.GARDENER,d)){
                        dir = d;
                        break;
                    }
                }
                int numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
                if (rc.getRobotCount() < 10) {
                    if (rc.canHireGardener(dir) && (rc.getRobotCount() == numArchons || rc.getRobotCount() == numArchons + 1 || rc.getRobotCount() == numArchons + 3 || rc.getRobotCount() == numArchons + 5 || rc.getRobotCount() == numArchons + 7)) {
                        rc.hireGardener(dir);
                    }
                }else if (rc.getRobotCount() % 5 == 4 && rc.canHireGardener(dir)){
                        rc.hireGardener(dir);
                }

                //tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                /*MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);*/

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
}