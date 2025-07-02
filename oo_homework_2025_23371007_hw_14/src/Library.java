import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryTrace;

import static com.oocourse.library2.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library2.LibraryBookState.BOOKSHELF;
import static com.oocourse.library2.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library2.LibraryBookState.HOT_BOOKSHELF;
import static com.oocourse.library2.LibraryBookState.READING_ROOM;
import static com.oocourse.library2.LibraryBookState.USER;
import static com.oocourse.library2.LibraryIO.PRINTER;
import static com.oocourse.library2.LibraryReqCmd.Type.BORROWED;
import static com.oocourse.library2.LibraryReqCmd.Type.ORDERED;
import static com.oocourse.library2.LibraryReqCmd.Type.READ;
import static com.oocourse.library2.LibraryReqCmd.Type.RESTORED;
import static com.oocourse.library2.LibraryReqCmd.Type.RETURNED;
import static com.oocourse.library2.LibraryReqCmd.Type.PICKED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Library {
    private final BookShelf bookShelf;
    private final HotBookShelf hotBookShelf;
    private final AppointOffice appointOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final ReadingRoom readingRoom;
    private final QueryMachine queryMachine;
    private final HashMap<String, Student> students;
    private final HashMap<Student, LibraryBookIsbn> orderQueue; //暂存预定的书
    private final List<LibraryMoveInfo> moveInfos;
    private final Set<LibraryBookIsbn> memoryInfos;

    public Library(HashMap<LibraryBookIsbn, HashSet<Book>> bookList) {
        this.bookShelf = new BookShelf(bookList);
        this.hotBookShelf = new HotBookShelf();
        this.appointOffice = AppointOffice.getInstance();
        this.borrowAndReturnOffice = BorrowAndReturnOffice.getInstance();
        this.readingRoom = ReadingRoom.getInstance();
        this.queryMachine = QueryMachine.getInstance();
        this.students = new HashMap<>();
        this.orderQueue = new HashMap<>();
        this.moveInfos = new ArrayList<>();
        this.memoryInfos = new HashSet<>();
    }

    public void queryBook(LibraryBookId bookId, String studentId, LocalDate date) {
        List<LibraryTrace> traces = queryMachine.getTraces(bookId);
        PRINTER.info(date, bookId, traces);
    }

    public void borrowBook(LibraryBookIsbn bookIsbn, String studentId, LocalDate date) {
        Student student = getStudent(studentId);
        if (!containsBook(bookIsbn)) {
            PRINTER.reject(date, BORROWED, studentId, bookIsbn);
            return;
        }
        if (bookIsbn.isTypeA()) {
            PRINTER.reject(date, BORROWED, studentId, bookIsbn);
        } else if (bookIsbn.isTypeB() && !student.containsTypeB()) {
            //从书架移除该书，获取书的对象
            Book book;
            if (hotBookShelf.containsHotBook(bookIsbn)) {
                book = hotBookShelf.removeHotBook(bookIsbn);
                queryMachine.updateTrace(book.getBookId(),
                        new LibraryTrace(date, HOT_BOOKSHELF, USER));
            } else { //bookShelf.containsBook(bookIsbn)
                book = bookShelf.removeBook(bookIsbn);
                queryMachine.updateTrace(book.getBookId(),
                        new LibraryTrace(date, BOOKSHELF, USER));
            }
            student.addBook(book); //把这个对象转移到学生
            PRINTER.accept(date, BORROWED, studentId, book.getBookId());
            memoryInfos.add(book.getBookId().getBookIsbn());
        } else if (bookIsbn.isTypeC() && !student.containsSameIsbn(bookIsbn)) {
            Book book;
            if (hotBookShelf.containsHotBook(bookIsbn)) {
                book = hotBookShelf.removeHotBook(bookIsbn);
                queryMachine.updateTrace(book.getBookId(),
                        new LibraryTrace(date, HOT_BOOKSHELF, USER));
            } else {
                book = bookShelf.removeBook(bookIsbn);
                queryMachine.updateTrace(book.getBookId(),
                        new LibraryTrace(date, BOOKSHELF, USER));
            }
            student.addBook(book);
            PRINTER.accept(date, BORROWED, studentId, book.getBookId());
            memoryInfos.add(book.getBookId().getBookIsbn());
        } else {
            PRINTER.reject(date, BORROWED, studentId, bookIsbn);
        }
    }

    public void orderBook(LibraryBookIsbn bookIsbn, String studentId, LocalDate date) {
        Student student = getStudent(studentId);
        if (student.isOrdered()) { //若此前已经预定过书籍且还未取书，则预约失败
            PRINTER.reject(date, ORDERED, studentId, bookIsbn);
            return;
        }
        if (bookIsbn.isTypeA()) {
            PRINTER.reject(date, ORDERED, studentId, bookIsbn);
        } else if (bookIsbn.isTypeB() && !student.containsTypeB()) {
            student.setOrdered(true);
            orderQueue.put(student, bookIsbn);
            PRINTER.accept(date, ORDERED, studentId, bookIsbn);
        } else if (bookIsbn.isTypeC() && !student.containsSameIsbn(bookIsbn)) {
            student.setOrdered(true);
            orderQueue.put(student, bookIsbn);
            PRINTER.accept(date, ORDERED, studentId, bookIsbn);
        } else {
            PRINTER.reject(date, ORDERED, studentId, bookIsbn);
        }
    }

    public void returnBook(LibraryBookId bookId, String studentId, LocalDate date) {
        Student student = getStudent(studentId);
        Book book = student.removeBook(bookId);
        borrowAndReturnOffice.addBook(book);
        PRINTER.accept(date, RETURNED, studentId, book.getBookId());
        queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                USER, BORROW_RETURN_OFFICE));
    }

    public void pickBook(LibraryBookIsbn bookIsbn, String studentId, LocalDate date) {
        Student student = getStudent(studentId);
        if (!appointOffice.containsBook(student)) {
            PRINTER.reject(date, PICKED, studentId, bookIsbn);
            return;
        }
        if (bookIsbn.isTypeB() && !student.containsTypeB()) {
            Book book = appointOffice.removeBook(student);
            student.addBook(book);
            student.setOrdered(false);
            PRINTER.accept(date, PICKED, studentId, book.getBookId());
            queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                    APPOINTMENT_OFFICE, USER));
        } else if (bookIsbn.isTypeC() && !student.containsSameIsbn(bookIsbn)) {
            Book book = appointOffice.removeBook(student);
            student.addBook(book);
            student.setOrdered(false);
            PRINTER.accept(date, PICKED, studentId, book.getBookId());
            queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                    APPOINTMENT_OFFICE, USER));
        } else {
            PRINTER.reject(date, PICKED, studentId, bookIsbn);
        }
    }

    public void readBook(LibraryBookIsbn bookIsbn, String studentId, LocalDate date) {
        Student student = getStudent(studentId);
        if (!containsBook(bookIsbn)) {
            PRINTER.reject(date, READ, studentId, bookIsbn);
        } else if (!student.isRestored()) {
            PRINTER.reject(date, READ, studentId, bookIsbn);
        } else {
            //阅读成功,学生被标记为当日阅读后未归还
            student.setRestored(false);
            Book book;
            if (hotBookShelf.containsHotBook(bookIsbn)) {
                book = hotBookShelf.removeHotBook(bookIsbn);
                queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                        HOT_BOOKSHELF, READING_ROOM));
            } else { //bookShelf.containsBook(bookIsbn)
                book = bookShelf.removeBook(bookIsbn);
                queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                        BOOKSHELF, READING_ROOM));
            }
            //该书视为从普通书架/热门书架移动到阅览室,不能再被任何人借阅
            //阅读书不代表学生持有书籍,仍视为阅览室持有书籍
            readingRoom.addBook(book);
            PRINTER.accept(date, READ, studentId, book.getBookId());
            memoryInfos.add(book.getBookId().getBookIsbn());
        }
    }

    public void restoreBook(LibraryBookId bookId, String studentId, LocalDate date) {
        //该用户会前往借还处，阅读归还立即成功，从此刻起，借还处持有该书，阅览室不再持有该书
        //归还书籍仅限于当天操作
        Student student = getStudent(studentId);
        Book book = readingRoom.removeBook(bookId);
        borrowAndReturnOffice.addBook(book);
        student.setRestored(true);
        PRINTER.accept(date, RESTORED, studentId, book.getBookId());
        queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                READING_ROOM, BORROW_RETURN_OFFICE));
    }

    public void arrangeBookAtMorning(LocalDate date) {
        //在开馆对应的整理后（即在 OPEN 指令对应的整理后），借还处不应该有书，预约处不应该有逾期的书
        //bs2hbs(date);
        hbs2bs(date);
        ao2bs(date);
        bs2hbs(date);
        allbs2ao(date);
        //整理完成后输出整理信息
        PRINTER.move(date, moveInfos);
        this.moveInfos.clear();
        this.memoryInfos.clear();
    }

    public void arrangeBookAtNight(LocalDate date) {
        rr2bs(date);
        bro2bs(date);
        //整理完成后输出整理信息
        PRINTER.move(date, moveInfos);
        this.moveInfos.clear();
    }

    public void bs2hbs(LocalDate date) {
        Book book;
        //上次开馆时被阅读或借阅成功的ISBN号的所有副本视为热门书籍
        //本次开馆整理后，热门图书所有副本都应移动或保留至热门书架
        for (LibraryBookIsbn bookIsbn : memoryInfos) {
            while ((book = bookShelf.removeBook(bookIsbn)) != null) {
                hotBookShelf.addHotBook(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), BOOKSHELF, HOT_BOOKSHELF));
                queryMachine.updateTrace(book.getBookId(),
                        new LibraryTrace(date, BOOKSHELF, HOT_BOOKSHELF));
            }
        }
    }

    public void hbs2bs(LocalDate date) {
        List<Book> coldBooks = hotBookShelf.removeColdBook(memoryInfos);
        if (!coldBooks.isEmpty()) {
            for (Book book : coldBooks) {
                bookShelf.addBook(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), HOT_BOOKSHELF, BOOKSHELF));
                queryMachine.updateTrace(book.getBookId(),
                        new LibraryTrace(date, HOT_BOOKSHELF, BOOKSHELF));
            }
            //System.out.println("数量 " + coldBooks.size());
        }
    }

    public void rr2bs(LocalDate date) {
        resetStudent(); //把所有学生的阅读状态重置为已经restore
        Book book;
        while ((book = readingRoom.removeBook2()) != null) {
            bookShelf.addBook(book);
            moveInfos.add(new LibraryMoveInfo(book.getBookId(), READING_ROOM, BOOKSHELF));
            queryMachine.updateTrace(book.getBookId(),
                    new LibraryTrace(date, READING_ROOM, BOOKSHELF));
        }
    }

    public void bro2bs(LocalDate date) {
        Book book;
        while ((book = borrowAndReturnOffice.removeBook()) != null) {
            bookShelf.addBook(book);
            moveInfos.add(new LibraryMoveInfo(book.getBookId(), BORROW_RETURN_OFFICE, BOOKSHELF));
            queryMachine.updateTrace(book.getBookId(),
                    new LibraryTrace(date, BORROW_RETURN_OFFICE, BOOKSHELF));
        }
    }

    public void ao2bs(LocalDate date) {
        List<Book> overdueBooks = appointOffice.getOverdueBooksAndReset(date);
        //所有在预约处已经逾期的书
        for (Book book : overdueBooks) {
            bookShelf.addBook(book);
            moveInfos.add(new LibraryMoveInfo(book.getBookId(), APPOINTMENT_OFFICE, BOOKSHELF));
            queryMachine.updateTrace(book.getBookId(),
                    new LibraryTrace(date, APPOINTMENT_OFFICE, BOOKSHELF));
        }
    }

    public void allbs2ao(LocalDate date) {
        Iterator<Student> iterator = orderQueue.keySet().iterator();
        while (iterator.hasNext()) {
            Student student = iterator.next();
            LibraryBookIsbn bookIsbn = orderQueue.get(student);
            Book book;
            if (containsBook(bookIsbn)) {
                if (hotBookShelf.containsHotBook(bookIsbn)) {
                    book = hotBookShelf.removeHotBook(bookIsbn);
                    moveInfos.add(new LibraryMoveInfo(book.getBookId(),
                            HOT_BOOKSHELF, APPOINTMENT_OFFICE, student.getId()));
                    queryMachine.updateTrace(book.getBookId(),
                            new LibraryTrace(date, HOT_BOOKSHELF, APPOINTMENT_OFFICE));
                } else { //bookShelf.containsBook(bookIsbn)
                    book = bookShelf.removeBook(bookIsbn);
                    moveInfos.add(new LibraryMoveInfo(book.getBookId(),
                            BOOKSHELF, APPOINTMENT_OFFICE, student.getId()));
                    queryMachine.updateTrace(book.getBookId(),
                            new LibraryTrace(date, BOOKSHELF, APPOINTMENT_OFFICE));
                }
                book.setBookInfo(date, date.plusDays(5), student);
                appointOffice.addBook(book);
                iterator.remove();
            }
        }
    }

    public Student getStudent(String studentId) {
        if (!students.containsKey(studentId)) {
            students.put(studentId, new Student(studentId));
        }
        return students.get(studentId);
    }

    public boolean containsBook(LibraryBookIsbn bookIsbn) {
        return bookShelf.containsBook(bookIsbn) || hotBookShelf.containsHotBook(bookIsbn);
    }

    public void resetStudent() {
        for (Student student : students.values()) {
            student.setRestored(true);
        }
    }
}
