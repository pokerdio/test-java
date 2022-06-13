import java.io.*;
import java.util.*;
import gameobjects.*; 
import globals.*;


public class Game {
    ArrayList<Room> map; 

    public Game() {
        map = new ArrayList<Room>(); 
    }
    private static void InitializeCommand() {
        Command.IgnoreWords("to", "from", "at");
        Command.IgnoreWords("the", "a", "an", "to");

        Command.Translation("see", "look");
        Command.Translation("watch", "look");
        Command.Translation("ogle", "look");
        Command.Translation("exa", "examine");
        Command.Translation("l", "look");
        Command.Translation("q", "quit");

        Command.Translation("i", "inventory");
        Command.Translation("inv", "inventory");

        Command.Translation("s", "south");
        Command.Translation("n", "north");
        Command.Translation("w", "west");
        Command.Translation("e", "east");

        Command.Translation("kitchen", "sink", "sink");
    }

    private static void p(Object s)  {
        System.out.println(s);
    }


    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in));

        Game game = new Game(); 
        Thing pc = new Thing("me", "...just plain old me."); // player character
        Room r;
        try {
            r = Room.JSONLoad("world.json", "hallway");
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
            if (com.Match("examine")) { //debug command only
                p(r);
                p(r.contents);
            } else if (com.Match("look")) {
                p(r.info);
                p("");
                p(r.ConnectionInfo());
                String itemInfo = r.ItemsInfo();
                if (!itemInfo.equals("")) {
                    p("");
                    p(itemInfo);
                }
            } else if (com.Match("look", "outside")) {
                for (Thing t : r.contents) {
                    if (t.HasTrait("outside")) {
                        p(t.info);
                        break;
                    }
                }
            } else if (com.Match("look", "?")) {
                String item_name = com.matchData.get(0);
                // p("trying to find " + item_name + " in:");
                // p("=====================================");                
                // r.contents.forEach(x -> p(x));
                // p("           ---------");
                // pc.contents.forEach(x -> p(x));
                // p("=====================================");
                Thing t = r.FindInContents(item_name);
                if (t != null) {
                    p(t.info);                    
                } else {
                    t = pc.FindInContents(item_name);
                    if (t != null) {
                        p(t.info);
                    }
                }
            } else if (com.Match("inventory")) {
                if (pc.contents.size() > 0) {
                    String s = "You have"; 
                    String and_a = " a ";
                    for (Thing t : pc.contents) {
                        s = s + and_a + t.name;
                        and_a = " and a ";
                    }
                    p(s + ".");
                } else {
                    p("You don't carry anything.");
                }
            } else if (com.Match("take", "?")) {
                String item_name = com.matchData.get(0);
                Thing t = r.RemoveFromContents(item_name);
                if (t != null && t.HasTrait("pickable")) {
                    p("You take the " + item_name + ".");
                    pc.AddToContents(t);
                } else {
                    p("You can't take that.");
                }
            } else if (com.Match("drop", "?")) {
                String item_name = com.matchData.get(0);
                Thing t = pc.RemoveFromContents(item_name);
                if (t != null) {
                    p("You drop the " + item_name + ".");
                    r.AddToContents(t);
                } else {
                    p("You don't have a " + item_name + ".");
                }
            } else if (com.Match("count", "*")) {
                p("You used " + Integer.toString(com.matchData.size()) + " words.");
            } else if (com.Match("go", "?")) {
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
