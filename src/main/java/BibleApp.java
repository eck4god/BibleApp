package main.java;

/*
Copyright 2023 Gregory Echelberry
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.
    THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
    LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
    IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
    OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
import main.java.Service.DatabaseConnection;
import main.java.Service.ProcessJSON;
import main.java.Service.ProgramDirectoryService;
import main.java.Views.Application;
import main.java.Views.WelcomeScreen;

import javax.swing.*;
import java.io.File;


public class BibleApp {

    public static void main(String[] args) {

        if (System.getProperty("os.name").equals("Mac OS X")) {
            System.setProperty( "apple.awt.application.appearance", "system" );
            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
            System.setProperty( "apple.awt.application.name", "Bibles" );
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Gets program absolute path
        ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
        String path = programDirectoryService.getProgramDirectory();

        String version = "";
        ProcessJSON config = new ProcessJSON(new File(path + "/Resources/config.json"));
        try {
            version = config.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        WelcomeScreen welcomeScreen = new WelcomeScreen(version);
        welcomeScreen.setVisible(true);

        boolean firstRun = false;
        int dbComplete;

        try {
            firstRun = config.isFirstRun();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(welcomeScreen, "There was a problem loading configuration", "System Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (firstRun) {
            try {
                DatabaseConnection connection = new DatabaseConnection();
                dbComplete = connection.setUpDatabase();
                connection.close();
                ProcessJSON processJSON = new ProcessJSON(new File(path + "/Resources/Bibles/kjv.Json"));
                int complete = processJSON.saveBibleToDatabase(null);
                config.setFirstRun();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(welcomeScreen, "There was and error setting up data structure", "System Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        }

        SwingUtilities.invokeLater(() -> {
            Application application = new Application();
            boolean setupComplete = application.setUpFrame();

            if (setupComplete) {
                welcomeScreen.setVisible(false);
                welcomeScreen.dispose();
                application.setVisible(true);
            }
        });
    }
}
