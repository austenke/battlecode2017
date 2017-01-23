package Robots;

import battlecode.common.*;
import Helpers.HelperMethods;
import Helpers.Movement;

/**
 * Class for soldier robot.
 */
public class Soldier {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Soldier(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() throws GameActionException {
        MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
        MapLocation myArchon = archons[0];

        for (MapLocation archonLoc : archons) {
            if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                myArchon = archonLoc;
            }
        }

        int soldierCount = rc.readBroadcast(3);
        int stayWithMe = -1;
        if (soldierCount < 3 || (soldierCount + 1) % 3 == 0) {
            stayWithMe = 1;
            //System.out.println("I'm a builder gardener!");
        } else {
            stayWithMe = 0;
            //System.out.println("I'm a planter gardener!");
        }
        rc.broadcast(3, soldierCount + 1);

        while (true){
            try{
                if(stayWithMe == 0){
                    attackSoldier();
                }else if(stayWithMe == 1){
                    defenseSoldier(myArchon);
                }
                Clock.yield();
            }catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
    static void attackSoldier() throws GameActionException{
        Team enemy = rc.getTeam().opponent();

        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();
                MapLocation tankLoc = new MapLocation(rc.readBroadcast(0),rc.readBroadcast(1));

                // See if there are any nearby enemy robots
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);

                // See if there are any nearby friendly robots
                RobotInfo[] friendlyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

                // If there are some...
                if (enemyRobots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        RobotInfo lowestHealthRobot = enemyRobots[0];
                        for (RobotInfo robot : enemyRobots) {
                            if (lowestHealthRobot.health > robot.health) {
                                lowestHealthRobot = robot;
                            }
                        }
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(lowestHealthRobot.getLocation()));
                    }

                    move.stayInLocationRange(enemyRobots[0].getLocation(),
                            (int) rc.getType().sensorRadius - 2, (int) rc.getType().sensorRadius);
                }
                else {
                    move.move();
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                try {
                    rc.setIndicatorDot(rc.getLocation(), 66, 134, 244);
                }
                catch (Exception f) { }
                e.printStackTrace();
            }
        }
    }

    static void defenseSoldier(MapLocation myArchon) throws GameActionException{
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        move.stayInLocationRange(myArchon, 0, 20);

        if(enemyRobots.length != 0){
            RobotInfo lowestHealthRobot = enemyRobots[0];
            for (RobotInfo robot : enemyRobots) {
                if (lowestHealthRobot.health > robot.health) {
                    lowestHealthRobot = robot;
                }
            }
            rc.fireSingleShot(rc.getLocation().directionTo(lowestHealthRobot.getLocation()));
        }
    }
}
