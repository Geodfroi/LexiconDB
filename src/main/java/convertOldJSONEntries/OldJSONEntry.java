package convertOldJSONEntries;

import ch.azure.aurore.Strings.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OldJSONEntry {
    private String content;
    private String labels;
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
        try{
            List<String> list = labels.stream().
                    map(Strings::toFirstLower).
                    sorted(String::compareToIgnoreCase).collect(Collectors.toList());

            this.labels = Strings.toString(list, ", ");

        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println(this.labels);
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getLabels() {
        return labels;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setId(int id) {
        this.id=id;
    }
}