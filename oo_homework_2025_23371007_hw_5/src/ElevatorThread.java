import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class ElevatorThread extends Thread {
    private final int id;
    private int currentNum;
    private int currentFloor;
    private boolean direction;
    private final RequestTable requestTable;
    private final ArrayList<Person> persons;
    private final Strategy strategy;

    public ElevatorThread(int id, RequestTable requestTable) {
        this.id = id;
        this.currentNum = 0;
        this.currentFloor = 1;//初始位于第一层
        this.direction = true;//表示上行
        this.requestTable = requestTable;//该电梯外部的请求队列
        this.persons = new ArrayList<>();//电梯内的人员
        strategy = new Strategy(requestTable, persons);
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = strategy.getAdvice(currentNum, currentFloor, direction);
            if (advice == Advice.OVER) {
                break;
            } else if (advice == Advice.MOVE) {
                Move();
            } else if (advice == Advice.REVERSE) {
                direction = !direction;
            } else if (advice == Advice.WAIT) {
                requestTable.waitRequest();
            } else if (advice == Advice.OPEN) {
                OpenAndClose();
            }
        }
    }

    public void OpenAndClose() {
        String outputCurrentFloor = StringCurrentFloor();
        TimableOutput.println("OPEN-" + outputCurrentFloor + "-" + id);
        Out(); //这里有可能人已经出尽了，一旦出尽，需要重新判断是上行还是下行
        Advice advice = strategy.getAdvice(currentNum, currentFloor, direction);
        if (advice == Advice.REVERSE) {
            direction = !direction;
        }
        In();
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TimableOutput.println("CLOSE-" + outputCurrentFloor + "-" + id);
    }

    public void Move() { //电梯移动一层
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (direction) {
            if (currentFloor == -1) {
                currentFloor = 1;//跳过0层
            } else {
                currentFloor++;
            }
        } else {
            if (currentFloor == 1) {
                currentFloor = -1;//跳过0层
            } else {
                currentFloor--;
            }
        }
        String output = StringCurrentFloor();
        TimableOutput.println("ARRIVE-" + output + "-" + id);
    }

    public void Out() {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            if (person.getToFloor() == currentFloor) {
                iterator.remove();
                currentNum--;
                String output = StringCurrentFloor();
                TimableOutput.println("OUT-" + person.getPersonId() + "-" + output + "-" + id);
            }
        }
    }

    public void In() {
        while (currentNum < 6) {
            Person person = requestTable.pullGivenRequest(currentFloor, direction);
            if (person != null) {
                persons.add(person);
                currentNum++;
                String output = StringCurrentFloor();
                TimableOutput.println("IN-" + person.getPersonId() + "-" + output + "-" + id);
            } else {
                break;
            }
        }
    }

    public String StringCurrentFloor() {
        StringBuilder sb = new StringBuilder();
        if (currentFloor > 0) {
            sb.append('F').append(currentFloor);
        } else if (currentFloor < 0) {
            int tempFloor = -currentFloor;
            sb.append('B').append(tempFloor);
        } else {
            System.out.println("ERROR: Invalid Floor");
        }
        return sb.toString();
    }
}
