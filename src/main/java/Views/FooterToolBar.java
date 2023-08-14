package main.java.Views;

import main.java.Data.Chapter;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class FooterToolBar extends JToolBar {

    private String path;
    private ReaderPanel parentPanel;
    private JButton nextPage;
    private JButton prevPage;
    private Long bibleId;
    private Long book;
    private Long chapter;
    private Vector<Chapter> chapters = new Vector<>();
    private Vector<Chapter> prevChapters = new Vector<>();

    public FooterToolBar(ReaderPanel readerPanel, Long bibleId, Long book, Long chapter) {
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        this.path = programDirectoryService.getProgramDirectory();
        this.parentPanel = readerPanel;
        this.bibleId = bibleId;
        this.book = book;
        this.chapter = chapter;

        getChapters();

        createStatusPane();
        createPageButtons();
        createTextSizeButtons();

        this.setFloatable(false);

    }

    private void createTextSizeButtons() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(parentPanel.getWidth() / 3, this.getHeight()));
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton smaller = new JButton();
        Icon minus = new ImageIcon(path + "/Resources/icons8-minus-24.png");
        smaller.setIcon(minus);
        smaller.setToolTipText("Font size smaller");
        smaller.setPreferredSize(new Dimension(25,25));
        smaller.addActionListener(e -> {
            resizeText(true);
        });
        JButton larger = new JButton();
        Icon plus = new ImageIcon(path + "/Resources/icons8-plus-24.png");
        larger.setPreferredSize(new Dimension(25,25));
        larger.setIcon(plus);
        larger.setToolTipText("Font size larger");
        larger.addActionListener(e -> {
            resizeText(false);
        });
        panel.add(smaller);
        panel.add(larger);
        this.add(panel);
    }

    private void createPageButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        nextPage = new JButton();
        nextPage.setIcon(new ImageIcon(path + "/Resources/icons8-arrow-64.png"));
        nextPage.setPreferredSize(new Dimension(25, 25));
        nextPage.setToolTipText("Next page");
        if (book == 66 && chapter == 22)
            nextPage.setEnabled(false);
        else
            nextPage.setEnabled(true);
        nextPage.addActionListener(event -> {
            navigateChapters(true);
        });

        prevPage = new JButton();
        prevPage.setIcon(new ImageIcon(path + "/Resources/icons8-arrow-64-left.png"));
        prevPage.setPreferredSize(new Dimension(25, 25));
        prevPage.setToolTipText("Previous page");
        if (book == 1L && chapter == 1L)
            prevPage.setEnabled(false);
        else
            prevPage.setEnabled(true);
        prevPage.addActionListener(event -> {
            navigateChapters(false);
        });

        panel.add(prevPage);
        panel.add(nextPage);
        this.add(panel);
    }

    private void createStatusPane() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(parentPanel.getWidth() / 3, this.getHeight()));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        this.add(panel);
    }

    private void resizeText(boolean isSmaller) {
        int currentSize = parentPanel.getTextSize();
        int size;
        if (isSmaller)
            size = currentSize - 2;
        else
            size = currentSize + 2;

        parentPanel.setTextSize(size);
    }

    public void updateBookAndChapter(Long book, Long chapter) {
        this.book = book;
        this.chapter = chapter;

        getChapters();

        if (book == 1 && chapter == 1) {
            prevPage.setEnabled(false);
            nextPage.setEnabled(true);
        }
        else if (book == 66 && chapter == 22) {
            nextPage.setEnabled(false);
            prevPage.setEnabled(true);
        }
        else {
            prevPage.setEnabled(true);
            nextPage.setEnabled(true);
        }
    }

    private void getChapters() {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            chapters = connection.getChapters(bibleId, book);
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Retrieving Chapters", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void navigateChapters(boolean nextPage) {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            if (book > 1 && chapter == 1) {
                prevChapters = connection.getChapters(bibleId, book - 1L);
            }
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Retrieving Chapters", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (nextPage) {
            if (chapter == chapters.get(chapters.size() - 1).getChapterId()) {
                parentPanel.setSearchFields(bibleId, book + 1, 1L);
            } else {
                parentPanel.setSearchFields(bibleId, book, chapter + 1L);
            }
        } else {
            if (chapter == 1L) {
                parentPanel.setSearchFields(bibleId, book - 1, prevChapters.get(prevChapters.size() - 1).getChapterId());
            } else {
                parentPanel.setSearchFields(bibleId, book, chapter - 1L);
            }
        }
    }
}
