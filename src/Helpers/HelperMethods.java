package Helpers;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Class for common helper methods
 */
public class HelperMethods {
    static RobotController rc;
    static Random rand;

    public HelperMethods(RobotController rc) {
        rand = new Random();
        this.rc = rc;
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return new Direction((float)HelperMethods.randomNum() * 2 * (float)Math.PI);
    }

    public static Direction dodgeBullets(Direction dir, int degreeOffset, int checksPerSide) throws GameActionException {
        // Find nearby bullets
        BulletInfo[] bullets = rc.senseNearbyBullets();

        if (bullets.length > 0) {
            // List for bullets on collision course with robot
            ArrayList<BulletInfo> bulletsWillCollide = new ArrayList<>();

            // Initialize bulletsWillCollide
            for (BulletInfo bullet : bullets) {
                if (willCollideWithMe(rc.getLocation(), bullet)) {
                    bulletsWillCollide.add(bullet);
                }
            }

            // If there are bullets in range...
            if (bulletsWillCollide.size() > 1) {
                // Find the closest bullet
                BulletInfo closestBullet = bulletsWillCollide.get(bulletsWillCollide.size() - 1);
                BulletInfo secondClosestBullet = bulletsWillCollide.get(bulletsWillCollide.size() - 2);

                // Find direction that will move the robot out of the closest bullet path and away from second
                // closest bullet and move.
                Direction closestBulletDirection = closestBullet.getDir();
                MapLocation leftOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateLeftDegrees(90));
                MapLocation rightOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateRightDegrees(90));

                if (secondClosestBullet.getLocation().distanceTo(leftOfClosestBullet) >
                        secondClosestBullet.getLocation().distanceTo(rightOfClosestBullet)) {
                    return tryMove(closestBulletDirection.rotateLeftDegrees(90), degreeOffset, checksPerSide);
                } else {
                    return tryMove(closestBulletDirection.rotateRightDegrees(90), degreeOffset, checksPerSide);
                }
            } else if (bulletsWillCollide.size() == 1) {
                BulletInfo secondClosest = bullets[bullets.length - 1];
                if (secondClosest.getID() == bulletsWillCollide.get(0).getID() && bullets.length > 1) {
                    secondClosest = bullets[bullets.length - 2];
                }

                Direction closestBulletDirection = bulletsWillCollide.get(0).getDir();
                MapLocation leftOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateLeftDegrees(90));
                MapLocation rightOfClosestBullet = rc.getLocation().add(closestBulletDirection.rotateRightDegrees(90));

                if (secondClosest.getLocation().distanceTo(leftOfClosestBullet) >
                        secondClosest.getLocation().distanceTo(rightOfClosestBullet)) {
                    return tryMove(closestBulletDirection.rotateLeftDegrees(90), degreeOffset, checksPerSide);
                } else {
                    return tryMove(closestBulletDirection.rotateRightDegrees(90), degreeOffset, checksPerSide);
                }
            }
            else {
                for (BulletInfo bullet : bullets) {
                    if (willCollideWithMe(rc.getLocation().add(dir), bullet)) {
                        if (willCollideWithMe(rc.getLocation().add(dir.opposite()), bullet)) {
                            // Don't move, but there's nothing wrong with the direction so return it
                            return dir;
                        }
                        else {
                            return tryMove(dir.opposite());
                        }
                    }
                }
                // After looping through each bullet, nothing will collide with me if I move forwards
                // TODO: the second tryMove function could move you into the path of a bullet
                return tryMove(dir, degreeOffset, checksPerSide);
            }
        }
        else {
            return null;
        }
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static Direction tryMove(Direction dir) throws GameActionException {
        int degreeOffset = 20;
        int checksPerSide = 10;

        Direction dodge = dodgeBullets(dir, degreeOffset, checksPerSide);
        if (dodge == null) {
            return tryMove(dir, degreeOffset, checksPerSide);
        }
        else {
            return dodge;
        }
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static Direction tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return dir;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                Direction newDir = dir.rotateLeftDegrees(degreeOffset*currentCheck);
                rc.move(newDir);
                return newDir;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                Direction newDir = dir.rotateRightDegrees(degreeOffset*currentCheck);
                rc.move(newDir);
                return newDir;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return null;
    }

    //public static Direction findOpenLocation(MapLocation loc) {

    //}

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

    public static float randomNum() {
        return rand.nextFloat();
    }

    // Keeps robot in range and returns the direction it moved in
    public static Direction stayInLocationRange(Direction goingDir, MapLocation myLoc, int minDist, int maxDist) throws GameActionException {
        Direction togo = goingDir;
        float archonDist = rc.getLocation().distanceTo(myLoc);
        // Calculate random variation for direction to move in
        float toRotate = (float) (randomNum() * 140) - 70;
        // Check if robot has gone too far from archon
        if (archonDist >= maxDist) {
            if (!(rc.getLocation().add(goingDir).distanceTo(myLoc) < archonDist)) {
                Direction archonDir = rc.getLocation().directionTo(myLoc);
                togo = archonDir.rotateLeftDegrees(toRotate);
            }
        }
        // Check if robot has come too close to archon
        else if (archonDist <= minDist) {
            if (!(rc.getLocation().add(goingDir).distanceTo(myLoc) >= archonDist)) {
                Direction archonDir = rc.getLocation().directionTo(myLoc).opposite();
                togo = archonDir.rotateLeftDegrees(toRotate);
            }
        }

        Direction tryMoveResult = tryMove(togo);

        // Only receive null if tryMove cannot go anywhere, so try going opposite direction
        if (tryMoveResult == null) {
            togo = togo.opposite();
        }
        else {
            togo = tryMoveResult;
        }

        return togo;
    }
}
