import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class ElevatorThread extends Thread {
    private final int id;
    private int currentNum;
    private int currentFloor;
    private boolean direction;
    private boolean block;//在sche模式下，电梯被封闭，过程中无法上下电梯
    private final MainQueue mainQueue;
    private final SubQueue subQueue;
    private final ArrayList<Person> persons;
    private final Strategy strategy;

    public ElevatorThread(int id, MainQueue mainQueue, SubQueue subQueue) {
        this.id = id;
        this.currentNum = 0;
        this.currentFloor = 1;//初始位于第一层
        this.direction = true;//表示上行
        this.block = false;//默认不处于sche模式
        this.mainQueue = mainQueue;
        this.subQueue = subQueue;
        this.persons = new ArrayList<>();//电梯内的人员
        this.strategy = new Strategy(this.subQueue, this.persons);
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = strategy.getAdvice(currentNum, currentFloor, direction, block);
            if (advice == Advice.OVER) {
                break;
            } else if (advice == Advice.MOVE) {
                Move(block);
            } else if (advice == Advice.REVERSE) {
                direction = !direction;
            } else if (advice == Advice.WAIT) {
                synchronized (subQueue) {
                    try {
                        subQueue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (advice == Advice.OPEN) {
                OpenAndClose();
            } else if (advice == Advice.BLOCK) {
                block = true; //调度任务开始
                TimableOutput.println("SCHE-BEGIN-" + id);
                subQueue.transferFromSubToMain();
            } else if (advice == Advice.SCHE) {
                Scheduling();
                block = false; //调度任务完成
                TimableOutput.println("SCHE-END-" + id);
                subQueue.deleteFirstSche();
                subQueue.transferFromCacheToSub();
            }
        }
    }

    public void OpenAndClose() {
        String outputCurrentFloor = StringCurrentFloor();
        TimableOutput.println("OPEN-" + outputCurrentFloor + "-" + id);
        Out(); //这里有可能人已经出尽了，一旦出尽，需要重新判断是上行还是下行
        Advice advice = strategy.getAdvice(currentNum, currentFloor, direction, block);
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

    public void Move(boolean block) { //电梯移动一层
        long times;
        if (block) {
            synchronized (subQueue) {
                times = (long) (1000 * subQueue.getSches().get(0).getSpeed());
            }
        } else {
            times = 400;
        }
        try {
            sleep(times);
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
                mainQueue.subNotFinished();
                String output = StringCurrentFloor();
                TimableOutput.println("OUT-S-" + person.getPersonId() + "-" + output + "-" + id);
            }
        }
    }

    public void In() {
        while (currentNum < 6) {
            Person person = subQueue.pullGivenRequest(currentFloor, direction);
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

    public void Scheduling() {
        String outputCurrentFloor = StringCurrentFloor();
        TimableOutput.println("OPEN-" + outputCurrentFloor + "-" + id);//到达先开门
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        AllOut();
        TimableOutput.println("CLOSE-" + outputCurrentFloor + "-" + id);
        //subQueue.deleteFirstSche();
        //block = false; //解除封锁
    }

    public void AllOut() {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            if (person.getToFloor() == currentFloor) { //彻底出去
                iterator.remove();
                currentNum--;
                mainQueue.subNotFinished();
                String output = StringCurrentFloor();
                TimableOutput.println("OUT-S-" + person.getPersonId() + "-" + output + "-" + id);
            } else { //暂时出去，还要回到请求队列中
                iterator.remove();
                currentNum--;
                person.resetElevatorId();//重置搭乘的电梯序号为-1
                person.setFromFloor(currentFloor); //重置起始楼层
                String output = StringCurrentFloor();
                TimableOutput.println("OUT-F-" + person.getPersonId() + "-" + output + "-" + id);
                mainQueue.pushPersonRequest(person);//和Timable调换位置，先输出F，代表真正出去了，再进行调配才正确
                //subQueue.pushPersonRequest(person); //回到请求队列中
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

    public SubQueue getSubQueue() {
        return subQueue;
    }

    public ArrayList<Person> getPersons() {
        return persons;
    }

    public boolean isBlocked() {
        return block;
    }

    public int getCurrentNum() {
        return currentNum;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public boolean direction() {
        return direction;
    }
}