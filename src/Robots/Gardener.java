package Robots;

import Helpers.HelperMethods;
import Main.RobotPlayer;
import battlecode.common.*;

/**
 * Class for gardener robot.
 */
public class Gardener {
    static RobotController rc;
    static HelperMethods helpers;
    static Direction goingDir;
    static MapLocation myArchon;

    public Gardener(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException {
        goingDir = HelperMethods.randomDirection();
        int gardenerCount = rc.readBroadcast(2);
        int buildOrPlant = -1;
        int numSoldiers = 0;
        if(gardenerCount > 2 && ((gardenerCount + 1) % 2) == 0) {
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
        if (archons.length > 1) {
            for (MapLocation archonLoc : archons) {
                if (rc.getLocation().distanceTo(archonLoc) < rc.getLocation().distanceTo(myArchon)) {
                    myArchon = archonLoc;
                }
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
                        numSoldiers = builderGardener(numSoldiers);
                        minDist = 25;
                        maxDist = 35;
                    } else if (buildOrPlant == 1) {
                        planterGardener();
                        minDist = 1;
                        maxDist = 20;
                    }

                    if (!rc.hasMoved()) {
                        goingDir = HelperMethods.stayInArchonRange(goingDir, myArchon, minDist, maxDist);
                    }
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                System.out.println("Has moved error: " + rc.hasMoved());
                e.printStackTrace();
            }
        }
    }

    static int builderGardener(int soldiers) throws GameActionException {
        int soldierCount = soldiers;

        if (!rc.getLocation().isWithinDistance(myArchon, 20)) {
            // Generate a random direction
            Direction dir = helpers.randomDirection();
            // Randomly attempt to build a soldier or lumberjack in this direction
            if (rc.getRobotCount() < 15 || rc.getTeamBullets() >= 300) {
                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && soldierCount % 5 > 0) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                } else if (rc.canBuildRobot(RobotType.TANK, dir)) {
                    rc.buildRobot(RobotType.TANK, dir);
                }
                soldierCount++;
            }
        }
        return soldierCount;
    }

    static void planterGardener() throws GameActionException {
        if (rc.getTreeCount() < 20) {
            tryToPlant();
        }
    }

    public static boolean tryToWater() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(-1, rc.getTeam());

        // trees in area, need to filter for low hp trees
        if (trees.length > 0) {
            // set to null, if there are no low hp trees in area then it will stay null
            TreeInfo treeToWater = null;
            for (TreeInfo tree : trees) {
                if (tree.getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - (GameConstants.WATER_HEALTH_REGEN_RATE * 2)) {
                    if (treeToWater == null || treeToWater.getHealth() > tree.getHealth()) {
                        treeToWater = tree;
                    }
                }
            }

            if (treeToWater != null) {
                // Not updating goingDir because that would interfere with watering mechanics.
                // Instead handling movement by itself
                if (rc.canWater(treeToWater.getLocation())) {
                    int turnsToWater = (int) Math.ceil((GameConstants.BULLET_TREE_MAX_HEALTH - treeToWater.getHealth())
                            / GameConstants.WATER_HEALTH_REGEN_RATE);
                    for (int i = 0; i < turnsToWater; i++) {
                        if (rc.canWater(treeToWater.getLocation())) {
                            rc.water(treeToWater.getLocation());
                            Clock.yield();
                        }
                        else {
                            helpers.tryMove(rc.getLocation().directionTo(treeToWater.getLocation()));
                            Clock.yield();
                        }
                    }
                } else {
                    helpers.tryMove(rc.getLocation().directionTo(treeToWater.getLocation()));
                }
                // Return true for watering
                return true;
            }
        }
        return false;
    }

    public static void tryToPlant() throws GameActionException {
        Direction[] dirList = RobotPlayer.getDirList();
        if(rc.getTeamBullets()>GameConstants.BULLET_TREE_COST && rc.getLocation().distanceTo(myArchon) > 2) {//have enough bullets. assuming we haven't built already.
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

    static boolean noTreeInRange(int range) {
        for (TreeInfo tree : rc.senseNearbyTrees(-1, rc.getTeam())) {
            if (rc.getLocation().isWithinDistance(tree.getLocation(), range)) {
                return false;
            }
        }

        return true;
    }
}


