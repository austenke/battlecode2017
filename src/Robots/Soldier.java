package Robots;

import Main.RobotPlayer;
import battlecode.common.*;
import Helpers.HelperMethods;
import Helpers.Movement;

/**
 * Class for soldier robot.
 */
public class Soldier {
    static RobotController rc = RobotPlayer.rc;
    static HelperMethods helpers = RobotPlayer.helpers;
    static Movement move;

    public Soldier() {
        this.move = new Movement();
    }

    public static void run() throws GameActionException {
        MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
        MapLocation myArchon = archons[0];

        for (MapLocation archonLoc : archons) {
            if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                myArchon = archonLoc;
            }
        }

        // Channel 3 is initialized with -1, add 1 to this
        int soldierCount = rc.readBroadcast(3) + 1;
        int stayWithMe = -1;
        // Broadcasts are weird. This makes things work correctly, don't change this.
        if (soldierCount == 0 || (soldierCount + 1) % 10 < 2) {
            stayWithMe = 1;
        } else {
            stayWithMe = 0;
        }
        rc.broadcast(3, soldierCount + 1);

        while (true){
            try{
                if(stayWithMe == 0){
                    attackSoldier();
                }else{
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
                shootNearby();
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);
                TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, enemy);
                int enemyTankX = rc.readBroadcast(3);
                int enemyTankY = rc.readBroadcast(4);

                if (enemyTankX != -1 && enemyTankY != -1) {
                    MapLocation enemyTankLoc = new MapLocation(enemyTankX, enemyTankY);

                    // See if tank is in area, if not clear broadcast
                    if (rc.getLocation().distanceTo(enemyTankLoc) < 5) {
                        boolean didBroacast = false;
                        for (RobotInfo robot : enemyRobots) {
                            if (robot.getType() == RobotType.TANK) {
                                rc.broadcast(3, (int) robot.getLocation().x);
                                rc.broadcast(4, (int) robot.getLocation().y);
                                didBroacast = true;
                                break;
                            }
                        }
                        if (!didBroacast) {
                            rc.broadcast(3, -1);
                            rc.broadcast(4, -1);
                        }
                    }

                    move.stayInLocationRange(enemyTankLoc,
                            (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                }
                // If there are some enemy robots in area
                else if (enemyRobots.length > 0) {
                    move.stayInLocationRange(enemyRobots[0].getLocation(),
                            (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                    if (enemyRobots[0].getType() == RobotType.SOLDIER) {
                        rc.broadcast(5, (int)enemyRobots[0].getLocation().x);
                        rc.broadcast(6, (int)enemyRobots[0].getLocation().y);
                    }
                    else if (enemyRobots[0].getType() == RobotType.TANK) {
                        rc.broadcast(3, (int)enemyRobots[0].getLocation().x);
                        rc.broadcast(4, (int)enemyRobots[0].getLocation().y);
                    }
                }
                else if (enemyTrees.length > 0) {
                    move.stayInLocationRange(enemyTrees[0].getLocation(),
                            (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                }
                else {
                    int enemySoldierX = rc.readBroadcast(5);
                    int enemySoldierY = rc.readBroadcast(6);
                    int enemyGardeningX = rc.readBroadcast(7);
                    int enemyGardeningY = rc.readBroadcast(8);

                    if (enemySoldierX != -1 && enemySoldierY!= -1) {
                        MapLocation enemySoldierLoc = new MapLocation(enemySoldierX, enemySoldierY);

                        // See if soldier is in area, if not clear broadcast
                        if (rc.getLocation().distanceTo(enemySoldierLoc) < 5) {
                            boolean didBroacast = false;
                            for (RobotInfo robot : enemyRobots) {
                                if (robot.getType() == RobotType.SOLDIER) {
                                    rc.broadcast(5, (int) robot.getLocation().x);
                                    rc.broadcast(6, (int) robot.getLocation().y);
                                    didBroacast = true;
                                    break;
                                }
                            }
                            if (!didBroacast) {
                                rc.broadcast(5, -1);
                                rc.broadcast(6, -1);
                            }
                        }

                        move.stayInLocationRange(enemySoldierLoc,
                                (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                    }
                    else if (enemyGardeningX != -1 && enemyGardeningY!= -1) {
                        MapLocation enemyGardeningLoc = new MapLocation(enemyGardeningX, enemyGardeningY);

                        // See if tree is in area, if not clear broadcast
                        if (rc.getLocation().distanceTo(enemyGardeningLoc) < 5) {
                            if (enemyTrees.length > 0) {
                                rc.broadcast(7, (int) enemyTrees[0].getLocation().x);
                                rc.broadcast(8, (int) enemyTrees[0].getLocation().y);
                            }
                            else {
                                rc.broadcast(7, -1);
                                rc.broadcast(8, -1);
                            }
                        }

                        move.stayInLocationRange(enemyGardeningLoc,
                                (int) rc.getType().sensorRadius - 3, (int) rc.getType().sensorRadius - 1);
                    }
                    else {
                        move.move();
                    }
                }
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


    static void defenseSoldier(MapLocation myArchon) throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        move.stayInLocationRange(myArchon, 0, 20);

        if (enemyRobots.length != 0) {
            RobotInfo lowestHealthRobot = enemyRobots[0];
            for (RobotInfo robot : enemyRobots) {
                if (lowestHealthRobot.health > robot.health) {
                    lowestHealthRobot = robot;
                }
            }
            if (rc.canFireTriadShot()) {
                rc.fireTriadShot(rc.getLocation().directionTo(lowestHealthRobot.getLocation()));
            }
            else if(rc.canFireSingleShot()){
                rc.fireSingleShot(rc.getLocation().directionTo(lowestHealthRobot.getLocation()));
            }
        }
    }

    public static void shootNearby() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            if (rc.canFireTriadShot()) {
                RobotInfo lowestHealthRobot = null;
                for (RobotInfo robot : enemyRobots) {
                    // Immediately shoot at tank
                    if (robot.getType() == RobotType.TANK) {
                        lowestHealthRobot = robot;
                        break;
                    }
                    else if (robot.getType() == RobotType.SOLDIER &&
                            (lowestHealthRobot == null || lowestHealthRobot.health > robot.health)) {
                        lowestHealthRobot = robot;
                    }
                }

                if (lowestHealthRobot != null) {
                    Direction dirToAdd = rc.getLocation().directionTo(lowestHealthRobot.getLocation());
                    // ...Then fire three bullets in the direction of the enemy.
                    rc.fireTriadShot(dirToAdd);
                    rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirToAdd, 5), 66, 134, 244);
                    return;
                }
            }

            if (rc.canFireSingleShot()) {
                RobotInfo lowestHealthRobot = enemyRobots[0];
                for (RobotInfo robot : enemyRobots) {
                    // Immediately shoot at tank
                    if (robot.getType() == RobotType.TANK) {
                        lowestHealthRobot = robot;
                        break;
                    }
                    else if (lowestHealthRobot.health > robot.health) {
                        lowestHealthRobot = robot;
                    }
                }
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(lowestHealthRobot.getLocation()));
                return;
            }

            //System.out.println("CANT FIRE SHOT " + rc.getTeamBullets() + rc.hasAttacked());
            //rc.setIndicatorDot(rc.getLocation(), 244, 80, 66);
        }
        else if (enemyTrees.length > 0) {
            if (rc.canFireSingleShot()) {
                TreeInfo lowestHealthTree = enemyTrees[0];
                for (TreeInfo tree : enemyTrees) {
                    if (lowestHealthTree.health > tree.health) {
                        lowestHealthTree = tree;
                    }
                }
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(lowestHealthTree.getLocation()));
            }
        }
    }
}
