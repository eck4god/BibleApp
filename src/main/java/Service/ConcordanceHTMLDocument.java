package main.java.Service;

import main.java.Data.Book;
import main.java.Data.Reference;
import main.java.Data.Word;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Vector;

public class ConcordanceHTMLDocument extends HTMLDocument {

    private Vector<Book> books;
    private Vector<Word> words;
    private int textSize;
    private boolean isExpanded;
    private ArrayList<String> expandedWords = new ArrayList<>();

    public ConcordanceHTMLDocument(Vector<Word> words, int textSize, boolean isExpanded) {
        this.words = words;
        this.textSize = textSize;
        this.isExpanded = isExpanded;

        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            books = databaseConnection.getBooks();
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ConcordanceHTMLDocument(Vector<Word> words, int textSize, boolean isExpanded, ArrayList<String> expandedWords) {
        this.words = words;
        this.textSize = textSize;
        this.isExpanded = isExpanded;
        this.expandedWords = expandedWords;

        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            books = databaseConnection.getBooks();
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HTMLDocument createDocument() {
        StringReader reader = new StringReader(createString());
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet ss = editorKit.getStyleSheet();
        ss.addRule("body {font-size: " + textSize + "px}");
        ss.addRule(".refTable, .data {text-align: left; margin-left: 40px}");
        HTMLDocument document = (HTMLDocument) editorKit.createDefaultDocument();

        try {
            editorKit.read(reader, document, 0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was an error creating the document", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return document;
    }

    private String createString() {
        String text = "";
        String HTML1 = "<HTML><body>";
        String HTML2 = "</body></HTML>";

        for (Word word : words) {
            if (isExpanded) {
                text = text.concat("<H2 id='" + word.getWordId() + "' style=\"color: #0000cc; margin-left: 20px; font-size:" +
                        textSize + "px\">" + word.getWord() + "</H2>");
            } else {
                text = text.concat("<H2 style=\"color: #0000cc; margin-left: 20px; font-size:" + textSize + "px\">" +
                        "<a href=\'" + word.getWord() + "\'>" + word.getWord() + "</a><H2>");
            }

            if (isExpanded || expandedWords.contains(word.getWord())) {
                Vector<Reference> references = new Vector<>();
                try {
                    DatabaseConnection databaseConnection = new DatabaseConnection();
                    references = databaseConnection.getReferenceByWordId(word.getWordId());
                    databaseConnection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                text.concat("<Table class=\"refTable\">");
                if (references.isEmpty()) {
                    text = text.concat("<tr><td class=\"data\">See the Appendix *Not yet available</td><tr>");
                }
                for (Reference reference : references) {
                    if (reference.getBookId() == null || reference.getBookId() == 0) {
                        text = text.concat("<tr><td class=\"data\">" + reference.getCitation() + "</td></tr>");
                    } else {
                        text = text.concat("<tr>");
                        text = text.concat("<td class=\"data\">" + books.get(reference.getBookId().intValue() - 1).getBookAbbrev() + " " + reference.getChapterId() + ":" + reference.getVerseNum() + "</td>");
                        text = text.concat("<td>" + reference.getText() + "</td>");
                        text = text.concat("<td>" + reference.getLink() + "</td>");
                        text = text.concat("</tr>");
                    }
                }
                text = text.concat("</Table>");
            }
        }

        return HTML1 + text + HTML2;
    }

    public HTMLDocument setTextSize(int textSize) {
        this.textSize = textSize;
        return createDocument();
    }

    public HTMLDocument updatePage(Vector<Word> words) {
        this.words = words;
        return createDocument();
    }

    public HTMLDocument updateArrayList(ArrayList<String> expandedWords) {
        this.expandedWords = expandedWords;
        return createDocument();
    }
}
