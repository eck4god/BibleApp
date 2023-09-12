package main.java.Views;

import main.java.Data.*;
import main.java.Service.BibleHTMLDocument;
import main.java.Service.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

public class ReaderPanel extends JPanel {

    private Application parentFrame;
    private Vector<Materials> materials;
    private Vector<BibleLink> bibleLinks = new Vector<>();
    private ArrayList<Long> expandedRef = new ArrayList<>();
    private ArrayList<String> expandedWords = new ArrayList<>();
    private boolean showInlineNotes = false;
    private boolean showInlineRef = false;
    private BibleHTMLDocument doc;
    private final Long bibleId;
    private FooterToolBar footerToolBar;
    private JTextPane textArea = new JTextPane();
    private JScrollPane pane;
    private int textSize;

    public ReaderPanel(Application parentFrame, int textSize, Long bibleId, Long bookId, Long chapterId,
                       boolean showInlineNotes, boolean showInlineRefm, Vector<Materials> materials) {
        this.parentFrame = parentFrame;
        this.textSize = textSize;
        this.bibleId = bibleId;
        this.showInlineNotes = showInlineNotes;
        this.showInlineRef = showInlineRef;
        this.materials = materials;
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
        Materials concordance = new Materials();
        for (Materials m : materials) {
            if (m.getName().equals(References.Strongs.toString())) {
                concordance = m;
            }
        }
        doc = new BibleHTMLDocument(bibleLinks, textSize, showInlineNotes, showInlineRef, expandedRef, expandedWords, concordance);
        textArea.setContentType("text/html");
        textArea.setDocument(doc.createDocument());
        textArea.setEditable(false);
        textArea.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String[] link = event.getDescription().split("-");
                System.out.println(link[0] + " " + link[1]);
                switch (link[0]) {
                    case "ref": {
                        if (expandedRef.contains(Long.parseLong(link[1]))) {
                            expandedRef.remove(Long.parseLong(link[1]));
                        } else {
                            expandedRef.add(Long.parseLong(link[1]));
                        }
                        textArea.setDocument(doc.updateExpandedRef(expandedRef));
                        try {
                            String text = textArea.getDocument().getText(0, textArea.getDocument().getLength());
                            textArea.setCaretPosition(link[1].equals("0") ? 0 : text.indexOf(link[1] + " "));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "word": {
                        if (expandedWords.contains(link[1])) {
                            expandedWords.remove(link[1]);
                        } else {
                            expandedWords.add(link[1]);
                        }
                        textArea.setDocument(doc.updateExpandedWords(expandedWords));
                        try {
                            String text = textArea.getDocument().getText(0, textArea.getDocument().getLength());
                            textArea.setCaretPosition(text.indexOf(link[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "cit": {
                        expandedWords.clear();
                        expandedRef.clear();
                        setFieldsByReference(Long.parseLong(link[1]), Long.parseLong(link[2]), Long.parseLong(link[3]));
                        break;
                    }
                    case "add": {
                        addNote(Long.parseLong(link[1]),Long.parseLong(link[2]),Long.parseLong(link[3]),new Notes(), "");
                        break;
                    }
                    case "Edit": {
                        Notes note = new Notes();
                        try {
                            DatabaseConnection databaseConnection = new DatabaseConnection();
                            note = databaseConnection.getNoteById(Long.parseLong(link[1]));
                            databaseConnection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String html = "<HTML><head></head><body>" + note.getNoteText() + "</body></HTML>";
                        addNote(note.getBookId(), note.getChapterId(), note.getVerseId(), note, html);
                        break;
                    }
                    case "Delete": {
                        try {
                            DatabaseConnection databaseConnection = new DatabaseConnection();
                            databaseConnection.deleteNotes(Long.parseLong(link[1]));
                            databaseConnection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        parentFrame.updateNotes(bibleId, bibleLinks.get(0).getBook().getBookNumber(), bibleLinks.get(0).getChapter().getChapterId());
                        int caretPos = textArea.getCaretPosition();
                        textArea.setDocument(doc.updatePage(bibleLinks));
                        textArea.setCaretPosition(caretPos);
                        break;
                    }
                    default: {}
                }
            }
        });
        pane = new JScrollPane();
        pane.setPreferredSize(this.getPreferredSize());
        pane.setViewportView(textArea);
        this.add(pane, BorderLayout.CENTER);
    }

    public void setSearchFields(Long bibleId, Long bookId, Long chapterId) {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        getBibleLinks(bibleId, bookId, chapterId);
        textArea.setDocument(doc.updatePage(bibleLinks));
        pane.getVerticalScrollBar().setValue(0);
        footerToolBar.updateBookAndChapter(bookId, chapterId);
        parentFrame.updateNotes(bibleId, bookId, chapterId);
        parentFrame.updateConcordance(bookId, chapterId);
    }

    public void setFieldsByReference(Long bookId, Long chapterId, Long verseNum) {
        Long nextVerse = verseNum + 1;
        getBibleLinks(bibleId, bookId, chapterId);
        textArea.setDocument(doc.updatePage(bibleLinks));
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        try {
            String text = textArea.getDocument().getText(0, textArea.getDocument().getLength());
            textArea.setSelectionStart(text.indexOf(verseNum.toString() + " "));
            textArea.setSelectionEnd(text.indexOf("\n", textArea.getSelectionStart()));
            highlighter.addHighlight(textArea.getSelectionStart(), textArea.getSelectionEnd(), new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE));
            textArea.setCaretPosition(textArea.getSelectionStart());
        } catch (Exception e) {
            e.printStackTrace();
        }
        footerToolBar.updateBookAndChapter(bookId, chapterId);
        parentFrame.updateNotes(bibleId, bookId, chapterId);
        parentFrame.updateConcordance(bookId, chapterId);
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

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textArea.setDocument(doc.setSize(textSize));
        parentFrame.setTextSize(textSize);
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

    public void toggleShowInlineRef() {
        if (showInlineRef) {
            showInlineRef = false;
        } else {
            showInlineRef = true;
        }
        textArea.setDocument(doc.setShowRef(showInlineRef));
    }

    public void toggleShowInlineNotes() {
        if (showInlineNotes) {
            showInlineNotes = false;
        } else {
            showInlineNotes = true;
        }
        textArea.setDocument(doc.setShowNotes(showInlineNotes));
    }

    public boolean getShowInlineRef() {
        return showInlineRef;
    }

    public boolean getShowInlineNotes() {
        return showInlineNotes;
    }

    private void addNote(Long bookId, Long chapterId, Long verseId, Notes note, String html) {
        JDialog dialog = new JDialog();
        dialog.setSize(new Dimension(600,400));
        dialog.setLocationRelativeTo(parentFrame);

        // Setup Panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10,10,10,10));

        // Editor Layout
        TextEditor textEditor;
        if (html.equals("")) {
            textEditor = new TextEditor(textSize);
            System.out.println("null");
        } else {
            textEditor = new TextEditor(textSize, html);
        }
        JScrollPane textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(textEditor);

        // Button Layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        JButton submit = new JButton("Submit");
        submit.setBackground(Color.BLUE);
        submit.setForeground(Color.WHITE);
        submit.addActionListener(event -> {
            if (note.getNoteId() == null) {
                saveNote(textEditor.getText(), bookId, chapterId, verseId, note.getNoteId());
            } else {
                saveNote(textEditor.getText(), note.getBookId(), note.getChapterId(), note.getVerseId(), note.getNoteId());
            }
            dialog.setVisible(false);
            dialog.dispose();
        });

        buttonPanel.add(cancel, BorderLayout.WEST);
        buttonPanel.add(submit, BorderLayout.EAST);

        panel.add(textEditor, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void saveNote(String noteText, Long bookId, Long chapterId, Long associatedVerse, Long noteId) {
        Notes note = new Notes();
        Indexes indexes;
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            indexes = databaseConnection.getIndex();

            if (noteId == null) {
                note.setNoteId(indexes.getNotesId() + 1);
            } else {
                note.setNoteId(noteId);
            }
            note.setBibleId(bibleId);
            note.setBookId(bookId);
            note.setChapterId(chapterId);
            note.setVerseId(associatedVerse);
            note.setNoteText(noteText);

            if (noteId == null) {
                indexes.setNotesId(indexes.getNotesId() + 1);
                databaseConnection.writeIndexes(indexes);
            }

            if (noteId == null) {
                databaseConnection.writeToNotes(note);
            } else {
                databaseConnection.updateNotes(note);
            }

            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int caretPos = textArea.getCaretPosition();
        textArea.setDocument(doc.updatePage(bibleLinks));
        textArea.setCaretPosition(caretPos);
        parentFrame.updateNotes(bibleId, bookId, chapterId);
    }
}
