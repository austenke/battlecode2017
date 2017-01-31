package Helpers;

import battlecode.common.*;

import java.awt.*;

public class Priority {
    static int priority;
    static RobotType type;
    static boolean isTree;

    Priority(int p, RobotType t, boolean b){
        priority = p;
        type = t;
        isTree = b;
    }

    public static void setPriority(int p){
        priority = p;
    }

    public static int getPriority(){
        return priority;
    }

    public static RobotType getType(){
        return type;
    }

    public static boolean isTree(){
        return isTree;
    }
}
