import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳

        MainQueue mainQueue = new MainQueue();

        HashMap<Integer, SubQueue> subQueues = new HashMap<>();

        HashMap<Integer, ElevatorThread> elevators = new HashMap<>();

        for (int i = 1; i <= 6; i++) {
            SubQueue tempSubQueue = new SubQueue(mainQueue);
            subQueues.put(i, tempSubQueue);
            ElevatorThread elevatorThread = new ElevatorThread(i, mainQueue, tempSubQueue);
            elevators.put(i, elevatorThread);
            elevatorThread.start();
        }

        DispatchThread dispatchThread = new DispatchThread(mainQueue, subQueues, elevators);
        dispatchThread.start();

        InputThread inputThread = new InputThread(mainQueue, subQueues);
        inputThread.start();
    }
}