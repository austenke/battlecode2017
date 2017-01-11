package Robots;

import Helpers.HelperMethods;
import battlecode.common.*;

public class Tank {
    static RobotController rc;
    static HelperMethods helpers;

    public Tank(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException{

        while(true){
            MapLocation[] blocs = rc.senseBroadcastingRobotLocations();  //broadcasting locations
            RobotInfo[] nlocs = rc.senseNearbyRobots(10, rc.getTeam().opponent());  //nearby locations
            MapLocation floc = blocs[0];
            MapLocation Archon = rc.getInitialArchonLocations(rc.getTeam())[0];

            if(nlocs == null) {
                System.out.println("Tank code");
                //major flaw: if no enemy robots are broadcasting, tank will move towards its furthest ally
                for(MapLocation loc: blocs){
                    if(Archon.distanceTo(loc) > Archon.distanceTo(floc)){
                        floc = loc;
                    }
                }
                Direction enemyDir = rc.getLocation().directionTo(floc);
                helpers.tryMove(enemyDir);
            }else if (rc.canFireTriadShot()) {
                rc.fireTriadShot(rc.getLocation().directionTo(nlocs[0].getLocation()));
            }

        }
    }
}
