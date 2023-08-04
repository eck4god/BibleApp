package main.java.Views;

import main.java.Data.Book;
import main.java.Data.Chapter;
import main.java.Service.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;

public class NavigationDropDown extends JToolBar {

    Vector<Book> books = new Vector<>();
    Vector<Chapter> chapters = new Vector<>();
    ReaderPanel parentPanel;
    JComboBox chapterSelection = new JComboBox<>();
    public NavigationDropDown(ReaderPanel panel) {
        parentPanel = panel;
        getBooks();
        this.setFloatable(false);
        JLabel bookLabel = new JLabel("Navigate To:");
        JComboBox bookSelection = new JComboBox();
        bookSelection.addItem("-- Select a Book --");
        for (Book book : books) {
            bookSelection.addItem(book.getBookTitle());
        }
        bookSelection.addActionListener(e -> {
            Long bookNumber = null;
            Long bibleNumb = 1L;
            for (Book book : books) {
                if (bookSelection.getSelectedItem().toString().equals(book.getBookTitle()))
                    bookNumber = book.getBookNumber();
            }
            getChapters(bibleNumb, bookNumber);
        });
        bookSelection.setBorder(new EmptyBorder(5, 5, 5, 5));

        chapterSelection.addItem("-- Select a Chapter --");
        //JLabel chapterLabel = new JLabel("Chapter");
        for (Chapter chapter : chapters) {
            chapterSelection.addItem(chapter.getDisplayName());
        }
        chapterSelection.setBorder(new EmptyBorder(5,5,5,5));

        JButton button = new JButton("Go");
        button.addActionListener(event -> {
            Long chapterId = null;
            Long bookId = null;
            if (bookSelection.getSelectedItem() != null) {
                for (Book book: books) {
                    if (bookSelection.getSelectedItem().toString().equals(book.getBookTitle()))
                        bookId = book.getBookNumber();
                }
            }
            if (chapterSelection.getSelectedItem() != null) {
                for (Chapter chapter : chapters) {
                    if (chapterSelection.getSelectedItem().toString().equals(chapter.getDisplayName()))
                        chapterId = chapter.getChapterId();
                }
            }
            bookSelection.setSelectedIndex(0);
            chapterSelection.removeAllItems();
            chapterSelection.addItem("-- Select a Chapter --");
            chapterSelection.setSelectedIndex(0);
            parentPanel.setSearchFields((long) 1, bookId, chapterId);
        });

        bookSelection.setSelectedItem(0);
        chapterSelection.setSelectedItem(0);

        this.add(bookLabel);
        this.add(bookSelection);
        //this.add(chapterLabel);
        this.add(chapterSelection);
        this.add(button);
    }

    private void getBooks() {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            books = connection.getBooks();
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentPanel, "There was an error retrieving Books", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void getChapters(Long bibleId, Long bookId) {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            chapters = connection.getChapters(bibleId, bookId);
            connection.close();
            chapterSelection.removeAllItems();
            for (Chapter chapter : chapters) {
                chapterSelection.addItem(chapter.getDisplayName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentPanel, "There was an error retrieving Chapters", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
