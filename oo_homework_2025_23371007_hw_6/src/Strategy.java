import java.util.ArrayList;
import java.util.Comparator;

public class Strategy {
    private final SubQueue subQueue;
    private final ArrayList<Person> persons;//当前电梯里承的人

    public Strategy(SubQueue subQueue, ArrayList<Person> persons) {
        this.subQueue = subQueue;
        this.persons = persons;
    }

    public Advice getAdvice(int currentNum, int currentFloor, boolean direction, boolean block) {
        if (!subQueue.schesIsEmpty() && !block) { //在未封锁，也就是未执行sche任务的时候，检测到sche任务队列非空
            return Advice.BLOCK;
        }

        if (block) {
            if (canExecuteSche(currentFloor)) {
                return Advice.SCHE;
            } else if (hasScheInOriginDirection(currentFloor, direction)) {
                return Advice.MOVE;
            } else {
                return Advice.REVERSE;
            }
        }

        //block不成立时或者scheQueue为空，才执行以下代码
        //判断现在是否可以上下电梯
        if (canOpenForOut(currentNum, currentFloor) ||
                canOpenForIn(currentNum, currentFloor, direction)) {
            return Advice.OPEN;
        }
        //电梯内本身有乘客，但在这层既没有乘客要下，也没有乘客要上
        if (currentNum != 0) {
            return Advice.MOVE;//继续行驶
        }
        //没有乘客
        else {
            if (subQueue.bothEmpty()) { //没有请求
                if (subQueue.isEnd()) { //请求全部处理完了
                    return Advice.OVER;
                } else { //等待生产者发送等待新请求
                    return Advice.WAIT; //合理
                }
            } else { //没有乘客，但有请求-> !personQueue.isEmpty && scheQueue.isEmpty，不用再考虑sche的问题
                return processNonePeople(currentFloor, direction);
            }
        }
    }

    public boolean canExecuteSche(int currentFloor) {
        synchronized (subQueue) {
            Sche sche = subQueue.getSches().get(0);
            return sche.getToFloor() == currentFloor;
        }
    }

    public boolean hasScheInOriginDirection(int currentFloor, boolean direction) {
        synchronized (subQueue) {
            Sche sche = subQueue.getSches().get(0);
            return direction && sche.getToFloor() > currentFloor ||
                    !direction && sche.getToFloor() < currentFloor;
        }
    }

    public boolean canOpenForOut(int currentNum, int currentFloor) {
        return currentNum != 0 && (persons.stream().anyMatch(p -> p.getToFloor() == currentFloor));
        //自己独占的人员数组，不需要加锁
    }

    public boolean canOpenForIn(int currentNum, int currentFloor, boolean direction) {
        synchronized (subQueue) { //必须加锁，防止读写冲突
            return (currentNum < 6) &&
                    (subQueue.getPersons().stream().
                            anyMatch(p -> p.getFromFloor() == currentFloor
                                    && p.getDirection() == direction));
        }
    }

    public Advice processNonePeople(int currentFloor, boolean direction) {
        //此时电梯里无人
        synchronized (subQueue) {
            Person mainPerson = subQueue.getPersons().stream()
                    .max(Comparator.comparing(Person::getPriority)).orElse(null);
            //System.out.println(mainPerson.getPersonId()+" "+mainPerson.getPriority());
            if (mainPerson == null) { //理论上不可能产生
                System.out.println("Error Judgement In personQueue.getRequestCount() != 0");
                return Advice.OVER;
            }
            if ((mainPerson.getFromFloor() < currentFloor && direction) ||
                    (mainPerson.getFromFloor() > currentFloor && !direction) ||
                    mainPerson.getFromFloor() == currentFloor &&
                            mainPerson.getDirection() != direction) {
                //最高优先级乘客在下层，但此时电梯将要上行
                //最高优先级乘客在上层，但此时电梯将要下行
                //或者是最高优先级就在当前层，但电梯原本行进方向与该乘客目标方向不一致，导致在第一次判定能否进入时失败，需要反转方向，再次判定能否进入
                //System.out.println("Reverse");
                return Advice.REVERSE; //反转方向
            } else {
                //最高优先级乘客在下层，但此时方向也向下
                //最高优先级乘客在上层，但此时方向也要向上
                return Advice.MOVE;
            }
        }
    }
}
