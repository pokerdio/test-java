package gameobjects;

import java.io.*;
import java.util.*;


import org.json.simple.*;
import org.json.simple.parser.*;


public class Thing {
    public String name, info;
    protected Set<String> traits;
    public List<Thing> contents;
    
    private static void p(Object o) {
        System.out.println(o);
    }

    public boolean HasTrait(String trait) {
        return traits.contains(trait);
    }

    @Override
    public String toString() {
        return name +  "|" + info + "|" + traits.toString();
    }
    public Thing FindInContents(String name) {
        for (Thing t : contents) {
            if (t.name.equals(name)) {
                return t;                
            }
        }
        return null;
    }
    public Thing RemoveFromContents(String name) {
        Thing t = FindInContents(name);
        if (t != null) {
            int idx = contents.indexOf(t);
            contents.remove(idx);
            return t;
        }
        return null;
    }

    public void AddToContents(Thing t) {
        contents.add(t);
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
                String new_trait = (String) it.next();
                if (new_trait != null) {
                    traits.add(new_trait);
                }
            }
        }
    }
    public Thing (String name, String info) {
        this.name = name;
        this.info = info;
        this.contents = new ArrayList<Thing>();
        this.traits = new HashSet<String>();
    }
}

