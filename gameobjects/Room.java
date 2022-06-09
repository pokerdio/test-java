package gameobjects;

import java.util.*;
import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Room extends Thing {

    public Map<String, Room> con;

    @SuppressWarnings("unchecked")
    public static Room JSONLoad(String filename, String start_room) throws Exception {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        Object o = jsonParser.parse(reader);
        JSONObject world = (JSONObject)o;
        JSONArray map = (JSONArray)(world.get("map"));
        JSONObject room; 

        List<String> roomfrom = new ArrayList<String>(); 
        List<String> roomto = new ArrayList<String>();
        List<String> roomdir = new ArrayList<String>();
        Map<String,Room> rooms = new HashMap<String,Room>();
        
        
        for (Iterator it = map.iterator() ; it.hasNext() ;){
            room = (JSONObject)it.next();
            String name = (String)room.get("name");
            String info = (String)room.get("info");
            JSONObject links = (JSONObject)room.get("links");
            rooms.put(name, new Room(name, info));

            for (Iterator it2 = links.keySet().iterator() ; it2.hasNext() ; ){
                String dir = (String)it2.next();
                roomfrom.add(name);
                roomdir.add(dir);
                roomto.add((String)links.get(dir));
            }
        }

        Iterator<String> itfrom=roomfrom.iterator();
        Iterator<String> itto=roomto.iterator();
        Iterator<String> itdir = roomdir.iterator();
        for (; itfrom.hasNext();) {
            rooms.get(itfrom.next()).Connect(rooms.get(itto.next()), itdir.next());
        }
        return rooms.get(start_room);
    }

    public void Connect(Room r, String dir) {
        con.put(dir, r);
    }
    public String ConnectionInfo() {
        if (con.isEmpty()) {
            return "There is no way out of here. ";
        }
        String s = ""; 
        String nl = "";
        for (String dir : con.keySet()) {
            
            s += nl + "Towards " + dir + " there is the " + con.get(dir).name + ".";
            nl = "\n";
        }
        return s;
    }
    public Room(String name, String info) {
        super(name, info);
        con = new HashMap<String, Room>();
    }
}
