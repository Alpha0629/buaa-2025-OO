import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryMachine {
    private static QueryMachine queryMachine;
    private final HashMap<LibraryBookId, List<LibraryTrace>> bookTrace;
    //查询机:针对每本书都记录它的踪迹,在每次移动位置的时候更新

    private QueryMachine() {
        bookTrace = new HashMap<>();
    }

    public static QueryMachine getInstance() {
        if (queryMachine == null) {
            queryMachine = new QueryMachine();
        }
        return queryMachine;
    }

    public void updateTrace(LibraryBookId bookId, LibraryTrace trace) {
        bookTrace.computeIfAbsent(bookId, k -> new ArrayList<>()).add(trace);
    }

    public List<LibraryTrace> getTraces(LibraryBookId bookId) {
        List<LibraryTrace> traces = bookTrace.get(bookId);
        return traces == null ? new ArrayList<>() : traces;
    }
}
