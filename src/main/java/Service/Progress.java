package main.java.Service;

public class Progress {
    private String label;
    private Integer overallProgress;
    private Integer innerProgress;
    private int maxFile;
    private int maxWord;

    public Progress(String label, Integer overallProgress, Integer innerProgress, int maxFile, int maxWord) {
        this.label = label;
        this.overallProgress = overallProgress;
        this.innerProgress = innerProgress;
        this.maxFile = maxFile;
        this.maxWord = maxWord;
    }

    public String getLabel() {
        return label;
    }

    public Integer getOverallProgress() {
        return overallProgress;
    }

    public Integer getInnerProgress() {
        return innerProgress;
    }

    public int getMaxFile() {
        return maxFile;
    }

    public int getMaxWord() {
        return maxWord;
    }
}
