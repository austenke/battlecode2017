package Helpers;

import Main.RobotPlayer;
import Robots.Archon;
import Robots.Gardener;
import Robots.Scout;
import battlecode.common.*;
import java.util.Vector;

import java.awt.*;
import java.util.*;

public class Economy {
    static RobotController rc;
    static int numArchons;
    static int soldierPriority, gardenerPriority, scoutPriority;
    static int[] priorities;
    static PriorityComparator pc = new PriorityComparator();
    static int myArchon;
    static int shitBuilt;
    static int pauseStart;
    static int gardenerID;
    static int numGardeners;
    static boolean builderGardener;


    public Economy(RobotController bot) {
        rc = bot;

        pauseStart = -400;

        shitBuilt = 0;

        numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;

        priorities = new int[3];

        soldierPriority = 0;
        priorities[0] = soldierPriority;

        scoutPriority = -10;
        priorities[1] = scoutPriority;

        gardenerPriority = -5;
        priorities[2] = gardenerPriority;

        MapLocation archons[] = rc.getInitialArchonLocations(rc.getTeam());
        myArchon = 0;
        for (int i = 1; i < numArchons; i++) {
            if (rc.getLocation().distanceTo(archons[myArchon]) > rc.getLocation().distanceTo(archons[i])) {
                myArchon = i;
            }
        }
        myArchon++;

        if (rc.getType() == RobotType.GARDENER) {
            try {
                gardenerID = rc.readBroadcast(15);
                rc.broadcast(15, gardenerID + 1);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            //System.out.println("Gardener " + gardenerID + "numGardeners " + numGardeners);
        }
    }

    public static RobotType getHighestPriority(){
        int temp = -9999;
        int pos = 0;

        for(int i = 0; i < 3; i++){
            if(temp <= priorities[i]){
                pos = i;
                temp = priorities[pos];
            }
        }
        if(pos == 0){
            return RobotType.SOLDIER;
        } else if (pos == 1){
            return RobotType.SCOUT;
        } else{
            return RobotType.GARDENER;
        }

    }

    public static void build(){
        try{
            if(rc.getTeamBullets() >= 1000){
                if(rc.getType() == RobotType.ARCHON){
                    rc.donate(800);
                }
            }
            int numRobots = rc.readBroadcast(12 * myArchon);
            if(rc.getType() == RobotType.ARCHON){
                if(shitBuilt == 0 || rc.readBroadcast(13 * myArchon) == 1){
                    archonBuild();
                }
            } else if(rc.getType() == RobotType.GARDENER){
                numGardeners = rc.readBroadcast(15) - 1;
                if(numGardeners + 1 <= numArchons){
                    if(rc.readBroadcast(14) == gardenerID){
                        gardenerBuild();
                    }
                }else if(numGardeners + 1 > numArchons) {
                    if (rc.getRoundNum() - pauseStart >= 100) {
                        gardenerBuild();
                    }
                }
            }

        }catch (GameActionException e) {
            e.printStackTrace();
        }

    }

    public static void archonBuild(){
        try {
                if(rc.getTeamBullets() >= 100) {
                    Direction[] dirList = RobotPlayer.getDirList();
                    Direction bestDir = null, dir;
                    float dirToEnemyArchon = 9999;
                    MapLocation[] enemyArchs = rc.getInitialArchonLocations(rc.getTeam().opponent());

                    for (Direction d : dirList) {
                        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(d, 5), 104, 244, 66);
                        if (d != null && rc.canHireGardener(d)) {
                            float dirTo = Math.abs(d.radiansBetween(rc.getLocation().directionTo(enemyArchs[0])));
                            if (dirTo < dirToEnemyArchon) {
                                bestDir = d;
                                dirToEnemyArchon = dirTo;
                            }
                        }
                    }

                    dir = bestDir;

                    if (dir == null) {
                        dir = dirList[0];
                        if (dirList.length < 10) {
                            dirList = RobotPlayer.initDirList(20);
                        }
                    }else if(rc.canHireGardener(dir)){
                        rc.hireGardener(dir);
                        shitBuilt++;
                        rc.broadcast(13 * myArchon, 0);
                    }
                }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static void gardenerBuild(){
        try{
            //System.out.println(gardenerID);
            if(shitBuilt < 5){
                earlyGameBuild();
            }else{
                //System.out.println(gardenerID);
                RobotType top = getHighestPriority();
                //System.out.println(scoutPriority.getPriority());
                if(top == RobotType.SOLDIER){
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.buildRobot(RobotType.SOLDIER, d);
                                priorities[0] = priorities[0] - 1;
                                //System.out.println("Soldier: " + priorities[0]);
                                soldierPriority = priorities[0];
                                shitBuilt++;
                                if(gardenerID >= numGardeners){
                                    rc.broadcast(14, 0);
                                }else{
                                    rc.broadcast(14, gardenerID + 1);
                                }
                                pauseStart = rc.getRoundNum();
                            }
                        }
                    }
                } else if(top == RobotType.SCOUT){
                    if(rc.getTeamBullets() >= 80){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SCOUT,d)){
                                rc.buildRobot(RobotType.SCOUT, d);
                                priorities[1] = priorities[1] - 15;
                                //System.out.println("Scout: " + priorities[1]);
                                scoutPriority = priorities[1];
                                shitBuilt++;
                                if(gardenerID >= numGardeners){
                                    rc.broadcast(14, 0);
                                }else{
                                    rc.broadcast(14, gardenerID + 1);
                                }
                                pauseStart = rc.getRoundNum();
                            }
                        }
                    }
                } else if(top == RobotType.GARDENER){
                    rc.broadcast(13 * myArchon, 1);  //gardener request
                    pauseStart = rc.getRoundNum();
                    shitBuilt++;
                    priorities[2] = priorities[2] - 10;
                    //System.out.println("Gardener: " + priorities[2]);

                    if(gardenerID >= numGardeners){
                        rc.broadcast(14, 0);
                    }else{
                        rc.broadcast(14, gardenerID + 1);
                    }
                    pauseStart = rc.getRoundNum();
                    //System.out.println(gardenerID);
                }
            }
        }catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static void earlyGameBuild() {
        try{

            switch(shitBuilt){
                case 0:

                    if(rc.getTeamBullets() >= 50){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                dir = d;
                            }
                        }
                        if(dir != null){
                            rc.plantTree(dir);
                            shitBuilt++;
                            if(gardenerID >= numGardeners){
                                rc.broadcast(14, 0);
                            }else{
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 1:

                    if(rc.getTeamBullets() >= 80){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SCOUT,d)){
                                dir = d;
                            }
                        }

                        if(dir != null){
                            rc.buildRobot(RobotType.SCOUT, dir);
                            shitBuilt++;
                            if(gardenerID >= numGardeners){
                                rc.broadcast(14, 0);
                            }else{
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 2:
                    if(rc.getTeamBullets() >= 100) {
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.buildRobot(RobotType.SOLDIER, dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 3:

                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.LUMBERJACK, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.buildRobot(RobotType.LUMBERJACK, dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 4:

                    if(rc.getTeamBullets() >= 50){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.plantTree(dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 5:

                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.buildRobot(RobotType.SOLDIER, dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 6:

                    if(rc.getTeamBullets() >= 50){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.plantTree(dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 7:
                    //System.out.println(gardenerID);
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.buildRobot(RobotType.SOLDIER, dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 8:

                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.buildRobot(RobotType.SOLDIER, dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
                case 9:

                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();
                        Direction dir = null;

                        for (Direction d : dirList) {
                            if (rc.canBuildRobot(RobotType.LUMBERJACK, d)) {
                                dir = d;
                            }
                        }

                        if (dir != null) {
                            rc.buildRobot(RobotType.LUMBERJACK, dir);
                            shitBuilt++;
                            if (gardenerID >= numGardeners) {
                                rc.broadcast(14, 0);
                            } else {
                                rc.broadcast(14, gardenerID + 1);
                            }
                            pauseStart = rc.getRoundNum();
                        }
                    }
                    break;
            }
        }catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
