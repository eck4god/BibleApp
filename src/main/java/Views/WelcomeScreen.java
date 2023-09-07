package main.java.Views;

import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {

    private final String path;
    private final String version;
    public WelcomeScreen(String version) {
        this.version = version;
        this.setUndecorated(true);
        this.setSize(new Dimension(648, 348));
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        path = programDirectoryService.getProgramDirectory();

        createBackground();
    }

    private void createBackground() {

        ImageIcon img = new ImageIcon(path+"/Resources/Images/bibles.jpg");

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img.getImage(), 0, 0, null);
            }
        };
        panel.setLayout(new OverlayLayout(panel));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setOpaque(false);
        JLabel version = new JLabel("Version: " + this.version);
        version.setHorizontalAlignment(SwingConstants.LEFT);
        version.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        textPanel.add(version, BorderLayout.SOUTH);

        panel.add(textPanel);
        this.getContentPane().add(panel);
    }
}
