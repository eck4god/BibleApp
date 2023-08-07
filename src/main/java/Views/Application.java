package main.java.Views;
import main.java.Service.ProcessJSON;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class Application extends JFrame {

    File selectedFile;
    ReaderPanel readerPanel;
    public Application() {}
    public boolean setUpFrame() {
        BorderLayout layout = new BorderLayout();
        layout.maximumLayoutSize(this);
        this.setTitle("Bible Application");
        this.setMinimumSize(new Dimension(1024, 768));
        this.setSize(800,600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(layout);
        menuBar();
        toolbar();
        NavigationPane navigationPane = new NavigationPane(this);
        navigationPane.setVisible(true);
        readerPanel = new ReaderPanel((long)1, (long)1, (long)1);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigationPane, readerPanel);
        this.getContentPane().add(splitPane);
        this.pack();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                System.exit(0);
            }
        });

        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler((e, response) -> {
                boolean canQuit = true; // Enables or Disables Menu Quit command on Mac OS X
                if (canQuit) {
                    response.performQuit();
                } else
                    response.cancelQuit();
            });
        }
        return true;
    }
    private void menuBar() {
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        menu.add(file);
        this.setJMenuBar(menu);
        this.add(menu);

    }

    private void toolbar() {
        JToolBar toolBar = new JToolBar();
        JButton importBook = new JButton("Import");
        importBook.addActionListener(event -> {
            importDialog();
        });
        toolBar.add(importBook);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        this.getContentPane().add(toolBar, BorderLayout.NORTH);
    }

    private void importDialog() {
        JDialog jDialog = new JDialog(this, "Import Bible");
        jDialog.setLayout(new BorderLayout());
        jDialog.setSize(400, 100);
        jDialog.setLocation((this.getWidth() / 2) + this.getX() - 200, (this.getHeight() / 2) + this.getY() - 50);
        createSelectFilePane(jDialog);
        jDialog.setVisible(true);
    }

    private void createSelectFilePane(JDialog dialog) {
        JButton uploadButton = new JButton("Upload");
        // File Chooser pop-up
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("json", "json"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") +
                System.getProperty("file.separator") + "BibleApp"));

        // Panel for selection of file
        JPanel fileSelectPanel = new JPanel();
        JButton button = new JButton("Select File");
        JLabel label = new JLabel();
        label.setText("No file is selected");
        button.addActionListener(event -> {
            int result = fileChooser.showDialog(this, "Select");
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                label.setText(selectedFile.getName());
                uploadButton.setEnabled(true);
            }
        });
        fileSelectPanel.add(button);
        fileSelectPanel.add(label);

        // panel for Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(10,10,10,10));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        uploadButton.setEnabled(false);
        uploadButton.setOpaque(true);
        uploadButton.setBorderPainted(false);
        uploadButton.setBackground(Color.BLUE);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.addActionListener(event -> {
            processUpload();
            dialog.setVisible(false);
            dialog.dispose();
        });
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.add(uploadButton, BorderLayout.EAST);

        dialog.getContentPane().add(fileSelectPanel, BorderLayout.NORTH);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void processUpload() {
        //  TO DO: Add logic to process uploaded file
        ProcessJSON processJSON = new ProcessJSON(selectedFile);
        try {
            int complete = processJSON.saveBibleToDatabase(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void navigateTo(Long book, Long chapter) {
        readerPanel.setSearchFields(1L, book, chapter);
    }
}
