import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryTrace;

import static com.oocourse.library1.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library1.LibraryBookState.BOOKSHELF;
import static com.oocourse.library1.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library1.LibraryBookState.USER;
import static com.oocourse.library1.LibraryIO.PRINTER;
import static com.oocourse.library1.LibraryReqCmd.Type.BORROWED;
import static com.oocourse.library1.LibraryReqCmd.Type.ORDERED;
import static com.oocourse.library1.LibraryReqCmd.Type.RETURNED;
import static com.oocourse.library1.LibraryReqCmd.Type.PICKED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Library {
    private final BookShelf bookShelf;
    private final AppointOffice appointOffice;
    private final BorrowAndReturnOffice borrowAndReturnOffice;
    private final QueryMachine queryMachine;
    private final HashMap<String, Student> students;
    private final HashMap<Student, LibraryBookIsbn> orderQueue; //暂存预定的书
    private final List<LibraryMoveInfo> moveInfos;

    public Library(HashMap<LibraryBookIsbn, HashSet<Book>> bookList) {
        this.bookShelf = new BookShelf(bookList);
        this.appointOffice = AppointOffice.getInstance();
        this.borrowAndReturnOffice = BorrowAndReturnOffice.getInstance();
        this.queryMachine = QueryMachine.getInstance();
        this.students = new HashMap<>();
        this.orderQueue = new HashMap<>();
        this.moveInfos = new ArrayList<>();
    }

    public void queryBook(LibraryBookId bookId, String studentId, LocalDate date) {
        List<LibraryTrace> traces = queryMachine.getTraces(bookId);
        PRINTER.info(date, bookId, traces);
    }

    public void borrowBook(LibraryBookIsbn bookIsbn, String studentId, LocalDate date) {
        Student student = getStudent(studentId);
        if (!bookShelf.containsBook(bookIsbn)) {
            PRINTER.reject(date, BORROWED, studentId, bookIsbn);
            return;
        }
        if (bookIsbn.isTypeA()) {
            PRINTER.reject(date, BORROWED, studentId, bookIsbn);
            return;
        } else if (bookIsbn.isTypeB() && !student.containsTypeB()) {
            //从书架移除该书，获取书的对象
            Book book = bookShelf.removeBook(bookIsbn);
            student.addBook(book); //把这个对象转移到学生
            PRINTER.accept(date, BORROWED, studentId, book.getBookId());
            queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date, BOOKSHELF, USER));
        } else if (bookIsbn.isTypeC() && !student.containsSameIsbn(bookIsbn)) {
            Book book = bookShelf.removeBook(bookIsbn);
            student.addBook(book);
            PRINTER.accept(date, BORROWED, studentId, book.getBookId());
            queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date, BOOKSHELF, USER));
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

    public void arrangeBookAtMorning(LocalDate date) {
        //在开馆对应的整理后（即在 OPEN 指令对应的整理后），借还处不应该有书，预约处不应该有逾期的书
        //borrowAndReturnOffice2AppointOffice(date);
        appointOffice2BookShelf(date);
        bookShelf2AppointOffice(date);
        //整理完成后输出整理信息
        PRINTER.move(date, moveInfos);
        this.moveInfos.clear();
    }

    public void arrangeBookAtNight(LocalDate date) {
        borrowAndReturnOffice2BookShelf(date);
        //appointOffice2BookShelf(date);
        //整理完成后输出整理信息
        PRINTER.move(date, moveInfos);
        this.moveInfos.clear();
    }

    /*
    public void borrowAndReturnOffice2AppointOffice(LocalDate date) {
        Iterator<Student> iterator = orderQueue.keySet().iterator();
        while (iterator.hasNext()) {
            Student student = iterator.next();
            LibraryBookIsbn bookIsbn = orderQueue.get(student);
            if (borrowAndReturnOffice.containsBook(bookIsbn)) {
                LibraryBookId bookId = borrowAndReturnOffice.removeBook(bookIsbn);
                //从借还处移除
                appointOffice.addBook(new Book(bookId, date, student));
                //图书移动，终点为预约处，因此包含学生id
                moveInfos.add(new LibraryMoveInfo(bookId,
                        BORROW_RETURN_OFFICE, APPOINTMENT_OFFICE, student.getId()));
                queryMachine.updateTrace(bookId, new LibraryTrace(date,
                        BORROW_RETURN_OFFICE, APPOINTMENT_OFFICE));
                iterator.remove();
            }
        }
    } */

    public void borrowAndReturnOffice2BookShelf(LocalDate date) {
        HashMap<LibraryBookIsbn, HashSet<Book>> bookList = borrowAndReturnOffice.getBookList();
        for (LibraryBookIsbn bookIsbn : bookList.keySet()) {
            Book book;
            while ((book = borrowAndReturnOffice.removeBook(bookIsbn)) != null) {
                bookShelf.addBook(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(),
                        BORROW_RETURN_OFFICE, BOOKSHELF));
                queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                        BORROW_RETURN_OFFICE, BOOKSHELF));
            }
        }
    }

    public void appointOffice2BookShelf(LocalDate date) {
        List<Book> overdueBooks = appointOffice.getOverdueBooksAndReset(date);
        //所有在预约处已经逾期的书
        for (Book book : overdueBooks) {
            bookShelf.addBook(book);
            moveInfos.add(new LibraryMoveInfo(book.getBookId(), APPOINTMENT_OFFICE, BOOKSHELF));
            queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                    APPOINTMENT_OFFICE, BOOKSHELF));
        }
    }

    public void bookShelf2AppointOffice(LocalDate date) {
        Iterator<Student> iterator = orderQueue.keySet().iterator();
        while (iterator.hasNext()) {
            Student student = iterator.next();
            LibraryBookIsbn bookIsbn = orderQueue.get(student);
            if (bookShelf.containsBook(bookIsbn)) {
                Book book = bookShelf.removeBook(bookIsbn);
                book.setBookInfo(date, date.plusDays(5), student);
                appointOffice.addBook(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(),
                        BOOKSHELF, APPOINTMENT_OFFICE, student.getId()));
                queryMachine.updateTrace(book.getBookId(), new LibraryTrace(date,
                        BOOKSHELF, APPOINTMENT_OFFICE));
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
}
