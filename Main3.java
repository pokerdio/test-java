// test java file #1
import java.io.*;
import java.util.*;
public class Main3 {
    public static void main(String[] arg) {
        ArrayList<Thing2> things = new ArrayList<Thing2>();
        things.add(new Thing2(1,222));
        things.add(new Thing2(6,12));
        things.add(new Thing2(3,98));
        Collections.sort(things);
        System.out.println(String.format("%d %d %d", things.get(0).x, 
                                         things.get(1).x, things.get(2).x));
    }
}
