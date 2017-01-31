package Helpers;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Dan on 1/26/2017.
 */
public class PriorityComparator implements Comparator <Priority>{
    public int compare(Priority p1, Priority p2){
       return p1.getPriority() - p2.getPriority();
    }
}
