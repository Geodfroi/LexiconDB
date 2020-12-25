package ch.azure.aurore.convertOldJSONEntries;


import ch.azure.aurore.javaxt.IO.API.Disk;
import ch.azure.aurore.javaxt.IO.API.Settings;
import ch.azure.aurore.javaxt.sqlite.wrapper.SQLite;
import ch.azure.aurore.javaxt.strings.Strings;
import ch.azure.aurore.lexiconDB.EntryContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONToSQLite {
    public static final String CONVERT_DATABASE_NAME = "convertDatabase";
    private static final String CONVERT_FOLDER = "Conversions";

    public static void main(String[] args) throws IOException {

        String name = Settings.getInstance().getString(CONVERT_DATABASE_NAME);
        Files.createDirectories(Path.of(CONVERT_FOLDER));

        String pathStr = CONVERT_FOLDER + File.separator +  name;
        Path sourcePath = Paths.get(pathStr);
        if (!Files.exists(sourcePath.toAbsolutePath())){
            System.out.println("json file doesn't exist at <" + sourcePath + ">");
            return;
        }
        Disk.openFile(CONVERT_FOLDER);

        String dbStr = Disk.removeExtension(pathStr) + ".SQLite";
        Disk.removeFile(dbStr);

        List<OldJSONEntry> entries = getJSONEntries(sourcePath);
        SQLite sql = SQLite.connect(dbStr);
        if (entries == null)
            throw new IllegalStateException();

        Map<EntryContent, OldJSONEntry> map = insertEntries(sql, entries);
        createLinks(map);
        for (EntryContent e: map.keySet()) {
            sql.updateItem(e);
        }

        sql.close();
    }

    private static void createLinks(Map<EntryContent, OldJSONEntry> entries) {

        for (EntryContent newEntry : entries.keySet()) {
            OldJSONEntry oldEntry = entries.get(newEntry);

            for (String linkKey : oldEntry.getLinks()) {
                Optional<EntryContent> link = getEntryByKey(entries, linkKey);
                link.ifPresent(e -> EntryContent.createLink(e, newEntry));
            }
        }
    }

    private static Optional<EntryContent> getEntryByKey(Map<EntryContent, OldJSONEntry> entries, String linkKey) {
        for (EntryContent i:entries.keySet()) {
            OldJSONEntry oldEntry = entries.get(i);
            String key = oldEntry.getKey();
            if (key.equals(linkKey))
                return Optional.of(i);
        }
        return Optional.empty();
    }

    private static Map<EntryContent,OldJSONEntry> insertEntries(SQLite sql, List<OldJSONEntry> entries) {
        Map<EntryContent,OldJSONEntry> map = new HashMap<>();
        for (OldJSONEntry e : entries) {

            EntryContent entry = new EntryContent(0, e.getLabels(), e.getContent());
            sql.updateItem(entry);
            int id = entry.get_id();
            if (id == -1)
                throw new IllegalStateException();

            e.setId(id);
            EntryContent content = new EntryContent(id, e.getLabels(), e.getContent());
            map.put(content ,e);
        }
        return map;
    }

    private static List<OldJSONEntry> getJSONEntries(Path sourcePath) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            String txt = Files.readString(sourcePath);
            JsonNode rootNode = mapper.readTree(txt);

            ArrayList<OldJSONEntry> entries = new ArrayList<>();

            String regex = "\\[empty][ \\t\n]*";
            Pattern emptyPattern = Pattern.compile(regex);

            for (JsonNode childNode:rootNode) {
                OldJSONEntry entry = new OldJSONEntry();
                entries.add(entry);
                String contentStr = childNode.path("Content").asText();

                Matcher matcher = emptyPattern.matcher(contentStr);
                if (contentStr.isBlank() || contentStr.isEmpty() || matcher.matches())
                    entry.setContent("");
                else
                    entry.setContent(contentStr);

                JsonNode labelsArrayNode = childNode.path("Labels");
                List<String> labels = new ArrayList<>();
                for (JsonNode n:labelsArrayNode) {
                    String str = n.asText();
                    if (!Strings.isNullOrEmpty(str))
                        labels.add(n.asText());
                }
                entry.setLabels(labels);

                String key = childNode.path("Key").asText();
                entry.setKey(key);

                JsonNode linksArrayNode = childNode.path("Links");
                List<String> links = new ArrayList<>();
                for (var n:linksArrayNode
                ) {
                    links.add(n.asText());
                }
                entry.setLinks(links);
            }
            return entries;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}