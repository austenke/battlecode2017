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
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && rc.canStrike()) {
                        rc.strike();
                        Direction chase = rc.getLocation().directionTo(b.getLocation());
                        if (rc.canMove(chase) && rc.getLocation().distanceTo(myArchon) < 50) {
                            rc.move(chase);
                        }
                        break;
                    }
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                for (TreeInfo t : trees) {
                    if (rc.canChop(t.getLocation()) && t.getTeam() != rc.getTeam()) {
                        rc.chop(t.getLocation());
                        break;
                    }
                }
                if (! rc.hasAttacked()) {
                    move.stayInLocationRange(myArchon, 5, 40);
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
