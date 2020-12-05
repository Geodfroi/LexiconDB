package ch.azure.aurore.lexiconDB;

public class JSONToSQLite {
}

//public class JSONConverter {
//
//    private final static String JSON_PATH = "craftLexicon.json";
//
//    public static void main(String[] args) {
//
//        List<JSONEntry> entries = getJSONEntries();
////        for (JSONEntry entry: entries) {
////            System.out.println(entry.key);
////            System.out.println(entry.content);
////            System.out.println(entry.labels);
////            System.out.println(entry.links);
////            System.out.println("**********");
////        }
//        System.out.println("********\r\nobject count: " + entries.size());
//
//        DataAccess database = DataAccess.getInstance();
//
//        String databasePath = File.separator + "Database.SQLite";
//        database.open(databasePath);
//
//        System.out.println("Building entries");
//        setEntry(database, entries);
//        System.out.println("setting links");
//        setLinks(database,entries);
//
//        database.close();
//    }
//
//    private static void setLinks(DataAccess database, List<JSONEntry> entries) {
//
//        List<EntriesLink> links = new ArrayList<>();
//
//        for (JSONEntry entry:entries) {
//
//               entries.stream().
//                       filter(e -> entry.links.contains(e.getKey())).
//                       forEach(e -> createLink(links, entry.getID(), e.getID(), database));
//
//        }
//    }
//
//    private static void createLink(List<EntriesLink> links, int left, int right, DataAccess database) {
//        int firstID = Math.min(left, right);
//        int secondID = Math.max(left, right);
//
//        if (links.stream().noneMatch(link -> link.getMinID() == firstID && link.getMaxID() == secondID)){
//
//            var link = new EntriesLink(firstID, secondID);
//            links.add(link);
//            database.insertLink(link);
//            System.out.println("Insert link: " + link.getMinID() + "-" + link.getMaxID());
//        }
//        else
//            System.out.println("duplicate link");
//    }
//
//    private static void setEntry(DataAccess database, List<JSONEntry> entries) {
//
//        for (JSONEntry entry : entries) {
//
//            String labels = CollectionSt.toString(entry.getLabels(), ", ");
//            System.out.println("Entry : " + labels);
//            int id = database.createEntry(entry.content, labels);
//            entry.setId(id);
//        }
//    }
//
//    private static List<JSONEntry> getJSONEntries() {
//        ArrayList<JSONEntry> entries = new ArrayList<>();
//        Path path = Paths.get(JSON_PATH);
//
//       throw new UnsupportedOperationException("Not implemented yet: getJSONEntries");
////        try {
////            String str = Files.readString(path);
////            JSONArray array = new JSONArray(str);
////            for (int i = 0; i < array.length(); i++) {
////
////                JSONObject object = array.getJSONObject(i);
////                System.out.println(object);
////
////                JSONEntry entry = new JSONEntry();
////                entries.add(entry);
////
////                if (!object.isNull("Content")){
////                    entry.setContent(object.getString("Content"));
////                }
////
////                entry.setKey(object.getString("Key"));
////
////                JSONArray linksArray = object.getJSONArray("Links");
////                for (int n = 0; n < linksArray.length(); n++)
////                    entry.addLink(linksArray.getString(n));
////
////                JSONArray labelArray = object.getJSONArray("Labels");
////                for (int n = 0; n < labelArray.length(); n++)
////                    entry.addLabel(labelArray.getString(n));
////            }
////
////
////        } catch (IOException e) {
////            System.out.println(e.getMessage());
////        }
////        return entries;
//    }
//}

//package ch.azure.aurore.lexicon.data;
//
//import java.util.ArrayList;
//
//public class JSONEntry {
//
//    ArrayList<String> links = new ArrayList<>();
//    ArrayList<String> labels = new ArrayList<>();
//    String key;
//    String content;
//    private int id;
//
//    public int getID() {
//        return id;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public ArrayList<String> getLinks() {
//        return links;
//    }
//
//    public void setLinks(ArrayList<String> links) {
//        this.links = links;
//    }
//
//    public ArrayList<String> getLabels() {
//        return labels;
//    }
//
//    public void setLabels(ArrayList<String> labels) {
//        this.labels = labels;
//    }
//
//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }
//
//    public void addLink(String string) {
//        links.add(string);
//    }
//
//    public void addLabel(String string) {
//        labels.add(string);
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//}

