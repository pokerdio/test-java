package gameobjects;

import java.io.*;
import java.util.*;


import org.json.simple.*;
import org.json.simple.parser.*;


public class Thing {
    protected String name;
    protected String display_name; 
    protected String info;
    protected Set<String> traits;
    public String contents_prefix;
    private String indefinite_article;
    private String definite_article;
    public List<Thing> contents;

    public static final int DEF_ART = 1;
    public static final int INDEF_ART = 2;
    public static final int NO_ARTICLE = 3;

    private List<String> contents_by_name; //intermediate values that feed contents


    public String GetName(int article) {
        String s;
        if (display_name == null) {
            s = name;
        } else {
            s = display_name; 
        }
        switch (article) {
        case DEF_ART:
            return definite_article + s;
        case INDEF_ART:
            return indefinite_article + s;
        default:
            return name;
        }
    }

    private String AppendSpace(String s) {
        if ("".equals(s)) {
            return "";
        }
        if (s.charAt(s.length()-1) != ' ') {
                s = s + " ";
        }
        return s;
    }

    public String GetName() {
        return name;
    }
    
    private static void p(Object o) {
        System.out.println(o);
    }


    public String GetInfo() {
        String i = this.info;
        for (String s : traits) {
            i = i.replaceAll("<"+s+":([^>]*)>", "$1");
        }
        i = i.replaceAll("<[^>]*>", "");
        return "    " + i;
    }

    public String ItemsInfo(String prefix, String postfix) {
        if (contents.isEmpty()) {
            return "";
        }
        String s = "";

        int count = 0;
        for (Thing t : contents) {
            if (t.HasTrait("listable")) {
                count += 1;
            }
        }

        String and = "";
        for (Thing t : contents) {
            if (t.HasTrait("listable")) {
                if (count == 1) {
                    return prefix + t.GetName(INDEF_ART) + postfix; 
                } else {
                    s = s + and + t.GetName(INDEF_ART);
                    and = " and ";
                }
            }
        }
        return prefix + s + postfix;
    }



    public boolean HasTrait(String trait) {
        return traits.contains(trait);
    }
    public void RemoveTrait(String trait) {
        if (HasTrait(trait)) {
            traits.remove(trait);
        }
    }

    public void AddTrait(String trait){
        traits.add(trait);
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

        for(Map.Entry<String,Thing> e : ret.entrySet()) {
            String name = e.getKey();
            Thing t  = e.getValue();
            for (String item : t.contents_by_name) {
                if (!ret.containsKey(item)) {
                    throw new Exception("bad item " + item);
                }
                t.AddToContents(ret.get(item));
            }
        }

        return ret;
    }

    // private because it doesn't completely initialize all the properties
    // this only gets called from the world loading function in a subclass
    private Thing(JSONObject thing) {
        name = (String)(thing.get("name"));
        info = (String)(thing.get("info"));
        contents_prefix = "Inside you see ";
        if (thing.containsKey("contents_prefix")) {
            contents_prefix = (String)(thing.get("contents_prefix"));
            if (contents_prefix.charAt(contents_prefix.length()-1) != ' ') {
                contents_prefix = contents_prefix + " ";
            }
        }
        definite_article = "the ";
        indefinite_article = "a ";
        traits = new HashSet<String>();
        contents_by_name = new ArrayList<String>();
        contents = new ArrayList<Thing>();        
        if (thing.containsKey("traits")) {
            JSONArray ta = (JSONArray)(thing.get("traits"));

            for (Iterator it = ta.iterator() ; it.hasNext() ; ) {
                String new_trait = (String) it.next();
                if (new_trait != null) {
                    traits.add(new_trait);
                }
            }
        }
        if (thing.containsKey("indefinite_article")) {
            indefinite_article = AppendSpace((String)thing.get("indefinite_article"));
        }

        if (thing.containsKey("display_name")) {
            display_name = (String)thing.get("display_name");
        }

        if (thing.containsKey("definite_article")) {
            definite_article = AppendSpace((String)thing.get("definite_article"));
        }
        if (thing.containsKey("items")) {
            JSONArray ta = (JSONArray)(thing.get("items"));

            for (Iterator it = ta.iterator() ; it.hasNext() ; ) {
                String new_item = (String) it.next();
                if (new_item != null) {
                    contents_by_name.add(new_item);
                }
            }
        }
    }
    public Thing (String name, String info) {
        this.indefinite_article = "a ";
        this.definite_article = "the ";
        this.contents_prefix = "Inside you see ";
        this.name = name;
        this.info = info;
        this.contents = new ArrayList<Thing>();
        this.traits = new HashSet<String>();
        this.contents_by_name = new ArrayList<String>();
    }
}

