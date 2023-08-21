package main.java.Service;

import main.java.Data.Chapter;
import main.java.Data.Notes;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

public class NotesHTMLDocument extends HTMLDocument {

    private final String path;
    private Vector<Notes> notes;
    private Integer textSize;

    public NotesHTMLDocument(Vector<Notes> notes, int textSize) {
        this.notes = notes;
        this.textSize = textSize;
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
    }

    public HTMLDocument createHTMLDocument() {
        Reader stringReader = new StringReader(createString());
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        StyleSheet ss = htmlEditorKit.getStyleSheet();
        ss.addRule("body {font-size: " + textSize + "px}");
        ss.addRule("table, th, td {border: 1px solid black; text-align: center}");
        HTMLDocument htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();

        try {
            htmlEditorKit.read(stringReader, htmlDocument, 0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "There was an error creating document", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return htmlDocument;
    }

    private String createString() {
        Vector<Chapter> chapters;
        Vector<Long> chapterId = new Vector<>();
        notes.forEach(note -> chapterId.add(note.getChapterId()));
        chapters = getChapters(chapterId);

        String text = "";
        String htmlOpenTag = "<HTML>";
        String htmlCloseTag = "</HTML>";

        // No Notes Message
        if (notes.isEmpty()) {
            text = text.concat("<H2 style = \"text-align : center; font-size : " + textSize + "px\">There are no notes for this chapter</H2>");
        }else {
            for (Chapter chapter : chapters) {

                // Creates Chapter Notes Header and Grid
                text = text.concat("<H2 style = \"text-align : center ; font-size : " + textSize + "px\">Notes For " + chapter.getDisplayName() + "</H2>");

                text = text.concat("<Table><tr><th>Note</th><th>Edit</th><th>Delete</th></tr>");

                for (Notes note : notes) {
                    if (note.getVerseId() == 0L && note.getChapterId() == chapter.getChapterId()) {
                        text = text.concat("<tr><td>");
                        text = text.concat(note.getNoteText());
                        text = text.concat("</td><td><a href =\"Edit-" +
                                note.getNoteId() + "\"><img class=\"edit\" src=\"file:///" + path + "/Resources/Icons/edit.png\" border=\"0\"/></a></td>");
                        text = text.concat("<td><a href =\"Delete-" +
                                note.getNoteId() + "\"><img class=\"delete\" src=\"file:///" + path + "/Resources/Icons/delete.png\" border=\"0\"/></a></td></tr>");
                    }
                }

                text = text.concat("</Table>");

                // Creates Notes Associated with a verse header and grid
                text = text.concat("<H2 style = \"text-align : center ; font-size : " + textSize + "px\">Notes For Verses in " + chapter.getDisplayName() + "</H2>");
                text = text.concat("<Table><tr><th>Verse</th><th>Note</th><th>Edit</th><th>Delete</th></tr>");

                for (Notes note : notes) {
                    if (note.getVerseId() != 0L && note.getChapterId() == chapter.getChapterId()) {
                        text = text.concat("<tr>");
                        text = text.concat("<td>" + note.getVerseId() + "</td><td>" + note.getNoteText() + "</td>");
                        text = text.concat("</td><td><a href=\"Edit-" +
                                note.getNoteId() + "\"><img class=\"edit\" src=\"file:///" + path + "/Resources/Icons/edit.png\" border=\"0\"/></a></td>");
                        text = text.concat("<td><a href=\"Delete-" +
                                note.getNoteId() + "\"><img class=\"delete\" src=\"file:///" + path + "/Resources/Icons/delete.png\" border=\"0\"/></a></td></tr>");
                    }
                }

                text = text.concat("</Table>");
            }
        }

        return htmlOpenTag + text + htmlCloseTag;
    }

    public HTMLDocument updateHTMLDocument(Vector<Notes> notes) {
        this.notes = notes;
        return createHTMLDocument();
    }

    private Vector<Chapter> getChapters(Vector<Long> chapterId) {
        Vector<Chapter> chapters = new Vector<>();
        try {
            DatabaseConnection connection = new DatabaseConnection();
            chapters = connection.getChaptersByChapterId(chapterId);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chapters;
    }

    public HTMLDocument setTextSize(int textSize) {
        this.textSize = textSize;
        return createHTMLDocument();
    }
}
