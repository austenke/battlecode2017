package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import Main.RobotPlayer;
import battlecode.common.*;

/**
 * Class for lumberjack robot.
 */
public class Lumberjack {
    static RobotController rc = RobotPlayer.rc;
    static HelperMethods helpers = RobotPlayer.helpers;
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
                if (bots.length > 0 && rc.canStrike()) {
                    for (RobotInfo b : bots) {
                        if (rc.getLocation().distanceTo(b.getLocation()) <= GameConstants.LUMBERJACK_STRIKE_RADIUS) {
                            rc.strike();
                            action = true;
                        }
                        else {
                            if (rc.getLocation().distanceTo(myArchon) < 50) {
                                move.moveToLoc(b.getLocation());
                                action = true;
                            }
                        }
                        break;
                    }
                }
                else if (trees.length > 0) {
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
