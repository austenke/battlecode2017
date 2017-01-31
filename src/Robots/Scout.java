package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import Main.RobotPlayer;
import battlecode.common.*;

import java.util.ArrayList;

/**
 * Class for scout robot
 */
public class Scout {
    static RobotController rc = RobotPlayer.rc;
    static Movement move;


    public Scout() {
        this.move = new Movement();
    }

    public static void run() throws GameActionException {
        int scoutCount = rc.readBroadcast(10);
        boolean goShake = false;

        if (scoutCount < 2) {
            goShake = true;
            rc.broadcast(10, scoutCount + 1);
        }

        ArrayList<Integer> alreadyShook = new ArrayList<>();
        MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam().opponent());
        MapLocation myArchon = archons[0];

        for (MapLocation archonLoc : archons) {
            if (rc.getLocation().distanceTo(myArchon) > rc.getLocation().distanceTo(archonLoc)) {
                myArchon = archonLoc;
            }
        }

        // The code you want your robot to perform every round should be in this loop
        while (true) {
            try {
                if (goShake) {
                    shakeTrees(myArchon, alreadyShook);
                }
                else {
                    RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                    if (robots.length > 0) {
                        if (robots[0].getLocation().distanceTo(rc.getLocation()) < rc.getType().sensorRadius - 4) {
                            move.moveToLoc(myArchon);
                        }
                    }

                    if (!rc.hasMoved()) {
                        move.move();
                    }
                }
                sense();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }

        }
    }

    public static void shakeTrees(MapLocation myArchon, ArrayList<Integer> alreadyShook) throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);

        if (trees.length > 0) {
            for (TreeInfo tree : trees) {
                int treeId = tree.getID();
                if (!alreadyShook.contains(treeId)) {
                    if (rc.canShake(treeId)) {
                        rc.shake(treeId);
                        alreadyShook.add(treeId);
                    }
                    else {
                        rc.setIndicatorDot(tree.getLocation(), 66, 134, 244);
                        move.moveToLoc(tree.getLocation());
                    }
                    return;
                }
            }
        }

        move.stayInLocationRange(myArchon, 0, 100);
    }

    public static void sense() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());

        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.TANK) {
                // Broadcast to tank channels
                rc.broadcast(3, (int) robot.getLocation().x);
                rc.broadcast(4, (int) robot.getLocation().y);
            }
            else if (robot.getType() == RobotType.SOLDIER) {
                // Broadcast to soldier channels
                rc.broadcast(5, (int) robot.getLocation().x);
                rc.broadcast(6, (int) robot.getLocation().y);
            }
            else if (robot.getType() == RobotType.GARDENER) {
                // Broadcast to gardener channels
                rc.broadcast(7, (int) robot.getLocation().x);
                rc.broadcast(8, (int) robot.getLocation().y);
            }
        }

        if (enemyTrees.length > 0) {
            // Broadcast to gardener channels
            rc.broadcast(7, (int) enemyTrees[0].getLocation().x);
            rc.broadcast(8, (int) enemyTrees[0].getLocation().y);
        }
    }
}
