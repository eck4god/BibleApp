package main.java.Views;

import main.java.Service.ProgramDirectoryService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;
import java.security.spec.ECField;

public class TextEditor extends JPanel implements DocumentListener {

    private final Color activeColor = Color.CYAN;
    private final Color inactiveColor = Color.WHITE;
    private String path;
    private JEditorPane editorPane;
    private HTMLEditorKit htmlEditorKit;
    private HTMLDocument doc;
    private JToolBar toolBar;
    private JButton italicButton;
    private JButton boldButton;
    private JButton underlineButton;
    private SimpleAttributeSet attributeSet = new SimpleAttributeSet();
    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderline = false;
    private JButton colorButton;
    private Color textColor = Color.BLACK;
    private int textSize;

    public TextEditor(int textSize) {
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        this.textSize = textSize;
        this.setLayout(new BorderLayout());
        createToolbar();
        htmlEditorKit = new HTMLEditorKit();
        StyleSheet ss = htmlEditorKit.getStyleSheet();
        ss.addRule("body {font-size: " + textSize + "px}");
        doc = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        createEditor();

        // Set Up Styles
        StyleConstants.setBold(attributeSet, false);
        StyleConstants.setItalic(attributeSet, false);
        StyleConstants.setUnderline(attributeSet, false);
        StyleConstants.setForeground(attributeSet, textColor);
    }

