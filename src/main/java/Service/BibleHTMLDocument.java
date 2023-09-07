package main.java.Service;

import main.java.Data.*;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Vector;

public class BibleHTMLDocument extends HTMLDocument {

    private final String path;
    private Vector<BibleLink> bibleLinks;
    private ArrayList<Long> expandedRef;
    private ArrayList<String> expandedWords;
    private boolean showNotes = false;
    private boolean showRef = false;
    private int textSize;

    public BibleHTMLDocument(Vector<BibleLink> bibleLinks, int textSize, boolean showNotes, boolean showRef,
                             ArrayList<Long> expandedRef, ArrayList<String> expandedWords) {
        this.bibleLinks = bibleLinks;
        this.textSize = textSize;
        this.showRef = showRef;
        this.showNotes = showNotes;
        this.expandedRef = expandedRef;
        this.expandedWords = expandedWords;
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
    }

    public HTMLDocument createDocument() {
        StringReader reader = new StringReader(createString());
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet ss = editorKit.getStyleSheet();
        ss.addRule(".bible {text-align: center; font-size: " + textSize * 1.5 + "px; margin: 5px 5px 0px 0px");
        ss.addRule(".chapter {text-align: center; font-size: " + textSize + "px; margin: 5px 5px 0px 0px}");
        ss.addRule("body, p {font-size: " + textSize + "px}");
        ss.addRule("body {font-size: " + textSize + "px}");
        ss.addRule("th, .tableData {border: 1px solid black; text-align: left}");
        ss.addRule(".inlineNoteTable {border: 1px solid black; text-align: left; margin-left: 40px}");
        HTMLDocument document = (HTMLDocument) editorKit.createDefaultDocument();

        try {
            editorKit.read(reader, document, 0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was an error creating document", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return document;
    }

    private String createString() {
        String text = "";
        String HTML1 = "<HTML><body>";
        String HTML2 = "</body></HTML>";

        text = text.concat("<H1 class=\"bible\">" + bibleLinks.get(0).getBook().getBookTitle() + "</H1>");
        text = text.concat("<H2 class=\"chapter\">" + bibleLinks.get(0).getChapter().getDisplayName() + "</H2>");
        if (showRef) {
            Vector<Word> words = new Vector<>();
            try {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                words = databaseConnection.getWordByCitation(bibleLinks.get(0).getBook().getBookNumber(), bibleLinks.get(0).getChapter().getChapterId(), 0L);
                databaseConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!words.isEmpty()) {
                text = text.concat("<a href=\"ref-0\">Chapter References</a><br>");
                if (expandedRef.contains(0L)) {
                    for (Word word : words) {
                        text = text.concat("<span>&ensp;</span><a href=\"word-" + word.getWord() + "\">" + word.getWord() + "</a><br>");
                        if (expandedWords.contains(word.getWord())) {
                            Vector<Reference> references = new Vector<>();
                            Vector<Book> books = new Vector<>();
                            try {
                                DatabaseConnection databaseConnection = new DatabaseConnection();
                                references = databaseConnection.getReferenceByWordId(word.getWordId());
                                books = databaseConnection.getBooks();
                                databaseConnection.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            for (Reference reference : references) {
                                text = text.concat("<span>&emsp;</span><a href=\'cit-" + reference.getBookId() + "-" + reference.getChapterId() + "-" + reference.getVerseNum() + "\'>" +
                                        books.get(reference.getBookId().intValue() - 1).getBookAbbrev() + " " + reference.getChapterId() + ":" + reference.getVerseNum() +
                                        "</a>");
                                text = text.concat("<span>" + reference.getText() + "</span><br>");
                            }
                        }
                    }
                }
            }
        }
        if (showNotes) {
            Vector<Notes> notes = new Vector<>();
            try {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                notes = databaseConnection.getNotesByVerse(bibleLinks.get(0).getBible().getBibleId(),
                        bibleLinks.get(0).getBook().getBookNumber(), bibleLinks.get(0).getChapter().getChapterId(), 0L);
                databaseConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!notes.isEmpty()) {
                text = text.concat("<a>Chapter Notes</a><br>");

                text = text.concat("<Table class=\"inlineNoteTable\"><tr><th>Note</th><th>Edit</th><th>Delete</th></tr>");

                for (Notes note : notes) {
                    text = text.concat("<tr class=\"tableRow\"><td class=\"tableData\">");
                    text = text.concat(note.getNoteText());
                    text = text.concat("</td><td class=\"tableData\"><a href =\"Edit-" +
                            note.getNoteId() + "\"><img class=\"edit-0\" src=\"file:///" + path + "/Resources/Icons/edit.png\" border=\"0\"/></a></td>");
                    text = text.concat("<td class=\"tableData\"><a href =\"Delete-" +
                            note.getNoteId() + "\"><img class=\"delete-0\" src=\"file:///" + path + "/Resources/Icons/delete.png\" border=\"0\"/></a></td></tr>");
                }
                text = text.concat("<tr class=\"tableRow\"><td clas\"tableData\"><a href=\"add-" +
                        bibleLinks.get(0).getBook().getBookNumber() + "-" + bibleLinks.get(0).getChapter().getChapterId() +
                        "-0\">Add New</a></td></tr>");
                text = text.concat("</Table>");
            }
        }

        text = text.concat("<p>");
        for (BibleLink bibleLink : bibleLinks) {
            text = text.concat("<span id=\"" + bibleLink.getVerse().getVerseId() + "\">" +
                    bibleLink.getVerse().getVerseNumber() + " " + bibleLink.getVerse().getVerseText() +
                    "</span><br>");
            if (showRef) {
                Vector<Word> words = new Vector<>();
                try {
                    DatabaseConnection databaseConnection = new DatabaseConnection();
                    words = databaseConnection.getWordByCitation(bibleLink.getBook().getBookNumber(), bibleLink.getChapter().getChapterId(),
                            Long.parseLong(bibleLink.getVerse().getVerseNumber()));
                    databaseConnection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!words.isEmpty()) {
                    text = text.concat("<p style=\"margin-left: 40px\"><a href=\"ref-" + bibleLink.getVerse().getVerseNumber() + "\">Verse References</a><<br>");
                    if (expandedRef.contains(Long.parseLong(bibleLink.getVerse().getVerseNumber()))) {
                        for (Word word : words) {
                            text = text.concat("<span>&ensp;</span><a href=\"word-" + word.getWord() + "\">" + word.getWord() + "</a><br>");
                            if (expandedWords.contains(word.getWord())) {
                                Vector<Reference> references = new Vector<>();
                                Vector<Book> books = new Vector<>();
                                try {
                                    DatabaseConnection databaseConnection = new DatabaseConnection();
                                    references = databaseConnection.getReferenceByWordId(word.getWordId());
                                    books = databaseConnection.getBooks();
                                    databaseConnection.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                for (Reference reference : references) {
                                    text = text.concat("<span>&emsp;</span><a href=\'cit-" + reference.getBookId() + "-" + reference.getChapterId() + "-" + reference.getVerseNum() + "\'>" +
                                            books.get(reference.getBookId().intValue() - 1).getBookAbbrev() + " " + reference.getChapterId() + ":" + reference.getVerseNum() +
                                            "</a>");
                                    text = text.concat("<span>" + reference.getText() + "</span><br>");
                                }
                            }
                        }
                    }
                    text = text.concat("</p><br>");
                }
            }
            if (showNotes) {
                Vector<Notes> notes = new Vector<>();
                try {
                    DatabaseConnection databaseConnection = new DatabaseConnection();
                    notes = databaseConnection.getNotesByVerse(bibleLink.getBible().getBibleId(),
                            bibleLink.getBook().getBookNumber(), bibleLink.getChapter().getChapterId(), Long.parseLong(bibleLink.getVerse().getVerseNumber()));
                    databaseConnection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                text = text.concat("<Table class=\"inlineNoteTable\"><tr><th>Note</th><th>Edit</th><th>Delete</th></tr>");

                for (Notes note : notes) {
                    text = text.concat("<tr class=\"tableRow\"><td class=\"tableData\">");
                    text = text.concat(note.getNoteText());
                    text = text.concat("</td><td class=\"tableData\"><a href =\"Edit-" +
                            note.getNoteId() + "\"><img class=\"edit-" + note.getNoteId() + "\" src=\"file:///" + path + "/Resources/Icons/edit.png\" border=\"0\"/></a></td>");
                    text = text.concat("<td class=\"tableData\"><a href =\"Delete-" +
                            note.getNoteId() + "\"><img class=\"delete\" src=\"file:///" + path + "/Resources/Icons/delete.png\" border=\"0\"/></a></td></tr>");
                }
                text = text.concat("<tr class=\"tableRow\"><td clas\"tableData\"><a href=\"add-" +
                        bibleLink.getBook().getBookNumber() + "-" + bibleLink.getChapter().getChapterId() + "-" +
                        bibleLink.getVerse().getVerseNumber() + "\">Add New</a></td></tr>");
                text = text.concat("</Table>");

            }
        }
        text = text.concat("</p>");

        return HTML1 + text + HTML2;
    }

    public HTMLDocument updatePage(Vector<BibleLink> bibleLinks) {
        this.bibleLinks = bibleLinks;
        expandedWords.clear();
        expandedRef.clear();
        return createDocument();
    }

    public HTMLDocument setSize(int textSize) {
        this.textSize = textSize;
        return createDocument();
    }

    public HTMLDocument setShowNotes(boolean showNotes) {
        this.showNotes = showNotes;
        return createDocument();
    }

    public HTMLDocument setShowRef(boolean showRef) {
        this.showRef = showRef;
        return createDocument();
    }

    public HTMLDocument updateExpandedRef(ArrayList<Long> expandedRef) {
        this.expandedRef = expandedRef;
        return createDocument();
    }

    public HTMLDocument updateExpandedWords(ArrayList<String> expandedWords) {
        this.expandedWords = expandedWords;
        return createDocument();
    }
}
