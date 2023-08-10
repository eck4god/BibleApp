package main.java.Views;

import main.java.Data.Book;
import main.java.Data.Chapter;
import main.java.Service.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Vector;

public class NavigationPane extends JPanel {

    Application parentFrame;
    Vector<Book> books = new Vector<>();

    public NavigationPane(Application parentFrame) {
        this.parentFrame = parentFrame;
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(200, 600));
        this.setBorder(new EmptyBorder(10,10,10,10));
        getBooks();
        createNavigationScrollPane();
    }

    private void createNavigationScrollPane() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Bible");
        JTree tree = new JTree(rootNode);

        for (Book book : books) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(book);
            rootNode.add(treeNode);
            try {
                DatabaseConnection connection = new DatabaseConnection();
                Vector<Chapter> chapters;
                chapters = connection.getChapters(1L, book.getBookNumber());
                for (Chapter chapter : chapters) {
                    treeNode.add(new DefaultMutableTreeNode(chapter));
                }
                connection.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error connecting with database", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        tree.setCellRenderer(
                // creating TreeRender class here and passed

                new DefaultTreeCellRenderer() {
                    @Override
                    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                        Object userObject = node.getUserObject();


                        if (userObject instanceof Book) {
                            Book book = (Book) userObject;
                            this.setText(book.getBookTitle());
                        } else if (userObject instanceof Chapter) {
                            Chapter chapter = (Chapter) userObject;
                            this.setText(chapter.getDisplayName());
                        }

                        return this;
                    }
                });

        tree.expandPath(new TreePath(tree.getModel().getRoot()));
        tree.setRootVisible(false);
        tree.setScrollsOnExpand(true);
        tree.getSelectionModel().addTreeSelectionListener(event -> {
            TreePath path = event.getPath();
            if (path.getPathCount() == 2) {
                if (tree.isCollapsed(path)) {
                    tree.expandPath(path);
                } else {
                    tree.collapsePath(path);
                }
            }
            else if (path.getPathCount() == 3) {
                Chapter chapter = null;
                Book book = null;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getPathComponent(1);
                Object userObject = node.getUserObject();
                Object bookObject = parentNode.getUserObject();

                if (userObject instanceof Chapter) {
                    chapter = (Chapter) userObject;
                }
                if (bookObject instanceof Book) {
                    book = (Book) bookObject;
                }

                if (chapter != null && book != null) {
                    parentFrame.navigateTo(book.getBookNumber(), chapter.getChapterId());
                }
            }
        });
        JScrollPane pane = new JScrollPane(tree);
        this.add(pane, BorderLayout.CENTER);
    }

    private void getBooks() {
        try {
            DatabaseConnection connection = new DatabaseConnection();
            books = connection.getBooks();
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting with database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
