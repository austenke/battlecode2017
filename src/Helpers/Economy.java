package Helpers;

import Main.RobotPlayer;
import Robots.Archon;
import Robots.Gardener;
import battlecode.common.*;

import java.awt.*;
import java.util.*;

public class Economy {
    static RobotController rc;
    static int numArchons;
    static Priority soldierPriority, gardenerPriority, scoutPriority, tankPriority, treePriority;
    static ArrayList<Priority> priorities;
    static PriorityComparator pc = new PriorityComparator();
    static int myArchon;
    static int shitBuilt;
    static int tankRequestRound;
    static boolean tankBuilder;


    public Economy(RobotController bot){
        rc = bot;

        numArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
        soldierPriority = new Priority(0, RobotType.SOLDIER, false);
        scoutPriority = new Priority(11, RobotType.SCOUT, false);
        tankPriority = new Priority(5, RobotType.TANK, false);


        priorities = new ArrayList<Priority>();

        priorities.add(soldierPriority);
        priorities.add(scoutPriority);
        priorities.add(tankPriority);

        MapLocation archons[] = rc.getInitialArchonLocations(rc.getTeam());
        myArchon = 1;
        for(int i = 1; i < archons.length; i++){
            if (rc.getLocation().distanceTo(archons[myArchon - 1]) > rc.getLocation().distanceTo(archons[i])) {
                myArchon = i + 1;
            }
        }
        if(rc.getType() == RobotType.GARDENER){
            try{
                int gardenerCount = rc.readBroadcast(2);
                boolean tankBuilder;
                if((gardenerCount + 1) % 2 == 1){
                    tankBuilder = false;
                    rc.broadcast(2, gardenerCount + 1);
                } else{
                    tankBuilder = true;
                    rc.broadcast(2, gardenerCount + 1);
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void order(){
        Collections.sort(priorities, pc);
    }

    public static void build(){
        try{
            int numRobots = rc.readBroadcast(12 * myArchon);
            if(rc.getType() == RobotType.GARDENER){
                if(tankBuilder){
                    System.out.println(tankBuilder);
                    if(shitBuilt == 0){
                        if(rc.getTeamBullets() >= 50){
                            Direction[] dirList = RobotPlayer.getDirList();

                            for(Direction d : dirList){
                                if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                    rc.plantTree(d);
                                    shitBuilt++;
                                    //rc.broadcast(12 * myArchon, numRobots + 1);
                                }
                            }
                        }
                    }
                    if(rc.readBroadcast(13 * myArchon) == 1){
                        buildTank(numRobots);
                    }
                }else{
                    System.out.println(rc.readBroadcast(13 * myArchon));
                    if(rc.readBroadcast(13 * myArchon) == 0 || rc.getRoundNum() - tankRequestRound >= 200){
                        order();
                        gardenerBuild(numRobots);
                    }
                }
            }
            else if(rc.getType() == RobotType.ARCHON){
                if(shitBuilt == 0 || numRobots%(5 * shitBuilt) == 0){
                    archonBuild(numRobots);
                }
            }
        }catch (GameActionException e) {
            e.printStackTrace();
        }

    }

    public static void archonBuild(int numBots){
        try {
                if(rc.getTeamBullets() >= 100) {
                    Direction[] dirList = RobotPlayer.getDirList();
                    Direction bestDir = null, dir;
                    float dirToEnemyArchon = 9999;
                    MapLocation[] enemyArchs = rc.getInitialArchonLocations(rc.getTeam().opponent());

                    for (Direction d : dirList) {
                        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(d, 5), 104, 244, 66);
                        if (rc.canHireGardener(d)) {
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
                    }
                    if(rc.canHireGardener(dir)){
                        rc.hireGardener(dir);
                        shitBuilt++;
                        rc.broadcast(12 * myArchon, numBots + 1);
                    }
                }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static void gardenerBuild(int numRobots){
        try{
            if(shitBuilt <= 10){
                earlyGameBuild(numRobots);
                return;
            }
            Priority top = priorities.get(0);

            if(top.getType() == RobotType.SOLDIER){
                if(rc.getTeamBullets() >= 100){
                    Direction[] dirList = RobotPlayer.getDirList();

                    for(Direction d : dirList){
                        if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                            rc.buildRobot(RobotType.SOLDIER, d);
                            top.setPriority(top.getPriority() + 1);
                            shitBuilt++;
                            rc.broadcast(12 * myArchon, numRobots + 1);
                        }
                    }
                }
            } else if(top.getType() == RobotType.SCOUT){
                if(rc.getTeamBullets() >= 100){
                    Direction[] dirList = RobotPlayer.getDirList();

                    for(Direction d : dirList){
                        if(rc.canBuildRobot(RobotType.SCOUT,d)){
                            rc.buildRobot(RobotType.SCOUT, d);
                            top.setPriority(top.getPriority() + 10);
                            shitBuilt++;
                            rc.broadcast(12 * myArchon, numRobots + 1);
                        }
                    }
                }
            } else if(top.getType() == RobotType.TANK){
                rc.broadcast(13 * myArchon, 1);  //tank request
                tankRequestRound = rc.getRoundNum();
                shitBuilt++;
                top.setPriority(top.getPriority() + 5);
            }

        }catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public static void buildTank(int numRobots){
        try{
            Priority top = priorities.get(0);

            if(rc.getTeamBullets() >= 300){
                Direction[] dirList = RobotPlayer.getDirList();

                for(Direction d : dirList){
                    if(rc.canBuildRobot(RobotType.TANK,d)){
                        rc.buildRobot(RobotType.TANK, d);
                        top.setPriority(top.getPriority() + 5);
                        shitBuilt++;
                        rc.broadcast(12 * myArchon, numRobots + 1);
                        rc.broadcast(13 * myArchon, 0); //tank build response
                    }
                }
            }
        }catch (GameActionException e) {
            e.printStackTrace();
        }

    }

    public static void earlyGameBuild(int numBots) {
        try{
            switch(shitBuilt){
                case 0:
                    if(rc.getTeamBullets() >= 50){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.plantTree(d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 1:
                    if(rc.getTeamBullets() >= 80){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SCOUT,d)){
                                rc.buildRobot(RobotType.SCOUT, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 2:
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.buildRobot(RobotType.SOLDIER, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 3:
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.LUMBERJACK,d)){
                                rc.buildRobot(RobotType.LUMBERJACK, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 4:
                    if(rc.getTeamBullets() >= 50){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.plantTree(d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 5:
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.buildRobot(RobotType.SOLDIER, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 6:
                    if(rc.getTeamBullets() >= 50){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.plantTree(d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 7:
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.buildRobot(RobotType.SOLDIER, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 8:
                    if(rc.getTeamBullets() >= 100){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.SOLDIER,d)){
                                rc.buildRobot(RobotType.SOLDIER, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
                case 9:
                    if(rc.getTeamBullets() >= 300){
                        Direction[] dirList = RobotPlayer.getDirList();

                        for(Direction d : dirList){
                            if(rc.canBuildRobot(RobotType.TANK,d)){
                                rc.buildRobot(RobotType.TANK, d);
                                shitBuilt++;
                                rc.broadcast(12 * myArchon, numBots + 1);
                            }
                        }
                    }
                    break;
            }
        }catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
