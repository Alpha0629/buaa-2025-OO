import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳
        RequestTable mainTable = new RequestTable();
        HashMap<Integer, RequestTable> subTables = new HashMap<>();
        DispatchThread dispatchThread = new DispatchThread(mainTable, subTables);
        for (int i = 1; i <= 6; i++) {
            RequestTable requestTable = new RequestTable();
            subTables.put(i, requestTable);
            ElevatorThread elevatorThread = new ElevatorThread(i, requestTable);
            elevatorThread.start();
        }
        dispatchThread.start();
        InputThread inputThread = new InputThread(mainTable);
        inputThread.start();
    }
}