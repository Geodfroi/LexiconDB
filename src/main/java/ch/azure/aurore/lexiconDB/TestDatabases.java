package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.IO.API.Disk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class TestDatabases {

    private static final String TEST_FOLDER = "TestFolder";
    public static final String TEST_DATABASE_PATH = TEST_FOLDER + File.separator + "TestLexicon.SQLite";
    public static final String TEST_OTHER_DATABASE_PATH = TEST_FOLDER + File.separator + "OtherTestLexicon.SQLite";
    private static final String LION_IMAGE_PATH = "lion.png";

    public static void main(String[] args) {
        createDummyDB(true);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    static String[] createDummyDB(boolean openFolder) {
        try {
            Files.createDirectories(Path.of(TEST_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (openFolder)
            Disk.openFile(TEST_FOLDER);

        Disk.removeFile(TEST_DATABASE_PATH);

        LexiconDatabase.getInstance().open(TEST_DATABASE_PATH);

        Optional<Integer> animal = LexiconDatabase.getInstance().insertEntry(Set.of("animal"), "Wolves, elephants and black cats are animals");
        Optional<Integer> wolf =   LexiconDatabase.getInstance().insertEntry(Set.of("wolf"),"The wolf is an animal");
        Optional<Integer> elephant = LexiconDatabase.getInstance().insertEntry(Set.of( "elephant"), "Elephants are very big");
        Optional<Integer> cat = LexiconDatabase.getInstance().insertEntry(Set.of( "blackCat"), "Cats are sly animals");
        Optional<Integer> lionID = LexiconDatabase.getInstance().insertEntry(Set.of("whiteLion"), "Lions are big felines");
        Optional<Integer> feline = LexiconDatabase.getInstance().insertEntry(Set.of("feline"), "Felines are hunting animals");

        EntryContent.createLink(animal.get(), elephant.get());
        EntryContent.createLink(animal.get(), wolf.get());
        EntryContent.createLink(animal.get(), feline.get());
        EntryContent.createLink(feline.get(), cat.get());
        EntryContent.createLink(feline.get(), lionID.get());

        try {
            byte[] bytes = Objects.requireNonNull(TestDatabases.class.getClassLoader().
                    getResourceAsStream(LION_IMAGE_PATH)).
                    readAllBytes();

            Optional<EntryContent> entry = LexiconDatabase.getInstance().queryEntries().stream().
                    filter(e -> e.getId() == lionID.get()).findAny();
            if (entry.isEmpty())
                throw new IllegalStateException();

            entry.get().setImage(bytes);
            LexiconDatabase.getInstance().updateEntry(entry.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
        LexiconDatabase.getInstance().close();

        Disk.removeFile(TEST_OTHER_DATABASE_PATH);

        LexiconDatabase.getInstance().open(TEST_OTHER_DATABASE_PATH);
        LexiconDatabase.getInstance().insertEntry(Set.of("car"), "Cars are vehicles");
        LexiconDatabase.getInstance().insertEntry(Set.of("vehicle"),"vehicles are very fast");
        LexiconDatabase.getInstance().insertEntry(Set.of("bus"),"Buses are often late");
        LexiconDatabase.getInstance().close();

        return new String[]{TEST_DATABASE_PATH, TEST_OTHER_DATABASE_PATH};
    }
}
