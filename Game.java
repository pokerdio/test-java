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

        Command.Translation("o", "open");
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
            e.printStackTrace();
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
            } else if (com.Match("open", "?")) {
                String item_name = com.matchData.get(0);
                Thing t = r.FindInContents(item_name);
                if (t == null || !t.HasTrait("closed")) {
                    p("You can't open that.");
                } else {
                    if (t.HasTrait("locked")) {
                        p("The " + item_name + " is locked.");
                    } else {
                        t.RemoveTrait("closed");
                        t.AddTrait("open");
                        p("You open the " + item_name + ".");
                    }
                }
            } else if (com.Match("close", "?")) {
                String item_name = com.matchData.get(0);
                Thing t = r.FindInContents(item_name);
                if (t == null || !t.HasTrait("open")) {
                    p("You can't close that.");
                } else {
                    t.RemoveTrait("open");
                    t.AddTrait("closed");
                    p("You close the " + item_name + ".");
                }
            } else if (com.Match("look")) {
                p(r.GetInfo());
                p("");
                p(r.ConnectionInfo());
                String itemInfo = r.ItemsInfo("There is ", " here.");
                if (!itemInfo.equals("")) {
                    p("");
                    p(itemInfo);
                }
            } else if (com.Match("look", "outside")) {
                for (Thing t : r.contents) {
                    if (t.HasTrait("outside")) {
                        p(t.GetInfo());
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
                    p(t.GetInfo());
                    if(t.HasTrait("open")) {
                        String contents_info = t.ItemsInfo("Inside you see ", ".");
                        if (!contents_info.equals ("")) {
                            p(contents_info);
                        }
                    }
                } else {
                    t = pc.FindInContents(item_name);
                    if (t != null) {
                        p(t.GetInfo());
                    } else {
                        p("There's nothing interesting about that.");
                    }
                } 
            } else if (com.Match("inventory")) {
                if (pc.contents.size() > 0) {
                    String s = "You have"; 
                    String and = " ";
                    for (Thing t : pc.contents) {
                        s = s + and + t.GetName(Thing.INDEFINITE_ARTICLE);
                        and = " and ";
                    }
                    p(s + ".");
                } else {
                    p("You don't carry anything.");
                }
            } else if (com.Match("take", "?", "?")) {
                String item_name = com.matchData.get(0);
                String place_name = com.matchData.get(1);
                Thing place = r.FindInContents(place_name);

                if (place == null || place.HasTrait("closed")) {
                    p("You can't take that.");
                    continue;
                }

                Thing t = place.FindInContents(item_name);
                if (t != null && t.HasTrait("pickable")) {
                    p("You take the " + item_name + ".");
                    place.RemoveFromContents(item_name);
                    pc.AddToContents(t);
                } else {
                    p("You can't take that.");
                }
            } else if (com.Match("take", "?")) {
                String item_name = com.matchData.get(0);
                Thing t = r.FindInContents(item_name);
                if (t != null && t.HasTrait("pickable")) {
                    r.RemoveFromContents(item_name);
                    pc.AddToContents(t);
                    p("You take the " + item_name + ".");
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
                    p("You go " + desto + " into " + r.GetName(Thing.DEFINITE_ARTICLE) + ".");
                } else {
                    p("You cannot go there.");
                }
            }
        } while (!com.Match("quit")); 
    }
}
