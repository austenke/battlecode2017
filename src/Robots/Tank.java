package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import battlecode.common.*;

public class Tank {
    static RobotController rc;
    static HelperMethods helpers;
    static Movement move;

    public Tank(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
        this.move = new Movement(rc);
    }

    public static void run() throws GameActionException{


            while (true) {
                try {
                    rc.broadcast(0,(int)rc.getLocation().x);
                    rc.broadcast(1,(int)rc.getLocation().y);
                    RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(10, rc.getTeam().opponent());  //nearby locations
                    MapLocation Archon = rc.getInitialArchonLocations(rc.getTeam())[0];
                    MapLocation enemyArchon = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];

                    if (nearbyEnemies.length == 0) {
                        move.tankMoveToLoc(enemyArchon);
                    } else {
                        if (rc.canFireTriadShot()) {
                            rc.fireTriadShot(rc.getLocation().directionTo(nearbyEnemies[nearbyEnemies.length - 1].getLocation()));
                        }
                        if (rc.canFireSingleShot()){
                            rc.fireSingleShot(rc.getLocation().directionTo(nearbyEnemies[nearbyEnemies.length - 1].getLocation()));
                        }
                        move.tankStayInLocationRange(nearbyEnemies[0].getLocation(),
                                (int) rc.getType().sensorRadius - 1, (int) rc.getType().sensorRadius);
                    }
                    Clock.yield();
                }catch(Exception e) {
                    System.out.println("Tank Exception");
                    e.printStackTrace();
                }
            }
    }
}
