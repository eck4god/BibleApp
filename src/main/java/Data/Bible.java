package main.java.Data;

public class Bible {

    private Long bibleId;
    private String bibleName;
    private String bibleShortName;
    private String yearOfPublication;
    private String lang;
    private String copyrightInfo;

    public Long getBibleId() {
        return bibleId;
    }

    public void setBibleId(Long bibleId) {
        this.bibleId = bibleId;
    }

    public String getBibleName() {
        return bibleName;
    }

    public void setBibleName(String bibleName) {
        this.bibleName = bibleName;
    }

    public String getBibleShortName() {
        return bibleShortName;
    }

    public void setBibleShortName(String bibleShortName) {
        this.bibleShortName = bibleShortName;
    }

    public String getYearOfPublication() {
        return yearOfPublication;
    }

    public void setYearOfPublication(String yearOfPublication) {
        this.yearOfPublication = yearOfPublication;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCopyrightInfo() {
        return copyrightInfo;
    }

    public void setCopyrightInfo(String copyrightInfo) {
        this.copyrightInfo = copyrightInfo;
    }
}
