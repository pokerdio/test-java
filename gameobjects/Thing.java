package gameobjects;

import java.io.*;
import java.util.*;


import org.json.simple.*;
import org.json.simple.parser.*;


public class Thing {
    public String name, info;
    public Set<String> traits;
    private static void p(Object o) {
        System.out.println(o);
    }

    @Override
    public String toString() {
        return name +  "|" + info + "|" + traits.toString();
        
    }
    public static Map<String, Thing> JSONReadThingList(String filename, String field) throws Exception {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        Object o = jsonParser.parse(reader);
        JSONObject all = (JSONObject)o;
        JSONArray things = (JSONArray)(all.get(field));

        Map<String, Thing> ret = new HashMap<String, Thing>();
        for(int i=0 ; i<things.size() ; ++i) {
            Thing thng = new Thing((JSONObject)(things.get(i)));
            ret.put(thng.name, thng);
        }
        return ret;
    }
    public Thing(JSONObject thing) {
        name = (String)(thing.get("name"));
        info = (String)(thing.get("info"));
        traits = new HashSet<String>();
        if (thing.containsKey("traits")) {
            JSONArray ta = (JSONArray)(thing.get("traits"));

            for (Iterator it = ta.iterator() ; it.hasNext() ; ) {
                traits.add((String) it.next());
            }
        }
    }
    public Thing (String name, String info) {
        this.name = name;
        this.info = info;

        this.traits = new HashSet<String>();
    }
    public Thing(Thing other) {
        other = new Thing(name, info);
        other.traits = new HashSet<String>(other.traits);
    }
}

