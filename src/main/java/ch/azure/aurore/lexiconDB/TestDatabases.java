package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.IO.API.Disk;
import ch.azure.aurore.sqlite.wrapper.SQLite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class TestDatabases {

    private static final String TEST_FOLDER = "TestFolder";
    public static final String TEST_DATABASE_PATH = TEST_FOLDER + File.separator + "TestLexicon.SQLite";
    public static final String TEST_OTHER_DATABASE_PATH = TEST_FOLDER + File.separator + "OtherTestLexicon.SQLite";
    private static final String LION_IMAGE_PATH = "lion.png";

    public static void main(String[] args) {
        createDummyDB(true);
    }

    static void createDummyDB(boolean openFolder) {
        try {
            Files.createDirectories(Path.of(TEST_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (openFolder)
            Disk.openFile(TEST_FOLDER);

        Disk.removeFile(TEST_DATABASE_PATH);
        SQLite sqLite = SQLite.connect(TEST_DATABASE_PATH);

        byte[] bytes;
        try {
            bytes = Objects.requireNonNull(TestDatabases.class.getClassLoader().
                    getResourceAsStream(LION_IMAGE_PATH)).
                    readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("can't read lion image");
        }

        EntryContent animal = new EntryContent(0, Set.of("animal"), "Wolves, elephants and black cats are animals");
        EntryContent wolf = new EntryContent(0, Set.of("wolf"), "The wolf is an animal");
        EntryContent elephant = new EntryContent(0, Set.of("elephant"), "Elephants are very big");
        EntryContent cat = new EntryContent(0, Set.of("blackCat"), "Cats are sly animals");
        EntryContent lion = new EntryContent(0, Set.of("whiteLion"), "Lions are big felines");
        lion.setImage(bytes);
        EntryContent feline = new EntryContent(0, Set.of("feline"), "Felines are hunting animals");

        EntryLink.create(animal, elephant);
        EntryLink.create(animal, wolf);
        EntryLink.create(animal, feline);
        EntryLink.create(feline, cat);
        EntryLink.create(feline, lion);

        sqLite.updateItems(animal, wolf, elephant, cat, lion, feline);
        sqLite.close();

        Disk.removeFile(TEST_OTHER_DATABASE_PATH);

        sqLite = SQLite.connect(TEST_OTHER_DATABASE_PATH);

        EntryContent car = new EntryContent(0,Set.of("car"), "Cars are vehicles");
        EntryContent vehicle = new EntryContent(0, Set.of("vehicle"), "vehicles are very fast");
        EntryContent bus = new EntryContent(0, Set.of("bus"), "Buses are often late");

        sqLite.updateItems(car, vehicle, bus);
        sqLite.close();
    }
}
