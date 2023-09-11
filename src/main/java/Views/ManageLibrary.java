package main.java.Views;

import main.java.Data.*;
import main.java.Service.DatabaseConnection;
import main.java.Service.ProcessJSON;
import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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
                JOptionPane.showMessageDialog(this, "This feature is not set up", "Remove Bible", JOptionPane.QUESTION_MESSAGE);
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
            int answer = JOptionPane.showConfirmDialog(this, "Are you sure you wnat to install " + references.toString(),
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
                application.createConcordanceTab();
            }
        });
        addPanel.add(addButton);

        JPanel removePanel = new JPanel();
        removeButton.setIcon(new ImageIcon(path + "/Resources/Icons/rightArrow.png"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(event -> {
            JOptionPane.showMessageDialog(this, "This feature is not set up", "Not Set Up", JOptionPane.ERROR_MESSAGE);
            installedResource.clearSelection();
            updateMaterials();
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
}
