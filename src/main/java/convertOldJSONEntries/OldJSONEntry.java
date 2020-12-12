package convertOldJSONEntries;

import ch.azure.aurore.strings.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OldJSONEntry {
    private String content;
    private Set<String> labels;
    private String key;
    private List<String> links = new ArrayList<>();
    private int id;

    public String getKey() {
        return key;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String s) {
        content = s;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels.stream().
                filter(s -> !Strings.isNullOrEmpty(s)).
                map(Strings::toFirstLower).
                collect(Collectors.toSet());
        //      this.labels = Strings.toString(list, ", ");
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setId(int id) {
        this.id=id;
    }

    public String getLinkIDs() {
        return Strings.toString(this.links, "-");
    }
}