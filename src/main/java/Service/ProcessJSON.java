package main.java.Service;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import main.java.Data.Indexes;
import main.java.Data.Materials;
import main.java.Data.Reference;
import main.java.Data.Word;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProcessJSON {

    File file;
    ArrayList<File> files;

    public ProcessJSON() {

    }

    public ProcessJSON(File file) {
        this.file = file;
    }

    public ProcessJSON(ArrayList<File> files) { this.files = files; }

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

    public String getVersion() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        String version = jsonObject.get("version").toString();
        return version;
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

    public int saveBibleToDatabase(JDialog dialog) throws Exception {
        // Progress bar frame
        JDialog jDialog = new JDialog(dialog, "Loading Bible", Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.setLayout(new BorderLayout());
        jDialog.setSize(400,85);
        jDialog.setLocationRelativeTo(dialog);

        // Setup Progess bars
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("Loading...");
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.setBorder(new EmptyBorder(10,10,10,10));
        layout.add(panel, BorderLayout.CENTER);

        jDialog.add(layout, BorderLayout.CENTER);

        // Processing of JSON file
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) object;
        JSONObject metaData = (JSONObject) jsonObject.get("metadata");
        JSONArray verseData = (JSONArray) jsonObject.get("verses");
        label.setText(metaData.get("name").toString());
        progressBar.setMaximum(verseData.size());

        // Access Database and Save Data
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Indexes indexes = databaseConnection.getIndex();

        databaseConnection.insertBible(
                indexes.getBibleId() + 1,
                metaData.get("name").toString(),
                metaData.get("shortname").toString(),
                metaData.get("year").toString(),
                metaData.get("lang").toString(),
                metaData.get("copyright_statement").toString()
        );
        indexes.setBibleId(indexes.getBibleId() + 1);

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
                            indexes.getVerseId() + 1,
                            verse.get("verse").toString(),
                            text
                    );

                    databaseConnection.insertBibleLink(
                            indexes.getBibleLinkId() + 1,
                            indexes.getBibleId(),
                            (Long) verse.get("book"),
                            (Long) verse.get("chapter"),
                            indexes.getVerseId() + 1
                    );

                    indexes.setVerseId(indexes.getVerseId() + 1);
                    indexes.setBibleLinkId(indexes.getBibleLinkId() + 1);
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
                    databaseConnection.writeIndexes(indexes);

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

    public int addConcordance(JDialog parentFrame, String label) throws Exception {
        // Dialog box for progress bar
        JDialog dialog = new JDialog(parentFrame, "Adding " + label, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(new Dimension(400, 150));
        dialog.setLocationRelativeTo(parentFrame);

        // Label and progress bars
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());
        JLabel fileLabel = new JLabel("...");
        fileLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        fileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar fileProgress = new JProgressBar();
        fileProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileProgress.setVisible(true);
        fileProgress.setStringPainted(true);
        fileProgress.setValue(0);
        filePanel.add(fileLabel, BorderLayout.NORTH);
        filePanel.add(fileProgress, BorderLayout.CENTER);

        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BorderLayout());
        JLabel overallLabel = new JLabel("Overall Progress");
        overallLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        overallLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar overallProgress = new JProgressBar();
        overallProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        overallProgress.setVisible(true);
        overallProgress.setStringPainted(true);
        overallProgress.setValue(0);
        overallPanel.add(overallLabel, BorderLayout.NORTH);
        overallPanel.add(overallProgress, BorderLayout.SOUTH);

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.setBorder(new EmptyBorder(10,10,10,10));
        layout.add(filePanel, BorderLayout.NORTH);
        layout.add(overallPanel, BorderLayout.SOUTH);

        dialog.add(layout, BorderLayout.CENTER);

        overallProgress.setMaximum(files.size());

        DatabaseConnection databaseConnection = new DatabaseConnection();
        Indexes indexes = databaseConnection.getIndex();
        Materials materials = new Materials();
        materials.setMaterialsId(indexes.getMaterialsId() + 1);
        materials.setName(label);
        indexes.setMaterialsId(indexes.getMaterialsId() + 1);
        databaseConnection.writeToMaterials(materials);

        SwingWorker<Void, Progress> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {

                for (int n = 0; n < files.size(); n ++) {
                    File f = files.get(n);
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new FileReader(f));
                    JSONObject jsonObject = (JSONObject) obj;
                    JSONArray words = (JSONArray) jsonObject.get("words");

                    for (int i = 0; i < words.size(); i++) {
                        JSONObject word = (JSONObject) words.get(i);

                        Word w = new Word();
                        w.setWordId(indexes.getWordId() + 1);
                        w.setWord(word.get("Word").toString());
                        w.setMaterialId(materials.getMaterialsId());
                        indexes.setWordId(indexes.getWordId() + 1);
                        publish(new Progress(w.getWord(), n, i, files.size(), words.size()));

                        databaseConnection.writeToWords(w);

                        JSONArray references = (JSONArray) word.get("reference");

                        for (int j = 0; j < references.size(); j++) {
                            JSONObject reference = (JSONObject) references.get(j);
                            Reference r = new Reference();
                            r.setReferenceId(indexes.getReferenceId() + 1);
                            r.setWordId(w.getWordId());
                            r.setCitation(reference.get("ref").toString());
                            if ((Long) reference.get("Book") != null) {
                                r.setText(reference.get("text").toString());
                                if (reference.get("link") != null) {
                                    r.setLink(reference.get("link").toString());
                                }
                                r.setBookId((Long) reference.get("Book"));
                                r.setChapterId(Long.parseLong(reference.get("Chapter").toString()));
                                r.setVerseNum(Long.parseLong(reference.get("Verse").toString()));
                            }
                            indexes.setReferenceId(indexes.getReferenceId() + 1);

                            databaseConnection.writeToReference(r);
                            reference.clear();
                        }
                        word.clear();
                    }
                    words.clear();
                }

                return null;
            }

            @Override
            protected void process(List<Progress> chunks) {
                Progress chunk = chunks.get(chunks.size() - 1);
                overallProgress.setMaximum(chunk.getMaxFile());
                fileProgress.setMaximum(chunk.getMaxWord());
                fileLabel.setText(chunk.getLabel());
                overallProgress.setValue(chunk.getOverallProgress());
                fileProgress.setValue(chunk.getInnerProgress());
            }

            @Override
            protected void done() {

                try {
                    get();
                    databaseConnection.writeIndexes(indexes);
                    databaseConnection.close();
                    dialog.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
        dialog.setVisible(true);
        return 0;
    }

    public int getScreenWidth() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long screenWidth = (Long) jsonObject.get("screenWidth");
        jsonObject.clear();
        if (screenWidth == null) {
            screenWidth = 1024L;
        }
        return screenWidth.intValue();
    }


    public int getScreenHeight() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long screenHeight = (Long) jsonObject.get("screenHeight");
        jsonObject.clear();
        if (screenHeight == null) {
            screenHeight = 768L;
        }
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

    public ArrayList<Object[]> getTabs() throws Exception {
        ArrayList<Object[]> openTabs = new ArrayList<>();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray tabs = (JSONArray) jsonObject.get("openTabs");

        for (Object object : tabs) {
            JSONObject tab = (JSONObject) object;
            Object[] tabData = {
                    (Long) tab.get("bibleId"),
                    (Long) tab.get("bookId"),
                    (Long) tab.get("chapterId"),
                    (boolean) tab.get("showNote"),
                    (boolean) tab.get("showRef"),
                    (Long) tab.get("isSelected")
            };
            openTabs.add(tabData);
        }
        jsonObject.clear();

        return openTabs;
    }

    public void saveTabs(ArrayList<Object[]> tabs) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;

        JSONArray tabArray = new JSONArray();
        for (Object[] tab : tabs) {
            JSONObject tabData = new JSONObject();
            tabData.put("bibleId", tab[0]);
            tabData.put("bookId", tab[1]);
            tabData.put("chapterId", tab[2]);
            tabData.put("showNote", tab[3]);
            tabData.put("showRef", tab[4]);
            tabData.put("isSelected", tab[5]);

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
        jsonObject.clear();
        return isVisible;
    }

    public void setTextSize(Integer textSize) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        jsonObject.put("textSize", textSize);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        fileWriter.close();
        jsonObject.clear();
    }

    public Integer getTextSize() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        Long temp = (Long) jsonObject.get("textSize");
        Integer textSize = temp.intValue();
        jsonObject.clear();
        return textSize;
    }

    public void setReferencePaneVisible(boolean isVisible) throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        jsonObject.put("refPaneVisible", isVisible);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(jsonObject.toJSONString());
        jsonObject.clear();
        fileWriter.close();
    }

    public boolean getReferencePaneVisible() throws Exception {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));
        JSONObject jsonObject = (JSONObject) obj;
        boolean isVisible = (boolean) jsonObject.get("refPaneVisible");
        jsonObject.clear();
        return isVisible;
    }
}
