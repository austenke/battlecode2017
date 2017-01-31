package Robots;

import Helpers.Economy;
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
    static RobotController rc = RobotPlayer.rc;
    static HelperMethods helpers = RobotPlayer.helpers;
    static Movement move;
    static boolean watering;
    static MapLocation myArchon;
    static Economy econ;

    public Gardener() {
        this.move = new Movement();
        this.watering = false;
        this.econ = new Economy(rc);
    }

    public static void run() throws GameActionException {

        MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
        myArchon = archons[0];

        for (MapLocation archonLoc : archons) {
            if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                myArchon = archonLoc;
            }
        }

        MapLocation plantSpot = null;

        plantSpot = findGoodPlantSpot();
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                if(plantSpot != null){
                    if(rc.getLocation().distanceTo(plantSpot) < 2){
                        tryToWater();
                        econ.build();
                    }
                    else{
                        move.moveToLoc(plantSpot);

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
                rc.broadcast(9, archonDist + 4);
                return myLoc;
            }
            else if (attempts > 30) {
                rc.broadcast(9, archonDist + 3);
                return potentialPlantSpot;
            }
            else {
                if (plantCount >= highestPlantCount && myLoc.distanceTo(myArchon) > archonDist) {
                    highestPlantCount = plantCount;
                    potentialPlantSpot = myLoc;
                }
                Movement.stayInLocationRange(myArchon, 6, 80);
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