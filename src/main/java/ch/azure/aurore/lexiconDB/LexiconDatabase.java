package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.IO.API.Disk;

import java.sql.*;
import java.util.*;

public class LexiconDatabase {


    public static LexiconDatabase getInstance() {
        return instance;
    }

    static LexiconDatabase instance = new LexiconDatabase();

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

            openEntryStatements();
            openLinkStatements();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Can't open database");
            return false;
        }
    }

    public void close() {
        try {
            closeEntries();
            closeLinks();

            if (conn != null)
                conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //region entry statement

    private static final String ENTRIES_TABLE_NAME = "Entries";
    private static final String ENTRIES_CONTENT_FIELD = "Content";
    private static final String ENTRIES_LABELS_FIELD = "Labels";
    private static final String ENTRIES_IMAGE_FIELD = "Image";

    private static final String CREATE_ENTRIES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + ENTRIES_TABLE_NAME + " (_id INTEGER PRIMARY KEY, "
            + ENTRIES_CONTENT_FIELD + " TEXT, "
            + ENTRIES_LABELS_FIELD + " TEXT, "
            + ENTRIES_IMAGE_FIELD + " BLOB)";

    private static final String INSERT_ENTRY_STATEMENT = "INSERT INTO "
            + ENTRIES_TABLE_NAME + " ("
            + ENTRIES_CONTENT_FIELD + ", "
            + ENTRIES_LABELS_FIELD + ", "
            + ENTRIES_IMAGE_FIELD + ") VALUES (?,?,?)";

    private static final String QUERY_ENTRY_STATEMENT = "SELECT * FROM " + ENTRIES_TABLE_NAME + " WHERE _id = ?";
    private static final String QUERY_ENTRIES_STATEMENT = "SELECT * FROM " + ENTRIES_TABLE_NAME;
    private static final String REMOVE_ENTRY_STATEMENT = "DELETE FROM " + ENTRIES_TABLE_NAME + " WHERE _id = ?";
    private static final String UPDATE_ENTRY_STATEMENT = "UPDATE " + ENTRIES_TABLE_NAME
            + " SET "
            + ENTRIES_CONTENT_FIELD + " = ?, "
            + ENTRIES_LABELS_FIELD + " = ?, "
            + ENTRIES_IMAGE_FIELD + " = ? WHERE _id = ?";

    private PreparedStatement insertContentStatement;
    private PreparedStatement queryStatement;
    private PreparedStatement queryAllStatement;
    private PreparedStatement removeEntryStatement;
    private PreparedStatement updateEntryStatement;

    private void closeEntries() throws SQLException {
        if (insertContentStatement != null)
            insertContentStatement.close();

        if (queryStatement != null)
            queryStatement.close();

        if (queryAllStatement != null)
            queryAllStatement.close();

        if (removeEntryStatement != null)
            removeEntryStatement.close();

        if (updateEntryStatement != null)
            updateEntryStatement.close();

    }

    private void openEntryStatements() throws SQLException {
        insertContentStatement = conn.prepareStatement(INSERT_ENTRY_STATEMENT);
        queryStatement = conn.prepareStatement(QUERY_ENTRY_STATEMENT);
        queryAllStatement = conn.prepareStatement(QUERY_ENTRIES_STATEMENT);
        removeEntryStatement = conn.prepareStatement(REMOVE_ENTRY_STATEMENT);
        updateEntryStatement = conn.prepareStatement(UPDATE_ENTRY_STATEMENT);
    }

    public Optional<EntryContent> insertEntry(String contentStr, Set<String> labels) {
        ResultSet result = null;
        try {

            String labelStr = EntryContent.toLabelStr(labels);
            insertContentStatement.setString(1, contentStr);
            insertContentStatement.setString(2, labelStr);
            insertContentStatement.setBytes(3, null);

            int updateCount = insertContentStatement.executeUpdate();
            if (updateCount != 1)
                throw new SQLException("New content insert failed");

            result = insertContentStatement.getGeneratedKeys();
            if (result.next()) {
                int id = result.getInt(1);
                return Optional.of(new EntryContent(id, labels, contentStr));
            }
            throw new SQLException("New content insert failed: can't get id");

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
        return Optional.empty();
    }

    public Optional<EntryContent> queryEntry(int id){
        ResultSet result = null;
        try {
            queryStatement.setInt(1, id);
            result = queryStatement.executeQuery();
            if (result.next()) {

                String content = result.getString(2);
                String labelStr = result.getString(3);
                byte[] array = result.getBytes(4);

                Set<String> labels = EntryContent.toLabelSet(labelStr);
                EntryContent item = new EntryContent(id, labels, content, array);
                return Optional.of(item);
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
        return Optional.empty();
    }

    public List<EntryContent> queryEntries() {
        ResultSet result = null;
        List<EntryContent> list = new ArrayList<>();
        try {
            result = queryAllStatement.executeQuery();
            while (result.next()) {

                int id = result.getInt(1);
                String content = result.getString(2);
                String labelStr = result.getString(3);
                byte[] array = result.getBytes(4);

                Set<String> labels = EntryContent.toLabelSet(labelStr);
                EntryContent item = new EntryContent(id,labels, content, array);
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

    public boolean removeEntry(EntryContent item) {
        try {
            removeEntryStatement.setInt(1, item.getId());
            if (removeEntryStatement.executeUpdate() == 1){
                removeLinks(item.getId());
                return true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateEntry(EntryContent entry){

        String labelStr = EntryContent.toLabelStr(entry.getLabels());

        try {
            updateEntryStatement.setString(1, entry.getContent());
            updateEntryStatement.setString(2, labelStr);
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
    //endregion

    //region links statement

    private static final String LINKS_TABLE_NAME = "Links";
    private static final String LINKS_MIN_FIELD = "Id0";
    private static final String LINKS_MAX_FIELD = "Id1";

    private static final String CREATE_LINKS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + LINKS_TABLE_NAME + " ("
            + LINKS_MIN_FIELD + " INTEGER, "
            + LINKS_MAX_FIELD + " INTEGER)";

    private static final String INSERT_LINK_STATEMENT = "INSERT INTO "
            + LINKS_TABLE_NAME + " ("
            + LINKS_MIN_FIELD + ", "
            + LINKS_MAX_FIELD + ") VALUES (?,?)";

    private static final String QUERY_LINKS_STATEMENT = "SELECT * FROM "
            + LINKS_TABLE_NAME + " WHERE "
            + LINKS_MIN_FIELD + " = ? OR "
            + LINKS_MAX_FIELD + " = ?";

    private static final String REMOVE_LINK_STATEMENT = "DELETE FROM "
            + LINKS_TABLE_NAME + " WHERE "
            + LINKS_MIN_FIELD + " = ? AND "
            + LINKS_MAX_FIELD + " = ?";

    private PreparedStatement insertLinkStatement;
    private PreparedStatement queryLinksStatement;
    private PreparedStatement removeLinkStatement;

    private void closeLinks() throws SQLException {

        if (insertLinkStatement != null)
            insertLinkStatement.close();

        if (queryLinksStatement != null)
            queryLinksStatement.close();

        if (removeLinkStatement != null)
            removeLinkStatement.close();
    }

    private void openLinkStatements() throws SQLException {
        insertLinkStatement = conn.prepareStatement(INSERT_LINK_STATEMENT);
        queryLinksStatement = conn.prepareStatement(QUERY_LINKS_STATEMENT);
        removeLinkStatement = conn.prepareStatement(REMOVE_LINK_STATEMENT);
    }

    public boolean insertLink(int id0, int id1) {

        int min = Math.min(id0, id1);
        int max = Math.max(id0, id1);

        try {
            insertLinkStatement.setInt(1, min);
            insertLinkStatement.setInt(2, max);

            int updateCount = insertLinkStatement.executeUpdate();
            return updateCount == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("insert link has failed");
        return false;
    }

    public Set<Integer> queryEntryLinks(int entryId) {
        ResultSet result = null;
        Set<Integer> otherIds = new HashSet<>();
        try {
            queryLinksStatement.setInt(1, entryId);
            queryLinksStatement.setInt(2, entryId);
            result = queryLinksStatement.executeQuery();
            while (result.next()){
                int min = result.getInt(1);
                int max = result.getInt(2);

                int otherId = min == entryId ? max : min;
                otherIds.add(otherId);
            }
            return otherIds;

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
    return null;
//        return otherIds.stream().
//                map(this::queryEntry).
//                filter(Optional::isPresent).
//                map(otherEntry -> new EntriesLink(entry, otherEntry.get())).
//                collect(Collectors.toSet());
    }

    public boolean removeLink(int id1, int id0) {

        int min = Math.min(id0, id1);
        int max = Math.max(id0, id1);

        try {
            removeLinkStatement.setInt(1, min);
            removeLinkStatement.setInt(2, max);

            if (removeLinkStatement.executeUpdate() == 1)
                return true;

            throw new SQLException("Delete operation failed");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void removeLinks(int entryId){
        Set<Integer> set = queryEntryLinks(entryId);
        if (set != null){
            set.forEach(i -> removeLink(entryId, i));
        }
    }

    //endregion
}

//__________________----------------------------
//public class LexiconDatabase {
//
//    //region constants
//
//    private static final String ENTRIES_TABLE_NAME = "Entries";
//    //  private static final String LINKS_TABLE_NAME = "Links";
//
//    private static final String ENTRIES_CONTENT_FIELD = "Content";
//    private static final String ENTRIES_LABELS_FIELD = "Labels";
//    private static final String ENTRIES_IMAGE_FIELD = "Image";
//    private static final String ENTRIES_LINKS_FIELD = "Links";
//
//    private static final String CREATE_ENTRIES_TABLE = "CREATE TABLE IF NOT EXISTS "
//            + ENTRIES_TABLE_NAME + " (_id INTEGER PRIMARY KEY, "
//            + ENTRIES_CONTENT_FIELD + " TEXT, "
//            + ENTRIES_LABELS_FIELD + " TEXT, "
//            + ENTRIES_IMAGE_FIELD + " BLOB, "
//            + ENTRIES_LINKS_FIELD + " Text)";
//
//    // private static final String CREATE_LINKS_TABLE = "CREATE TABLE IF NOT EXISTS " + LINKS_TABLE_NAME + " (" + LINKS_MIN_FIELD + " INTEGER, " + LINKS_MAX_FIELD + " INTEGER)";
//    private static final String INSERT_ENTRY_STATEMENT = "INSERT INTO "
//            + ENTRIES_TABLE_NAME + " ("
//            + ENTRIES_CONTENT_FIELD + ", "
//            + ENTRIES_LABELS_FIELD + ", "
//            + ENTRIES_IMAGE_FIELD + ", "
//            + ENTRIES_LINKS_FIELD + ") VALUES (?,?,?,?)";
//
//    //private static final String INSERT_LINK_STATEMENT = "INSERT INTO " + LINKS_TABLE_NAME + " (" + LINKS_MIN_FIELD + ", " + LINKS_MAX_FIELD + ") VALUES (?,?)";
//    private static final String QUERY_ENTRY_STATEMENT = "SELECT * FROM " + ENTRIES_TABLE_NAME + " WHERE _id = ?";
//    private static final String QUERY_ENTRIES_STATEMENT = "SELECT * FROM " + ENTRIES_TABLE_NAME;
//    //private static final String QUERY_LINKS_STATEMENT = "SELECT * FROM " + LINKS_TABLE_NAME + " WHERE " + LINKS_MIN_FIELD + " = ? OR " + LINKS_MAX_FIELD + " = ?";
//    private static final String REMOVE_ENTRY_STATEMENT = "DELETE FROM " + ENTRIES_TABLE_NAME + " WHERE _id = ?";
//    //   private static final String REMOVE_LINK_STATEMENT = "DELETE FROM " + LINKS_TABLE_NAME + " WHERE " + LINKS_MIN_FIELD + " = ? AND " + LINKS_MAX_FIELD + " =?";
//    private static final String UPDATE_ENTRY_STATEMENT = "UPDATE " + ENTRIES_TABLE_NAME
//            + " SET "
//            + ENTRIES_CONTENT_FIELD + " = ?, "
//            + ENTRIES_LABELS_FIELD + " = ?, "
//            + ENTRIES_IMAGE_FIELD + " = ?, "
//            + ENTRIES_LINKS_FIELD + " = ? WHERE _id = ?";
//
//
//    //endregion
//
//    public static LexiconDatabase getInstance() {
//        return instance;
//    }
//
//    static LexiconDatabase instance = new LexiconDatabase();
//
//    private PreparedStatement insertContentStatement;
//    private  PreparedStatement queryStatement;
//    private PreparedStatement queryAllStatement;
//    private PreparedStatement removeEntryStatement;
//    private PreparedStatement updateEntryStatement;
//
//    private Connection conn;
//
//    public boolean open(String databasePath) {
//
//        if (conn != null)
//            close();
//
//        Disk.backupFile(databasePath);
//
//        String connectStr = "jdbc:sqlite:" + databasePath;
//        try {
//            conn = DriverManager.getConnection(connectStr);
//
//            Statement statement = conn.createStatement();
//            statement.execute(CREATE_ENTRIES_TABLE);
//            statement.close();
//
//            insertContentStatement = conn.prepareStatement(INSERT_ENTRY_STATEMENT);
//            queryStatement = conn.prepareStatement(QUERY_ENTRY_STATEMENT);
//            queryAllStatement = conn.prepareStatement(QUERY_ENTRIES_STATEMENT);
//            removeEntryStatement = conn.prepareStatement(REMOVE_ENTRY_STATEMENT);
//            updateEntryStatement = conn.prepareStatement(UPDATE_ENTRY_STATEMENT);
//            return true;
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("Can't open database");
//            return false;
//        }
//    }
//
//    public void close() {
//        try {
//            if (insertContentStatement != null)
//                insertContentStatement.close();
//
//            if (queryStatement != null)
//                queryStatement.close();
//
//            if (queryAllStatement != null)
//                queryAllStatement.close();
//
//            if (removeEntryStatement != null)
//                removeEntryStatement.close();
//
//            if (updateEntryStatement != null)
//                updateEntryStatement.close();
//
//            if (conn != null)
//                conn.close();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public Optional<EntryContent> insertEntry(String contentStr, Set<String> labels) {
//        ResultSet result = null;
//        try {
//
//            String labelStr = EntryContent.toLabelStr(labels);
//            insertContentStatement.setString(1, contentStr);
//            insertContentStatement.setString(2, labelStr);
//            insertContentStatement.setBytes(3, null);
//            insertContentStatement.setString(4, null);
//
//            int updateCount = insertContentStatement.executeUpdate();
//            if (updateCount != 1)
//                throw new SQLException("New content insert failed");
//
//            result = insertContentStatement.getGeneratedKeys();
//            if (result.next()) {
//                int id = result.getInt(1);
//                return Optional.of(new EntryContent(id, labels, contentStr));
//            }
//            throw new SQLException("New content insert failed: can't get id");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (result != null) {
//                    result.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return Optional.empty();
//    }
//
//    public Optional<EntryContent> queryEntry(int id){
//        ResultSet result = null;
//        try {
//            queryStatement.setInt(1, id);
//            result = queryStatement.executeQuery();
//            if (result.next()) {
//
//                String content = result.getString(2);
//                String labelStr = result.getString(3);
//                byte[] array = result.getBytes(4);
//                String linkStr = result.getString(5);
//
//                Set<String> labels = EntryContent.toLabelSet(labelStr);
//                Set<Integer> links = EntryContent.toLinkSet(linkStr);
//                EntryContent item = new EntryContent(id, labels, content, array, links );
//                return Optional.of(item);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (result != null) {
//                    result.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return Optional.empty();
//    }
//
//    public List<EntryContent> queryEntries() {
//        ResultSet result = null;
//        List<EntryContent> list = new ArrayList<>();
//        try {
//            result = queryAllStatement.executeQuery();
//            while (result.next()) {
//
//                int id = result.getInt(1);
//                String content = result.getString(2);
//                String labelStr = result.getString(3);
//                byte[] array = result.getBytes(4);
//                String linkStr = result.getString(5);
//
//                Set<String> labels = EntryContent.toLabelSet(labelStr);
//                Set<Integer> links = EntryContent.toLinkSet(linkStr);
//
//                EntryContent item = new EntryContent(id,labels, content, array, links);
//                list.add(item);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (result != null) {
//                    result.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return list;
//    }
//
//    public boolean removeEntry(EntryContent item) {
//        try {
//            removeEntryStatement.setInt(1, item.getId());
//            return removeEntryStatement.executeUpdate() == 1;
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public boolean updateEntry(EntryContent entry){
//
//        String labelStr = EntryContent.toLabelStr(entry.getLabels());
//        String linkStr = EntryContent.toLinksStr(entry.getLinks());
//
//        try {
//            updateEntryStatement.setString(1, entry.getContent());
//            updateEntryStatement.setString(2, labelStr);
//            updateEntryStatement.setBytes(3, entry.getImage());
//            updateEntryStatement.setString(4, linkStr);
//            updateEntryStatement.setInt(5, entry.getId());
//
//            int updateCount = updateEntryStatement.executeUpdate();
//            if (updateCount != 1)
//                throw new SQLException("Update failed");
//
//            return true;
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
