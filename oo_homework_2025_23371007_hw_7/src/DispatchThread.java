import java.util.ArrayList;
import java.util.HashMap;

public class DispatchThread extends Thread {
    private final MainQueue mainQueue;
    private final HashMap<Integer, SubQueue> subRequestQueues;
    private final HashMap<Integer, ElevatorThread> elevators;

    public DispatchThread(MainQueue mainQueue, HashMap<Integer, SubQueue> subRequestQueues,
                          HashMap<Integer, ElevatorThread> elevators) {
        this.mainQueue = mainQueue;
        this.subRequestQueues = subRequestQueues;
        this.elevators = elevators;
    }

    @Override
    public void run() {
        while (true) {
            if (mainQueue.isRealEnd()) { //只有当真正处理完全部请求，才结束线程
                //System.out.println("mainQueue is real end!");
                for (SubQueue subQueue : subRequestQueues.values()) {
                    subQueue.setEnd();
                }
                break;
            }
            Request request = mainQueue.pullRequest();
            if (request == null) {
                continue;
            } else {
                if (request instanceof Update) {
                    Update update = (Update) request;
                    dispatchUpdate(update);
                } else if (request instanceof Sche) {
                    Sche sche = (Sche) request;
                    dispatchSche(sche);
                } else if (request instanceof Person) {
                    Person person = (Person) request;
                    dispatchPerson(person);
                }
                continue; //不会发生
            }
        }
    }

    private void dispatchUpdate(Update update) {
        int aid = update.getElevatorAId();
        int bid = update.getElevatorBId();

        elevators.get(aid).setMoved(true);
        subRequestQueues.get(aid).setMoving(true);
        subRequestQueues.get(bid).pushUpdateRequest(update); //先让A进入被移动状态，B再进入更新状态

        //elevators.get(aid).setMoved(true);
        //subRequestQueues.get(aid).setMoved(true);
    }

    private void dispatchSche(Sche sche) {
        subRequestQueues.get(sche.getElevatorId()).pushScheRequest(sche);
    }

    private void dispatchPerson(Person person) {
        int parameter = -1;
        int target = 0;
        for (int i = 1; i <= 6; i++) {
            int tempParameter = weightCalculation(elevators.get(i), person);
            //System.out.println(tempParameter);
            if (tempParameter > parameter) { //大于再更新，保证优先放在小序号电梯内
                parameter = tempParameter;
                target = i;
            }
        }
        person.setElevatorId(target);
        subRequestQueues.get(target).pushPersonRequest(person);
    }

    public int weightCalculation(ElevatorThread elevator, Person person) {
        int weight;
        int distance;
        int minFloor = elevator.getMinFloor();
        int maxFloor = elevator.getMaxFloor();
        int needTransfer = 0;
        int isScheduling = 0;
        int isUpdating = 0; //B电梯正在更新
        int isMoved = 0; //A电梯正在被合并
        //先对关于双轿厢的性质进行判定
        if (canNotIn(person, maxFloor, minFloor)) { //根本无法接到，没有任何可能
            return 0;
        } else if (canInButMeaningless(person, maxFloor, minFloor)) {
            return 1;
        } else if (canInButNeedTransfer(person, maxFloor, minFloor)) {
            needTransfer = 1;
        }

        SubQueue subQueue = elevator.getSubQueue();

        synchronized (subQueue) {
            if (!elevator.getSubQueue().getSches().isEmpty()) { //如果电梯目前有SCHE指令
                isScheduling = 1;
            } else if (!elevator.getSubQueue().getUpdates().isEmpty()) { //正在update
                isUpdating = 1;
            } else if (elevator.isMoved()) { //正在被合并
                isMoved = 1;
            }

            if (subQueue.getPersons().isEmpty() && elevator.getPersons().isEmpty()) {
                distance = Math.abs(person.getFromFloor() - elevator.getCurrentFloor());
            } else {
                distance = caculateDistance(elevator, person);
            }
        }

        weight = 10000 - 50 * isScheduling - 50 * isUpdating - 50 * isMoved
                - 2 * elevator.getSubQueue().getAllPersonsCount()
                - 2 * elevator.getCurrentNum() - 4 * (elevator.getSpeed() / 100)
                - 3 * distance - 6 * needTransfer;
        //System.out.println("id: " + elevator.catchId() + " speed: " + elevator.getSpeed()
        // + " distance:" + distance + " needTransfer:" + needTransfer);
        return weight;
    }

    public boolean canNotIn(Person person, int maxFloor, int minFloor) {
        return !(person.getFromFloor() >= minFloor && person.getFromFloor() <= maxFloor);
    }

    public boolean canInButMeaningless(Person person, int maxFloor, int minFloor) {
        boolean direction = person.getDirection();
        int fromFloor = person.getFromFloor();
        /*if (minFloor > -4) { //上部的电梯
            return fromFloor == minFloor && !direction;
        }
        if (maxFloor < +7) {
            return fromFloor == maxFloor && direction;
        }*/
        return (fromFloor == minFloor && !direction) || (fromFloor == maxFloor && direction);
        //return false;
    }

    public boolean canInButNeedTransfer(Person person, int maxFloor, int minFloor) {
        int toFloor = person.getToFloor();
        /*if (minFloor > -4) {
            return toFloor < minFloor;
        }
        if (maxFloor < +7) {
            return toFloor > maxFloor;
        }*/
        return (toFloor < minFloor) || (toFloor > maxFloor); //如果是双轿厢，可以正常判定，如果是单轿厢，一定返回false
        //return false;
    }

