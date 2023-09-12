package main.java.Views;

import main.java.Data.Materials;
import main.java.Data.Word;
import main.java.Service.ConcordanceHTMLDocument;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

public class ConcordancePanel extends JPanel {

    private final String path;
    private Long bookId;
    private Long chapterId;
    private Application application;
    private Materials materials;
    private JTextPane textPane;
    private JScrollPane pane;
    private JTextField searchField;
    private String search = "";
    private Vector<Word> words;
    private ConcordanceHTMLDocument htmlDocument;
    private ArrayList<String> expandedWords = new ArrayList<>();
    private int textSize;
    private int scrollPos;

    public ConcordancePanel(Application application, Materials materials, int textSize, Long bookId, Long chapterId) {
        this.application = application;
        this.materials = materials;
        this.textSize = textSize;
        this.bookId = bookId;
        this.chapterId = chapterId;
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        getWordsByReference();

        setupPanel();
    }

    private void setupPanel() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10,10,10,10));
        createToolBar();
        createScrollPane();
    }

    private void createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new BorderLayout());
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Search Box
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(8);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> {
            if (searchField.getText().length() > 1) {
                search = searchField.getText();
                getWords(search);
                textPane.setDocument(htmlDocument.updatePage(words));
                pane.getVerticalScrollBar().setValue(0);
            }
        });
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(event -> {
            if (search.length() > 1) {
                getWordsByReference();
                textPane.setDocument(htmlDocument.updatePage(words));
                pane.getVerticalScrollBar().setValue(0);
                searchField.setText("");
                searchField.validate();
                searchField.repaint();
            } else {
                searchField.setText("");
                searchField.validate();
                searchField.repaint();
            }
        });
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // Open Concordance Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton openButton = new JButton("Open Concordance");
        openButton.addActionListener(e -> {
            ConcordanceWindow concordanceWindow = new ConcordanceWindow(application, materials, textSize);
            concordanceWindow.setVisible(true);
        });

        buttonPanel.add(openButton);

        navPanel.add(buttonPanel, BorderLayout.WEST);
        navPanel.add(searchPanel, BorderLayout.EAST);

        toolBar.add(navPanel, BorderLayout.CENTER);
        this.add(toolBar, BorderLayout.NORTH);
    }

    private void createScrollPane() {
        if (words.isEmpty()) {
            emptyLabel();
        } else {
            htmlDocument = new ConcordanceHTMLDocument(words,textSize, false, expandedWords);
            textPane = new JTextPane();
            textPane.setContentType("text/html");
            textPane.setEditable(false);
            textPane.setDocument(htmlDocument.createDocument());

            pane = new JScrollPane();
            pane.setPreferredSize(this.getPreferredSize());
            pane.setViewportView(textPane);
            textPane.addHyperlinkListener(event -> {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (!event.getDescription().contains(";")) {
                        if (expandedWords.contains(event.getDescription())) {
                            expandedWords.remove(event.getDescription());
                            textPane.setDocument(htmlDocument.updateArrayList(expandedWords));
                            try {
                                String text = textPane.getDocument().getText(0, textPane.getDocument().getLength());
                                textPane.setCaretPosition(text.indexOf("\n" + event.getDescription()));
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        } else {
                            expandedWords.add(event.getDescription());
                            textPane.setDocument(htmlDocument.updateArrayList(expandedWords));
                            try {
                                String text = textPane.getDocument().getText(0, textPane.getDocument().getLength());
                                textPane.setCaretPosition(text.indexOf("\n" + event.getDescription()));
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        String[] ref = event.getDescription().split(";");
                        application.navigateToReference(Long.parseLong(ref[0]), Long.parseLong(ref[1]), Long.parseLong(ref[2]));
                    }
                }
            });

            this.add(pane, BorderLayout.CENTER);
        }
    }

    private void emptyLabel() {
        JLabel label = new JLabel("Nothing matches your search");
        label.setHorizontalAlignment(JLabel.CENTER);
        this.add(label, BorderLayout.CENTER);
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textPane.setDocument(htmlDocument.setTextSize(textSize));
    }

    private void getWords(String search) {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            int totalCount = databaseConnection.getTotalCountOfPagedWordByString(search, materials.getMaterialsId());
            words = databaseConnection.getPagedWordByString(search, materials.getMaterialsId(), 0, totalCount);
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getWordsByReference() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            words = databaseConnection.getWordByReference(materials.getMaterialsId(), bookId, chapterId);
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateReference(Long bookId, Long chapterId) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        getWordsByReference();
        textPane.setDocument(htmlDocument.updatePage(words));
        search = "";
        searchField.setText("");
    }
}
