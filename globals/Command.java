package globals;
import java.util.*;

public class Command {
    private static void p(String s) {
        System.out.println(s);
    }
    private static Set<String> ignoreSet = new HashSet<String>();
    public static void IgnoreWords(String... ignoreWords) {
        for (String w : ignoreWords) {
            ignoreSet.add(w);
        }
    }
    private static Map<String, String> translate = new HashMap<String,String>();
    public static void Translation(String src, String dest) {
        translate.put(src, dest);
    }

    public List<String> com;
    public List<String> matchData;

    public boolean Match(String... pat) {
        if (com == null) {
            matchData = null;
            return false;
        }
        matchData = new ArrayList<String>();

        Iterator<String> it = com.iterator();
        for (String c : pat) {
            switch(c) {
            case "?":
                if (it.hasNext()) {
                    matchData.add(it.next());
                }
            case "*":
                while (it.hasNext()) {
                    matchData.add(it.next());
                }
            default:
                if (it.hasNext()) {
                    if (!c.equals(it.next())) {
                        matchData = null;
                        return false;
                    }
                }
            }
        }
        if (it.hasNext()) {
            matchData = null;
            return false;
        }
        return true; 
    }
    public Command (String s) {
        com = new ArrayList<String>();
        
        for (String c : s.split("\\W+")) {
            if (ignoreSet.contains(c)) {
                continue;
            }
            if (translate.get(c) != null) {
                c = translate.get(c);
            }
            com.add(c);
        }
    }
}
