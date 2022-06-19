package gameobjects;

import java.util.*;
import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Room extends Thing {
    private static void p(Object o) {
        System.out.println(o);
    }

    public Map<String, Room> con; //connections to other rooms

    @SuppressWarnings("unchecked")
    public static Room JSONLoad(String filename, String start_room) throws Exception {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        Object o = jsonParser.parse(reader);
        JSONObject world = (JSONObject)o;
        JSONArray map = (JSONArray)(world.get("map"));
        List<String> roomfrom = new ArrayList<String>(); 
        List<String> roomto = new ArrayList<String>();
        List<String> roomdir = new ArrayList<String>();
        Map<String,Room> rooms = new HashMap<String,Room>();
        Map<String,Thing> world_items = Thing.JSONReadThingList("world.json", "items");;
        

        for (Iterator it = map.iterator() ; it.hasNext() ;){
            JSONObject element = (JSONObject)it.next();
            JSONObject links = (JSONObject)element.get("links");
            Room room = new Room(element);
            String name = room.name;
            String info = room.info;

            rooms.put(name, room);

            for (Iterator it2 = links.keySet().iterator() ; it2.hasNext() ; ){
                String dir = (String)it2.next();
                roomfrom.add(name);
                roomdir.add(dir);
                roomto.add((String)links.get(dir));
            }
            JSONArray room_items = (JSONArray)(element.get("items"));
            if (room_items != null) {
                for (Object obj : room_items) {
                    if (world_items.get(obj) == null) {
                        throw new Exception("bad item " + (String) obj);
                    }
                }
                room_items.forEach(x -> room.contents.add(world_items.get(x)));
            }
        }

        Iterator<String> itfrom=roomfrom.iterator();
        Iterator<String> itto=roomto.iterator();
        Iterator<String> itdir = roomdir.iterator();
        for (; itfrom.hasNext();) {
            String sfrom = itfrom.next();
            String sto = itto.next();
            String sdir = itdir.next();
            rooms.get(sfrom).Connect(rooms.get(sto), sdir);
        }
        return rooms.get(start_room);
    }

    public void Connect(Room r, String dir) {
        con.put(dir, r);
    }


    protected String Capitalize(String s) {
        if("".equals(s) || null == s)  {
            return "";
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    protected String TowardsPrefix(String direction) {
        switch(direction) {
        case "north":
        case "south":
        case "east":                
        case "west":
            return "Towards " + direction + " there is the ";
        case "up":
        case "down":
            return Capitalize(direction + "wards there is the ");
        default:
            return "ERROR";
        }
    }
    public String ConnectionInfo() {
        if (con.isEmpty()) {
            return "There is no way out of here. ";
        }
        String s = ""; 
        String nl = "";
        for (String dir : con.keySet()) {
            s += nl + TowardsPrefix(dir) + con.get(dir).name + ".";
            nl = "\n";
        }
        return s;
    }
    public Room(String name, String info) {
        super(name, info);
        con = new HashMap<String, Room>();
    }
    protected Room(JSONObject obj) {
        super(obj);
        con = new HashMap<String, Room>();
    }
}
