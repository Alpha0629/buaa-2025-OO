import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;

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

    public Book removeBook(LibraryBookIsbn bookIsbn) {
        HashSet<Book> books = bookList.get(bookIsbn);
        if (books == null || books.isEmpty()) {
            return null;
        }
        Book tempBook = null;
        for (Book book : books) {
            if (tempBook == null) {
                tempBook = book;
                continue;
            }
            String currentId = book.getBookId().getCopyId();
            String tempId = tempBook.getBookId().getCopyId();
            if (Integer.valueOf(currentId).compareTo(Integer.valueOf(tempId)) < 0) {
                tempBook = book;
            }
        }
        books.remove(tempBook);
        return tempBook;
    }

    public HashMap<LibraryBookIsbn, HashSet<Book>> getBookList() {
        return bookList;
    }

    public boolean containsBook(LibraryBookIsbn bookIsbn) {
        return bookList.containsKey(bookIsbn) && !bookList.get(bookIsbn).isEmpty();
    }
}
