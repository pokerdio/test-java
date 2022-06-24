import java.io.*;
import java.util.*;
import gameobjects.*; 
import globals.*;


public class Game {
    ArrayList<Room> map; 

    public Game() {
        map = new ArrayList<Room>(); 
    }

    private static void p(Object s)  {
        System.out.println(s);
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in));

        Game game = new Game(); 
        Command com;
        Command.LoadConfig("command.txt");
        Thing.TestHasTrait();
    main_loop:
        while (true) {
            Thing pc; 
            Room r;
            try {
                r = Room.JSONLoad("world.json", "hallway");
                pc = r.RemoveFromContents("player"); // some hack :P
            }
            catch (Exception e) {
                p("...failed loading the world...\n" + e.toString());
                e.printStackTrace();
                return;
            }

            p("Welcome to a new game!");
            // p("==============test================");
            // com = new Command("use batteries");
            // p(com.Match("use", "batteries", "remote"));
            // p("============end test==============");
        game_loop:
            do {
                System.out.print("> ");
                com = new Command(in.readLine());
                if (com.Match("dbg")) { 
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
                } else if (com.Match("wait")) {
                    int state = pc.AdvanceTraitState("wait_trait", 10);
                    switch(state) {
                    case 0:
                    case 5:
                        p("You space out for a few moments.");
                        break;
                    case 1:
                    case 6:
                        p("You count sheep.");
                        break;
                    case 2:
                    case 7:
                        p("You flex your fingers.");
                        break;
                    case 3:
                        p("You stretch out. ");
                        break;
                    case 8:
                        p("You stretch out. It feels good! ");
                        break;
                    case 4:
                    case 9:
                        p("You flex your fingers.");
                        break;
                    }
                    continue game_loop;
                } else if (com.Match("give", "apple", "baby")) {
                    Thing baby = r.Has("baby");
                    Thing apple = pc.Has("apple");
                    if (baby == null) {
                        p("No baby here.");
                        continue;
                    }
                    if (apple == null) {
                        p("You don't have an apple.");
                        continue;
                    }
                    if (apple.HasTrait("clean")) {
                        p("The baby takes the apple and starts nibbling it.");
                        baby.AddTrait("has_apple");
                        if(baby.HasTrait("has_key")) {
                            p("The baby drops the key.");
                            baby.DelTrait("has_key");
                            r.AddToContents(baby.RemoveFromContents("key"));
                        }
                    } else {
                        p("The baby looks unhappily at the apple and pushes it away.");
                    }
                } else if (com.Match("open", "?")) {
                    String item_name = com.matchData.get(0);
                    Thing t = r.Has(item_name);

                    if (t == null) {
                        p("Open what?");
                        continue;
                    }
                    if (t.HasTrait("locked")) {
                        p("It is locked.");
                        continue; 
                    } else {
                        if (t.HasTrait("unlocked & !closed & !open")) {
                            p("You don't see any point in doing that.");
                            continue;
                        }
                    }
                    if (!t.HasTrait("closed")) {
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
                    Thing t = r.Has(item_name, "open");
                    if (t == null) {
                        p("You can't close that.");
                    } else {
                        t.ReplaceTrait("open", "closed");
                        p("You close the " + item_name + ".");
                    }
                } else if (com.Match("look|examine") || 
                           com.Match("look|examine", "room|around|here")) { 
                    if("examine".equals(com.com.get(0))) {
                        r.AddTrait("examine");
                    }
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
                    r.DelTrait("examine");
                } else if (com.Match("look", "outside")) {
                    Thing t = r.Has("", "outside");
                    if (null != t) {
                        p(t.GetInfo());
                        break;
                    }
                } else if (com.Match("look|examine", "?")) {
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
                        if ("examine".equals(com.com.get(0))) {
                            t.AddTrait("examine");
                        }
                        p(t.GetInfo());
                        if(t.HasTrait("contents_visible|open")) {
                            String info = t.ItemsInfo(t.contents_prefix, ".");
                            if (!info.equals ("")) {
                                p(info);
                            }
                        }
                        t.DelTrait("examine");
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
                    Thing place = r.Has(place_name, "open");

                    if (place == null) {
                        p("You can't take that.");
                        continue;
                    }

                    Thing t = place.Has(item_name, "pickable");
                    if (t != null) {
                        p("You take " + t.GetName(Thing.DEF_ART) + ".");
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
                        p("You drop " + t.GetName(Thing.DEF_ART) + ".");
                        r.AddToContents(t);
                    } else {
                        p("You don't have " + t.GetName(Thing.INDEF_ART) + ".");
                    }
                } else if (com.Match("count", "*")) {
                    p("You used " + Integer.toString(com.matchData.size()) + " words.");
                } else if (com.Match("go", "?") || com.Match("up|down|west|east|north|south")) {
                    String desto;
                    if ("go".equals(com.com.get(0))) {
                        desto = com.matchData.get(0);
                    } else {
                        desto = com.com.get(0);
                    }
                    if (r.con.containsKey(desto)) {
                        for (Thing t : r.contents) {
                            if(t.HasTrait("locked&block." + r.GetName()+"."+desto)) {
                                p("You can't. " + Thing.Capitalize(t.GetName(Thing.DEF_ART)) + " is locked.");
                                continue game_loop;
                            }
                        }
                        
                        r = r.con.get(desto);
                        p("You go " + desto + " into " + r.GetName(Thing.DEF_ART) + ".");
                    } else {
                        p("You cannot go there.");
                    }
                } else if (com.Match("unlock", "?")) { //todo do fix to allow multiple key/lock pairs
                    Thing t = r.Has(com.matchData.get(0)); //todo: make locking possible
                    if (t == null || !t.HasTrait("locked")) {
                        p("You can't unlock that.");
                        continue;
                    }
                    for (Thing k : pc.contents) {
                        if (t.HasTrait("key." + k.GetName())) {
                            t.ReplaceTrait("locked", "unlocked");
                            p("You unlock " + t.GetName(Thing.DEF_ART) + " with " + 
                              k.GetName(Thing.DEF_ART) + ".");
                            continue game_loop;
                        }
                    }
                    p("You don't have the right key.");
                } else if (com.Match("lock", "?")) { //todo do fix to allow multiple key/lock pairs
                    Thing t = r.Has(com.matchData.get(0)); //todo: make locking possible
                    if (t == null || !t.HasTrait("unlocked")) {
                        p("You can't lock that.");
                        continue; 
                    }
                    for (Thing k : pc.contents) {
                        if (t.HasTrait("key." + k.GetName())) {
                            t.ReplaceTrait("unlocked", "locked");
                            p("You lock " + t.GetName(Thing.DEF_ART) + " with " + 
                              k.GetName(Thing.DEF_ART) + ".");
                            continue game_loop;
                        }
                    }
                    p("You don't have the right key.");
                }else if (com.Match("unlock", "?", "?")) { //todo do fix to allow multiple key/lock pairs
                    Thing t = r.Has(com.matchData.get(0), "locked"); //todo: make locking possible
                    if (t == null) {
                        p("You can't unlock that.");
                        continue;
                    }
                    Thing k = pc.Has(com.matchData.get(1), t.KeyTrait());
                    if (k != null) {
                        t.ReplaceTrait("locked", "unlocked");
                        p("You unlock " + t.GetName(Thing.DEF_ART) + " with " + 
                          k.GetName(Thing.DEF_ART) + ".");
                        continue game_loop;
                    }
                    p("You don't have the right key.");
                } else if (com.Match("lock", "?", "?")) { //todo do fix to allow multiple key/lock pairs
                    Thing t = r.Has(com.matchData.get(0), "unlocked"); //todo: make locking possible
                    if (t == null) {
                        p("You can't lock that.");
                        continue; 
                    }
                    Thing k = pc.Has(com.matchData.get(1), t.KeyTrait());
                    if (k != null) {
                        t.ReplaceTrait("unlocked", "locked");
                        p("You lock " + t.GetName(Thing.DEF_ART) + " with " + 
                          k.GetName(Thing.DEF_ART) + ".");
                        continue game_loop;
                    }
                    p("You don't have the right key.");
                } else if (com.Match("use", "batteries", "remote") ||
                           com.Match("put", "batteries", "remote") || 
                           com.Match("charge", "remote") ||
                           com.Match("replace", "batteries") ||
                           com.Match("replace", "batteries", "remote"))  {

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

                    Thing baby = r.Has("baby");
                    if(baby != null) {
                        p("The baby starts watching TV.");
                        baby.AddTrait("watch_tv");
                        if (baby.HasTrait("has_key")) {
                            p("The baby drops the key he was playing with.");
                            r.AddToContents(baby.RemoveFromContents("key"));
                            baby.DelTrait("has_key");
                        }
                    }
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
                    if(bowl.HasTrait("!water & !milk")) {
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
                        p("You don't see anything to wash the apple with.");
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
