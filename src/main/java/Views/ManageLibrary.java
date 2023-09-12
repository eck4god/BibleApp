package main.java.Views;

import main.java.Data.*;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProcessJSON;
import main.java.Service.ProgramDirectoryService;
import main.java.Service.Progress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ManageLibrary extends JDialog {

    private final Application application;
    private final String path;
    private Vector<Bible> installedBibles;
    private Vector<BibleName> availableBibles;
    private Vector<Materials> installedResources;
    private Vector<References> availableResources;

    public ManageLibrary(Application application, Vector<Bible> installedBibles, Vector<Materials> installedResources) {
        this.application = application;
        this.installedBibles = installedBibles;
        this.installedResources = installedResources;
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();
        buildAvailableBibleList();
        buildAvailableResourceList();

        // Setup Dialog Box
        this.setTitle("Manage Library");
        this.setMinimumSize(new Dimension(600, 400));
        this.setResizable(false);
        this.setLocationRelativeTo(application);

        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JLabel bibleLabel = new JLabel("Manage Bibles");
        bibleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        bibleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        bibleLabel.setForeground(Color.BLUE);
        layout.add(bibleLabel);
        layout.add(manageBiblePanel());

        JLabel refLabel = new JLabel("Manage Reference Materials");
        refLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        refLabel.setForeground(Color.BLUE);
        refLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));

        layout.add(refLabel);
        layout.add(manageReferencePanel());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
        });
        buttonPanel.add(closeButton);
        layout.add(buttonPanel);

        this.getContentPane().add(layout);
    }

    private JPanel manageBiblePanel() {
        // Create Layout Panel
        JPanel layout = new JPanel();
        layout.setLayout(new FlowLayout(FlowLayout.CENTER));
        layout.setPreferredSize(new Dimension(this.getWidth(), 100));

        // Create Components so they are available in all components
        JButton addButton = new JButton();
        JButton removeButton = new JButton();
        JList availableList = new JList(availableBibles);

        // Create Installed List
        JPanel installedListPanel = new JPanel();
        installedListPanel.setLayout(new BorderLayout());
        JLabel label = new JLabel("Installed Bibles");
        JList installedList = new JList(installedBibles);
        installedList.setLayoutOrientation(JList.VERTICAL);
        installedList.setVisibleRowCount(-1);
        installedList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Bible) {
                    Bible bible = (Bible) value;
                    this.setText(bible.getBibleName());
                }
                return this;
            }
        });
        installedList.addListSelectionListener(event -> {
            availableList.clearSelection();
            if (!installedList.isSelectionEmpty()) {
                removeButton.setEnabled(true);
            } else {
                removeButton.setEnabled(false);
            }
        });
        JScrollPane pane = new JScrollPane(installedList);
        pane.setPreferredSize(new Dimension(200, 100));

        installedListPanel.add(pane, BorderLayout.CENTER);
        installedListPanel.add(label, BorderLayout.NORTH);
        layout.add(installedListPanel);

        // Create Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel addPanel = new JPanel();
        addButton.setIcon(new ImageIcon(path + "/Resources/Icons/leftArrow.png"));
        addButton.setEnabled(false);
        addButton.addActionListener(event -> {
            BibleName bibleName = (BibleName) availableList.getSelectedValue();
            int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to install " + bibleName.toString(),
                    bibleName.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == 0) {
                ProcessJSON processJSON = new ProcessJSON(new File(path + "/Resources/Bibles/" + bibleName.toFileName()));
                try {
                    processJSON.saveBibleToDatabase(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "There was a problem adding " + bibleName,
                            "Error Adding Bible", JOptionPane.ERROR_MESSAGE);
                }
            }
            availableList.clearSelection();
            updateBibles();
            buildAvailableBibleList();
            installedList.setListData(installedBibles);
            availableList.setListData(availableBibles);
        });
        addPanel.add(addButton);

        JPanel removePanel = new JPanel();
        removeButton.setIcon(new ImageIcon(path + "/Resources/Icons/rightArrow.png"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(event -> {
            if (installedBibles.size() == 1) {
                JOptionPane.showMessageDialog(this, "At least one Bible must be installed",
                        "Unable to Remove", JOptionPane.ERROR_MESSAGE);
            } else {
                Bible b = (Bible) installedList.getSelectedValue();
                int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + b.getBibleName(),
                        "Are You Sure", JOptionPane.YES_NO_OPTION);
                if (answer == 0) {
                    removeBible(b);
                }
            }
            installedList.clearSelection();
            updateBibles();
            buildAvailableBibleList();
            installedList.setListData(installedBibles);
            availableList.setListData(availableBibles);
        });
        removePanel.add(removeButton);

        buttonPanel.add(addPanel);
        buttonPanel.add(removePanel);
        layout.add(buttonPanel);

        // Create Available List
        JPanel availablePanel = new JPanel();
        availablePanel.setLayout(new BorderLayout());
        JLabel availableLabel = new JLabel("Available Bibles");

        availableList.setLayoutOrientation(JList.VERTICAL);
        availableList.setVisibleRowCount(-1);
        availableList.addListSelectionListener(event -> {
            installedList.clearSelection();
            if (!availableList.isSelectionEmpty()) {
                addButton.setEnabled(true);
            } else {
                addButton.setEnabled(false);
            }
        });
        JScrollPane availablePane = new JScrollPane(availableList);
        availablePane.setPreferredSize(new Dimension(200,100));

        availablePanel.add(availablePane, BorderLayout.CENTER);
        availablePanel.add(availableLabel, BorderLayout.NORTH);
        layout.add(availablePanel);

        return layout;
    }

    private JPanel manageReferencePanel() {
        JPanel layout = new JPanel();
        layout.setLayout(new FlowLayout(FlowLayout.CENTER));
        layout.setPreferredSize(new Dimension(this.getWidth(), 100));

        // Create Components so they are available from all components
        JButton addButton = new JButton();
        JButton removeButton = new JButton();
        JList availableResource = new JList<>(availableResources);

        // Create installed list
        JPanel installedPanel = new JPanel();
        installedPanel.setLayout(new BorderLayout());
        JLabel installedLabel = new JLabel("Installed Resources");
        JList installedResource = new JList<>(installedResources);
        installedResource.setLayoutOrientation(JList.VERTICAL);
        installedResource.setVisibleRowCount(-1);
        installedResource.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Materials) {
                    Materials materials = (Materials) value;
                    this.setText(materials.getName());
                }

                return this;
            }
        });
        installedResource.addListSelectionListener(event -> {
            availableResource.clearSelection();
            if (!installedResource.isSelectionEmpty()) {
                removeButton.setEnabled(true);
            } else {
                removeButton.setEnabled(false);
            }
        });
        JScrollPane pane = new JScrollPane(installedResource);
        pane.setPreferredSize(new Dimension(200,100));

        installedPanel.add(installedLabel, BorderLayout.NORTH);
        installedPanel.add(pane, BorderLayout.CENTER);

        layout.add(installedPanel);

        // Create Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel addPanel = new JPanel();
        addButton.setIcon(new ImageIcon(path + "/Resources/Icons/leftArrow.png"));
        addButton.setEnabled(false);
        addButton.addActionListener(event -> {
            References references = (References) availableResource.getSelectedValue();
            int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to install " + references.toString(),
                    references.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == 0) {
                ProcessJSON processJSON = new ProcessJSON(references.toFiles());
                try {
                    processJSON.addConcordance(this, references.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "There was a problem installing " + references.toString(),
                            "There was a problem", JOptionPane.ERROR_MESSAGE);
                }
                updateMaterials();
                for (Materials material : installedResources) {
                    if (material.getName().equals(availableResource.getSelectedValue().toString())) {
                        application.createConcordanceTab(material);
                    }
                }
            }
            availableResource.clearSelection();
            updateMaterials();
            buildAvailableResourceList();
            installedResource.setListData(installedResources);
            availableResource.setListData(availableResources);
        });
        addPanel.add(addButton);

        JPanel removePanel = new JPanel();
        removeButton.setIcon(new ImageIcon(path + "/Resources/Icons/rightArrow.png"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(event -> {
            Materials m = (Materials) installedResource.getSelectedValue();
            int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " +
                    m.getName(), "Are You Sure", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                removeConcordance((Materials) installedResource.getSelectedValue());
            }
            installedResource.clearSelection();
            updateMaterials();
            buildAvailableResourceList();
            installedResource.setListData(installedResources);
            availableResource.setListData(availableResources);
        });
        removePanel.add(removeButton);

        buttonPanel.add(addPanel);
        buttonPanel.add(removePanel);
        layout.add(buttonPanel);

        // Create Available Resources
        JPanel availablePanel = new JPanel();
        availablePanel.setLayout(new BorderLayout());
        JLabel availableLabel = new JLabel("Available Resources");
        availableResource.setLayoutOrientation(JList.VERTICAL);
        availableResource.setVisibleRowCount(-1);
        availableResource.addListSelectionListener(event -> {
            installedResource.clearSelection();
            if (!availableResource.isSelectionEmpty()) {
                addButton.setEnabled(true);
            } else {
                addButton.setEnabled(false);
            }
        });
        JScrollPane scrollPane = new JScrollPane(availableResource);
        scrollPane.setPreferredSize(new Dimension(200,100));

        availablePanel.add(availableLabel, BorderLayout.NORTH);
        availablePanel.add(scrollPane, BorderLayout.CENTER);

        layout.add(availablePanel);

        return layout;
    }

    private void buildAvailableBibleList() {
        availableBibles = new Vector<>();
        Vector<String> installedName = new Vector<>();
        for (Bible bible : installedBibles) {
            installedName.add(bible.getBibleName());
        }
        for (BibleName bibleName : BibleName.values()) {
            if (!bibleName.equals(BibleName.PLACEHOLDER)) {
                if (!installedName.contains(bibleName.toString())) {
                    availableBibles.add(bibleName);
                }
            }
        }
    }

    private void buildAvailableResourceList() {
        availableResources = new Vector<>();
        Vector<String> installedName = new Vector<>();
        for (Materials materials : installedResources) {
            installedName.add(materials.getName());
        }
        for (References references : References.values()) {
            if (!references.equals(References.Placeholder)) {
                if (!installedName.contains(references.toString())) {
                    availableResources.add(references);
                }
            }
        }
    }

    public void updateBibles() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            installedBibles = databaseConnection.getBibles();
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        application.getBibles();
    }

    public void updateMaterials() {
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            installedResources = databaseConnection.getMaterials();
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        application.getMaterials();
    }

    private void removeConcordance(Materials materials) {
        // Create Dialog Box
        JDialog dialog = new JDialog(this, "Removing " + materials.getName(), ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(new Dimension(400,130));
        dialog.setLocationRelativeTo(this);

        // Create Labels and Progress Bars
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("...");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setHorizontalAlignment(JLabel.LEFT);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BorderLayout());
        JLabel overallLabel = new JLabel("Overall Progress");
        overallLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        overallLabel.setHorizontalAlignment(JLabel.LEFT);
        JProgressBar overallProgress = new JProgressBar();
        overallProgress.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
        overallProgress.setVisible(true);
        overallProgress.setStringPainted(true);
        overallProgress.setValue(0);
        overallPanel.add(overallLabel, BorderLayout.NORTH);
        overallPanel.add(overallProgress, BorderLayout.CENTER);

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.setBorder(new EmptyBorder(10,10,10,10));
        layout.add(panel, BorderLayout.NORTH);
        layout.add(overallPanel, BorderLayout.CENTER);

        dialog.add(layout, BorderLayout.CENTER);

        SwingWorker<Void, Progress> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                for (int i = 0; i < createLetters().size(); i++) {
                    Vector<Word> words;
                    words = databaseConnection.getWordByString(createLetters().get(i), materials.getMaterialsId());

                    for (int j = 0; j < words.size(); j++) {
                        Word word = words.get(j);
                        Vector<Reference> references = databaseConnection.getReferenceByWordId(word.getWordId());
                        publish(new Progress(word.getWord(), i, j, createLetters().size(), words.size()));

                        for (int k = 0; k < references.size(); k++) {
                            Reference reference = references.get(k);
                            databaseConnection.deleteReference(reference.getReferenceId());
                        }
                        databaseConnection.deleteWord(word.getWordId());
                    }
                }
                databaseConnection.deleteMaterials(materials.getMaterialsId());
                databaseConnection.close();
                return null;
            }

            @Override
            protected void process(List<Progress> chunks) {
                Progress chunk = chunks.get(chunks.size() - 1);
                overallProgress.setMaximum(chunk.getMaxFile());
                progressBar.setMaximum(chunk.getMaxWord());
                label.setText(chunk.getLabel());
                overallProgress.setValue(chunk.getOverallProgress());
                progressBar.setValue(chunk.getInnerProgress());
            }

            @Override
            protected void done() {
                application.removeConcordancePane();
                dialog.setVisible(false);
            }
        };

        try {
            worker.execute();
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeBible(Bible bible) {
        // Setup Dialog Box
        JDialog dialog = new JDialog(this, "Removing " + bible.getBibleName() , ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(new Dimension(400,130));
        dialog.setLocationRelativeTo(this);

        // Create Label and Progress Bar
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("...");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setHorizontalAlignment(JLabel.LEFT);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BorderLayout());
        JLabel overallLabel = new JLabel("Overall Progress");
        overallLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        overallLabel.setHorizontalAlignment(JLabel.LEFT);
        JProgressBar overallProgress = new JProgressBar();
        overallProgress.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
        overallProgress.setVisible(true);
        overallProgress.setStringPainted(true);
        overallProgress.setValue(0);
        overallPanel.add(overallLabel, BorderLayout.NORTH);
        overallPanel.add(overallProgress, BorderLayout.CENTER);

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.setBorder(new EmptyBorder(10,10,10,10));
        layout.add(panel, BorderLayout.NORTH);
        layout.add(overallPanel, BorderLayout.CENTER);

        dialog.add(layout, BorderLayout.CENTER);

        SwingWorker<Void, Progress> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                Vector<Book> books = databaseConnection.getBooks();
                for (int i = 0; i < books.size(); i++) {
                    Book book = books.get(i);
                    Vector<Chapter> chapters = databaseConnection.getChapters(bible.getBibleId(), book.getBookNumber());
                    for (int j = 0; j < chapters.size(); j++) {
                        Chapter chapter = chapters.get(j);
                        Vector<BibleLink> bibleLinks = databaseConnection.getBibleLink(bible.getBibleId(), book.getBookNumber(), chapter.getChapterId());
                        publish(new Progress(book.getBookTitle(), i, j, books.size(), chapters.size()));
                        for (int k = 0; k < bibleLinks.size(); k++) {
                            Vector<Notes> notes = databaseConnection.getNotes(bible.getBibleId(), book.getBookNumber(), chapter.getChapterId());
                            for (Notes note : notes) {
                                databaseConnection.deleteNotes(note.getNoteId());
                            }
                            BibleLink bibleLink = bibleLinks.get(k);
                            databaseConnection.deleteVerse(bibleLink.getVerse().getVerseId());
                            try {
                                databaseConnection.deleteBibleLink(bibleLink.getBibleLinkId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                databaseConnection.deleteBible(bible.getBibleId());
                databaseConnection.close();
                return null;
            }

            @Override
            protected void process(List<Progress> chunks) {
                Progress chunk = chunks.get(chunks.size() - 1);
                overallProgress.setMaximum(chunk.getMaxFile());
                progressBar.setMaximum(chunk.getMaxWord());
                label.setText(chunk.getLabel());
                progressBar.setValue(chunk.getInnerProgress());
                overallProgress.setValue(chunk.getOverallProgress());
            }

            @Override
            protected void done() {
                application.removeReaderPanelTab(bible.getBibleId());
                dialog.setVisible(false);
            }
        };
        try {
            worker.execute();
            dialog.setVisible(true);
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
