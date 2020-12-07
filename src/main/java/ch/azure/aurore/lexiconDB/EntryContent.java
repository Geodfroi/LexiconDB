package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.Strings.Strings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntryContent {

    //region fields

    private final int id;
    private Set<String> labelSet;
    private String content;
    private byte[] image;
    //endregion

    //region constructors
    public EntryContent(int id, Set<String> labels, String contentStr) {
        this.id = id;
        this.labelSet = labels;
        this.content = contentStr;
    }

    public EntryContent(int id, Set<String> labels, String content, byte[] array) {
        this.id =id;
        this.labelSet = labels;
        this.content = content;
       // this.links = links;
        this.image = array;
    }


    //endregion

    //region accessors
    public String getContent() {
        if (this.content == null)
            return "";
        return content;
    }

    public int getId() {
        return id;
    }

    public byte[] getImage() {
        return image;
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(this.labelSet);
    }


    public boolean hasImage(){
        return image != null && image.length > 0;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty() && !content.isBlank();
    }

    public void setContent(String content) {
        if (!Strings.isNullOrEmpty(content) && !content.equals(this.content)){
            this.content = content;

            onModified();
        }
    }

    public void setImage(byte[] image) {
        if (!Arrays.equals(this.image,image)){
            this.image = image;
            onModified();
        }
    }

    public void setLabels(Set<String> array){
        if (array == null || array.size()==0){
            this.labelSet = new HashSet<>();
        }
        else {
            this.labelSet = array;
        }
        onModified();
    }

    //endregion

    //region events
    private final List<IEntryListener> listeners = new ArrayList<>();

    public void addListener(IEntryListener listener){
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    private void onModified() {
//        if (!updatesDisabled){
            listeners.forEach(listener -> listener.entryModified(this));
            LexiconDatabase.getInstance().updateEntry(this);
       // }
    }
    //endregion

    public String getFirstLabel(){
        Optional<String> str = this.labelSet.stream().
                min(Comparator.naturalOrder());

        return str.orElse("[]");
    }

    public String getLabelStr(){
        return toLabelStr(this.labelSet);
    }


    @Override
    public String toString() {
        return getFirstLabel();
    }

//    void addLink(EntriesLink link) {
//        this.links.add(link);
//    }
//
//    void removeLink(EntriesLink link) {
//        this.links.remove(link);
//    }

    //region static methods

    public static boolean createLink(EntryContent o1, EntryContent o2) {
        return LexiconDatabase.getInstance().insertLink(o1.getId(), o2.getId());
    }

    public static Set<String> toLabelSet(String labelStr) {
        return Arrays.stream(labelStr.split(", ")).
                collect(Collectors.toSet());
    }

    public static String toLabelStr(Set<String> labels) {
        Stream<String> st  = labels.stream().
                map(Strings::toFirstLower).
                sorted(String::compareToIgnoreCase);

        return Strings.toString(st, ", ");
    }

    //endregion
}

// private boolean updatesDisabled;
// private Set<Integer> links = new HashSet<>();

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

//   public boolean saveEntry() {
//        if (modified){
//            modified = false;
//            return  LexiconDatabase.getInstance().updateEntry(this);
//        }
//        return false;
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