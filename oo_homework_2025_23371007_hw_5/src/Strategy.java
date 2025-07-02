import java.util.ArrayList;
import java.util.Comparator;

public class Strategy {
    private final RequestTable requestTable;//当前电梯的请求队列
    private final ArrayList<Person> persons;//当前电梯里承的人

    public Strategy(RequestTable requestTable, ArrayList<Person> persons) {
        this.requestTable = requestTable;
        this.persons = persons;
    }

    public Advice getAdvice(int currentNum, int currentFloor, boolean direction) {
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
            if (requestTable.getRequestCount() == 0) { //没有请求
                if (requestTable.isEnd()) { //请求全部处理完了
                    return Advice.OVER;
                } else { //等待生产者发送等待新请求
                    return Advice.WAIT;
                }
            } else { //没有乘客，但有请求，电梯初次也是运行从这里开始的
                return processNonePeople(currentFloor, direction);
            }
        }
    }

    public boolean canOpenForOut(int currentNum, int currentFloor) {
        return currentNum != 0 && (persons.stream().anyMatch(p -> p.getToFloor() == currentFloor));
        //自己独占的人员数组，不需要加锁
    }

    public boolean canOpenForIn(int currentNum, int currentFloor, boolean direction) {
        synchronized (requestTable) { //必须加锁，防止读写冲突
            return (currentNum < 6) &&
                    (requestTable.getPersons().stream().
                            anyMatch(p -> p.getFromFloor() == currentFloor
                                    && p.getDirection() == direction));
        }
    }

    public Advice processNonePeople(int currentFloor, boolean direction) {
        //此时电梯里无人
        synchronized (requestTable) {
            Person mainPerson = requestTable.getPersons().stream()
                    .max(Comparator.comparing(Person::getPriority)).orElse(null);
            //System.out.println(mainPerson.getPersonId()+" "+mainPerson.getPriority());
            if (mainPerson == null) { //理论上不可能产生
                System.out.println("Error Judgement In requestTable.getRequestCount() != 0");
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
