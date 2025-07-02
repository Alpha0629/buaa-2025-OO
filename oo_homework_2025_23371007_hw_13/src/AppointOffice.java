import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AppointOffice {
    private static AppointOffice appointOffice;
    private final HashMap<LibraryBookId, Book> bookList;

    private AppointOffice() {
        bookList = new HashMap<>();
    }

    public static AppointOffice getInstance() {
        if (appointOffice == null) {
            appointOffice = new AppointOffice();
        }
        return appointOffice;
    }

    public void addBook(Book book) {
        bookList.put(book.getBookId(), book);
    }

    public List<Book> getOverdueBooksAndReset(LocalDate date) {
        List<Book> overdueBooks = new ArrayList<>();
        Iterator<LibraryBookId> iterator = bookList.keySet().iterator();
        while (iterator.hasNext()) {
            LibraryBookId bookId = iterator.next();
            Book book = bookList.get(bookId);
            if (book.isOverdue(date)) {
                book.getStudent().setOrdered(false);
                book.resetBookInfo();
                overdueBooks.add(book);
                iterator.remove();
            }
        }
        return overdueBooks;
    }

    public Book removeBook(Student student) {
        for (Book book : bookList.values()) {
            if (book.getStudent().equals(student)) {
                bookList.remove(book.getBookId());
                return book;
            }
        }
        return null;
    }

    public boolean containsBook(Student student) {
        for (Book book : bookList.values()) {
            if (book.getStudent().equals(student)) {
                return true;
            }
        }
        return false;
    }
}
