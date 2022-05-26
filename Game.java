import java.io.*;

import gameobjects.*; 

public class Game {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
        String s; 
        Room r = new Room(); 
        r.name = "Troll Gameobjects.Room";
        r.s = 1;
        r.w = 2;
        r.e = 3;
        r.n = 4;
        
        do {
            System.out.print("> ");
            s = in.readLine();
            if (s.equals("look")) {
                System.out.println(r.name + String.format(" %d %d %d %d", r.s, r.w, r.e, r.n));
            } else {
                System.out.println("What do you mean by " + s + "?");
            }
        } while (!"q".equals(s)); 
    }
}
