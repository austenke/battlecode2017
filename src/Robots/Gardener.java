package Robots;

import Helpers.HelperMethods;
import Main.RobotPlayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Class for gardener robot.
 */
public class Gardener {
    static RobotController rc;
    static HelperMethods helpers;
    static Direction goingDir;

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
            System.out.println("I'm a builder gardener!");
        }
        else {
            buildOrPlant = 1;
            System.out.println("I'm a planter gardener!");
        }
        rc.broadcast(2,gardenerCount + 1);

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                MapLocation archonLoc = rc.getInitialArchonLocations(rc.getTeam())[0];

                if (!rc.getLocation().isWithinDistance(archonLoc, 20)) {
                    float toRotate = (float) Math.random() * 90;
                    Direction archonDir = rc.getLocation().directionTo(archonLoc);
                    if (Math.random() > .5) {
                        archonDir = archonDir.rotateLeftDegrees(toRotate);
                    }
                    else {
                        archonDir = archonDir.rotateRightDegrees(toRotate);
                    }
                    goingDir = archonDir;
                    helpers.tryMove(archonDir);
                }
                else if (buildOrPlant == 0) {
                    numSoldiers = builderGardener(numSoldiers);
                }
                else if (buildOrPlant == 1) {
                    planterGardener();
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static int builderGardener(int soldiers) throws GameActionException {
        int soldierCount = soldiers;
        boolean watering = false;

        TreeInfo[] trees = rc.senseNearbyTrees(-1, rc.getTeam());

        for (TreeInfo tree : trees) {
            if (tree.health < 30) {
                watering = true;
                if (rc.canWater(tree.getLocation())) {
                    for (int i = 0; i < 5; i++) {
                        rc.water(tree.getLocation());
                        Clock.yield();
                    }
                }
                else {
                    helpers.tryMove(rc.getLocation().directionTo(tree.getLocation()));
                    Clock.yield();
                }
            }
        }

        if (!watering) {
            // Generate a random direction
            Direction dir = helpers.randomDirection();

            // Randomly attempt to build a soldier or lumberjack in this direction
            if (rc.getRobotCount() < 15 || rc.getTeamBullets() >= 300) {
                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && soldierCount % 5 > 0) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
                else if(rc.canBuildRobot(RobotType.TANK, dir)){
                    rc.buildRobot(RobotType.TANK, dir);
                }
                soldierCount ++;
            }
            helpers.tryMove(helpers.randomDirection());
        }
        return soldierCount;
    }

    static void planterGardener() throws GameActionException {
        boolean watering = false;

        TreeInfo[] trees = rc.senseNearbyTrees(-1, rc.getTeam());
        ArrayList<TreeInfo> treesToWater = new ArrayList<>();

        if (trees.length > 0) {
            for (TreeInfo tree : trees) {
                if (tree.health < 30) {
                    treesToWater.add(tree);
                }
            }

            if (treesToWater.size() > 0) {
                TreeInfo tree = treesToWater.get(treesToWater.size() - 1);
                watering = true;
                if (rc.canWater(tree.getLocation())) {
                    for (int i = 0; i < 5; i++) {
                        rc.water(tree.getLocation());
                        Clock.yield();
                    }
                } else {
                    helpers.tryMove(rc.getLocation().directionTo(tree.getLocation()));
                    Clock.yield();
                }
            }
        }

        if (!watering) {
            if (rc.getTreeCount() < 15) {
                tryToPlant();
                if(rc.canMove(goingDir)){
                    HelperMethods.tryMove(goingDir);
                }else{
                    goingDir = HelperMethods.randomDirection();
                }
            }
            else {
                if (trees.length > 0) {
                    if (noTreeInRange(4)) {
                        int random = (int) Math.floor(Math.random() * (trees.length - 1));
                        helpers.tryMove(rc.getLocation().directionTo(trees[random].getLocation()));
                    }
                    else {
                        helpers.tryMove(HelperMethods.randomDirection());
                    }
                }
                else {
                    helpers.tryMove(HelperMethods.randomDirection());
                }
            }
        }
    }

    public static void tryToPlant() throws GameActionException{
        Direction[] dirList = RobotPlayer.getDirList();
        if(rc.getTeamBullets()>GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 4; i++) {
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

    static boolean noArchonInRange() {
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if (robot.type == RobotType.ARCHON && rc.getLocation().isWithinDistance(robot.getLocation(), 3)) {
                return false;
            }
        }

        return true;
    }
}


