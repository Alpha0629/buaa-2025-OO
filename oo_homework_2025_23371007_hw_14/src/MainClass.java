import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryReqCmd.Type;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.oocourse.library2.LibraryIO.SCANNER;
// 此处省略一些 import

public class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();    // 获取图书馆内所有书籍ISBN号及相应副本数
        HashMap<LibraryBookIsbn, HashSet<Book>> books = isbn2Id(bookList);
        Library library = new Library(books);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                // 在开馆时做点什么
                library.arrangeBookAtMorning(today);
            } else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                library.arrangeBookAtNight(today);
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                Type type = req.getType(); // 指令对应的类型（查询/阅读/借阅/预约/还书/取书/归还）
                LibraryBookIsbn bookIsbn = req.getBookIsbn(); // 指令对应的书籍ISBN号（type-uid）
                //LibraryBookId bookId = req.getBookId(); // 指令对应书籍编号（type-uid-copyId）
                LibraryBookId bookId;
                String studentId = req.getStudentId(); // 指令对应的用户Id
                // 对指令进行处理
                switch (type) {
                    case QUERIED:
                        bookId = req.getBookId();
                        library.queryBook(bookId, studentId, today);
                        break;
                    case BORROWED:
                        library.borrowBook(bookIsbn, studentId, today);
                        break;
                    case ORDERED:
                        library.orderBook(bookIsbn, studentId, today);
                        break;
                    case RETURNED:
                        bookId = req.getBookId();
                        library.returnBook(bookId, studentId, today);
                        break;
                    case PICKED:
                        library.pickBook(bookIsbn, studentId, today);
                        break;
                    case READ:
                        library.readBook(bookIsbn, studentId, today);
                        break;
                    case RESTORED:
                        bookId = req.getBookId();
                        library.restoreBook(bookId, studentId, today);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static HashMap<LibraryBookIsbn, HashSet<Book>> isbn2Id(Map<LibraryBookIsbn, Integer>
                                                                          bookList) {
        HashMap<LibraryBookIsbn, HashSet<Book>> isbn2Id = new HashMap<>();
        for (Map.Entry<LibraryBookIsbn, Integer> entry : bookList.entrySet()) {
            LibraryBookIsbn bookIsbn = entry.getKey();
            Integer num = entry.getValue();
            isbn2Id.put(bookIsbn.getBookIsbn(), new HashSet<>());
            for (int i = 1; i <= num; i++) {
                LibraryBookIsbn.Type type = bookIsbn.getType();
                String uid = bookIsbn.getUid();
                LibraryBookId bookId = new LibraryBookId(type, uid, toPaddedString(i));
                isbn2Id.get(bookIsbn.getBookIsbn()).add(new Book(bookId)); //创建副本
            }
        }
        return isbn2Id;
    }

    public static String toPaddedString(int num) {
        return num < 10 ? "0" + num : String.valueOf(num);
    }
}
