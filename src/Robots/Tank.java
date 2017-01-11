package Robots;

import Helpers.HelperMethods;
import battlecode.common.*;
import static Helpers.HelperMethods.randomDirection;

public class Tank {
    static RobotController rc;
    static HelperMethods helpers;

    public Tank(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException{
        System.out.println("I'm a tank!");

        while(true){
            MapLocation[] locs = rc.senseBroadcastingRobotLocations();
            RobotInfo[] nlocs = rc.senseNearbyRobots();
            if(nlocs == null){

            }
        }
    }
}
