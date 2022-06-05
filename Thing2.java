import java.io.*;
import java.util.*;

class Thing2 implements Comparable<Thing2>{
    public int x, y; 
    public Thing2 (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int distTo(Thing2 other)  {
        if (other != null) {
            return (other.x - x) * (other.x - x) + (other.y - y) * (other.y - y);
        }
        return 0; 
    }
    @Override 
    public int compareTo(Thing other) {
        int other_dist = other.distTo(null);
        return x * x + y * y - other_dist * other_dist;
    }
}
