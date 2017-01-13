package Robots;

import Helpers.HelperMethods;
import battlecode.common.*;
import org.apache.commons.lang3.ObjectUtils;
import scala.tools.nsc.backend.jvm.analysis.Null;

public class Tank {
    static RobotController rc;
    static HelperMethods helpers;

    public Tank(RobotController rc, HelperMethods helpers) {
        this.rc = rc;
        this.helpers = helpers;
    }

    public static void run() throws GameActionException{


            while (true) {
                try {
                    rc.broadcast(0,(int)rc.getLocation().x);
                    rc.broadcast(1,(int)rc.getLocation().y);
                    MapLocation[] blocs = rc.senseBroadcastingRobotLocations();  //broadcasting locations
                    RobotInfo[] nlocs = rc.senseNearbyRobots(10, rc.getTeam().opponent());  //nearby locations
                    MapLocation floc = rc.getLocation();
                    MapLocation Archon = rc.getInitialArchonLocations(rc.getTeam())[0];
                    MapLocation enemyArchon = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];

                    if (nlocs.length == 0) {
                        //System.out.println("Tank code");
                        //major flaw: if no enemy robots are broadcasting, tank will move towards its furthest ally
                        if(blocs.length != 0){
                            for (MapLocation loc : blocs) {
                                if (Archon.distanceTo(loc) > Archon.distanceTo(floc)) {
                                    floc = loc;
                                }
                            }
                            Direction enemyDir = rc.getLocation().directionTo(floc);
                            if(enemyDir != null){
                                helpers.tryMove(enemyDir);
                            }
                            else{
                                helpers.tryMove(helpers.randomDirection());
                            }
                        }else{
                            helpers.tryMove(rc.getLocation().directionTo(enemyArchon));
                        }
                    } else {
                        if (rc.canFireTriadShot()) {
                            rc.fireTriadShot(rc.getLocation().directionTo(nlocs[nlocs.length - 1].getLocation()));
                        }
                        if (rc.canFireSingleShot()){
                            rc.fireTriadShot(rc.getLocation().directionTo(nlocs[nlocs.length - 1].getLocation()));
                        }
                         if (nlocs[0].getLocation().isWithinDistance(rc.getLocation(), rc.getType().sensorRadius - 2)) {
                            helpers.tryMove(rc.getLocation().directionTo(nlocs[0].getLocation()).opposite());
                        } else {
                            helpers.tryMove(rc.getLocation().directionTo(nlocs[0].getLocation()));
                        }
                    }
                    Clock.yield();
                }catch(Exception e) {
                    System.out.println("Tank Exception");
                    e.printStackTrace();
                }
            }
    }
}
