package main.java.Service;

import main.java.Data.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;

public class DatabaseConnection {

    private final Connection conn;
    private final String path;

    public DatabaseConnection() throws Exception {
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        String url = "jdbc:sqlite:" + path + "/Resources/Data/bibles.db";
        conn = DriverManager.getConnection(url);
    }

    public void close() throws Exception {
        conn.close();
    }

    public int setUpDatabase() throws Exception {


        Statement statement = conn.createStatement();

        // Setup Tables
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "/Resources/Data/Tables.sql"));
        String line;
        StringBuilder query = new StringBuilder();
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.trim().endsWith(";")) {
                query.append(line);
                statement.execute(query.toString());
                query = new StringBuilder();
            } else {
                query.append(line);
            }
        }

        bufferedReader.close();

        // Setup Data
        bufferedReader = new BufferedReader(new FileReader(path + "/Resources/Data/preloadData.sql"));
        while ( (line = bufferedReader.readLine()) != null) {
            if (line.trim().endsWith(";")) {
                query.append(line);
                statement.execute(query.toString());
                query = new StringBuilder();
            } else {
                query.append(line);
            }
        }

        bufferedReader.close();

        return 0;
    }

    public int insertBible(Long bibleId, String bibleName, String bibleShortName,
                           String yearOfPublication, String language, String copyrightInfo) throws Exception {

        Statement statement = conn.createStatement();
        String sql = "INSERT INTO Bible (bibleId, bibleName, bibleShortName, yearOfPublication, language, copyrightInfo) VALUES (" +
            bibleId + ", '" + bibleName + "', '" + bibleShortName + "', '" + yearOfPublication + "', '" + language +
            "', '" + copyrightInfo + "');";

        statement.execute(sql);

        return 0;
    }

    public int insertVerse(Long verseId, String verseNum, String text) throws Exception {
        String sql = "INSERT INTO Verses (verseId, verseNumber, verseText) VALUES (" + verseId + ", '" + verseNum +
                "','" + text + "');";
        Statement statement = conn.createStatement();
        statement.execute(sql);

        return 0;
    }

    public int insertBibleLink(Long bibleLinkId, Long bibleId, Long bookId, Long chapterId, Long verseId) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "INSERT INTO BibleLink (bibleLinkId, bibleId, bookId, chapterId, verseId) VALUES (" +
                bibleLinkId + ", " + bibleId + ", " + bookId + ", " + chapterId + ", " + verseId + ");";

        statement.execute(sql);

        return 0;
    }

    public Indexes getIndex() throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM Indexes;";

        ResultSet resultSet = statement.executeQuery(sql);
        Indexes indexes = new Indexes();

        indexes.setIndexId(resultSet.getLong("indexId"));
        indexes.setBibleId(resultSet.getLong("bibleId"));
        indexes.setBookId(resultSet.getLong("bookId"));
        indexes.setChapterId(resultSet.getLong("chapterId"));
        indexes.setVerseId(resultSet.getLong("verseId"));
        indexes.setBibleLinkId(resultSet.getLong("bibleLinkId"));
        indexes.setNotesId(resultSet.getLong("noteId"));

        return indexes;
    }

    public void writeIndexes(Indexes indexes) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "UPDATE Indexes SET bibleId = " + indexes.getBibleId() +
                ", bookId = " + indexes.getBookId() +
                ", chapterId = " + indexes.getChapterId() +
                ", verseId = " + indexes.getVerseId() +
                ", bibleLinkId = " + indexes.getBibleLinkId() +
                ", noteId = " + indexes.getNotesId() +
                " Where indexId = " + indexes.getIndexId() + ";";

        statement.execute(sql);
    }

    public Vector<Bible> getBibles() throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM Bible;";
        Vector<Bible> bibles = new Vector<>();

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            Bible bible = new Bible();
            bible.setBibleId(resultSet.getLong("bibleId"));
            bible.setBibleName(resultSet.getString("bibleName"));
            bible.setBibleShortName(resultSet.getString("bibleShortName"));
            bible.setYearOfPublication(resultSet.getString("yearOfPublication"));
            bible.setLang(resultSet.getString("language"));
            bible.setCopyrightInfo(resultSet.getString("copyrightInfo"));
            bibles.add(bible);
        }

        return bibles;
    }

    public Vector<Book> getBooks() throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * from Book";
        Vector<Book> books = new Vector<>();

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            Book book = new Book();
            book.setBookNumber(resultSet.getLong("bookId"));
            book.setBookTitle(resultSet.getString("bookTitle"));
            book.setBookAbbrev(resultSet.getString("bookAbbrev"));
            books.add(book);
        }

        return books;
    }

    public Vector<Chapter> getChapters(Long bibleId, Long bookId) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT DISTINCT * FROM Chapters AS c LEFT JOIN BibleLink AS bl on c.chapterId = bl.chapterId WHERE bl.bookId = " +
                bookId + " AND bl.bibleId = " + bibleId + " GROUP BY c.chapterId;";

        Vector<Chapter> chapters = new Vector<>();

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            Chapter chapter = new Chapter();
            chapter.setChapterId(resultSet.getLong("chapterId"));
            chapter.setChapter(resultSet.getString("chapter"));
            chapter.setDisplayName(resultSet.getString("displayName"));
            chapters.add(chapter);
        }

        return chapters;
    }

    public Vector<Chapter> getChaptersByChapterId(Vector<Long> chapterId) throws Exception {
        Vector<Chapter> chapters = new Vector<>();
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM Chapters WHERE chapterId in (";
        for (int i = 0; i < chapterId.size(); i++) {
            if (i + 1 == chapterId.size()) {
                sql = sql.concat(chapterId.get(i).toString());
            } else {
                sql = sql.concat(chapterId.get(i).toString() + ", ");
            }
        }
        sql = sql.concat(");");

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            Chapter chapter = new Chapter();

            chapter.setChapterId(resultSet.getLong("chapterId"));
            chapter.setChapter(resultSet.getString("chapter"));
            chapter.setDisplayName(resultSet.getString("displayName"));

            chapters.add(chapter);
        }

        return chapters;
    }

    public Long getVerseCount(Long bibleId, Long bookId, Long chapterId) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT COUNT(verseId) FROM BibleLink WHERE bibleId = " + bibleId +
                " AND bookId = " + bookId + " AND chapterId = " + chapterId + ";";

        ResultSet resultSet = statement.executeQuery(sql);
        Long count = 0L;

        while (resultSet.next()) {
            count = resultSet.getLong(1);
        }
        return count;
    }

    public Vector<BibleLink> getBibleLink(Long bibleId, Long bookId, Long chapterId) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM BibleLink AS bl LEFT JOIN Bible AS b ON bl.bibleId = b.bibleId LEFT JOIN Book AS bo " +
                "ON bl.bookId = bo.bookId LEFT JOIN Chapters AS c ON bl.chapterId = c.chapterId LEFT JOIN verses AS v ON " +
                "bl.verseId = v.verseId WHERE b.bibleId = " + bibleId + " AND bo.bookId = " + bookId + " AND c.chapterId = " +
                chapterId + ";";

        Vector<BibleLink> bibleLinks = new Vector<>();

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            BibleLink bibleLink = new BibleLink();
            Bible bible = new Bible();
            Book book = new Book();
            Chapter chapter = new Chapter();
            Verse verse = new Verse();

            bible.setBibleId(resultSet.getLong("bibleId"));
            bible.setBibleName(resultSet.getString("bibleName"));
            bible.setBibleShortName(resultSet.getString("bibleShortName"));
            bible.setYearOfPublication(resultSet.getString("yearOfPublication"));
            bible.setCopyrightInfo(resultSet.getString("copyrightInfo"));

            book.setBookNumber(resultSet.getLong("bookId"));
            book.setBookTitle(resultSet.getString("bookTitle"));
            book.setBookAbbrev(resultSet.getString("bookAbbrev"));

            chapter.setChapterId(resultSet.getLong("chapterId"));
            chapter.setChapter(resultSet.getString("chapter"));
            chapter.setDisplayName(resultSet.getString("displayName"));

            verse.setVerseId(resultSet.getLong("verseId"));
            verse.setVerseNumber(resultSet.getString("verseNumber"));
            verse.setVerseText(resultSet.getString("verseText"));

            bibleLink.setBible(bible);
            bibleLink.setBook(book);
            bibleLink.setChapter(chapter);
            bibleLink.setVerse(verse);

            bibleLinks.add(bibleLink);
        }

        return bibleLinks;
    }

    public Vector<Notes> getNotes(Long bibleId, Long bookId, Long chapterId) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM Notes where bibleId = " + bibleId + " AND bookId = " + bookId +
                " AND chapterId = " + chapterId + " ORDER BY verseId;";

        Vector<Notes> notes = new Vector<>();

        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            Notes note = new Notes();

            note.setNoteId(resultSet.getLong("noteId"));
            note.setBibleId(resultSet.getLong("bibleId"));
            note.setBookId(resultSet.getLong("bookId"));
            note.setChapterId(resultSet.getLong("chapterId"));
            note.setVerseId(resultSet.getLong("verseId"));
            note.setNoteText(resultSet.getString("noteText"));

            notes.add(note);
        }

        return notes;
    }

    public int writeToNotes(Notes notes) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "INSERT INTO Notes (noteId, bibleId, bookId, chapterId, verseId, noteText)" +
                " VALUES (" + notes.getNoteId() + ", " + notes.getBibleId() + ", " + notes.getBookId() + ", " +
                notes.getChapterId() + ", " + notes.getVerseId() + ", '" + notes.getNoteText() + "');";

        statement.execute(sql);

        return 0;
    }

    public int updateNotes(Notes notes) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "UPDATE Notes SET bibleId = " + notes.getBibleId() + ", bookId = " + notes.getBookId() +
                ", chapterId = " + notes.getChapterId() + ", verseId = " + notes.getVerseId() +
                ", noteText = '" + notes.getNoteText() + "' WHERE NoteId = " + notes.getNoteId() + ";";

        statement.execute(sql);

        return 0;
    }

    public void deleteNotes(Long noteId) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "DELETE FROM Notes WHERE noteId = " + noteId + ";";

        statement.execute(sql);
    }
}
