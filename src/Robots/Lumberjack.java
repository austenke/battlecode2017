package Robots;

import Helpers.Movement;
import Main.RobotPlayer;
import battlecode.common.*;

/**
 * Class for lumberjack robot.
 */
public class Lumberjack {
    static RobotController rc = RobotPlayer.rc;
    static Movement move;

    public Lumberjack() {
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

        while (true) {
            try {
                boolean action = false;
                RobotInfo[] bots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                TreeInfo[] trees = rc.senseNearbyTrees();
                if (trees.length > 0) {
                    for (TreeInfo t : trees) {
                        if (t.getTeam() != rc.getTeam()) {
                            if (rc.canChop(t.getLocation())) {
                                rc.chop(t.getLocation());
                            } else {
                                move.moveToLoc(t.getLocation());
                            }
                            action = true;
                            break;
                        }
                    }
                }
                else if (bots.length > 0 && rc.getLocation().distanceTo(bots[0].getLocation()) < 3 && rc.canStrike()) {
                    if (rc.getLocation().distanceTo(bots[0].getLocation()) <= 2) {
                        rc.strike();
                    }
                    else {
                        move.moveToLoc(bots[0].getLocation());
                    }
                    action = true;
                }

                if (!action) {
                    move.stayInLocationRange(myArchon, 5, 40);
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
