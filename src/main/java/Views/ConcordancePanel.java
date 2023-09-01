package main.java.Views;

import main.java.Data.Word;
import main.java.Service.ConcordanceHTMLDocument;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

public class ConcordancePanel extends JPanel {

    private final String path;
    private Application application;
    private JTextPane textPane;
    private JScrollPane pane;
    private String searchLetter = "A";
    private String searchWord = "";
    private Vector<Word> words;
    private ConcordanceHTMLDocument htmlDocument;
    private int textSize;
    private int startIndex = 0;
    private int batchSize = 10;
    private int totalCount = 0;

    public ConcordancePanel(Application application, int textSize) {
        this.application = application;
        this.textSize = textSize;
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        getWords(searchLetter);

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

        JButton nextButton = new JButton();
        JButton prevButton = new JButton();

        // Letter Navigator
        ArrayList<String> letters = createLetters();
        JPanel letterPanel = new JPanel();
        letterPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        for (String letter : letters) {
            JButton button = new JButton(letter);
            button.addActionListener(event -> {
                startIndex = 0;
                searchLetter = letter;
                getWords(searchLetter);
                for (Word word : words) {
                    System.out.println(word.getWord());
                }
                textPane.setDocument(htmlDocument.updatePage(words));
                pane.getVerticalScrollBar().setValue(0);
                if (searchLetter.equals("A")) {
                    prevButton.setEnabled(false);
                } else {
                    prevButton.setEnabled(true);
                }
            });
            letterPanel.add(button);
        }

        // Search Box
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JTextField search = new JTextField(8);
        search.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(event -> {
            ConcordanceWindow concordanceWindow = new ConcordanceWindow(application, textSize);
            concordanceWindow.setVisible(true);
        });
        searchPanel.add(search);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        prevButton.setIcon(new ImageIcon(path + "/Resources/Icons/leftArrow.png"));
        prevButton.setPreferredSize(new Dimension(25,25));
        if (startIndex == 0) {
            prevButton.setEnabled(false);
        }
        prevButton.addActionListener(event -> {
            if (startIndex - batchSize <= 0) {
                startIndex = 0;
                if (searchLetter.equals(letters.get(0))) {
                    prevButton.setEnabled(false);
                } else {
                    searchLetter = letters.get(letters.indexOf(searchLetter) - 1);
                    try {
                        DatabaseConnection databaseConnection = new DatabaseConnection();
                        totalCount = databaseConnection.getTotalCountOfPagedWordByString(searchLetter);
                        databaseConnection.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startIndex = (int) Math.ceil((totalCount / 10)) * 10;
                }
            } else {
                startIndex -= batchSize;
            }
            getWords(searchLetter);
            textPane.setDocument(htmlDocument.updatePage(words));
            pane.getVerticalScrollBar().setValue(0);
            nextButton.setEnabled(true);
        });

        nextButton.setIcon(new ImageIcon(path + "/Resources/Icons/rightArrow.png"));
        nextButton.addActionListener(event -> {
            if (startIndex + batchSize > totalCount) {
                startIndex = 0;
                if (searchLetter.equals(letters.get(letters.size() -1))) {
                    nextButton.setEnabled(false);
                } else {
                    searchLetter = letters.get(letters.indexOf(searchLetter) + 1);
                }
            } else {
                startIndex += batchSize;
            }
            getWords(searchLetter);
            textPane.setDocument(htmlDocument.updatePage(words));
            pane.getVerticalScrollBar().setValue(0);
            prevButton.setEnabled(true);
        });
        nextButton.setPreferredSize(new Dimension(25,25));
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        navPanel.add(buttonPanel, BorderLayout.WEST);
        navPanel.add(searchPanel, BorderLayout.EAST);

        toolBar.add(letterPanel, BorderLayout.NORTH);
        toolBar.add(navPanel, BorderLayout.CENTER);
        this.add(toolBar, BorderLayout.NORTH);
    }

    private void createScrollPane() {
        if (words.isEmpty()) {
            emptyLabel();
        } else {
            htmlDocument = new ConcordanceHTMLDocument(words,textSize);
            textPane = new JTextPane();
            textPane.setContentType("text/html");
            textPane.setEditable(false);
            textPane.setDocument(htmlDocument.createDocument());

            pane = new JScrollPane();
            pane.setPreferredSize(this.getPreferredSize());
            pane.setViewportView(textPane);
            this.add(pane, BorderLayout.CENTER);
        }
    }

    private void emptyLabel() {
        JLabel label = new JLabel("Nothing matches your search");
        label.setHorizontalAlignment(JLabel.CENTER);
        this.add(label, BorderLayout.CENTER);
    }

    private ArrayList<String> createLetters() {
        ArrayList<String> letters = new ArrayList<>();
        letters.add("A"); letters.add("B"); letters.add("C"); letters.add("D");
        letters.add("E"); letters.add("F"); letters.add("G"); letters.add("H");
        letters.add("I"); letters.add("J"); letters.add("K"); letters.add("L");
        letters.add("M"); letters.add("N"); letters.add("O"); letters.add("P");
        letters.add("Q"); letters.add("R"); letters.add("S"); letters.add("T");
        letters.add("U"); letters.add("V"); letters.add("W"); letters.add("X");
        letters.add("Y"); letters.add("Z");

        return letters;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textPane.setDocument(htmlDocument.setTextSize(textSize));
    }

    private void getWords(String search) {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            words = databaseConnection.getPagedWordByString(search, startIndex, batchSize);
            totalCount = databaseConnection.getTotalCountOfPagedWordByString(search);
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
