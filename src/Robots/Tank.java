package Robots;

import Helpers.HelperMethods;
import Helpers.Movement;
import Main.RobotPlayer;
import battlecode.common.*;

public class Tank {
    static RobotController rc = RobotPlayer.rc;
    static HelperMethods helpers = RobotPlayer.helpers;
    static Movement move;

    public Tank() {
        this.move = new Movement();
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
