package main.java.Views;

import main.java.Data.Word;
import main.java.Service.ConcordanceHTMLDocument;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

public class ConcordanceWindow extends JFrame {

    private Application application;
    private final String path;
    private int textSize;
    private int startIndex = 0;
    private int batchSize = 10;
    private int totalCount = 0;
    private JSplitPane splitPane;
    private JPanel readerPanel;
    private JTextPane textPane;
    private JScrollPane pane;
    private JButton nextButton;
    private JButton prevButton;
    private ConcordanceHTMLDocument htmlDocument;
    private String search = "A";
    private Vector<Word> words;


    public ConcordanceWindow(Application application, int textSize) {
        this.application = application;
        this.textSize = textSize;
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(1004,768));
        this.setSize(new Dimension(1024,768));
        this.setLocationRelativeTo(application);

        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();

        getWords(search);
        setupReaderView();
    }

    private void setupReaderView() {

        readerPanel = new JPanel();
        readerPanel.setLayout(new BorderLayout());
        readerPanel.add(addToolbar(), BorderLayout.NORTH);
        readerPanel.add(addReaderPane(), BorderLayout.CENTER);
        readerPanel.add(addFooterBar(), BorderLayout.SOUTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigationPane(), readerPanel);
        this.getContentPane().add(splitPane);
    }

    private JToolBar addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new BorderLayout());
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        // Make Search Box
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JTextField search = new JTextField(8);
        search.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        searchPanel.add(search);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        toolBar.add(searchPanel, BorderLayout.CENTER);

        return toolBar;
    }

    private JToolBar addFooterBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        nextButton = new JButton();
        prevButton = new JButton();
        prevButton.setIcon(new ImageIcon(path + "/Resources/Icons/leftArrow.png"));
        prevButton.setPreferredSize(new Dimension(25,25));
        prevButton.setToolTipText("Previous Page");
        prevButton.setEnabled(false);
        prevButton.addActionListener(event -> {
            if (startIndex - batchSize <= 0) {
                startIndex = 0;
                if (search.equals(createLetters().get(0)) || search.length() > 1) {
                    prevButton.setEnabled(false);
                } else {
                    search = createLetters().get(createLetters().indexOf(search) - 1);
                    try {
                        DatabaseConnection databaseConnection = new DatabaseConnection();
                        totalCount = databaseConnection.getTotalCountOfPagedWordByString(search);
                        databaseConnection.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startIndex = (int) Math.ceil((totalCount / 10)) * 10;
                }
            } else {
                startIndex -= batchSize;
            }
            getWords(search);
            textPane.setDocument(htmlDocument.updatePage(words));
            pane.getVerticalScrollBar().setValue(0);
            nextButton.setEnabled(true);
        });

        nextButton.setIcon(new ImageIcon(path + "/Resources/Icons/rightArrow.png"));
        nextButton.setPreferredSize(new Dimension(25,25));
        nextButton.setToolTipText("Next Page");
        nextButton.addActionListener(event -> {
            if (startIndex + batchSize > totalCount) {
                if (createLetters().contains(search)) {
                    // If Search is done by letter
                    startIndex = 0;
                    if (search.equals(createLetters().get(createLetters().size() - 1))) {
                        nextButton.setEnabled(false);
                    } else {
                        search = createLetters().get(createLetters().indexOf(search) + 1);
                    }
                } else {
                    nextButton.setEnabled(false);
                }
            } else {
                startIndex += batchSize;
            }
            getWords(search);
            textPane.setDocument(htmlDocument.updatePage(words));
            pane.getVerticalScrollBar().setValue(0);
            prevButton.setEnabled(true);
        });

        toolBar.add(prevButton);
        toolBar.add(nextButton);

        return toolBar;
    }

    private JPanel addReaderPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Set up Text Pane
        htmlDocument = new ConcordanceHTMLDocument(words, textSize);
        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setDocument(htmlDocument.createDocument());
        pane = new JScrollPane();
        pane.setViewportView(textPane);

        panel.add(pane);
        return panel;
    }

    private JPanel navigationPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.setPreferredSize(new Dimension(200, 600));
        panel.setMinimumSize(new Dimension(200, 600));

        // Create tree view
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Letters");
        JTree tree = new JTree(rootNode);
        for (String letter : createLetters()) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(letter);
            rootNode.add(treeNode);

            Vector<Word> w = new Vector<>();
            try {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                w = databaseConnection.getWordByString(letter);
                databaseConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Word word : w) {
                treeNode.add(new DefaultMutableTreeNode(word));
            }
        }
        tree.setRootVisible(false);
        tree.expandPath(new TreePath(tree.getModel().getRoot()));
        tree.setScrollsOnExpand(true);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof String) {
                    String obj = (String) userObject;
                    this.setText(obj);
                } else if (userObject instanceof Word) {
                    Word word = (Word) userObject;
                    this.setText(word.getWord());
                }

                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        panel.add(scrollPane);
        return panel;
    }

    private void getWords(String s) {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            words = databaseConnection.getPagedWordByString(s, startIndex, batchSize);
            totalCount = databaseConnection.getTotalCountOfPagedWordByString(s);
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> createLetters() {
        ArrayList<String> letters = new ArrayList<>();
        letters.add("A"); letters.add("B"); letters.add("C"); letters.add("D");
        letters.add("E"); letters.add("F"); letters.add("G"); letters.add("H");
        letters.add("I"); letters.add("J"); letters.add("K"); letters.add("L");
        letters.add("M"); letters.add("N"); letters.add("O"); letters.add("P");
        letters.add("Q"); letters.add("R"); letters.add("S"); letters.add("T");
        letters.add("U"); letters.add("V"); letters.add("W");
        letters.add("Y"); letters.add("Z");

        return letters;
    }
}
