package convertOldJSONEntries;

import ch.azure.aurore.IO.API.Disk;
import ch.azure.aurore.IO.API.Settings;
import ch.azure.aurore.lexiconDB.EntriesLink;
import ch.azure.aurore.lexiconDB.LexiconDatabase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONToSQLite {
    public static final String CONVERT_DATABASE_NAME = "convertDatabase";

    public static void main(String[] args) {

        Optional<String> pathStr = Settings.getInstance().getString(CONVERT_DATABASE_NAME);
        if (pathStr.isEmpty())
            throw new IllegalStateException();

        Path sourcePath = Paths.get(pathStr.get());
        String dbStr = Disk.removeExtension(pathStr.get()) + ".SQLite";
        Disk.removeFile(dbStr);

        List<OldJSONEntry> entries = getJSONEntries(sourcePath);
        LexiconDatabase.getInstance().open(dbStr);

        assert entries != null;
        insertEntries(entries);
        Set<EntriesLink> links = createLinks(entries);
        insertLinks(links);

        LexiconDatabase.getInstance().close();
    }

    private static void insertLinks(Set<EntriesLink> links) {
        for (var l:links) {
            LexiconDatabase.getInstance().insertLink(l);
        }
    }

    private static Set<EntriesLink> createLinks(List<OldJSONEntry> entries) {

        Set<EntriesLink> links = new HashSet<>();
        for (OldJSONEntry e:entries)
            for (String linkKey : e.getLinks()) {
                Optional<OldJSONEntry> entry = entries.stream().
                        filter(oldJSONEntry -> oldJSONEntry.getKey().equals(linkKey)).findAny();

                if (entry.isPresent()){
                    EntriesLink newLink = new EntriesLink(e.getId(), entry.get().getId());
                    links.add(newLink);
                }
            }

        return links;
    }

    private static void insertEntries(List<OldJSONEntry> entries) {
        for (OldJSONEntry e : entries) {
            int newId = LexiconDatabase.getInstance().insertEntry(e.getContent(), e.getLabels(), null);
            e.setId(newId);
        }
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