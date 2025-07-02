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
                if (request instanceof Sche) {
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

    private void dispatchSche(Sche sche) {
        subRequestQueues.get(sche.getElevatorId()).pushScheRequest(sche);
    }

    private void dispatchPerson(Person person) {
        int parameter = 0;
        int target = 0;
        for (int i = 1; i <= 6; i++) {
            int tempParameter = weightedCaculation(elevators.get(i), person);
            if (tempParameter > parameter) { //大于再更新，保证优先放在小序号电梯内
                parameter = tempParameter;
                target = i;
            }
        }
        person.setElevatorId(target);
        subRequestQueues.get(target).pushPersonRequest(person);
    }

    private int weightedCaculation(ElevatorThread elevator, Person person) {
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
    }

    private int caculateDistance(ElevatorThread elevator, Person person) {
        boolean isSameDirection = true;
        int currentFloor = elevator.getCurrentFloor();
        boolean direction = elevator.direction();

        if (currentFloor == person.getFromFloor()) { //能够立刻接上
            //finalFloor = currentFloor;
            return 0;
        } else if (direction && person.getFromFloor() > currentFloor ||
                !direction && person.getFromFloor() < currentFloor) {
            return Math.abs(currentFloor - person.getFromFloor());
            //能够顺带接上
        }

        if (elevator.getPersons().isEmpty()) {
            return Math.abs(currentFloor - person.getFromFloor());
        }
        //无法立刻顺带接上
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

    private int caculateFinalFloorWhileSche(Sche sche) {
        int finalFloor = 0;
        finalFloor = sche.getToFloor();
        return finalFloor;
    }
}
