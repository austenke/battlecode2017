package Helpers;

import Robots.Tank;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Class used for standard robot movement
 */
public class Movement {
    private static Direction currentDir;
    private static Random rand;
    private static RobotController rc;

    public Movement(RobotController rc) {
        currentDir = HelperMethods.randomDirection();
        rand = new Random(rc.getID());
        this.rc = rc;
    }

    public static void move() throws GameActionException {
        // If can move in current direction and will not run into bullets go there, else try other directions
        if (rc.canMove(currentDir) && bulletsCollideAtLoc(rc.getLocation().add(currentDir, rc.getType().strideRadius)).size() == 0) {
            rc.move(currentDir);
        }
        else {
            // Generate a list of all possible directions that can be moved to
            ArrayList<Direction> possibleDirections = tryDirs();

            // Loop through list of possible directions to find one that will avoid contact with bullets
            for (Direction dir : possibleDirections) {
                MapLocation newLoc = rc.getLocation().add(dir, rc.getType().strideRadius);
                if (bulletsCollideAtLoc(newLoc).size() == 0) {
                    currentDir = dir;
                    rc.move(currentDir);
                    return;
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

    public static ArrayList<Float> bulletsCollideAtLoc(MapLocation loc) {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        ArrayList<Float> bulletDistances = new ArrayList<>();
        for (BulletInfo bullet : bullets) {
            if (HelperMethods.willCollideWithMe(loc, bullet)) {
                bulletDistances.add(loc.distanceTo(bullet.getLocation()));
            }
        }
        return bulletDistances;
    }

    public static ArrayList<Direction> tryDirs() throws GameActionException {
        int checksPerSide = 9;
        float degreeOffset = 20;

        return tryDirs(checksPerSide, degreeOffset);
    }

    /**
     * Gather a list of all viable movements in as close a direction to the original dir as possible
     */
    public static ArrayList<Direction> tryDirs(int checksPerSide, float degreeOffset) throws GameActionException {
        ArrayList<Direction> availableDirs = new ArrayList<>();

        // Now try a bunch of similar angles
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(currentDir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                Direction newDir = currentDir.rotateLeftDegrees(degreeOffset*currentCheck);
                availableDirs.add(newDir);
            }
            // Try the offset on the right side
            if(rc.canMove(currentDir.rotateRightDegrees(degreeOffset*currentCheck))) {
                Direction newDir = currentDir.rotateRightDegrees(degreeOffset*currentCheck);
                availableDirs.add(newDir);
            }
            currentCheck++;
        }

        return availableDirs;
    }

    // To be used when standing still, dodges bullets
    public static void standingDodge() throws GameActionException {
        if (bulletsCollideAtLoc(rc.getLocation()).size() > 0) {
            // Generate a list of all possible directions that can be moved to
            ArrayList<Direction> possibleDirections = tryDirs();

            // Loop through list of possible directions to find one that will avoid contact with bullets
            for (Direction dir : possibleDirections) {
                rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dir, 4),166, 193, 237);
                MapLocation newLoc = rc.getLocation().add(dir, rc.getType().strideRadius);
                if (bulletsCollideAtLoc(newLoc).size() == 0) {
                    currentDir = dir;
                    rc.move(currentDir);
                    return;
                }
            }
        }
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
        if (rc.getLocation().distanceTo(loc) < 3 && (rc.getType() != RobotType.TANK)) {
            standingDodge();
        }
        else {
            rc.setIndicatorLine(rc.getLocation(), loc, 244, 98, 66);
            Direction dirToLoc = rc.getLocation().directionTo(loc);
            MapLocation currentLoc = rc.getLocation();
            // Loop 5 moves in advance, see if area is clear
            for (int i = 0; i < 5; i++) {
                MapLocation newLoc = currentLoc.add(dirToLoc, rc.getType().strideRadius);
                if (rc.isLocationOccupied(newLoc) && newLoc.distanceTo(loc) > 2) {
                    move();
                    return;
                }
            }
            // Area is clear, proceed with move
            currentDir = dirToLoc;
            move();
        }
    }

    public static void tankMoveToLoc(MapLocation loc) throws GameActionException {
        if(rc.getLocation().distanceTo(loc) >= 3){
            rc.setIndicatorLine(rc.getLocation(), loc, 244, 98, 66);
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
}
