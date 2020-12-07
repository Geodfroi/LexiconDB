//package ch.azure.aurore.lexiconDB;
//
//import java.util.Objects;
//
//public class EntriesLink {
//
//    private final EntryContent entry1;
//    private final EntryContent entry2;
//
//    public EntryContent getEntry1() {
//        return entry1;
//    }
//
//    public EntryContent getEntry2() {
//        return entry2;
//    }
//
//    public EntriesLink(EntryContent o1, EntryContent o2) {
//        this.entry1 = o1;
//        this.entry2 = o2;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//            return true;
//        if (o == null || getClass() != o.getClass())
//            return false;
//        EntriesLink that = (EntriesLink) o;
//        return entry1 == that.entry1 && entry2 == that.entry2;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(entry1, entry2);
//    }
//
//    @Override
//    public String toString() {
//        return "EntriesLink{"
//                + entry1.getFirstLabel() + " - "
//                + entry2.getFirstLabel() + '}';
//    }
//
//    //region static methods
//
//    public static boolean createLink(EntryContent o1, EntryContent o2) {
//
//        if (LexiconDatabase.getInstance().insertLink(o1.getId(), o2.getId())){
//            EntriesLink link = new EntriesLink(o1, o2);
//            o1.addLink(link);
//            o2.addLink(link);
//            return true;
//        }
//        return false;
//    }
//
//    public static boolean removeLink(EntriesLink link){
//        if ( LexiconDatabase.getInstance().removeLink(link)){
//            link.entry1.removeLink(link);
//            link.entry2.removeLink(link);
//            return true;
//        }
//        return false;
//    }
//
////
////    public static List<EntryContent> removeLinkID(List<EntryContent> entries, int formerEntry){
////        return entries.stream().
////                filter(e -> !Integer.valueOf(e.id).equals(formerEntry) && e.removeLink(formerEntry)).
////                collect(Collectors.toList());
////
//////        entry.links.stream().map(new Function<Integer, Optional<EntryContent>>() {
//////            @Override
//////            public Optional<EntryContent> apply(Integer integer) {
//////                 return entries.stream().
//////                         filter(e-> integer.equals(e.getId())).
//////                         findAny();
//////            }
//////        }).forEach(new Consumer<Optional<EntryContent>>() {
//////            @Override
//////            public void accept(Optional<EntryContent> entryContent) {
//////                if (entryContent.isPresent())
//////                    entryContent.get().removeLink(entry);
//////            }
//////        });
////    }
//    //endregion
//}