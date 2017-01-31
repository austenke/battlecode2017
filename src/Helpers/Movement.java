package Helpers;

import Main.RobotPlayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Class used for standard robot movement
 */
public class Movement {
    private static Direction currentDir;
    private static Random rand;
    static RobotController rc = RobotPlayer.rc;

    public Movement() {
        currentDir = HelperMethods.randomDirection();
        rand = new Random(rc.getID());
    }

    public static void move() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();

        if (bullets.length > 0) {
            MapLocation goToLoc = findWeighting(currentDir, bullets);
            if (rc.canMove(goToLoc)) {
                rc.move(goToLoc);
            }
            else {
                rc.setIndicatorDot(rc.getLocation(), 244, 75, 66);
                System.out.println("DODGE ERROR");
            }
        }
        else {
            ArrayList<Direction> dirs = tryDirs();
            if (dirs.size() > 0) {
                currentDir = dirs.get(0);
                if (rc.canMove(currentDir)) {
                    rc.move(currentDir);
                }
                else {
                    rc.setIndicatorDot(rc.getLocation(), 244, 75, 66);
                    System.out.println("MOVE ERROR");
                }
            }
        }
    }

    public static void tankMove() throws GameActionException{
        if(rc.canMove(currentDir)){
            rc.move(currentDir);
        }
        else{
            ArrayList<Direction> possibleDirections = tryDirs();

            for(Direction dir: possibleDirections){
                if(rc.canMove(dir)){
                    rc.move(dir);
                    return;
                }
            }
        }
    }

    public static Direction newVariedDirection(Direction dir, int amtToVary) {
        // Multiply a random number 0 - 1 by twice the amount to vary, then subtract that amt, will generate a random
        // num from -amtToVary - amtToVary
        float toVary = (rand.nextFloat() * (amtToVary * 2)) - amtToVary;
        return dir.rotateLeftDegrees(toVary);
    }

    /**
     * Gather a list of all viable movements in as close a direction to the original dir as possible
     */
    public static ArrayList<Direction> tryDirs() throws GameActionException {
        int checksPerSide = 9;
        float degreeOffset = 20;

        ArrayList<Direction> availableDirs = new ArrayList<>();

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck <= checksPerSide) {
            float toRotate = degreeOffset * currentCheck;
            Direction rotateLeft = currentDir.rotateLeftDegrees(toRotate);
            Direction rotateRight = currentDir.rotateRightDegrees(toRotate);

            // Try the offset of the left side
            if(rc.canMove(rotateLeft)) {
                availableDirs.add(rotateLeft);
            }

            // Try the offset on the right side
            if(rc.canMove(rotateRight)) {
                availableDirs.add(rotateRight);
            }

            currentCheck++;
        }

        return availableDirs;
    }

    public static void stayInLocationRange(MapLocation loc, int minDist, int maxDist) throws GameActionException {
        MapLocation robotLoc = rc.getLocation();
        Direction dirToLoc = robotLoc.directionTo(loc);
        float disToLoc = robotLoc.distanceTo(loc);
        if (disToLoc > maxDist) {
            currentDir = newVariedDirection(dirToLoc, 60);
        }
        else if (disToLoc < minDist) {
            currentDir = newVariedDirection(dirToLoc.opposite(), 60);
        }

        move();
    }

    public static void tankStayInLocationRange(MapLocation loc, int minDist, int maxDist) throws GameActionException {
        MapLocation robotLoc = rc.getLocation();
        Direction dirToLoc = robotLoc.directionTo(loc);
        float disToLoc = robotLoc.distanceTo(loc);
        if (disToLoc > maxDist) {
            currentDir = newVariedDirection(dirToLoc, 60);
        }
        else if (disToLoc < minDist) {
            currentDir = newVariedDirection(dirToLoc.opposite(), 60);
        }

        tankMove();
    }

    public static void moveToLoc(MapLocation loc) throws GameActionException {
        Direction dirToLoc = rc.getLocation().directionTo(loc);
        currentDir = dirToLoc;
        move();
    }

    public static void tankMoveToLoc(MapLocation loc) throws GameActionException {
        if(rc.getLocation().distanceTo(loc) >= 3){
            Direction dirToLoc = rc.getLocation().directionTo(loc);
            MapLocation currentLoc = rc.getLocation();
            // Loop 5 moves in advance, see if area is clear
            for (int i = 0; i < 5; i++) {
                MapLocation newLoc = currentLoc.add(dirToLoc, rc.getType().strideRadius);
                if (rc.isLocationOccupied(newLoc) && newLoc.distanceTo(loc) > 2) {
                    tankMove();
                    return;
                }
            }
            // Area is clear, proceed with move
            currentDir = dirToLoc;
            tankMove();
        }
    }

    public static MapLocation findWeighting(Direction goingDir, BulletInfo[] bullets) {
        MapLocation goingLoc = rc.getLocation().add(goingDir, rc.getType().strideRadius);
        ArrayList<MapLocation> possibleLocs = new ArrayList<>();
        int bestScore = 9999;
        MapLocation bestLoc = null;

        for (Direction dir : RobotPlayer.getDirList()) {
            possibleLocs.add(rc.getLocation().add(dir, rc.getType().strideRadius));
        }

        for (MapLocation loc : possibleLocs) {
            float curScore = 0f;

            if (rc.canMove(loc)) {
                int collisionCount = 0;
                for (BulletInfo bullet : bullets) {
                    if (collisionCount > 4) {
                        break;
                    } else if (willCollideWithMe(loc, bullet)) {
                        // Since we will round curScore later, make this number larger and guarantee it is at least 1
                        curScore += ((bullet.damage / loc.distanceTo(bullet.getLocation())) + 1) * 15;
                        collisionCount++;
                    }
                }

                int compareScore = Math.round(curScore);
                if (compareScore < bestScore) {
                    bestScore = compareScore;
                    bestLoc = loc;
                } else if (compareScore == bestScore && loc.distanceTo(goingLoc) < bestLoc.distanceTo(goingLoc)) {
                    bestLoc = loc;
                }
            }
        }

        if (bestLoc == null || !rc.canMove(bestLoc)) {
            return rc.getLocation();
        }
        else {
            return bestLoc;
        }
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    public static boolean willCollideWithMe(MapLocation loc, BulletInfo bullet) {
        MapLocation myLocation = loc;

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
}
