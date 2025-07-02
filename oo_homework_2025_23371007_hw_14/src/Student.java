import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

import java.util.HashMap;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Student {
    private final String id;
    private final HashMap<LibraryBookId, Book> bookList;
    private boolean ordered;
    private boolean restored;

    public Student(String id) {
        this.id = id;
        this.bookList = new HashMap<>();
        this.ordered = false;
        this.restored = true;
    }

    public boolean containsTypeB() {
        for (LibraryBookId bookId : bookList.keySet()) {
            if (bookId.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsSameIsbn(LibraryBookIsbn bookIsbn) {
        for (LibraryBookId bookId : bookList.keySet()) {
            if (bookId.getBookIsbn().equals(bookIsbn)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsBook(LibraryBookId bookId) {
        return bookList.containsKey(bookId);
    }

    public void addBook(Book book) {
        this.bookList.put(book.getBookId(), book);
    }

    public Book removeBook(LibraryBookId bookId) {
        return this.bookList.remove(bookId);
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public boolean isOrdered() {
        return this.ordered;
    }

    public void setRestored(boolean restored) {
        this.restored = restored;
    }

    public boolean isRestored() {
        return this.restored;
    }

    public String getId() {
        return this.id;
    }
}
