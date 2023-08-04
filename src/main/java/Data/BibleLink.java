package main.java.Data;

public class BibleLink {

    private Long bibleLinkId;
    private Bible bible;
    private Book book;
    private Chapter chapter;
    private Verse verse;

    public Long getBibleLinkId() {
        return bibleLinkId;
    }

    public void setBibleLinkId(Long bibleLinkId) {
        this.bibleLinkId = bibleLinkId;
    }

    public Bible getBible() {
        return bible;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Verse getVerse() {
        return verse;
    }

    public void setVerse(Verse verse) {
        this.verse = verse;
    }
}
