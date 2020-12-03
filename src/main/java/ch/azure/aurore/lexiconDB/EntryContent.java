package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.Collections.CollectionSt;
import ch.azure.aurore.Strings.Strings;

import java.util.*;
import java.util.stream.Collectors;

public class EntryContent {

    private final int id;
    private final String labels;
    private String content;
    private boolean modified;

    public static String reorderLabels(String labels){
        if (labels == null || labels.isEmpty() || labels.isBlank())
            return "";

        List<String> list = Arrays.stream(labels.split(", *")).
                map(Strings::toFirstLower).
                sorted(String::compareToIgnoreCase).collect(Collectors.toList());

        return CollectionSt.toString(list, ", ");
    }

    public EntryContent(int id, String content, String labelStr) {
        this.id = id;
        this.content = content;
        this.labels = labelStr;
    }

    public int getId() {
        return id;
    }

    public String getLabels() {
        return labels;
    }

    public String getContent() {
        if (this.content == null)
            return "";
        return content;
    }

    public void setContent(String content) {
        if (!this.content.equals(content)){
            this.content = content;
            modified();
        }
    }

    private void modified() {
        this.modified = true;
    }

    public void save() {
        if (modified){
            modified = false;
            LexiconDatabase.getInstance().updateEntry(this);
            System.out.println("saved");
        }
    }

    @Override
    public String toString() {
        return labels;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty() || content.isBlank();
    }
}
