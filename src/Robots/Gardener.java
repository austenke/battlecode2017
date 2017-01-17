package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import Main.RobotPlayer;
import battlecode.common.*;

/**
 * Class for gardener robot.
 */
public class Gardener {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Gardener(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
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
        MapLocation myArchon = archons[0];

        for (MapLocation archonLoc : archons) {
            if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                myArchon = archonLoc;
            }
        }

        // The code you want your robot to perform every round should be in this loop
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                int minDist = -1;
                int maxDist = -1;
                if (!tryToWater()) {
                    if (buildOrPlant == 0) {
                        builderGardener();
                        minDist = 22;
                        maxDist = 30;
                    } else if (buildOrPlant == 1) {
                        tryToPlant();
                        if (rc.getTreeCount() < 5) {
                            minDist = 1;
                            maxDist = 10;
                        }
                        else {
                            minDist = 1;
                            maxDist = 20;
                        }
                    }

                    move.stayInLocationRange(myArchon, minDist, maxDist);
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
        Direction dir = helpers.randomDirection();
        while(!rc.canBuildRobot(RobotType.TANK, dir) && rc.getTeamBullets() >= 100){
            dir = helpers.randomDirection();
        }
        //System.out.println("dir");
        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.getRobotCount() < 10) {
            if (rc.canBuildRobot(RobotType.SOLDIER, dir) && (rc.getRobotCount() == 3 || rc.getRobotCount() == 5 || rc.getRobotCount() == 7 || rc.getRobotCount() == 9)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                //System.out.println("bot");
            }
        }else if(rc.getRobotCount() < 15 && rc.canBuildRobot(RobotType.SCOUT, dir)){
            rc.buildRobot(RobotType.SCOUT, dir);
        }else if(rc.getRobotCount() % 5 == 0 && rc.canBuildRobot(RobotType.TANK, dir)){
            rc.buildRobot(RobotType.TANK, dir);
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
                if (tree.getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - (GameConstants.WATER_HEALTH_REGEN_RATE * 3)) {
                    return tree;
                }
            }
        }

        // No tree that is low hp in area so return null
        return null;
    }

    // Code used by all gardeners, find nearby trees that need watering
    public static boolean tryToWater() throws GameActionException {
        TreeInfo treeToWater = nearbyDyingTree();

        if (treeToWater != null) {
            rc.setIndicatorDot(rc.getLocation(),66,244,69);

            if (rc.canWater(treeToWater.getLocation())) {
                int turnsToWater = (int) Math.ceil((GameConstants.BULLET_TREE_MAX_HEALTH - treeToWater.getHealth())
                        / GameConstants.WATER_HEALTH_REGEN_RATE);
                // After calculating how long it will take to fully regenerate tree, loop for that amount of
                // times. There was a bug where water sometimes failed so also check if watering is possible for
                // each loop and if not move towards tree again (making sure to increase how many turns are
                // needed to water by 1).
                for (int i = 0; i < turnsToWater; i++) {
                    if (rc.canWater(treeToWater.getLocation())) {
                        rc.water(treeToWater.getLocation());
                        Clock.yield();
                    }
                    else {
                        move.moveToLoc(treeToWater.getLocation());
                        turnsToWater++;
                        Clock.yield();
                    }
                }
            } else {
                move.moveToLoc(treeToWater.getLocation());
            }

            // Return true for watering
            return true;
        }
        else {
            return false;
        }
    }

    public static void tryToPlant() throws GameActionException {
        Direction[] dirList = RobotPlayer.getDirList();
        if(rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {
            for (int i = 0; i < dirList.length; i++) {
                //only plant trees on a sub-grid
                MapLocation p = rc.getLocation().add(dirList[i],GameConstants.GENERAL_SPAWN_OFFSET+GameConstants.BULLET_TREE_RADIUS+rc.getType().bodyRadius);
                if(modGood(p.x,6,0.2f)&&modGood(p.y,6,0.2f)) {
                    if (rc.canPlantTree(dirList[i])) {
                        rc.plantTree(dirList[i]);
                        break;
                    }
                }
            }
        }
    }

    public static boolean modGood(float number,float spacing, float fraction){
        return (number%spacing)<spacing*fraction;
    }
}