    /*private int weightedCalculation(ElevatorThread elevator, Person person) {
        int weight = -Integer.MAX_VALUE;
        int distance = -1;
        int isScheduling = 0;
        SubQueue subqueue = elevator.getSubQueue();
        synchronized (subqueue) {
            if (subqueue.bothEmpty()) { //如果当前电梯的请求队列全部为空
                if (elevator.getPersons().isEmpty()) {
                    //这个电梯处于wait状态
                    distance = Math.abs(person.getFromFloor() - elevator.getCurrentFloor());
                    weight = 100 - 2 * distance; //权重最高
                    //System.out.println(weight);
                    return weight; // 提前返回，不需要后续加权
                } else { //电梯正在运行最后一批乘客
                    distance = caculateDistance(elevator, person);
                }
            } else if (!subqueue.schesIsEmpty() &&
                    subqueue.personsIsEmpty()) {
                //电梯的请求队列只有一个调度请求
                Sche sche = subqueue.getSches().get(0);
                distance = Math.abs(caculateFinalFloorWhileSche(sche) - person.getFromFloor());
                isScheduling = 1;
            } else if (subqueue.schesIsEmpty() &&
                    !subqueue.personsIsEmpty()) {
                //没有调度请求，但是有等待的乘客，也就代表这部分人马上就要进入电梯内
                distance = caculateDistance(elevator, person);
            } else {
                Sche sche = subqueue.getSches().get(0);
                distance = Math.abs(caculateFinalFloorWhileSche(sche) - person.getFromFloor());
                isScheduling = 1;
            }
        }

        weight = 100 - 50 * isScheduling - 2 * elevator.getSubQueue().getPersons().size()
                - 2 * elevator.getCurrentNum() - 3 * distance;
        return weight;
    }*/

    private int caculateDistance(ElevatorThread elevator, Person person) {
        int currentFloor = elevator.getCurrentFloor();
        boolean direction = elevator.direction();
        int parameter1;
        int parameter2;

        if (currentFloor == person.getFromFloor()) { //能够立刻接上
            //finalFloor = currentFloor;
            return 0;
        } else if (direction && person.getFromFloor() > currentFloor ||
                !direction && person.getFromFloor() < currentFloor) {
            return Math.abs(currentFloor - person.getFromFloor());
            //能够顺带接上
        }

        //无法立刻顺带接上
        parameter1 = calculatePersonInEle(elevator, person);
        parameter2 = calculatePersonInSub(elevator, person);
        return (parameter1 * 2 + parameter2) / 3;
    }

    public int calculatePersonInEle(ElevatorThread elevator, Person person) {
        boolean isSameDirection = true;
        int currentFloor = elevator.getCurrentFloor();
        boolean direction = elevator.direction();

        if (elevator.getPersons().isEmpty()) {
            return 0;
        }

        for (Person personInEle : elevator.getPersons()) {
            if (direction && personInEle.getToFloor() > currentFloor ||
                    !direction && personInEle.getToFloor() < currentFloor) {
                //电梯里的人只需要继续往上坐即可
                //isSameDirection = true;
            } else {
                isSameDirection = false;
                break;
            }
        }
        int finalFloor;
        if (isSameDirection) {
            int maxToFloor = Integer.MIN_VALUE;
            int minToFloor = Integer.MAX_VALUE;
            for (Person personInEle : elevator.getPersons()) {
                if (personInEle.getToFloor() > maxToFloor) {
                    maxToFloor = personInEle.getToFloor();
                }
                if (personInEle.getToFloor() < minToFloor) {
                    minToFloor = personInEle.getToFloor();
                }
            }
            if (direction) {
                finalFloor = maxToFloor;
            } else {
                finalFloor = minToFloor;
            }
        } else {
            int maxToFloor = Integer.MIN_VALUE;
            int minToFloor = Integer.MAX_VALUE;
            for (Person personInEle : elevator.getPersons()) {
                if (direction && personInEle.getToFloor() < currentFloor ||
                        !direction && personInEle.getToFloor() > currentFloor) {
                    //对于全体乘客方向与当前电梯方向不一致的
                    if (personInEle.getToFloor() > maxToFloor) {
                        maxToFloor = personInEle.getToFloor();
                    }
                    if (personInEle.getToFloor() < minToFloor) {
                        minToFloor = personInEle.getToFloor();
                    }
                }
            }
            if (direction) {
                finalFloor = minToFloor;
            } else {
                finalFloor = maxToFloor;
            }
        }

        return Math.abs(finalFloor - person.getFromFloor());
    }

    public int calculatePersonInSub(ElevatorThread elevator, Person person) {
        SubQueue subQueue = elevator.getSubQueue();

        if (subQueue.getAllPersonsCount() == 0) {
            return 0;
        }

        ArrayList<Person> tempPersons = new ArrayList<>();
        tempPersons.addAll(subQueue.getPersons());
        tempPersons.addAll(subQueue.getCache());

        int currentFloor = elevator.getCurrentFloor();
        boolean direction = elevator.direction();

        int finalFloor1 = currentFloor;
        int finalFloor2 = currentFloor;
        for (Person temp : tempPersons) {
            if (direction && temp.getToFloor() > finalFloor1 ||
                    !direction && temp.getToFloor() < finalFloor1) {
                finalFloor1 = temp.getToFloor();
            } else {
                finalFloor2 = temp.getToFloor();
            }
        }
        return (Math.abs(finalFloor1 - person.getFromFloor()) +
                Math.abs(finalFloor2 - person.getFromFloor())) / 2;
    }

    /*private int caculateFinalFloorWhileSche(Sche sche) {
        int finalFloor = 0;
        finalFloor = sche.getToFloor();
        return finalFloor;
    }*/
}
