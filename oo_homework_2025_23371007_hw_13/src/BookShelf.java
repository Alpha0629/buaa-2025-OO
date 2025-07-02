import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;

public class BookShelf {
    private final HashMap<LibraryBookIsbn, HashSet<Book>> bookList;

    public BookShelf(HashMap<LibraryBookIsbn, HashSet<Book>> bookList) {
        this.bookList = bookList;
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

    public boolean containsBook(LibraryBookIsbn bookIsbn) {
        return bookList.containsKey(bookIsbn) && !bookList.get(bookIsbn).isEmpty();
    }
}
