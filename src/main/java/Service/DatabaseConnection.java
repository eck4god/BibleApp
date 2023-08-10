package main.java.Service;

import main.java.Data.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class DatabaseConnection {

    Connection conn = null;
    String url = "jdbc:sqlite:./Resources/test.db";
    public DatabaseConnection() throws Exception {
        conn = DriverManager.getConnection(url);
    }

    public void close() throws Exception {
        conn.close();
    }

    public int setUpDatabase() throws Exception {


        Statement statement = conn.createStatement();

        // Setup Tables
        BufferedReader bufferedReader = new BufferedReader(new FileReader("./Tables.sql"));
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
        bufferedReader = new BufferedReader(new FileReader("./preloadData.sql"));
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

    public Indexs getIndex() throws Exception {
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM Indexs;";

        ResultSet resultSet = statement.executeQuery(sql);
        Indexs indexs = new Indexs();

        indexs.setIndexId(resultSet.getLong("indexId"));
        indexs.setBibleId(resultSet.getLong("bibleId"));
        indexs.setBookId(resultSet.getLong("bookId"));
        indexs.setChapterId(resultSet.getLong("chapterId"));
        indexs.setVerseId(resultSet.getLong("verseId"));
        indexs.setBibleLinkId(resultSet.getLong("bibleLinkId"));

        return indexs;
    }

    public void writeIndexs(Indexs indexs) throws Exception {
        Statement statement = conn.createStatement();
        String sql = "UPDATE Indexs SET bibleId = " + indexs.getBibleId() +
                ", bookId = " + indexs.getBookId() +
                ", chapterId = " + indexs.getChapterId() +
                ", verseId = " + indexs.getVerseId() +
                ", bibleLinkId = " + indexs.getBibleLinkId() +
                " Where indexId = " + indexs.getIndexId() + ";";

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
}
