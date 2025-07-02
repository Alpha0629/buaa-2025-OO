import java.util.ArrayList;
import java.util.Comparator;

public class Strategy {
    private final SubQueue subQueue;
    private final ArrayList<Person> persons;//当前电梯里承的人

    public Strategy(SubQueue subQueue, ArrayList<Person> persons) {
        this.subQueue = subQueue;
        this.persons = persons;
    }

    public Advice getAdvice(int currentNum, int currentFloor, boolean direction,
                            boolean block, boolean moved, int maxFloor, int minFloor, int id) {
        if (!subQueue.updatesIsEmpty()) {
            return Advice.UPDATE;
        }

        if (moved) { //被移动的A电梯，如果A正在被移动，进入MOVED
            return Advice.MOVED;
        }

        if (!subQueue.schesIsEmpty() && !block) { //在未封锁，也就是未执行sche任务的时候，检测到sche任务队列非空
            return Advice.BLOCK;
        }

        if (block) { //代表进入临时调度，临时调度的电梯一定不是双轿厢电梯，故不需要考虑maxFloor，minFloor问题
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
        if (canOpenForOut(currentNum, currentFloor, maxFloor, minFloor) ||
                canOpenForIn(currentNum, currentFloor, direction)) {
            return Advice.OPEN;
        }
        //电梯内本身有乘客，但在这层既没有乘客要下，也没有乘客要上
        if (currentNum != 0) {
            System.out.println("策略中要移动");
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
                return processNonePeople(currentFloor, direction, id);
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

    public boolean canOpenForOut(int currentNum, int currentFloor, int maxFloor, int minFloor) {
        if (currentNum == 0) {
            return false;
        }
        if (persons.stream().anyMatch(p -> p.getToFloor() == currentFloor)) {
            return true; //存在一个在电梯中的人，就要在当前层下去
        }

        if (currentFloor == maxFloor) { //达到允许到达的最高层处
            if (persons.stream().anyMatch(p -> p.getToFloor() > currentFloor)) {
                //存在一个人还想继续向上，也就是超出该电梯的运行范围了
                return true;//现在必须出去，准备换乘其他电梯
            }
        }
        if (currentFloor == minFloor) { //达到允许到达的最低层处
            if (persons.stream().anyMatch(p -> p.getToFloor() < currentFloor)) {
                //存在一个人还想继续向下，也即是超出范围
                return true; //必须出去，准备换乘其他电梯
            }
        }
        return false;
    }

    public boolean canOpenForIn(int currentNum, int currentFloor, boolean direction) {
        synchronized (subQueue) { //必须加锁，防止读写冲突
            return (currentNum < 6) &&
                    (subQueue.getPersons().stream(). //方向一致即可，代表暂时可以搭乘这部电梯，不考虑潜在的换乘问题
                            anyMatch(p -> p.getFromFloor() == currentFloor
                            && p.getDirection() == direction));
        }
    }

    public Advice processNonePeople(int currentFloor, boolean direction, int id) {
        //此时电梯里无人
        synchronized (subQueue) {
            Person mainPerson = subQueue.getPersons().stream()
                    .max(Comparator.comparing(Person::getPriority)).orElse(null);
            //System.out.println(mainPerson.getPersonId()+" "+mainPerson.getPriority());
            if (mainPerson == null) {
                System.out.println("理论上不可能产生");
                return Advice.OVER;
            }
            if ((mainPerson.getFromFloor() < currentFloor && direction) ||
                    (mainPerson.getFromFloor() > currentFloor && !direction) ||
                    mainPerson.getFromFloor() == currentFloor &&
                            mainPerson.getDirection() != direction) {
                //最高优先级乘客在下层，但此时电梯将要上行
                //最高优先级乘客在上层，但此时电梯将要下行
                //或者是最高优先级就在当前层，但电梯原本行进方向与该乘客目标方向不一致，导致在第一次判定能否进入时失败，需要反转方向，再次判定能否进入
                System.out.println("此时电梯" + id + "内无人，Reverse去接人");
                return Advice.REVERSE; //反转方向
            } else {
                //最高优先级乘客在下层，但此时方向也向下
                //最高优先级乘客在上层，但此时方向也要向上
                System.out.println("此时电梯" + id + "内无人，按原方向MOVE去接人");
                return Advice.MOVE;
            }
        }
    }
}
