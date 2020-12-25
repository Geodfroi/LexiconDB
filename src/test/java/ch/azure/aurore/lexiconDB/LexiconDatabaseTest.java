package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.sqlite.wrapper.SQLite;

class LexiconDatabaseTest {

    private static SQLite sqlite;

    @org.junit.jupiter.api.Test
    void queryEntry() {
        TestDatabases.createDummyDB(false);
    }

//    @org.junit.jupiter.api.Test
//    void deleteEntry(){
//        int id = 1;
//        Optional<EntryContent> entry = LexiconDatabase.getInstance().queryEntry(id);
//        assert entry.isPresent();
//
//        boolean result = LexiconDatabase.getInstance().removeEntry(entry.get());
//        assert result;
//    }
}

//    void delete_removeLinks(){
//        int id = 1;
//
//    //  List<EntryContent> entries = LexiconDatabase.getInstance().queryEntries();
////        List<EntryContent> modified =   EntryContent.removeLinkID(entries, id);
////        assert modified.size() > 0;
//    }