package gameobjects;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.*;
import org.json.simple.parser.*;


public class Thing {
    protected String name;
    protected String generic_name; 
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
            i = i.replaceAll("<!"+s+"[^>]*>", "");
        }
        i = i.replaceAll("<![^:]*:([^>]*)>", "$1");
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
        if (count == 0) {
            return "";
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

    private boolean HasTraitAtom(String atom_sentence) {
        atom_sentence = atom_sentence.trim();
        if ("!".equals(atom_sentence.substring(0, 1))) {
            String s = atom_sentence.substring(1).trim();
            return !traits.contains(s);
        } else {
            return traits.contains(atom_sentence);
        }
    }

    //make sure when calling no "|" are given in the parameter
    private boolean HasTraitAnd(String and_sentence) {
        for (String s : and_sentence.split("\\&")) {
            s = s.trim();
            if (!HasTraitAtom(s)) {
                return false;
            }
        }
        return true;
    }
    // handles logical or and logical and of multiple traits
    // a&b|c&d parses as (a&b) | (c&d)
    public boolean HasTrait(String trait_sentence) {
        for (String and_sentence : trait_sentence.split("\\|")) {
            and_sentence = and_sentence.trim();
            if (HasTraitAnd(and_sentence)) {
                return true;
            }
        }
        return false;
    }

    private static void TestHasTrait_RunTest(Thing t, String s, boolean desired_result)  throws Exception {
        Boolean result = new Boolean(t.HasTrait(s));
        if (Boolean.compare(result, new Boolean(desired_result)) != 0) {
            String traits_s = t.traits.stream().collect(Collectors.joining(","));
            throw new Exception(String.format("traits:" + traits_s + " query:" + s + 
                                              " return:" + result.toString()));
        }
    }
    public static void TestHasTrait() throws Exception {
        Thing t = new Thing("test", "used to test");
        TestHasTrait_RunTest(t, "foo", false);
        TestHasTrait_RunTest(t, "foo|bar", false);
        TestHasTrait_RunTest(t, "foo&bar", false);

        t.AddTrait("foo");
        t.AddTrait("bar");

        TestHasTrait_RunTest(t, "foo", true);
        TestHasTrait_RunTest(t, "bar", true); 
        TestHasTrait_RunTest(t, "foo|bar", true);       
        TestHasTrait_RunTest(t, "foo|boo", true);       
        TestHasTrait_RunTest(t, "boo|bar", true);       
        TestHasTrait_RunTest(t, "boo|belch", false);
        TestHasTrait_RunTest(t, "foo&boo|belch&bar", false);        
        TestHasTrait_RunTest(t, "foo&bar|belch&boo|bro", true);
        TestHasTrait_RunTest(t, "foo&!boo|belch&bar", true);
        TestHasTrait_RunTest(t, "foo&boo|!belch&bar", true);
        TestHasTrait_RunTest(t, "foo&!bar|belch&boo|bro", false);
        TestHasTrait_RunTest(t, "!foo&!bar|belch&!boo|bro", false);
        TestHasTrait_RunTest(t, "!foo&!bar|belch&!boo|!bro", true);
        TestHasTrait_RunTest(t, "!foo&!bar|!belch&!boo|!bro", true);
        TestHasTrait_RunTest(t, "foo&bar|belch&!boo|!bro", true);
        TestHasTrait_RunTest(t, "foo&!boo|belch&!boo|bro", true);
        TestHasTrait_RunTest(t, "!foo&!boo|belch&!boo|!bar", false);
    }

    public int AdvanceTraitState(String trait_id, int N) {
        for (String s : traits) {
            if (s.startsWith(trait_id)) {
                int state = Integer.parseInt(s.substring(trait_id.length() + 1));
                state = (state + 1) % N;
                ReplaceTrait(s, trait_id + "." + String.valueOf(state));
                return state;
            }
        } 
        AddTrait(trait_id + ".0");
        return 0;
    }

    public void DelTrait(String trait) {
        if (HasTrait(trait)) {
            traits.remove(trait);
        }
    }

    public void AddTrait(String trait){
        traits.add(trait);
    }
    public void ReplaceTrait(String src, String dest) {
        DelTrait(src);
        AddTrait(dest);
    }

    @Override
    public String toString() {
        String s = name;
        if(!generic_name.equals(name)) {
            s = s + "(" + generic_name + ")";
        }
        return s +  "|" + info + "|" + traits.toString() + "\n";
    }
    public Thing Has(String s, String trait) {
        for (Thing t : contents) {
            if (("".equals(s) || t.name.equals(s) || t.generic_name.equals(s)) && 
                t.HasTrait(trait)) {
                return t;
            }
        }
        return null;
    }
    public Thing Has(String s) {
        for (Thing t : contents) {
            if (t.name.equals(s) || t.generic_name.equals(s)) {
                return t;                
            }
        }
        return null;
    }
    public Thing RemoveFromContents(String name) {
        Thing t = Has(name);
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

    // protected because it doesn't completely initialize all the properties
    // this only gets called from the world loading function in a subclass
    protected Thing(JSONObject thing) {
        name = (String)(thing.get("name"));
        if (thing.containsKey("generic_name")) {
            generic_name = (String)(thing.get("generic_name"));
        } else {
            generic_name = name;
        }
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
        this.generic_name = name;
        this.info = info;
        this.contents = new ArrayList<Thing>();
        this.traits = new HashSet<String>();
        this.contents_by_name = new ArrayList<String>();
    }


    public static String Capitalize(String s) {
        if("".equals(s) || null == s)  {
            return "";
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
    


    //returns a "key.key name" trait if present - doors & other unlockables
    //to identify the key that can unlock it
    public String KeyTrait() { 
        for (String tr : traits) {
            if (tr.startsWith("key.")) {
                return tr;
            }
        }
        return null;
    }
}

