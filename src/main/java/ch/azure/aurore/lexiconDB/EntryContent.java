package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.javaxt.conversions.Conversions;
import ch.azure.aurore.javaxt.sqlite.wrapper.annotations.DatabaseClass;
import ch.azure.aurore.javaxt.strings.Strings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@DatabaseClass
public class EntryContent {

    private final List<IEntryListener> listeners = new ArrayList<>();
    private int _id;
    private boolean _modified = true;
    private Set<Integer> links = new HashSet<>();
    private Set<String> labels;
    private String content;
    private byte[] image;

    public EntryContent() {
    }

    public EntryContent(int id, Set<String> labels, String contentStr) {
        this._id = id;
        this.labels = labels;
        this.content = contentStr;
    }

    public EntryContent(int id, Set<String> labels, String content, byte[] array) {
        this._id = id;
        this.labels = labels;
        this.content = content;
        this.image = array;
    }

    public static String[] createDummyDB() {
        TestDatabases.createDummyDB(false);
        return new String[]{TestDatabases.TEST_DATABASE_PATH, TestDatabases.TEST_OTHER_DATABASE_PATH};
    }

    public static void createLink(EntryContent e0, EntryContent e1) {
        e0.addLink(e1.get_id());
        e1.addLink(e0.get_id());
    }

    public static Set<String> toLabelSet(String labelStr) {
        return Arrays.stream(labelStr.split(", ")).
                collect(Collectors.toSet());
    }

    public static String toLabelStr(Set<String> labels) {
        Stream<String> st = labels.stream().
                map(Strings::toFirstLower).
                sorted(String::compareToIgnoreCase);

        return Conversions.toString(st, ", ");
    }

    public static void removeLink(EntryContent e0, EntryContent e1) {
        e0.removeLink(e1);
        e1.removeLink(e0);
    }

    private void removeLink(EntryContent e) {
        if (links.contains(e._id)){
            links.remove(e._id);
            modified();
        }
    }

    public void addListener(IEntryListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public String getContent() {
        if (Strings.isNullOrEmpty(content))
            return "";
        return content;
    }

    public void setContent(String content) {
        if (!Strings.isNullOrEmpty(content) && !content.equals(this.content)) {
            this.content = content;

            modified();
        }
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(this.labels);
    }

    public void setLabels(Set<String> array) {
        if (array == null || array.size() == 0) {
            this.labels = new HashSet<>();
        } else {
            this.labels = array;
        }
        modified();
    }

    public Set<Integer> getLinks() {
        return links;
    }

    public void setLinks(Set<Integer> links) {
        this.links = links;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        if (!Arrays.equals(this.image, image)) {
            this.image = image;
            modified();
        }
    }

    public boolean is_modified() {
        return _modified;
    }

    public void set_modified(boolean _modified) {
        this._modified = _modified;
    }

    private void addLink(int id) {
        if (!links.contains(id)) {
            links.add(id);
            modified();
        }
    }

    public String getFirstLabel() {
        if (labels == null)
            return "";

        Optional<String> str = this.labels.stream().
                min(Comparator.naturalOrder());

        return str.orElse("");
    }

    public String getLabelStr() {
        return toLabelStr(this.labels);
    }

    public boolean hasImage() {
        return image != null && image.length > 0;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty() && !content.isBlank();
    }

    private void modified() {
        _modified = true;
        listeners.forEach(listener -> listener.entryModified(this));
    }

    @Override
    public String toString() {
        return getFirstLabel();
    }
}

//    public boolean saveEntry() {
//        if (_modified) {
//            _modified = false;
//            return LexiconDatabase.getInstance().updateEntry(this);
//        }
//        return false;
//    }
// private boolean updatesDisabled;
// private Set<Integer> links = new HashSet<>();

//    private void disableUpdates() {
//        updatesDisabled = true;
//    }

//    public static Set<Integer> toLinkSet(String linkStr){
//        if (Strings.isNullOrEmpty(linkStr))
//            return new HashSet<>();
//
//        return Arrays.stream(linkStr.split("-")).
//                map(Integer::parseInt).collect(Collectors.toSet());
//    }
//
//    public static String toLinksStr(Set<Integer> links) {
//        Stream<Integer> st = links.stream().
//                sorted(Integer::compare);
//        return Strings.toString(st, "-");
//    }