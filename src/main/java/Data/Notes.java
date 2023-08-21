package main.java.Data;

public class Notes {

    private Long noteId;
    private Long bibleId;
    private Long bookId;
    private Long chapterId;
    private Long verseId;
    private String noteText;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getBibleId() {
        return bibleId;
    }

    public void setBibleId(Long bibleId) {
        this.bibleId = bibleId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getVerseId() {
        return verseId;
    }

    public void setVerseId(Long verseId) {
        this.verseId = verseId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}
