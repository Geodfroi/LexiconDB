package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.IO.API.Disk;

public class TestDatabases {

    public static void main(String[] args) {
        String pathStr =  "TestLexicon.SQLite";
        Disk.removeFile(pathStr);

        LexiconDatabase.getInstance().open(pathStr);
        int animalID = LexiconDatabase.getInstance().insertEntry("Wolves, elephants and black cats are animals", "animal", null);
        int wolfId =   LexiconDatabase.getInstance().insertEntry("The wolf is an animal", "wolf", null);
        int elephantId = LexiconDatabase.getInstance().insertEntry("Elephants are very big", "elephants", null);
        int catID = LexiconDatabase.getInstance().insertEntry("Cats are sly animals", "blackCat", null);
        int lionsID = LexiconDatabase.getInstance().insertEntry("Lions are big felines", "whiteLion", null);
        int felineID = LexiconDatabase.getInstance().insertEntry("Felines are hunting animals", "feline",null);

        LexiconDatabase.getInstance().insertLink(new EntriesLink(catID, felineID));
        LexiconDatabase.getInstance().insertLink(new EntriesLink(lionsID, felineID));
        LexiconDatabase.getInstance().insertLink(new EntriesLink(animalID, elephantId));
        LexiconDatabase.getInstance().insertLink(new EntriesLink(animalID, wolfId));

        LexiconDatabase.getInstance().close();

        pathStr = "OtherTestLexicon.SQLite";
        Disk.removeFile(pathStr);

        LexiconDatabase.getInstance().open(pathStr);
        LexiconDatabase.getInstance().insertEntry("Cars are vehicles", "car", null);
        LexiconDatabase.getInstance().insertEntry("vehicles are very fast", "vehicle",null);
        LexiconDatabase.getInstance().insertEntry("Buses are often late", "bus",null);
        LexiconDatabase.getInstance().close();
    }
}
