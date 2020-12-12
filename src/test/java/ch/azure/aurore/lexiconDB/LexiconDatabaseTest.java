package ch.azure.aurore.lexiconDB;

import org.junit.jupiter.api.Assertions;

import java.util.Optional;

class LexiconDatabaseTest {

    @org.junit.jupiter.api.BeforeAll
    static void beforeAll() {
        TestDatabases.createDummyDB(false);
        LexiconDatabase.getInstance().open(TestDatabases.TEST_DATABASE_PATH);
    }

    @org.junit.jupiter.api.Test
    void queryEntry() {
        int id = 3;
        Optional<EntryContent> entry = LexiconDatabase.getInstance().queryEntries().stream().
                filter(e -> e.getId() == id).findAny();
        assert  entry.isPresent();
        Assertions.assertEquals(entry.get().getId(), id);
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