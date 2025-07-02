import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ElevatorThread extends Thread {
    private final int id;
    private int currentNum;
    private int currentFloor;
    private int speed;
    private int minFloor;
    private int maxFloor;
    private boolean direction;
    private boolean block;//在sche模式下，电梯被封闭，过程中无法上下电梯;
    private boolean moved;
    private ElevatorThread friendEle;
    private final HashMap<Integer, ElevatorThread> elevators;
    private final MainQueue mainQueue;
    private final SubQueue subQueue;
    private final ArrayList<Person> persons;
    private final Strategy strategy;
    private volatile Advice advice;

    public ElevatorThread(HashMap<Integer, ElevatorThread> elevators,
                          int id, int speed, MainQueue mainQueue, SubQueue subQueue) {
        this.id = id;
        this.currentNum = 0;
        this.currentFloor = 1;//初始位于第一层
        this.speed = speed;
        this.minFloor = -4;
        this.maxFloor = +7;
        this.direction = true;//表示上行
        this.block = false;//默认不处于sche模式
        this.moved = false;
        this.friendEle = null; //不是双轿厢形态，暂时没有friend
        this.elevators = elevators;
        this.mainQueue = mainQueue;
        this.subQueue = subQueue;
        this.persons = new ArrayList<>();//电梯内的人员
        this.strategy = new Strategy(this.subQueue, this.persons);
        this.advice = null;
    }

    @Override
    public void run() {
        while (true) {
            advice = strategy.getAdvice(currentNum, currentFloor, direction,
                    block, moved, maxFloor, minFloor, id);
            if (advice == Advice.OVER) {
                break;
            } else if (advice == Advice.MOVE) {
                System.out.println("电梯获取到了移动指令");
                Move();
            } else if (advice == Advice.REVERSE) {
                direction = !direction;
            } else if (advice == Advice.WAIT) {
                WaitingOrMove();
            } else if (advice == Advice.OPEN) {
                OpenAndClose();
                System.out.println("门关了，准备下一次循环获取指令");
            } else if (advice == Advice.BLOCK) {
                block = true; //调度任务开始
                TimableOutput.println("SCHE-BEGIN-" + id);
                synchronized (subQueue) {
                    speed = subQueue.getSches().get(0).getSpeed(); //更新速度
                }
                subQueue.transferFromSubToMain();
            } else if (advice == Advice.SCHE) {
                Scheduling(1000);//无条件开门，也就是说，不论电梯中有没有人，都要开门
                block = false; //调度任务完成
                TimableOutput.println("SCHE-END-" + id);
                speed = 400; //重置速度
                subQueue.deleteFirstSche();
                subQueue.transferFromCacheToSub();
            } else if (advice == Advice.UPDATE) {
                //TimableOutput.println("Updating-" + id);
                Updating();
            } else if (advice == Advice.MOVED) { //该电梯正在被移动，无法接受乘客,该线程进入等待，直到moving结束
                synchronized (subQueue) {
                    try {
                        //TimableOutput.println("MOVED-WAIT-" + id);
                        subQueue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                //TimableOutput.println("END-MOVED-" + id);
            }
        }
    }

    public void WaitingOrMove() {
        if (minFloor > -4 && currentFloor == minFloor) { //上层电梯位于换乘层且空闲，应该避让
            direction = true; //确定方向
            Move(); //向下移动
            return; //不需要wait，直接返回
        }
        if (maxFloor < 7 && currentFloor == maxFloor) { //下层电梯位于换乘层且空闲，应该避让
            direction = false; //确定方向
            Move(); //向上移动
            return;
        }
        synchronized (subQueue) {
            try {
                TimableOutput.println("Waiting-" + id);
                subQueue.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void OpenAndClose() {
        String outputCurrentFloor = StringCurrentFloor();
        TimableOutput.println("OPEN-" + outputCurrentFloor + "-" + id);
        Out(); //这里有可能人已经出尽了，一旦出尽，需要重新判断是上行还是下行
        advice = strategy.getAdvice(currentNum, currentFloor, direction,
                block, moved, maxFloor, minFloor, id);
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
            sleep(speed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (direction) {
            IncreaseFloor();
            String output = StringCurrentFloor(); //先输出到达结果，再唤醒，否同伴电梯可能被立刻唤醒，立刻移动，抢先输出到换乘层！
            TimableOutput.println("ARRIVE-" + output + "-" + id);
            if (this.friendEle != null) {
                synchronized (this.friendEle) {
                    int subFloor = (minFloor == 1) ? -1 : minFloor - 1;
                    int addFloor = (minFloor == -1) ? 1 : minFloor + 1;
                    if (minFloor > -4 && friendEle.getCurrentFloor() == subFloor
                            && currentFloor == addFloor) { //主动避让,离开换乘层
                        System.out.println(id + "号电梯正在释放换乘层");
                        this.friendEle.notifyAll(); //唤醒this下面的电梯
                        System.out.println("id: " + id + "释放了换乘层，唤醒了id: " + friendEle.catchId());
                    }
                }
            }
        } else {
            DecreaseFloor();
            String output = StringCurrentFloor();
            TimableOutput.println("ARRIVE-" + output + "-" + id);
            if (this.friendEle != null) {
                synchronized (this.friendEle) {
                    int addFloor = (maxFloor == -1) ? 1 : maxFloor + 1;
                    int subFloor = (maxFloor == 1) ? -1 : maxFloor - 1;
                    if (maxFloor < +7 && friendEle.getCurrentFloor() == addFloor
                            && currentFloor == subFloor) { //主动避让,离开换乘层
                        System.out.println(id + "号电梯正在释放换乘层");
                        this.friendEle.notifyAll(); //唤醒this上面的电梯
                        System.out.println("id: " + id + "释放了换乘层，唤醒了id: " + friendEle.catchId());
                    }
                }
            }
        }
        //String output = StringCurrentFloor();
        //TimableOutput.println("ARRIVE-" + output + "-" + id);
    }

    public void IncreaseFloor() {
        int goalFloor = (currentFloor == -1) ? 1 : currentFloor + 1;
        if (goalFloor > maxFloor) {
            System.out.println("下层电梯越上界");
            return;
        }
        synchronized (this) {
            System.out.println("因要上升锁了自己，电梯id号" + id);
            while (friendEle != null && friendEle.getCurrentFloor() == goalFloor) { //换乘层被占据
                try {
                    System.out.println(id + "号电梯换乘层被占据");
                    this.wait(); //wait() 等待唤醒
                    System.out.println(id + "号电梯被唤醒");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        currentFloor = goalFloor;
    }

    public void DecreaseFloor() {
        int goalFloor = (currentFloor == 1) ? -1 : currentFloor - 1;
        if (goalFloor < minFloor) {
            System.out.println("上层电梯越下界");
            return;
        }
        //synchronized (this) {
            System.out.println("因要下降锁了自己，电梯id号" + id);
            while (friendEle != null && friendEle.getCurrentFloor() == goalFloor) { //换乘层被占据
                try {
                    System.out.println(id + "号电梯换乘层被占据");
                    this.wait(); //wait() 等待唤醒
                    System.out.println(id + "号电梯被唤醒");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        //}
        currentFloor = goalFloor;
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
            } else if (this.friendEle != null) { //是双轿厢
                if (minFloor > -4 && currentFloor == minFloor &&
                        person.getToFloor() < minFloor) { //下界大于-4，也就是位于上方的轿厢
                    iterator.remove();
                    currentNum--;
                    person.resetElevatorId();//重置搭乘的电梯序号为-1
                    person.setFromFloor(currentFloor); //重置起始楼层
                    String output = StringCurrentFloor();
                    int personId = person.getPersonId();
                    TimableOutput.println("OUT-F-" + personId + "-" + output + "-" + id);
                    mainQueue.pushPersonRequest(person);
                }
                if (maxFloor < 7 && currentFloor == maxFloor &&
                        person.getToFloor() > maxFloor) { //上界小于7，也就是位于下方的轿厢
                    iterator.remove();
                    currentNum--;
                    person.resetElevatorId();//重置搭乘的电梯序号为-1
                    person.setFromFloor(currentFloor); //重置起始楼层
                    String output = StringCurrentFloor();
                    int personId = person.getPersonId();
                    TimableOutput.println("OUT-F-" + personId + "-" + output + "-" + id);
                    mainQueue.pushPersonRequest(person);
                }
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

    public void Scheduling(int times) {
        String outputCurrentFloor = StringCurrentFloor();
        TimableOutput.println("OPEN-" + outputCurrentFloor + "-" + id);//到达先开门
        try {
            sleep(times);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        AllOut();
        TimableOutput.println("CLOSE-" + outputCurrentFloor + "-" + id);
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
            }
        }
    }

    public void Updating() {
        Update update = subQueue.getUpdates().get(0);
        //从A移动到B
        int aid = update.getElevatorAId();
        int bid = update.getElevatorBId();

        ElevatorThread elevatorA = elevators.get(aid);
        ElevatorThread elevatorB = elevators.get(bid); // = this
        //电梯B在UPDATE状态
        //但是,电梯A未必在MOVED状态，如果不在MOVED，下一次循环必然会到MOVED，但是此时二者不同步
        //因此B主动等待，等A循环一次进入到MOVED再开始
        while (elevatorA.getAdvice() != Advice.MOVED) {
            try {
                //System.out.println("主动等待，因为A还没有进入MOVED状态");
                sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //System.out.println("A已经进入了MOVED状态");

        if (elevatorA.getCurrentNum() > 0) { //此处与SCHE不同在于，如果电梯内没有人是不能开门的
            elevatorA.Scheduling(400);//并不是真正的调度，只是借用它的函数，函数功能是开门，全部放出去，关门
        }
        if (elevatorB.getCurrentNum() > 0) {
            elevatorB.Scheduling(400);
        }

        //elevatorA.getSubQueue().transferFromSubToMain();  //subQueue的persons清空
        //elevatorB.getSubQueue().transferFromSubToMain();

        TimableOutput.println("UPDATE-BEGIN-" + aid + "-" + bid); //保证两个电梯均为空
        //BEGIN之后才能扔回去，否则提前扔回去有可能RECEIVE两次
        elevatorA.getSubQueue().transferFromSubToMain();  //subQueue的persons清空
        elevatorB.getSubQueue().transferFromSubToMain();

        try {
            sleep(1000); //睡眠1s
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int transferFloor = this.getSubQueue().getUpdates().get(0).getTransferFloor();
        elevatorA.setCurrentFloor(calculateNewFloorOfA(transferFloor));//
        elevatorB.setCurrentFloor(calculateNewFloorOfB(transferFloor));//瞬移到新楼层

        elevatorA.setFriendEle(elevatorB);
        elevatorB.setFriendEle(elevatorA);//二者互相是对方的同伴电梯

        //A 电梯运行范围为 目标楼层 ~ F7，B 电梯运行范围为 B4 ~ 目标楼层
        elevatorA.setMinFloor(transferFloor);//限定范围
        elevatorB.setMaxFloor(transferFloor);

        elevatorA.setSpeed(200);
        elevatorB.setSpeed(200);

        elevatorA.setDirection(false);
        elevatorB.setDirection(true);

        TimableOutput.println("UPDATE-END-" + aid + "-" + bid);

        elevatorB.getSubQueue().deleteFirstUpdate(); //最后才能结束阻塞
        Free(elevatorA);
        elevatorA.getSubQueue().transferFromCacheToMain(); //最后，subQueue的cache清空
        elevatorB.getSubQueue().transferFromCacheToMain(); //?取决于分配算法
        //Free(elevatorA);
    }

    public void Free(ElevatorThread elevator) {
        elevator.setMoved(false);
        elevator.getSubQueue().setMoving(false);//唤醒电梯A
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

    public synchronized SubQueue getSubQueue() {
        return subQueue;
    }

    public synchronized ArrayList<Person> getPersons() {
        return persons;
    }

    public synchronized int getCurrentNum() {
        return currentNum;
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public synchronized boolean direction() {
        return direction;
    }

    public synchronized void setDirection(boolean direction) {
        this.direction = direction;
    }

    public int calculateNewFloorOfA(int transferFloor) { //+1
        return transferFloor == -1 ? 1 : transferFloor + 1;
    }

    public int calculateNewFloorOfB(int transferFloor) { //-1
        return transferFloor == 1 ? -1 : transferFloor - 1;
    }

    public void setMoved(boolean bool) {
        moved = bool;
    }

    public boolean isMoved() {
        return moved;
    }

    public synchronized void setSpeed(int speed) {
        this.speed = speed;
    }

    public synchronized int getSpeed() {
        return speed;
    }

    public synchronized void setMaxFloor(int maxFloor) {
        this.maxFloor = maxFloor;
    }

    public synchronized void setMinFloor(int minFloor) {
        this.minFloor = minFloor;
    }

    public synchronized int getMaxFloor() {
        return maxFloor;
    }

    public synchronized int getMinFloor() {
        return minFloor;
    }

    public synchronized void setFriendEle(ElevatorThread elevator) {
        this.friendEle = elevator;
    }

    public synchronized ElevatorThread getFriendEle() {
        return friendEle;
    }

    public synchronized int catchId() {
        return id;
    }

    public Advice getAdvice() {
        return advice;
    }
}