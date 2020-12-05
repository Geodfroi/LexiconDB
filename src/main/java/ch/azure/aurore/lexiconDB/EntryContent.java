package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.Strings.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntryContent {

    private final int id;
    private String labels;
    private String content;
    private boolean modified;
    private byte[] image;

    public EntryContent(int id, String content, String labelStr, byte[] image) {
        this.id = id;
        this.content = content;
        this.labels = labelStr;
        this.image = image;
    }

    //region accessors
    public String getContent() {
        if (this.content == null)
            return "";
        return content;
    }

    public int getId() {
        return id;
    }

    public String getLabels() {
        return labels;
    }

    public byte[] getImage() {
        return image;
    }

    public boolean hasImage(){
        return image != null && image.length > 0;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty() && !content.isBlank();
    }

    public void setContent(String content) {
        if (!this.content.equals(content)){
            this.content = content;
            onModified();
        }
    }

    public void setLabels(List<String> array){
        if (array == null || array.size()==0)
            return;

        List<String> list = array.stream().
                map(Strings::toFirstLower).
                sorted(String::compareToIgnoreCase).collect(Collectors.toList());

        this.labels = Strings.toString(list, ", ");
        onModified();
    }

    public void setImage(byte[] image) {
        if (!Arrays.equals(this.image,image)){
            this.image = image;
            onModified();
        }
    }
    //endregion

    //region events
    private final List<IEntryListener> listeners = new ArrayList<>();

    public void addListener(IEntryListener listener){
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    private void onModified() {
        this.modified = true;
        for (var listener:listeners) {
            listener.entryModified(this);
        }
    }
    //endregion

    public boolean saveEntry() {
        if (modified){
            modified = false;
            return  LexiconDatabase.getInstance().updateEntry(this);
        }
        return false;
    }

    @Override
    public String toString() {
        return labels;
    }
}