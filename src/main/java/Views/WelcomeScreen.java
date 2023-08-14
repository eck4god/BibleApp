package main.java.Views;

import main.java.Service.ProgramDirectoryService;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {

    ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
    public WelcomeScreen() {
        this.setUndecorated(true);
        this.setSize(new Dimension(648, 348));
        this.setResizable(false);
        this.setLocationRelativeTo(null);


        createBackground();
    }

    private void createBackground() {


        String path = programDirectoryService.getProgramDirectory();

        ImageIcon img = new ImageIcon(path+"/Resources/Images/bible.jpg");

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
        JLabel title = new JLabel("Bible Reader");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        textPanel.add(title, BorderLayout.CENTER);

        panel.add(textPanel);
        this.getContentPane().add(panel);
    }
}
