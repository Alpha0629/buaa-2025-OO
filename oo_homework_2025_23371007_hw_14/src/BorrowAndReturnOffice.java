import com.oocourse.library2.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class BorrowAndReturnOffice {
    private static BorrowAndReturnOffice borrowAndReturnOffice;
    private final HashMap<LibraryBookIsbn, HashSet<Book>> bookList;

    private BorrowAndReturnOffice() {
        bookList = new HashMap<>();
    }

    public static BorrowAndReturnOffice getInstance() {
        if (borrowAndReturnOffice == null) {
            borrowAndReturnOffice = new BorrowAndReturnOffice();
        }
        return borrowAndReturnOffice;
    }

    public void addBook(Book book) {
        LibraryBookIsbn bookIsbn = book.getBookId().getBookIsbn();
        bookList.computeIfAbsent(bookIsbn, k -> new HashSet<>()).add(book);
    }

    public Book removeBook() {
        Iterator<Entry<LibraryBookIsbn, HashSet<Book>>> iterator = bookList.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<LibraryBookIsbn, HashSet<Book>> entry = iterator.next();
            HashSet<Book> books = entry.getValue();
            if (!books.isEmpty()) {
                Iterator<Book> bookIterator = books.iterator();
                Book removedBook = bookIterator.next();
                bookIterator.remove();
                if (books.isEmpty()) {
                    iterator.remove();
                }
                return removedBook;
            }
        }
        return null;
    }

    public boolean containsBook(LibraryBookIsbn bookIsbn) {
        return bookList.containsKey(bookIsbn) && !bookList.get(bookIsbn).isEmpty();
    }
}
