package main.java.Service;

import main.java.Data.Book;
import main.java.Data.Chapter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class JTreeRenderer extends DefaultTreeCellRenderer {
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
}