    public TextEditor(int textSize, String html) {
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        this.textSize = textSize;
        this.setLayout(new BorderLayout());
        createToolbar();

        // Recreate old note to edit
        Reader reader = new StringReader(html);
        htmlEditorKit = new HTMLEditorKit();
        doc = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        try {
            htmlEditorKit.read(reader, doc, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        createEditor();

        // Set Up Styles
        StyleConstants.setBold(attributeSet, false);
        StyleConstants.setItalic(attributeSet, false);
        StyleConstants.setUnderline(attributeSet, false);
        StyleConstants.setForeground(attributeSet, textColor);
    }

    private void createEditor() {
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditorKit(htmlEditorKit);
        editorPane.setDocument(doc);
        editorPane.getDocument().addDocumentListener(this);
        editorPane.addCaretListener(event -> getFormat(event.getDot()));

        this.add(editorPane, BorderLayout.CENTER);
    }

    private void createToolbar() {
        toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        // Bold button
        boldButton = new JButton();
        boldButton.setIcon(new ImageIcon(path + "/Resources/Icons/bold.png"));
        boldButton.setPreferredSize(new Dimension(18, 18));
        boldButton.setToolTipText("Bold");
        boldButton.addActionListener(event -> {
            if (isBold) {
                StyleConstants.setBold(attributeSet, false);
                isBold = false;
                boldButton.setBackground(inactiveColor);
            } else {
                StyleConstants.setBold(attributeSet, true);
                isBold = true;
                boldButton.setBackground(activeColor);
            }
        });

        // Italic Button
        italicButton = new JButton();
        italicButton.setIcon(new ImageIcon(path + "/Resources/Icons/italic.png"));
        italicButton.setPreferredSize(new Dimension(18,18));
        italicButton.setToolTipText("Italic");
        italicButton.addActionListener(event -> {
            if (isItalic) {
                StyleConstants.setItalic(attributeSet, false);
                isItalic = false;
                italicButton.setBackground(inactiveColor);
            } else {
                StyleConstants.setItalic(attributeSet, true);
                isItalic = true;
                italicButton.setBackground(activeColor);
            }
        });

        // Underline Button
        underlineButton = new JButton();
        underlineButton.setIcon(new ImageIcon(path + "/Resources/Icons/underline.png"));
        underlineButton.setPreferredSize(new Dimension(18,18));
        underlineButton.setToolTipText("Underline");
        underlineButton.addActionListener(event -> {
            if (isUnderline) {
                StyleConstants.setUnderline(attributeSet, false);
                isUnderline = false;
                underlineButton.setBackground(inactiveColor);
            } else {
                StyleConstants.setUnderline(attributeSet, true);
                isUnderline = true;
                underlineButton.setBackground(activeColor);
            }
        });

        // Color Chooser
        colorButton = new JButton("Text Color");
        colorButton.setForeground(textColor);
        colorButton.setToolTipText("Text Color");

        colorButton.addActionListener(event -> {
//            JColorChooser colorChooser = new JColorChooser();
            textColor = JColorChooser.showDialog(this, "Choose a Text Color", textColor);
            StyleConstants.setForeground(attributeSet, textColor);
            colorButton.setForeground(textColor);
        });

        // Font Size Dropdown
        JLabel sizeLabel = new JLabel("Text Size");
        sizeLabel.setBorder(new EmptyBorder(0,10,0,10));
        Integer sizes[] = {12, 14, 16, 18, 20, 22, 24, 26};
        JComboBox<Integer> sizeBox = new JComboBox<>();
        sizeBox.setMaximumSize(new Dimension(75, sizeBox.getPreferredSize().height));
        for (Integer num : sizes) {
            sizeBox.addItem(num);
        }
        sizeBox.setSelectedItem(textSize);
        sizeBox.addActionListener(event -> {
            textSize = (int) sizeBox.getSelectedItem();
            StyleSheet ss = htmlEditorKit.getStyleSheet();
            ss.addRule("body {font-size: " + textSize + "px}");
            doc.setCharacterAttributes(0, doc.getLength(), attributeSet, false);
        });

        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);
        toolBar.addSeparator();
        toolBar.add(colorButton);
        toolBar.addSeparator();
        toolBar.add(sizeLabel);
        toolBar.add(sizeBox);
        this.add(toolBar, BorderLayout.NORTH);
    }

    public String getText() {
        String html = editorPane.getText();
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        String text = body.html();

        return text;
    }

    private void applyFormat(int offset, int length) {
        Runnable applyFormat = () -> doc.setCharacterAttributes(offset, length, attributeSet, false);
        SwingUtilities.invokeLater(applyFormat);
    }

    private void getFormat(int loc) {
        AttributeSet attrib = doc.getCharacterElement(loc).getAttributes();
        Object italic = attrib.getAttribute(StyleConstants.Italic);
        Object bold = attrib.getAttribute(StyleConstants.Bold);
        Object underline = attrib.getAttribute(StyleConstants.Underline);
        Object color = attrib.getAttribute(StyleConstants.Foreground);

        // Check if Bold
        if (bold != null) {
            if ("true".equals(bold.toString())) {
                isBold = true;
                StyleConstants.setBold(attributeSet, true);
                boldButton.setBackground(activeColor);
            } else if ("false".equals(bold.toString())) {
                isBold = false;
                StyleConstants.setBold(attributeSet, false);
                boldButton.setBackground(inactiveColor);
            }
        }

        // Check if is Italic
        if (italic != null) {
            if ("true".equals(italic.toString())) {
                isItalic = true;
                StyleConstants.setItalic(attributeSet, true);
                italicButton.setBackground(activeColor);
            } else if ("false".equals(italic.toString())) {
                isItalic = false;
                StyleConstants.setItalic(attributeSet, false);
                italicButton.setBackground(inactiveColor);
            }
        }

        // Check if Underlined
        if (underline != null) {
            if ("true".equals(underline.toString())) {
                isUnderline = true;
                StyleConstants.setUnderline(attributeSet, true);
                underlineButton.setBackground(activeColor);
            } else if ("false".equals(underline.toString())) {
                isUnderline = false;
                StyleConstants.setUnderline(attributeSet, false);
                underlineButton.setBackground(inactiveColor);
            }
        }

        // Check Color
        if (color != null) {
            StyleConstants.setForeground(attributeSet, (Color) color);
            textColor = (Color) color;
            colorButton.setForeground(textColor);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        applyFormat(e.getOffset(), e.getLength());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {

    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }
}
