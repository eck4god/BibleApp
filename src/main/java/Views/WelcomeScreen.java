package main.java.Views;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {

    public WelcomeScreen() {
        this.setUndecorated(true);
        this.setSize(new Dimension(648, 348));
        this.setResizable(false);
        this.setLocationRelativeTo(null);


        createBackground();
    }

    private void createBackground() {
        ImageIcon img = new ImageIcon("./Resources/bible.jpg");

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
