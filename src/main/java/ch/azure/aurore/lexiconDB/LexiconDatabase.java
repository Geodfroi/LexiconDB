package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.IO.API.Disk;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LexiconDatabase {

    //region constants

    private static final String ENTRIES_TABLE_NAME = "Entries";
    private static final String LINKS_TABLE_NAME = "Links";

    private static final String ENTRIES_CONTENT_FIELD = "Content";
    private static final String ENTRIES_LABEL_FIELD = "Labels";
    private static final String ENTRIES_IMAGE_FIELD = "Image";

    private static final String LINKS_MIN_FIELD = "MinID";
    private static final String LINKS_MAX_FIELD = "MaxID";

    private static final String CREATE_ENTRIES_TABLE = "CREATE TABLE IF NOT EXISTS " + ENTRIES_TABLE_NAME + " (_id INTEGER PRIMARY KEY, " + ENTRIES_CONTENT_FIELD + " TEXT, " + ENTRIES_LABEL_FIELD + " TEXT, " + ENTRIES_IMAGE_FIELD + " BLOB)";
    private static final String CREATE_LINKS_TABLE = "CREATE TABLE IF NOT EXISTS " + LINKS_TABLE_NAME + " (" + LINKS_MIN_FIELD + " INTEGER, " + LINKS_MAX_FIELD + " INTEGER)";
    private static final String INSERT_ENTRY_STATEMENT = "INSERT INTO " + ENTRIES_TABLE_NAME + " (" + ENTRIES_CONTENT_FIELD + ", " + ENTRIES_LABEL_FIELD + ", " + ENTRIES_IMAGE_FIELD + ") VALUES (?,?,?)";
    private static final String INSERT_LINK_STATEMENT = "INSERT INTO " + LINKS_TABLE_NAME + " (" + LINKS_MIN_FIELD + ", " + LINKS_MAX_FIELD + ") VALUES (?,?)";
    private static final String QUERY_ENTRIES_STATEMENT = "SELECT * FROM " + ENTRIES_TABLE_NAME;
    private static final String QUERY_LINKS_STATEMENT = "SELECT * FROM " + LINKS_TABLE_NAME + " WHERE " + LINKS_MIN_FIELD + " = ? OR " + LINKS_MAX_FIELD + " = ?";
    private static final String REMOVE_ENTRY_STATEMENT = "DELETE FROM " + ENTRIES_TABLE_NAME + " WHERE _id = ?";
    private static final String REMOVE_LINK_STATEMENT = "DELETE FROM " + LINKS_TABLE_NAME + " WHERE " + LINKS_MIN_FIELD + " = ? AND " + LINKS_MAX_FIELD + " =?";
    private static final String UPDATE_ENTRY_STATEMENT = "UPDATE " + ENTRIES_TABLE_NAME + " SET " + ENTRIES_CONTENT_FIELD + " = ?, " + ENTRIES_LABEL_FIELD + " = ?, " + ENTRIES_IMAGE_FIELD + " = ? WHERE _id = ?";

    //endregion

    public static void main(String[] args) {
        String pathStr =  "C:\\Users\\auror\\OneDrive\\Apps\\Lexicon\\TestLexicon.SQLite";
        Disk.removeFile(pathStr);

        LexiconDatabase.getInstance().open(pathStr);
        int animalID = LexiconDatabase.getInstance().createEntry("Wolves, elephants and black cats are animals", "animal", null);
        int wolfId =   LexiconDatabase.getInstance().createEntry("The wolf is an animal", "wolf", null);
        int elephantId = LexiconDatabase.getInstance().createEntry("Elephants are very big", "elephants", null);
        int catID = LexiconDatabase.getInstance().createEntry("Cats are sly animals", "blackCat", null);
        int lionsID = LexiconDatabase.getInstance().createEntry("Lions are big felines", "whiteLion", null);
        int felineID = LexiconDatabase.getInstance().createEntry("Felines are hunting animals", "feline",null);

        LexiconDatabase.getInstance().insertLink(new EntriesLink(catID, felineID));
        LexiconDatabase.getInstance().insertLink(new EntriesLink(lionsID, felineID));
        LexiconDatabase.getInstance().insertLink(new EntriesLink(animalID, elephantId));
        LexiconDatabase.getInstance().insertLink(new EntriesLink(animalID, wolfId));

        LexiconDatabase.getInstance().close();

        pathStr = "C:\\Users\\auror\\OneDrive\\Apps\\Lexicon\\OtherLexicon.SQLite";
        Disk.removeFile(pathStr);

        LexiconDatabase.getInstance().open(pathStr);
        LexiconDatabase.getInstance().createEntry("Cars are vehicles", "car", null);
        LexiconDatabase.getInstance().createEntry("vehicles are very fast", "vehicle",null);
        LexiconDatabase.getInstance().createEntry("Buses are often late", "bus",null);
        LexiconDatabase.getInstance().close();

        Disk.openFile("C:\\Users\\auror\\OneDrive\\Apps\\Lexicon");
    }

    public static LexiconDatabase getInstance() {
        return instance;
    }

    static LexiconDatabase instance = new LexiconDatabase();

    private PreparedStatement insertContentStatement;
    private PreparedStatement insertLinkStatement;
    private PreparedStatement queryAllStatement;
    private PreparedStatement removeEntryStatement;
    private PreparedStatement removeLinkStatement;
    private PreparedStatement updateEntryStatement;
    private PreparedStatement queryLinksStatement;

    private Connection conn;

    public boolean open(String databasePath) {

        if (conn != null)
            close();

        Disk.backupFile(databasePath);

        String connectStr = "jdbc:sqlite:" + databasePath;
        try {
            conn = DriverManager.getConnection(connectStr);

            Statement statement = conn.createStatement();
            statement.execute(CREATE_ENTRIES_TABLE);
            statement.execute(CREATE_LINKS_TABLE);
            statement.close();

            insertContentStatement = conn.prepareStatement(INSERT_ENTRY_STATEMENT);
            insertLinkStatement = conn.prepareStatement(INSERT_LINK_STATEMENT);
            queryAllStatement = conn.prepareStatement(QUERY_ENTRIES_STATEMENT);
            queryLinksStatement = conn.prepareStatement(QUERY_LINKS_STATEMENT);
            removeEntryStatement = conn.prepareStatement(REMOVE_ENTRY_STATEMENT);
            removeLinkStatement = conn.prepareStatement(REMOVE_LINK_STATEMENT);
            updateEntryStatement = conn.prepareStatement(UPDATE_ENTRY_STATEMENT);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Can't open database");
            return false;
        }
    }

    public void close() {
        try {
            if (insertContentStatement != null)
                insertContentStatement.close();

            if (insertLinkStatement != null)
                insertLinkStatement.close();

            if (queryAllStatement != null)
                queryAllStatement.close();

            if (queryLinksStatement != null)
                queryLinksStatement.close();

            if (removeEntryStatement != null)
                removeEntryStatement.close();

            if (removeLinkStatement != null)
                removeLinkStatement.close();

            if (updateEntryStatement != null)
                updateEntryStatement.close();

            if (conn != null)
                conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int createEntry(String contentStr, String labels, byte[] imageArray) {
        ResultSet result = null;
        try {
            insertContentStatement.setString(1, contentStr);
            insertContentStatement.setString(2, labels);
            insertContentStatement.setBytes(3, imageArray);

            int updateCount = insertContentStatement.executeUpdate();
            if (updateCount != 1)
                throw new SQLException("New content insert failed");

            result = insertContentStatement.getGeneratedKeys();
            if (result.next()) {
                return result.getInt(1);
            }
            throw new SQLException("New content insert failed: can't get id");

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertLink(EntriesLink link) {

        try {
            insertLinkStatement.setInt(1, link.getMinID());
            insertLinkStatement.setInt(2, link.getMaxID());

            int updateCount = insertLinkStatement.executeUpdate();
            if (updateCount != 1)
                throw new SQLException("New content insert failed");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<EntryContent> queryEntries() {
        ResultSet result = null;
        List<EntryContent> list = new ArrayList<>();
        try {
            result = queryAllStatement.executeQuery();
            while (result.next()) {

                int id = result.getInt(1);
                String content = result.getString(2);
                String labels = result.getString(3);
                var array = result.getBytes(4);
                EntryContent item = new EntryContent(id, content, labels, array);
                list.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public Set<EntriesLink> queryLinks(int entryID) {
        ResultSet result = null;
        Set<EntriesLink> links = new HashSet<>();
        try {
            queryLinksStatement.setInt(1, entryID);
            queryLinksStatement.setInt(2, entryID);
            result = queryLinksStatement.executeQuery();
            while (result.next()){
                int min = result.getInt(1);
                int max = result.getInt(2);
                links.add(new EntriesLink(min, max));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return links;
    }

    public boolean removeEntry(EntryContent item) {
        try {
            removeEntryStatement.setInt(1, item.getId());
            if (removeEntryStatement.executeUpdate() == 1) {

                Set<EntriesLink> links = queryLinks(item.getId());
                links.forEach(this::removeLink);

                return true;
            }

            throw new SQLException("Delete operation failed");

        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeLink(EntriesLink link) {

        try {
            removeLinkStatement.setInt(1, link.getMinID());
            removeLinkStatement.setInt(2, link.getMaxID());

            if (removeLinkStatement.executeUpdate() == 1)
                return true;

            throw new SQLException("Delete operation failed");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean updateEntry(EntryContent entry){

        try {
            updateEntryStatement.setString(1, entry.getContent());
            updateEntryStatement.setString(2, entry.getLabels());
            updateEntryStatement.setBytes(3, entry.getImage());
            updateEntryStatement.setInt(4, entry.getId());

            int updateCount = updateEntryStatement.executeUpdate();
            if (updateCount != 1)
                throw new SQLException("Update failed");

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}