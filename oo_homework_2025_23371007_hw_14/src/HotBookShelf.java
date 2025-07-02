import com.oocourse.library2.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HotBookShelf {
    private final HashMap<LibraryBookIsbn, HashSet<Book>> hotBookList;

    public HotBookShelf() {
        this.hotBookList = new HashMap<>();
    }

    public void addHotBook(Book book) {
        LibraryBookIsbn bookIsbn = book.getBookId().getBookIsbn();
        hotBookList.computeIfAbsent(bookIsbn, k -> new HashSet<>()).add(book);
    }

    public Book removeHotBook(LibraryBookIsbn bookIsbn) {
        HashSet<Book> books = hotBookList.get(bookIsbn);
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

    public List<Book> removeColdBook(Set<LibraryBookIsbn> memoryInfos) {
        List<Book> coldBooks = new ArrayList<>();
        Iterator<Entry<LibraryBookIsbn, HashSet<Book>>> iterator =
                hotBookList.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LibraryBookIsbn, HashSet<Book>> entry = iterator.next();
            LibraryBookIsbn bookIsbn = entry.getKey();
            if (!memoryInfos.contains(bookIsbn)) {
                coldBooks.addAll(entry.getValue());
                iterator.remove();  // 使用迭代器安全地移除元素
            }
        }

        return coldBooks;
    }

    public boolean containsHotBook(LibraryBookIsbn bookIsbn) {
        return hotBookList.containsKey(bookIsbn) && !hotBookList.get(bookIsbn).isEmpty();
    }
}