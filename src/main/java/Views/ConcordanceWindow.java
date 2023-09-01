package main.java.Views;

import main.java.Data.Word;
import main.java.Service.ConcordanceHTMLDocument;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

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
        JTextField searchField = new JTextField(8);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> {
            if (searchField.getText().length() > 1) {
                search = searchField.getText();
                startIndex = 0;
                getWords(search);
                if (words.isEmpty() || words.size() < 11) {
                    nextButton.setEnabled(false);
                }
                textPane.setDocument(htmlDocument.updatePage(words));
                pane.getVerticalScrollBar().setValue(0);
            }
        });
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(event -> {
            if (search.length() > 1) {
                startIndex = 0;
                search = createLetters().get(0);
                getWords(search);
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

        toolBar.add(searchPanel, BorderLayout.CENTER);

        return toolBar;
    }

    private JToolBar addFooterBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new BorderLayout());
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        JPanel nevButton = new JPanel();
        nevButton.setLayout(new FlowLayout(FlowLayout.CENTER));
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
                if (search.length() > 1) {
                    nextButton.setEnabled(false);
                } else if (createLetters().contains(search)) {
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

        nevButton.add(prevButton);
        nevButton.add(nextButton);
        toolBar.add(nevButton, BorderLayout.CENTER);

        // Size Buttons
        JPanel sizeButtons = new JPanel();
        sizeButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton larger = new JButton();
        larger.addActionListener(event -> {
            textSize += 2;
            textPane.setDocument(htmlDocument.setTextSize(textSize));
        });
        larger.setIcon(new ImageIcon(path + "/Resources/Icons/plus.png"));
        larger.setPreferredSize(new Dimension(25,25));
        JButton smaller = new JButton();
        smaller.setIcon(new ImageIcon(path + "/Resources/Icons/minus.png"));
        smaller.setPreferredSize(new Dimension(25,25));
        smaller.addActionListener(event -> {
            textSize -= 2;
            textPane.setDocument(htmlDocument.setTextSize(textSize));
        });

        sizeButtons.add(smaller);
        sizeButtons.add(larger);
        toolBar.add(sizeButtons, BorderLayout.EAST);

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
        tree.getSelectionModel().addTreeSelectionListener(event -> {
            TreePath path = event.getPath();
            if (path.getPathCount() == 2) {
                if (tree.isCollapsed(path)) {
                    tree.expandPath(path);
                } else {
                    tree.collapsePath(path);
                }
            } else if (path.getPathCount() == 3) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getPathComponent(1);
                Word word = (Word) node.getUserObject();

                // Get Location of node and the start index
                int nodeIndex = parentNode.getIndex(node);
                startIndex = (int) Math.floor(nodeIndex / 10.0) * 10;
                search = parentNode.getUserObject().toString();
                getWords(search);
                textPane.setDocument(htmlDocument.updatePage(words));

                try {
                    String text = textPane.getDocument().getText(0, textPane.getDocument().getLength());
                    textPane.setCaretPosition(text.indexOf(word.getWord()));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                nextButton.setEnabled(true);
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
