package main.java.Service;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import main.java.Data.Indexs;
import main.java.Data.Verse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;

public class ProcessJSON {

    JFrame parentFrame;
    File file;

    public ProcessJSON() {

    }

    public ProcessJSON(File file) {
        this.file = file;
    }

    public boolean isFirstRun() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        String firstRun = jsonObject.get("firstRun").toString();
        jsonObject.clear();
        if (firstRun.equals("true"))
            return true;
        else
            return false;
    }

    public void setFirstRun() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        jsonObject.put("firstRun", "false");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
        jsonObject.clear();
    }

    public int saveBibleToDatabase(JFrame parentFrame) throws Exception {
        // Progress bar frame
        JDialog jDialog = new JDialog(parentFrame, "Loading Bible", Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.setLayout(new BorderLayout());
        jDialog.setSize(400,100);
        jDialog.setLocation(parentFrame.getWidth() / 2 + parentFrame.getX() - 200, parentFrame.getHeight() / 2 + parentFrame.getY() - 50);
        JLabel label = new JLabel("Loading...");
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        JPanel barPanel = new JPanel();
        barPanel.add(progressBar);
        jDialog.getContentPane().add(labelPanel, BorderLayout.NORTH);
        jDialog.getContentPane().add(barPanel, BorderLayout.CENTER);

        // Processing of JSON file
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) object;
        JSONObject metaData = (JSONObject) jsonObject.get("metadata");
        JSONArray verseData = (JSONArray) jsonObject.get("verses");
        progressBar.setMaximum(verseData.size());
//        jDialog.setVisible(true);

        // Access Database and Save Data
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Indexs indexs = databaseConnection.getIndex();

        databaseConnection.insertBible(
                indexs.getBibleId() + 1,
                metaData.get("name").toString(),
                metaData.get("shortname").toString(),
                metaData.get("year").toString(),
                metaData.get("lang").toString(),
                metaData.get("copyright_statement").toString()
        );
        indexs.setBibleId(indexs.getBibleId() + 1);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < verseData.size(); i++) {
                    JSONObject verse = (JSONObject) verseData.get(i);
                    // Clean Text
                    String text = verse.get("text").toString();
                    text = text.replace("\u00b6", "");
                    text = text.replace("'", "''");
                    text = text.replace("[", "<i>");
                    text = text.replace("]", "</i>");
                    text = text.replace("‹", "<font color=#cc0000>");
                    text = text.replace("›", "</font>");
                    text = text.trim();
                    StringBuilder newText = new StringBuilder(text);

                    databaseConnection.insertVerse(
                            indexs.getVerseId() + 1,
                            verse.get("verse").toString(),
                            text
                    );

                    databaseConnection.insertBibleLink(
                            indexs.getBibleLinkId() + 1,
                            indexs.getBibleId(),
                            (Long) verse.get("book"),
                            (Long) verse.get("chapter"),
                            indexs.getVerseId() + 1
                    );

                    indexs.setVerseId(indexs.getVerseId() + 1);
                    indexs.setBibleLinkId(indexs.getBibleLinkId() + 1);
                    publish(i + 1);
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int value = chunks.get(chunks.size() - 1);
                progressBar.setValue(value);
            }

            @Override
            protected void done() {
                try {
                    databaseConnection.writeIndexs(indexs);

                    databaseConnection.close();
                    jDialog.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
        jDialog.setVisible(true);
        return 0;
    }
}
