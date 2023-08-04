package main.java.Data;

public class Book {

    private Long bookId;
    private String bookTitle;
    private String bookAbbrev;

    public void setBookNumber(Long bookId) {
        this.bookId = bookId;
    }

    public Long getBookNumber() { return bookId; }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookAbbrev(String bookAbbrev) {
        this.bookAbbrev = bookAbbrev;
    }

    public String getBookAbbrev() {
        return bookAbbrev;
    }
}
