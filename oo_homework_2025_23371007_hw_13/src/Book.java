import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private final LibraryBookId bookId;
    private LocalDate orderDate;
    private LocalDate deadline;
    private Student student;

    public Book(LibraryBookId bookId) {
        this.bookId = bookId;
        this.orderDate = null;
        this.deadline = null;
        this.student = null;
    }

    public void setBookInfo(LocalDate orderDate, LocalDate deadline, Student student) {
        this.orderDate = orderDate;
        this.deadline = deadline;
        this.student = student;
    }

    public void resetBookInfo() {
        this.orderDate = null;
        this.deadline = null;
        this.student = null;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public boolean isOverdue(LocalDate date) {
        return date.isAfter(this.deadline) || date.isEqual(this.deadline);
    }

    public Student getStudent() {
        return student;
    }
}
