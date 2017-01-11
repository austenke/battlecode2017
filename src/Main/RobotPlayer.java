package Main;
import battlecode.common.*;
import Helpers.HelperMethods;
import Robots.*;
import com.sun.tools.corba.se.idl.toJavaPortable.Helper;

public strictfp class RobotPlayer {
    static RobotController rc;
    static HelperMethods helpers;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        helpers = new HelperMethods(rc);

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                new Archon(rc, helpers).run();
                break;
            case GARDENER:
                new Gardener(rc, helpers).run();
                break;
            case SOLDIER:
                new Soldier(rc, helpers).run();
                break;
            case LUMBERJACK:
                new Lumberjack(rc, helpers).run();
                break;
            case TANK:
                new Tank(rc, helpers).run();
                break;
        }
	}
}
