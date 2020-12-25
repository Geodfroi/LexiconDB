package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.conversions.Conversions;
import ch.azure.aurore.sqlite.wrapper.annotations.DatabaseClass;
import ch.azure.aurore.strings.Strings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DatabaseClass
public class EntryContent {

    private int _id;
    private boolean _modified = true;
    private List<EntryLink> links = new ArrayList<>();
    private Set<String> labelSet;
    private String content;
    private byte[] image;

    //region constructors
    public EntryContent(){
    }

    public EntryContent(int id, Set<String> labels, String contentStr) {
        this._id = id;
        this.labelSet = labels;
        this.content = contentStr;
    }

    public EntryContent(int id, Set<String> labels, String content, byte[] array) {
        this._id = id;
        this.labelSet = labels;
        this.content = content;
        this.image = array;
    }
    //endregion

    //region events
    private final List<IEntryListener> listeners = new ArrayList<>();

    public void addListener(IEntryListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    //endregion

    public void addLink(EntryLink link) {
        links.add(link);
        links.sort(Comparator.comparingInt(EntryLink::get_id));
        modified();
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

    //region Accessors
    public String getContent() {
        if (Strings.isNullOrEmpty(content))
            return "";
        return content;
    }

    public int get_id() {
        return _id;
    }

    public byte[] getImage() {
        return image;
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(this.labelSet);
    }

    public List<EntryLink> getLinks() {
        return links;
    }

    public boolean is_modified() {
        return _modified;
    }
    //endregion

    //region Mutators
    public void setContent(String content) {
        if (!Strings.isNullOrEmpty(content) && !content.equals(this.content)) {
            this.content = content;

            modified();
        }
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setImage(byte[] image) {
        if (!Arrays.equals(this.image, image)) {
            this.image = image;
            modified();
        }
    }

    public void setLabels(Set<String> array) {
        if (array == null || array.size() == 0) {
            this.labelSet = new HashSet<>();
        } else {
            this.labelSet = array;
        }
        modified();
    }

    public void setLinks(List<EntryLink> links) {
        this.links = links;
    }

    public void set_modified(boolean _modified) {
        this._modified = _modified;
    }
    //endregion

    public String getFirstLabel() {
        Optional<String> str = this.labelSet.stream().
                min(Comparator.naturalOrder());

        return str.orElse("[]");
    }

    public String getLabelStr() {
        return toLabelStr(this.labelSet);
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

//    void addLink(EntriesLink link) {
//        this.links.add(link);
//    }
//
//    void removeLink(EntriesLink link) {
//        this.links.remove(link);
//    }

//    public Set<Integer> getLinks(){
//        return Set.copyOf(this.links);
//    }

//    private void createLink(int id) {
//        links.add(id);
//        onModified();
//    }

//    public String getLinkStr(){
//        return toLinksStr(this.links);
//    }

//    public boolean removeLink(int id){
//        if (this.links.remove(id)) {
//            onModified();
//            return true;
//        }
//        return false;
//    }

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