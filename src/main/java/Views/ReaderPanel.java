package main.java.Views;

import main.java.Data.BibleLink;
import main.java.Service.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

public class ReaderPanel extends JPanel {

    private Vector<BibleLink> bibleLinks = new Vector<>();
    private Long bibleId;
    private FooterToolBar footerToolBar;
    private JTextPane textArea = new JTextPane();
    private int textSize = 12;

    public ReaderPanel(Long bibleId, Long bookId, Long chapterId) {
        this.bibleId = bibleId;
        BorderLayout layout = new BorderLayout();
        this.setLayout(layout);
        this.setBorder(new EmptyBorder(10,10,10,10));
        NavigationDropDown navigationDropDown = new NavigationDropDown(this, bibleId);
        this.add(navigationDropDown, BorderLayout.NORTH);

        getBibleLinks(bibleId, bookId, chapterId);

        if (bibleLinks.isEmpty())
            emptyLabel();
        else
            scrollPane();

        footerToolBar = new FooterToolBar(this, bibleId, getBook(), getChapter());
        this.add(footerToolBar, BorderLayout.SOUTH);
    }

    private void emptyLabel() {
        JLabel label = new JLabel("Nothing to display");
        label.setHorizontalAlignment(JLabel.CENTER);
        this.add(label, BorderLayout.CENTER);
    }

    private void scrollPane() {
        textArea.setContentType("text/html");
        textArea.setDocument(createHTMLDocument());
        textArea.setEditable(false);
        JScrollPane pane = new JScrollPane();
        pane.setPreferredSize(this.getPreferredSize());
        pane.setViewportView(textArea);
        this.add(pane, BorderLayout.CENTER);
    }

    public void setSearchFields(Long bibleId, Long bookId, Long chapterId) {
        getBibleLinks(bibleId, bookId, chapterId);
        textArea.setDocument(createHTMLDocument());
        footerToolBar.updateBookAndChapter(bookId, chapterId);
    }

    public void getBibleLinks(Long bibleId, Long bookId, Long chapterId) {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            bibleLinks = connection.getBibleLink(bibleId, bookId, chapterId);
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "There was an error in the database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String createString() {
        String text = "";
        String HTML1 = "<HTML><H1 style=\"text-align:center; font-size:" + textSize * 1.5 +"px; margin:5px 5px 0px\">" + bibleLinks.get(0).getBook().getBookTitle() + "</H1>" +
                "<H2 style=\"text-align:center;font-size:" + textSize + "px; margin: 5px 5px 0px 0px\">" + bibleLinks.get(0).getChapter().getDisplayName() + "</H2>" +
                "<p style=font-size:" + textSize + "px>";
        String HTML2 = "</p></div><HTML>";

        text = text.concat(HTML1);
        for (BibleLink bibleLink : bibleLinks) {
            text = text.concat("<span class name=\"" + bibleLink.getVerse().getVerseNumber() + "\">"
                    + bibleLink.getVerse().getVerseNumber() + " " + bibleLink.getVerse().getVerseText().trim() + "</span><br>");
        }
        text = text.concat(HTML2);

        return text;
    }

    private HTMLDocument createHTMLDocument() {

        Reader stringReader = new StringReader(createString());
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        try {
            htmlKit.read(stringReader, htmlDoc, 0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "There was an error creating document", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return htmlDoc;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textArea.setDocument(createHTMLDocument());
    }

    public int getTextSize() {
        return textSize;
    }

    public Long getBible() { return bibleId; }

    public Long getChapter() {
        return bibleLinks.get(0).getChapter().getChapterId();
    }

    public Long getBook() {
        return bibleLinks.get(0).getBook().getBookNumber();
    }
}
