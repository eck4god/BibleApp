package main.java.Views;

import main.java.Data.Indexes;
import main.java.Data.Notes;
import main.java.Service.DatabaseConnection;
import main.java.Service.NotesHTMLDocument;
import main.java.Service.ProgramDirectoryService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.Vector;

public class NotesPanel extends JPanel {

    private final String path;
    private final JFrame parentFrame;
    private int textSize;
    private Vector<Notes> notes = new Vector<>();
    private NotesHTMLDocument notesHTMLDocument;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private Long bibleId;
    private Long bookId;
    private Long chapterId;

    public NotesPanel(JFrame parentFrame, int textSize, Long bibleId, Long bookId, Long chapterId) {
        this.parentFrame = parentFrame;
        this.textSize = textSize;
        this.bibleId = bibleId;
        this.bookId = bookId;
        this.chapterId = chapterId;
        getNotes(bibleId, bookId, chapterId);
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        setupPanel();
        footerBar();
    }

    private void footerBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton();
        button.setIcon(new ImageIcon(path + "/Resources/Icons/plus.png"));
        button.setText("Add Note");
        button.setPreferredSize(new Dimension(120, 25));
        button.setToolTipText("Add Note");
        button.addActionListener(e -> addNote("", 0L));

        panel.add(button);
        toolBar.add(panel);

        this.add(toolBar, BorderLayout.SOUTH);
    }

    private void setupPanel() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        createScrollPane();
    }

    private void createScrollPane() {
        notesHTMLDocument = new NotesHTMLDocument(notes, textSize);
        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setDocument(notesHTMLDocument.createHTMLDocument());
        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String[] str = e.getDescription().split("-", 2);
                if (str[0].equals("Edit")) {
                    editNote(Long.parseLong(str[1]));
                } else if (str[0].equals("Delete")) {
                    deleteNote(Long.parseLong(str[1]));
                }
            }
        });

        scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(this.getPreferredSize());
        scrollPane.setViewportView(textPane);

        this.add(scrollPane);
    }

    private void editNote(Long noteId) {
        Notes note = new Notes();
        for (Notes n : notes) {
            if (n.getNoteId() == noteId) {
                note = n;
            }
        }
        String html = "<HTML><head></head><body>" + note.getNoteText() + "</body></HTML>";
        addNote(html, note.getNoteId());
    }

    private void deleteNote(Long noteId) {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            databaseConnection.deleteNotes(noteId);
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateNotes(bibleId, bookId, chapterId);
    }

    private void addNote(String html, Long noteId) {
        JDialog dialog = new JDialog();
        dialog.setSize(new Dimension(600, 400));
        dialog.setLocationRelativeTo(parentFrame);

        // Set up panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Selection Layout
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BorderLayout());

        // Radio Button Layout
        JPanel radioButtonPanel = new JPanel();
        radioButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel label = new JLabel("Add note to: ");
        JRadioButton chapterButton = new JRadioButton("Chapter");
        chapterButton.setSelected(true);
        JRadioButton verseButton = new JRadioButton("Verse");

        radioButtonPanel.add(label);
        radioButtonPanel.add(chapterButton);
        radioButtonPanel.add(verseButton);

        // DropDown Layout
        JPanel dropDownPanel = new JPanel();
        dropDownPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel vLabel = new JLabel("Associated verse");
        JComboBox<Long> verseSelection = new JComboBox<>();
        Long verseCount = 0L;
        try {
            DatabaseConnection connection = new DatabaseConnection();
            verseCount = connection.getVerseCount(bibleId, bookId, chapterId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Long i = 0L; i < verseCount; i++) {
            verseSelection.addItem(i + 1);
        }

        dropDownPanel.add(vLabel);
        dropDownPanel.add(verseSelection);
        dropDownPanel.setVisible(false);

        // Build selection Layout
        selectionPanel.add(radioButtonPanel, BorderLayout.NORTH);
        selectionPanel.add(dropDownPanel, BorderLayout.SOUTH);

        // Editor Layout
        TextEditor textEditor;
        if (html.equals("")) {
            textEditor = new TextEditor(textSize);
        } else {
            textEditor = new TextEditor(textSize, html);
        }
        JScrollPane textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(textEditor);


        //Button Layout
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
        submit.addActionListener(e -> {
            if (chapterButton.isSelected()) {
                saveNote(textEditor.getText(), null, noteId);
                dialog.setVisible(false);
                dialog.dispose();
            } else {
                saveNote(textEditor.getText(), (Long) verseSelection.getSelectedItem(), noteId);
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        buttonPanel.add(cancel, BorderLayout.WEST);
        buttonPanel.add(submit, BorderLayout.EAST);

        // Action Listeners
        chapterButton.addActionListener(event -> {
            if (verseButton.isSelected()) {
                verseButton.setSelected(false);
                dropDownPanel.setVisible(false);
            }
        });
        verseButton.addActionListener(event -> {
            if (chapterButton.isSelected()) {
                chapterButton.setSelected(false);
                dropDownPanel.setVisible(true);
            }
        });

        panel.add(selectionPanel, BorderLayout.NORTH);
        panel.add(textScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void getNotes(Long bibleId, Long bookId, Long chapterId) {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            notes = databaseConnection.getNotes(bibleId, bookId, chapterId);
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNotes(Long bibleId, Long bookId, Long chapterId) {
        this.bibleId = bibleId;
        this.bookId = bookId;
        this.chapterId = chapterId;
        getNotes(bibleId, bookId, chapterId);
        textPane.setDocument(notesHTMLDocument.updateHTMLDocument(notes));
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textPane.setDocument(notesHTMLDocument.setTextSize(textSize));
    }

    private void saveNote(String noteText, Long associatedVerse, Long noteId) {
        Notes note = new Notes();
        Indexes indexes;
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            indexes = databaseConnection.getIndex();

            if (noteId == 0L) {
                note.setNoteId(indexes.getNotesId() + 1);
            } else {
                note.setNoteId(noteId);
            }
            note.setBibleId(bibleId);
            note.setBookId(bookId);
            note.setChapterId(chapterId);
            note.setVerseId(associatedVerse);
            note.setNoteText(noteText);

            if (noteId == 0L) {
                indexes.setNotesId(indexes.getNotesId() + 1);
                databaseConnection.writeIndexes(indexes);
            }

            if (noteId == 0L) {
                databaseConnection.writeToNotes(note);
            } else {
                databaseConnection.updateNotes(note);
            }

            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateNotes(bibleId, bookId, chapterId);
    }
}
