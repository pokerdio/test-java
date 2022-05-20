// test java file #1
import java.io.*;
import java.util.*;
public class Main {
    public static void main(String[] arg) {
        ArrayList<Thing> things = new ArrayList<Thing>();
        things.add(new Thing(1,222));
        things.add(new Thing(6,12));
        things.add(new Thing(3,98));
        Collections.sort(things, (Thing t1, Thing t2) -> t1.y - t2.y);
        System.out.println(String.format("%d %d %d", things.get(0).x, 
                                         things.get(1).x, things.get(2).x));

        Thing[] things2 = new Thing[3];
        things2[0] = new Thing(1,2);
        things2[1] = new Thing(2,3);
        things2[2] = new Thing(0,5);

        Collections.sort(things, (Thing t1, Thing t2) -> t1.y - t2.y);
    }
}
