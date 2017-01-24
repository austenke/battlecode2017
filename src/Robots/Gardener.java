package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import Main.RobotPlayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for gardener robot.
 */
public class Gardener {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;
    static boolean watering;
    static MapLocation myArchon;

    public Gardener(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
        this.watering = false;
    }

    public static void run() throws GameActionException {
        int gardenerCount = rc.readBroadcast(2);
        int buildOrPlant = -1;
        if((gardenerCount + 1) % 2 == 0) {
            buildOrPlant = 0;
            //System.out.println("I'm a builder gardener!");
        }
        else {
            buildOrPlant = 1;
            //System.out.println("I'm a planter gardener!");
        }
        rc.broadcast(2,gardenerCount + 1);

        MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
        myArchon = archons[0];

        for (MapLocation archonLoc : archons) {
            if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                myArchon = archonLoc;
            }
        }

        MapLocation plantSpot = findGoodPlantSpot();
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            rc.setIndicatorDot(plantSpot, 244, 66, 66);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                int minDist = -1;
                int maxDist = -1;

                if (buildOrPlant == 0) {
                    builderGardener();
                    minDist = 0;
                    maxDist = 10;

                    if (!rc.hasMoved()) {
                        move.stayInLocationRange(myArchon, minDist, maxDist);
                    }
                } else if (buildOrPlant == 1) {
                    tryToWater();
                    if (plantSpot != null) {
                        if (rc.getLocation().distanceTo(plantSpot) < 2) {
                            tryToPlant();
                        }
                        else {
                            move.moveToLoc(plantSpot);
                        }
                    }
                    else {
                        System.out.println("BIG ERROR");
                    }
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void builderGardener() throws GameActionException {

        //if (!rc.getLocation().isWithinDistance(myArchon, 20)) {
            // Generate a random direction
        Direction[] dirList = RobotPlayer.getDirList();
        Direction dir = dirList[0];
        for(Direction d : dirList){
            if(rc.canBuildRobot(RobotType.TANK,d)){
                dir = d;
                break;
            }
        }
        int numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
        if (rc.getRobotCount() < 10) {
            if (rc.canBuildRobot(RobotType.SOLDIER, dir) && (rc.getRobotCount() == numArchons + 2 || rc.getRobotCount() == numArchons + 4 || rc.getRobotCount() == numArchons + 6 || rc.getRobotCount() == numArchons + 8)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                //System.out.println("bot");
            }
        }else if(rc.getRobotCount() < 15 && rc.canBuildRobot(RobotType.SCOUT, dir)){
            rc.buildRobot(RobotType.SCOUT, dir);
        }else if(rc.getRobotCount() % 5 == 0 && rc.canBuildRobot(RobotType.TANK, dir)){
            rc.buildRobot(RobotType.TANK, dir);
            System.out.println("built tank");
        }else if(rc.getRobotCount() % 5 < 3 && rc.canBuildRobot(RobotType.SOLDIER, dir)){
            rc.buildRobot(RobotType.SOLDIER, dir);
        }else if(rc.getRobotCount() % 5 == 3 && rc.canBuildRobot(RobotType.SCOUT, dir)){
            rc.buildRobot(RobotType.SCOUT, dir);
        }
    }

    public static TreeInfo nearbyDyingTree() {
        TreeInfo[] trees = rc.senseNearbyTrees(-1, rc.getTeam());

        // trees in area, need to filter for low hp trees
        if (trees.length > 0) {
            // Tree list is sorted by distance, so find closest low hp tree
            for (TreeInfo tree : trees) {
                if (tree.getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - (GameConstants.WATER_HEALTH_REGEN_RATE )) {
                    return tree;
                }
            }
        }

        // No tree that is low hp in area so return null
        return null;
    }

    // Code used by all gardeners, find nearby trees that need watering
    public static void tryToWater() throws GameActionException {
        TreeInfo treeToWater = nearbyDyingTree();

        if (treeToWater != null) {
            if (rc.canWater(treeToWater.getLocation())) {
                rc.water(treeToWater.getLocation());
            }
        }
    }

    public static MapLocation findGoodPlantSpot() throws GameActionException {
        MapLocation plantSpot = null;
        MapLocation potentialPlantSpot = null;
        int highestPlantCount = 0;
        int attempts = 0;

        int archonDist = rc.readBroadcast(9);
        while(true) {
            int plantCount = 0;
            for (Direction dir : RobotPlayer.getDirList()) {
                if (rc.canPlantTree(dir)) {
                    plantCount++;
                }
            }

            MapLocation myLoc = rc.getLocation();
            if (plantCount > 3 && myLoc.distanceTo(myArchon) > archonDist) {
                rc.readBroadcast(archonDist + 2);
                return myLoc;
            }
            else if (attempts > 40) {
                rc.readBroadcast(archonDist + 2);
                return potentialPlantSpot;
            }
            else {
                if (plantCount >= highestPlantCount) {
                    highestPlantCount = plantCount;
                    potentialPlantSpot = myLoc;
                }
                Movement.stayInLocationRange(myArchon, 0, 10);
                attempts++;
                Clock.yield();
            }
        }
    }

    public static void tryToPlant() throws GameActionException {
        Direction[] dirList = RobotPlayer.getDirList();
        if(rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {
            for (Direction dir : dirList) {
                if (rc.canPlantTree(dir)) {
                    rc.plantTree(dir);
                }
            }
        }
    }
}