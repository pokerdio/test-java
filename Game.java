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
        Command.IgnoreWords("to", "from", "at", "in", "with", "inside");
        Command.IgnoreWords("the", "a", "an", "to", "on");

        Command.Translation("r", "restart");
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
        Command.Translation("window", "outside");
        
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

        Command.Translation("downstairs", "down");
        Command.Translation("downstair", "down");

        Command.Translation("upstairs", "up");
        Command.Translation("upstair", "up");

        Command.Translation("up", "stair", "up");
        Command.Translation("up", "stairs", "up");

        Command.Translation("down", "stair", "down");
        Command.Translation("down", "stairs", "down");

        Command.Translation("turn", "on", "power"); 
        Command.Translation("climb", "stairs", "up"); 
        
        Command.Translation("turn", "off", "unpower"); 
        Command.Translation("light", "switch", "switch"); 
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
        Command com;
        InitializeCommand();

    main_loop:
        while (true) {
            Thing pc; 
            Room r;
            try {
                r = Room.JSONLoad("world.json", "hallway");
                pc = r.RemoveFromContents("player"); // some hack :P
            }
            catch (Exception e) {
                p("...failed loading the world..." + e.toString());
                e.printStackTrace();
                return;
            }

            p("Welcome to a new game!");
            // p("==============test================");
            // com = new Command("use batteries");
            // p(com.Match("use", "batteries", "remote"));
            // p("============end test==============");
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
                
                } else if (com.Match("restart")) {
                    continue main_loop;
                } else if (com.Match("open", "?")) {
                    String item_name = com.matchData.get(0);
                    Thing t = r.Has(item_name);
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
                            t.ReplaceTrait("closed", "open");
                            p("You open the " + item_name + ".");
                        }
                    }
                } else if (com.Match("close", "?")) {
                    String item_name = com.matchData.get(0);
                    Thing t = r.Has(item_name);
                    if (t == null || !t.HasTrait("open")) {
                        if (t.HasTrait("closed")) {
                            p("It's already closed.");
                        } else {
                            p("You can't close that.");
                        }
                    } else {
                        t.ReplaceTrait("open", "closed");
                        p("You close the " + item_name + ".");
                    }
                } else if (com.Match("look")) {
                    p(r.GetInfo());
                    p("");
                    p(r.ConnectionInfo());

                    if(!r.HasTrait("dark")) {
                        String itemInfo = r.ItemsInfo("There is ", " here.");
                        if (!itemInfo.equals("")) {
                            p("");
                            p(itemInfo);
                        }
                    }
                } else if (com.Match("look", "outside")) {
                    for (Thing t : r.contents) {
                        if (t.HasTrait("outside")) {
                            p(t.GetInfo());
                            break;
                        }
                    }
                } else if (com.Match("look", "?")) {
                    if(r.HasTrait("dark")) {
                        p("It is too dark to see anything.");
                        continue;
                    }

                    String item_name = com.matchData.get(0);
                    // p("trying to find " + item_name + " in:");
                    // p("=====================================");                
                    // r.contents.forEach(x -> p(x));
                    // p("           ---------");
                    // pc.contents.forEach(x -> p(x));
                    // p("=====================================");
                    Thing t = r.Has(item_name);
                    if (t != null) {
                        p(t.GetInfo());
                        if(t.HasTrait("open")) {
                            String contents_info = t.ItemsInfo(t.contents_prefix, ".");
                            if (!contents_info.equals ("")) {
                                p(contents_info);
                            }
                        }
                    } else {
                        t = pc.Has(item_name);
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
                    Thing fridge = r.Has("refrigerator");
                    if ((null != fridge) && fridge.HasTrait("open") && 
                        (fridge.Has("milk") != null)) {
                        p("You'd rather not carry that around, it is too heavy. ");
                    } else {
                        p("You can't take that.");
                    }
                } else if (com.Match("take", "?", "?")) {
                    if(r.HasTrait("dark")) {
                        p("It is too dark to see anything.");
                        continue;
                    }
                    String item_name = com.matchData.get(0);
                    String place_name = com.matchData.get(1);
                    Thing place = r.Has(place_name);

                    if (place == null || place.HasTrait("closed")) {
                        p("You can't take that.");
                        continue;
                    }

                    Thing t = place.Has(item_name);
                    if (t != null && t.HasTrait("pickable")) {
                        p("You take the " + item_name + ".");
                        place.RemoveFromContents(item_name);
                        pc.AddToContents(t);
                    } else {
                        p("You can't take that.");
                    }
                } else if (com.Match("take", "?")) {
                    if(r.HasTrait("dark")) {
                        p("It is too dark to see anything.");
                        continue;
                    }

                    String item_name = com.matchData.get(0);
                    Thing t = r.Has(item_name);
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

                    if (null == pc.Has("batteries"))  {
                        p("You don't have any batteries.");
                        continue;
                    }

                    Thing remote = pc.Has("remote");
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
                    Thing sofa = r.Has("sofa");
                    if (sofa != null) {
                        if (sofa.HasTrait("closed")) {
                            p("Between the pillows of the sofa you find a remote control. ");
                            sofa.ReplaceTrait("closed", "open");
                        }
                    } else {
                        p("There is no such thing here.");
                    }
                } else if ((com.Match("power", "tv")) ||
                           (com.Match("use", "remote")) ||
                           (com.Match("use", "remote", "tv")) ||
                           (com.Match("activate", "tv"))) {
                    Thing tv = r.Has("tv");
                    if(tv == null) {
                        p("No TV here.");
                        continue;
                    }

                    Thing remote = pc.Has("remote");
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
                    tv.ReplaceTrait("off", "on");
                } else  if (com.Match("use", "batteries")) {
                    if(null != pc.Has("batteries")) {
                        p("You chain a few batteries and touch the plus " +
                          "and minus to your tongue. ZAP! Ouch.");
                    } else {
                        p("What batteries?");
                    }
                } else if (com.Match("eat", "?")) {
                    String food_name = com.matchData.get(0);
                    Thing food = pc.Has(food_name);
                    if (null != food && food.HasTrait("edible")) {
                        p("You eat " + food.GetName(Thing.DEF_ART) + ".");
                        pc.RemoveFromContents(food_name);
                    } else {
                        p("You can't eat that.");
                    }
                } else if (com.Match("drink", "?")) {
                    String drink_name = com.matchData.get(0);

                    Thing drink = pc.Has(drink_name);
                    if (drink == null) {
                        drink = r.Has(drink_name);
                    }
                    if (null != drink) {
                        if (drink.HasTrait("water")) {
                            p("You drink the water from " + drink.GetName(Thing.DEF_ART) + ".");
                            pc.AddTrait("hydrated");
                            drink.DelTrait("water");
                        } else if (drink.HasTrait("milk")) {
                            p("You drink the milk from " + drink.GetName(Thing.DEF_ART) + ".");
                            pc.AddTrait("hydrated");
                            drink.DelTrait("milk");
                        } else {
                            p("You can't drink that.");
                        }
                    } else {
                        p("You can't drink that.");
                    }
                } else if (com.Match("fill|put|pour", "bowl", "milk", "*") ||
                           com.Match("fill|put|pour", "milk", "bowl", "*")) { 
                    Thing bowl = pc.Has("bowl");
                    if (null == bowl) {
                        p("You don't have a bowl.");
                        continue;
                    } 
                    Thing fridge = r.Has("refrigerator");
                    if ((fridge == null) || !fridge.HasTrait("open") || 
                        (fridge.Has("milk") == null)) {
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
                    Thing bowl = pc.Has("bowl");
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
                    Thing bowl = pc.Has("bowl");
                    if (null == bowl) {
                        p("You don't have a bowl.");
                        continue;
                    } 
                    if(!bowl.HasTrait("water") && !bowl.HasTrait("milk")) {
                        p("The bowl is already empty.");
                        continue;
                    }
                    if (null != r.Has("sink")) {
                        p("You empty out the bowl into the sink.");
                        bowl.DelTrait("water");
                        bowl.DelTrait("milk");
                        continue;
                    }
                    p("Nothing seems suitable for disposing of unwanted liquid here.");
                } else if (com.Match("quit")) {
                    p("Bye!");
                    break;
                } else if (com.Match("power", "light|switch")) {
                    if (!r.HasTrait("light switch")) {
                        p("You see no light switch here.");
                        continue;
                    }
                    if (r.HasTrait("dark")) {
                        p("You turn on the light.");
                        r.ReplaceTrait("dark", "lit");
                    }  else if (r.HasTrait("lit")) {
                        p("The light is already on.");
                    } else {
                        p("You can't do that.");
                    }
                } else if (com.Match("unpower", "light|switch")) {
                    if (!r.HasTrait("light switch")) {
                        p("You see no light switch here.");
                        continue;
                    }
                    if (r.HasTrait("lit")) {
                        p("You turn off the light.");
                        r.ReplaceTrait("lit", "dark");
                    }  else if (r.HasTrait("dark")) {
                        p("The light is already off.");
                    } else {
                        p("You can't do that.");
                    }
                } else if (com.Match("flick|flip", "light|switch")) {
                    if (!r.HasTrait("light switch")) {
                        p("You see no light switch here.");
                        continue;
                    }
                    if (r.HasTrait("dark")) {
                        p("You turn on the light.");
                        r.ReplaceTrait("dark", "lit");
                    }  else if (r.HasTrait("lit")) {
                        p("You turn off the light.");
                        r.ReplaceTrait("lit", "dark");
                    } else {
                        p("You can't do that.");
                    }
                } else if (com.Match("wash", "apple", "*")) {
                    Thing apple = pc.Has("apple");
                    if (null == apple) {
                        p("You don't have an apple.");
                        continue;
                    }
                    Thing sink = r.Has("sink");
                    if (null == sink) {
                        p("You don't see anything to wash the apple in.");
                        continue;
                    }
                    p("You wash the apple vigurously in the sink.");
                    apple.AddTrait("clean");
                } else if (com.Match("use", "lavatory|toilet")) {
                    if (r.HasTrait("lavatory")) {
                        if (pc.HasTrait("hydrated")) {
                            if(r.HasTrait("dark")) {
                                p("You can't in the dark.");
                            } else {
                                p("Okay.");
                                pc.DelTrait("hydrated");
                            }
                        } else {
                            p("You can't anymore.");
                        }
                    } else {
                        p("You don't see a toilet so you decide it can wait.");
                    }
                } else {
                    p("You can't do that.");
                } 

            } while (true); 
            break;
        }
    }
}
