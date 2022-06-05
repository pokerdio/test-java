import java.io.*;
import java.util.*;
import gameobjects.*; 
import globals.*;


public class Game {
    ArrayList<Room> map; 
    Direction foo; 

    public Game() {
        map = new ArrayList<Room>(); 
    }
    private static void InitializeCommand() {
        Command.IgnoreWords("to", "from", "at");
        Command.IgnoreWords("the", "a", "an");

        Command.Translation("see", "look");
        Command.Translation("watch", "look");
        Command.Translation("ogle", "look");
        Command.Translation("exa", "examine");
        Command.Translation("l", "look");
        Command.Translation("q", "quit");

        Command.Translation("s", "south");
        Command.Translation("n", "north");
        Command.Translation("w", "west");
        Command.Translation("e", "east");
    }
    private static void p(String s)  {
        System.out.println(s);
    }
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
        String s; 

        Game game = new Game(); 
        Room r;
        try {
            r = Room.JSONLoad("world.JSON", "hallway");
        }
        catch (Exception e) {
            p("...failed loading the world..." + e.toString());
            return;
        }

        Command com;
        InitializeCommand();
        
        do {
            System.out.print("> ");
            com = new Command(in.readLine());
            if (com.Match("look")) {
                p(r.info);
                p("");
                p(r.ConnectionInfo());
            } else if (com.Match("look", "?")){
                p("you try to look at " + com.matchData.get(0));
            } else if (com.Match("go", "?")){
                String desto = com.matchData.get(0);
                if (r.con.containsKey(desto)) {
                    r = r.con.get(desto);
                    p("You go " + desto + " into the " + r.name + ".");
                } else {
                    p("You cannot go there.");
                }
            }
        } while (!com.Match("quit")); 
    }
}
