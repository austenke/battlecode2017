package Robots;

import Main.RobotPlayer;
import battlecode.common.*;
import Helpers.Movement;

/**
 * Class for soldier robot.
 */
public class Soldier {
    static RobotController rc = RobotPlayer.rc;
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
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);
                TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, enemy);
                shootNearby(enemyRobots);
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
                        // If can't find anything, just stay in enemy area
                        MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
                        move.stayInLocationRange(enemyArchons[0], 0, 30);
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

        int cryForHelpX = rc.readBroadcast(16);
        int cryForHelpY = rc.readBroadcast(17);

        if (cryForHelpX == -1 || cryForHelpY == -1) {
            move.stayInLocationRange(myArchon, 0, 20);
        }
        else {
            MapLocation cryForHelp = new MapLocation(cryForHelpX, cryForHelpY);
            if (rc.getLocation().distanceTo(cryForHelp) < 5) {
                if (enemyRobots.length == 0) {
                    // No enemy robots nearby, clear cry for help channel
                    rc.broadcast(16, -1);
                    rc.broadcast(17, -1);
                    move.stayInLocationRange(myArchon, 0, 20);
                }
                else {
                    move.stayInLocationRange(cryForHelp, 0, 5);
                }
            }
            else {
                move.tankMoveToLoc(cryForHelp);
            }
        }

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

    public static RobotInfo findTarget(RobotInfo[] enemyRobots) throws GameActionException {
        if (enemyRobots.length > 0) {
            RobotInfo target = null;
            int targetScore = 0;

            for (RobotInfo robot : enemyRobots) {
                int tempScore = 0;

                switch (robot.getType()) {
                    case TANK:
                        tempScore += 1000;
                        break;
                    case ARCHON:
                        tempScore += 500;
                        break;
                    case SOLDIER:
                        tempScore += 110;
                        break;
                    case GARDENER:
                        tempScore += 90;
                        break;
                    case SCOUT:
                        tempScore += 70;
                        break;
                    case LUMBERJACK:
                        tempScore += 50;
                        break;
                }

                tempScore = tempScore - (int) (robot.getHealth() / robot.getType().maxHealth) * 30;

                if (tempScore > targetScore) {
                    targetScore = tempScore;
                    target = robot;
                }
            }

            return target;
        }
        else {
            return null;
        }
    }

    public static void shootNearby(RobotInfo[] enemyRobots) throws GameActionException {
        TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());

        RobotInfo target = findTarget(enemyRobots);

        if (target != null) {
            Direction enemyDir = rc.getLocation().directionTo(target.getLocation());

            if ((target.getType() == RobotType.TANK || target.getType() == RobotType.SOLDIER)
                    && rc.canFireTriadShot()) {
                rc.fireTriadShot(enemyDir);
            }
            else if (rc.canFireSingleShot()) {
                rc.fireSingleShot(enemyDir);
            }
        }
        else if (enemyTrees.length > 0) {
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(rc.getLocation().directionTo(enemyTrees[0].getLocation()));
            }
        }
    }
}
