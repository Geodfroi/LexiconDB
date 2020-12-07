package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.IO.API.Disk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class TestDatabases {

    private static final String TEST_FOLDER = "TestFolder";
    public static final String TEST_DATABASE_PATH = TEST_FOLDER + File.separator + "TestLexicon.SQLite";
    public static final String TEST_OTHER_DATABASE_PATH = TEST_FOLDER + File.separator + "OtherTestLexicon.SQLite";

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void main(String[] args) {

        try {
            Files.createDirectories(Path.of(TEST_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (args.length ==0)
            Disk.openFile(TEST_FOLDER);

        Disk.removeFile(TEST_DATABASE_PATH);

        LexiconDatabase.getInstance().open(TEST_DATABASE_PATH);

        Optional<EntryContent> animal = LexiconDatabase.getInstance().insertEntry("Wolves, elephants and black cats are animals", Set.of("animal"));
        Optional<EntryContent> wolf =   LexiconDatabase.getInstance().insertEntry("The wolf is an animal", Set.of("wolf"));
        Optional<EntryContent> elephant = LexiconDatabase.getInstance().insertEntry("Elephants are very big",Set.of( "elephant"));
        Optional<EntryContent> cat = LexiconDatabase.getInstance().insertEntry("Cats are sly animals",Set.of( "blackCat"));
        Optional<EntryContent> lions = LexiconDatabase.getInstance().insertEntry("Lions are big felines", Set.of("whiteLion"));
        Optional<EntryContent> feline = LexiconDatabase.getInstance().insertEntry("Felines are hunting animals", Set.of("feline"));

        EntryContent.createLink(animal.get(), elephant.get());
        EntryContent.createLink(animal.get(), wolf.get());
        EntryContent.createLink(animal.get(), feline.get());
        EntryContent.createLink(feline.get(), cat.get());
        EntryContent.createLink(feline.get(), lions.get());

        LexiconDatabase.getInstance().close();

        Disk.removeFile(TEST_OTHER_DATABASE_PATH);

        LexiconDatabase.getInstance().open(TEST_OTHER_DATABASE_PATH);
        LexiconDatabase.getInstance().insertEntry("Cars are vehicles", Set.of("car"));
        LexiconDatabase.getInstance().insertEntry("vehicles are very fast", Set.of("vehicle"));
        LexiconDatabase.getInstance().insertEntry("Buses are often late", Set.of("bus"));
        LexiconDatabase.getInstance().close();
    }
}
