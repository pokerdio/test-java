package globals;
import java.util.*;

public class Command {
    private static void p(Object s) {
        System.out.println(s);
    }

    @Override
    public String toString() {
        return com.toString();
    }

    private static Set<String> ignoreSet = new HashSet<String>();
    private static Map<String, String> translate = new HashMap<String,String>();
    public List<String> com;
    public List<String> matchData;

    public static void IgnoreWords(String... ignoreWords) {
        for (String w : ignoreWords) {
            ignoreSet.add(w);
        }
    }

    public static void Translation(String src, String dest) {
        translate.put(src, dest);
    }

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
                break;
            case "*":
                while (it.hasNext()) {
                    matchData.add(it.next());
                }
                break; 
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
