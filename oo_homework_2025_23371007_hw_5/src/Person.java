import com.oocourse.elevator1.PersonRequest;

public class Person {
    private final int personId;
    private final int fromFloor;
    private final int toFloor;
    private final int elevatorId;
    private final int priority;
    private final boolean direction;

    public Person(PersonRequest personRequest) {
        this.personId = personRequest.getPersonId();
        //System.out.println(personId);
        this.elevatorId = personRequest.getElevatorId();
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
    }

    public int getPersonId() {
        return personId;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getPriority() {
        return priority;
    }

    public boolean getDirection() {
        return direction;
    }
}
