package main.java.Service;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
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

    public int getScreenWidth() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long screenWidth = (Long) jsonObject.get("screenWidth");
        jsonObject.clear();
        return screenWidth.intValue();
    }


    public int getScreenHeight() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long screenHeight = (Long) jsonObject.get("screenHeight");
        jsonObject.clear();
        return screenHeight.intValue();
    }

    public void setScreenHeight(Integer screenWidth, Integer screenHeight) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        jsonObject.put("screenWidth", screenWidth.longValue());
        jsonObject.put("screenHeight", screenHeight.longValue());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
        jsonObject.clear();
    }

    public Integer getX() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long x = (Long) jsonObject.get("windowX");
        jsonObject.clear();
        if (x == null)
            return null;
        else
            return x.intValue();
    }

    public Integer getY() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long y = (Long) jsonObject.get("windowY");
        jsonObject.clear();
        if (y == null)
            return null;
        else
            return y.intValue();
    }

    public void setWindowPosition(Integer x, Integer y) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        jsonObject.put("windowX", x.longValue());
        jsonObject.put("windowY", y.longValue());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
        jsonObject.clear();
    }

    public ArrayList<Long[]> getTabs() throws Exception {
        ArrayList<Long[]> openTabs = new ArrayList<>();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray tabs = (JSONArray) jsonObject.get("openTabs");

        for (Object object : tabs) {
            JSONObject tab = (JSONObject) object;
            Long[] tabData = {
                    (Long) tab.get("bibleId"),
                    (Long) tab.get("bookId"),
                    (Long) tab.get("chapterId"),
                    (Long) tab.get("isSelected")
            };
            openTabs.add(tabData);
        }

        return openTabs;
    }

    public void saveTabs(ArrayList<Long[]> tabs) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;

        JSONArray tabArray = new JSONArray();
        for (Long[] tab : tabs) {
            JSONObject tabData = new JSONObject();
            tabData.put("bibleId", tab[0]);
            tabData.put("bookId", tab[1]);
            tabData.put("chapterId", tab[2]);
            tabData.put("isSelected", tab[3]);

            tabArray.add(tabData);
        }
        jsonObject.put("openTabs", tabArray);

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
        jsonObject.clear();
    }

    public void setNavPaneVisible(boolean isVisible) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        jsonObject.put("navPaneVisible", isVisible);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
        jsonObject.clear();
    }

    public boolean getNavPaneVisible() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        boolean isVisible = (boolean) jsonObject.get("navPaneVisible");
        return isVisible;
    }
}
