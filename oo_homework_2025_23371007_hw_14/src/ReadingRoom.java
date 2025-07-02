import com.oocourse.library2.LibraryBookId;

import java.util.HashMap;
import java.util.Map;

public class ReadingRoom {
    private static ReadingRoom readingRoom;
    private final HashMap<LibraryBookId, Book> bookList;

    private ReadingRoom() {
        bookList = new HashMap<>();
    }

    public static ReadingRoom getInstance() {
        if (readingRoom == null) {
            readingRoom = new ReadingRoom();
        }
        return readingRoom;
    }

    public void addBook(Book book) {
        bookList.put(book.getBookId(), book);
    }

    public Book removeBook(LibraryBookId bookId) {
        return bookList.remove(bookId);
    }

    public Book removeBook2() {
        if (bookList.isEmpty()) {
            return null;
        }
        Map.Entry<LibraryBookId, Book> entry = bookList.entrySet().iterator().next();
        bookList.remove(entry.getKey());
        return entry.getValue();
    }
}
