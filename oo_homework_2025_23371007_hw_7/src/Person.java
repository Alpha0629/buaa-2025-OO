import com.oocourse.elevator3.PersonRequest;

public class Person implements Request {
    private final int personId;
    private int fromFloor;
    private final int toFloor;
    private int elevatorId;
    private final int priority;
    private boolean direction;
    private boolean distributed;

    public Person(PersonRequest personRequest) {
        this.personId = personRequest.getPersonId();
        this.elevatorId = -1; //表示未分配
        this.priority = personRequest.getPriority();
        if (personRequest.getFromFloor().charAt(0) == 'B') {
            this.fromFloor = -1 * Integer.parseInt(personRequest.getFromFloor().substring(1));
        } else { //F
            this.fromFloor = Integer.parseInt(personRequest.getFromFloor().substring(1));
        }
        if (personRequest.getToFloor().charAt(0) == 'B') {
            this.toFloor = -1 * Integer.parseInt(personRequest.getToFloor().substring(1));
        } else { //F
            this.toFloor = Integer.parseInt(personRequest.getToFloor().substring(1));
        }

        //上行or下行
        this.direction = fromFloor < toFloor;
        this.distributed = false;
    }

    public int getPersonId() {
        return personId;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public void setFromFloor(int fromFloor) {
        this.fromFloor = fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public void setElevatorId(int elevatorId) {
        this.distributed = true;
        this.elevatorId = elevatorId; //提供重新分配电梯号的接口
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getPriority() {
        return priority;
    }

    public boolean getDirection() {
        return fromFloor < toFloor;
    }

    public void setDirection() {
        this.direction = fromFloor < toFloor;
    }

    public void resetElevatorId() {
        this.elevatorId = -1;
        this.distributed = false;
    }
}
