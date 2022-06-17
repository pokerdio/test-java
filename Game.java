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
        Command.IgnoreWords("to", "from", "at", "in", "with");
        Command.IgnoreWords("the", "a", "an", "to");

        Command.Translation("o", "open");
        Command.Translation("television", "tv");
        Command.Translation("telly", "tv");
        Command.Translation("tube", "tv");
        Command.Translation("fridge", "refrigerator");
        Command.Translation("see", "look");
        Command.Translation("watch", "look");
        Command.Translation("ogle", "look");
        Command.Translation("exa", "examine");
        Command.Translation("x", "examine");
        
        Command.Translation("l", "look");
        Command.Translation("q", "quit");

        Command.Translation("i", "inventory");
        Command.Translation("inv", "inventory");

        Command.Translation("insert", "put");
        Command.Translation("stick", "put"); 
        
        Command.Translation("s", "south");
        Command.Translation("n", "north");
        Command.Translation("w", "west");
        Command.Translation("e", "east");


        Command.Translation("turn", "on", "power");
        Command.Translation("jug", "milk", "milk");
        Command.Translation("tv", "set", "tv");
        Command.Translation("tv", "tube", "tv");                
        Command.Translation("kitchen", "sink", "sink");
        Command.Translation("remote", "control", "remote");        

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

        p("==============test================");
        com = new Command("use batteries");
        p(com.Match("use", "batteries", "remote"));
        p("============end test==============");
        do {
            System.out.print("> ");
            com = new Command(in.readLine());
            if (com.Match("examine")) { //debug command only
                p("=========r==========");
                p(r);
                p(r.contents);
                p("=========pc==========");
                p(pc);
                p("=========inv=========");
                p(pc.contents);
                p("---------------------");

            } else if (com.Match("open", "?")) {
                String item_name = com.matchData.get(0);
                Thing t = r.FindInContents(item_name);
                if (t == null || !t.HasTrait("closed")) {
                    if (t.HasTrait("open")) {
                        p("It's already open.");
                    } else {
                        p("You can't open that.");
                    }
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
                    if (t.HasTrait("closed")) {
                        p("It's already closed.");
                    } else {
                        p("You can't close that.");
                    }
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
                        String contents_info = t.ItemsInfo(t.contents_prefix, ".");
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
                        s = s + and + t.GetName(Thing.INDEF_ART);
                        and = " and ";
                    }
                    p(s + ".");
                } else {
                    p("You don't carry anything.");
                }
            } else if (com.Match("take", "milk", "refrigerator")) {
                Thing fridge = r.FindInContents("refrigerator");
                if ((null != fridge) && fridge.HasTrait("open") && 
                    (fridge.FindInContents("milk") != null)) {
                    p("You'd rather not carry that around, it is too heavy. ");
                } else {
                    p("You can't take that.");
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
                    p("You go " + desto + " into " + r.GetName(Thing.DEF_ART) + ".");
                } else {
                    p("You cannot go there.");
                }
            } else if (com.Match("use", "batteries", "remote") ||
                       com.Match("put", "batteries", "remote") || 
                       com.Match("charge", "remote") ||
                       com.Match("replace", "batteries") ||
                       com.Match("replace", "batteries", "remote") 
                )  {

                if (null == pc.FindInContents("batteries"))  {
                    p("You don't have any batteries.");
                    continue;
                }

                Thing remote = pc.FindInContents("remote");
                if (remote == null)  {
                    p("You don't have anything that needs replacing batteries.");
                    continue;
                }
                if (remote.HasTrait("powered")) {
                    p("The remote control is already powered.");
                }
                p("You insert new batteries in the remote control. ");
                remote.AddTrait("powered");
            } else if (com.Match("search", "sofa")) {
                Thing sofa = r.FindInContents("sofa");
                if (sofa != null) {
                    if (sofa.HasTrait("closed")) {
                        p("Between the pillows of the sofa you find a remote control. ");
                        sofa.RemoveTrait("closed");
                        sofa.AddTrait("open");
                    }
                } else {
                    p("There is no such thing here.");
                }
            } else if ((com.Match("power", "tv")) ||
                       (com.Match("use", "remote")) ||
                       (com.Match("use", "remote", "tv")) ||
                       (com.Match("activate", "tv"))) {
                Thing tv = r.FindInContents("tv");
                if(tv == null) {
                    p("No TV here.");
                    continue;
                }

                Thing remote = pc.FindInContents("remote");
                if (remote == null) {
                    p("You don't have a remote.");
                    continue;
                }
                if (!remote.HasTrait("powered")) {
                    p("The remote is not powered.");
                    continue;
                }
                if (tv.HasTrait("on")) {
                    p("The TV is already on.");
                    continue;
                }
                p("You turn on the TV.");
                tv.AddTrait("on");
                tv.RemoveTrait("off");
            } else  if (com.Match("use", "batteries")) {
                if(null != pc.FindInContents("batteries")) {
                    p("You chain a few batteries and touch the plus " +
                      "and minus to your tongue. ZAP! Ouch.");
                } else {
                    p("What batteries?");
                }
            } else if (com.Match("eat", "?")) {
                String food_name = com.matchData.get(0);
                Thing food = pc.FindInContents(food_name);
                if (null != food && food.HasTrait("edible")) {
                    p("You eat " + food.GetName(Thing.DEF_ART) + ".");
                    pc.RemoveFromContents(food_name);
                } else {
                    p("You can't eat that.");
                }
            } else if (com.Match("drink", "?")) {
                String drink_name = com.matchData.get(0);

                Thing drink = pc.FindInContents(drink_name);
                if (drink == null) {
                    drink = r.FindInContents(drink_name);
                }
                if (null != drink) {
                    if (drink.HasTrait("water")) {
                        p("You drink the water from " + 
                          drink.GetName(Thing.DEF_ART) + ".");
                        drink.RemoveTrait("water");
                    } else if (drink.HasTrait("milk")) {
                        p("You drink the milk from " + 
                          drink.GetName(Thing.DEF_ART) + ".");
                        drink.RemoveTrait("milk");
                    } else {
                        p("You can't drink that.");
                    }
                } else {
                    p("You can't drink that.");
                }
            } else if (com.Match("fill|put|pour", "bowl", "milk", "*") ||
                       com.Match("fill|put|pour", "milk", "bowl", "*")) { 
                Thing bowl = pc.FindInContents("bowl");
                if (null == bowl) {
                    p("You don't have a bowl.");
                    continue;
                } 
                Thing fridge = r.FindInContents("refrigerator");
                if ((fridge == null) || !fridge.HasTrait("open") || 
                    (fridge.FindInContents("milk") == null)) {
                    p("There's no milk here.");
                    continue;
                }
                if(bowl.HasTrait("water")) {
                    p("The bowl is already filled with water.");
                    continue;
                }
                if(bowl.HasTrait("milk")) {
                    p("The bowl is already filled with milk.");
                    continue;
                }
                p("You fill the bowl with milk from the jug in the refrigerator.");
                bowl.AddTrait("milk");
            } else if (com.Match("fill|put|pour", "bowl", "water", "*") ||
                       com.Match("fill|put|pour", "water", "bowl", "*")) {
                Thing bowl = pc.FindInContents("bowl");
                if (null == bowl) {
                    p("You don't have a bowl.");
                    continue;
                } 
                if(bowl.HasTrait("water")) {
                    p("The bowl is already filled with water.");
                    continue;
                }
                if(bowl.HasTrait("milk")) {
                    p("The bowl is already filled with milk.");
                    continue;
                }
                p("You fill the bowl with water from the kitchen sink.");
                bowl.AddTrait("water");
            } else if (com.Match("empty", "bowl", "*")) {
                Thing bowl = pc.FindInContents("bowl");
                if (null == bowl) {
                    p("You don't have a bowl.");
                    continue;
                } 
                if(!bowl.HasTrait("water") && !bowl.HasTrait("milk")) {
                    p("The bowl is already empty.");
                    continue;
                }
                if (null != r.FindInContents("sink")) {
                    p("You empty out the bowl into the sink.");
                    bowl.RemoveTrait("water");
                    bowl.RemoveTrait("milk");
                    continue;
                }
                p("Nothing seems suitable for disposing of unwanted liquid here.");
            } else {
                p("You can't do that.");
            } 

        } while (!com.Match("quit")); 
    }
}
