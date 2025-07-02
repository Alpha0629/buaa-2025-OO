import com.oocourse.elevator3.ScheRequest;

public class Sche implements Request {
    private final int elevatorId;
    private final int speed;
    private final int toFloor;

    public Sche(ScheRequest scheRequest) {
        this.elevatorId = scheRequest.getElevatorId();
        this.speed = (int) (1000 * scheRequest.getSpeed());

        if (scheRequest.getToFloor().charAt(0) == 'B') {
            this.toFloor = -1 * Integer.parseInt(scheRequest.getToFloor().substring(1));
        } else { //F
            this.toFloor = Integer.parseInt(scheRequest.getToFloor().substring(1));
        }
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getSpeed() {
        return this.speed;
    }

    public int getToFloor() {
        return this.toFloor;
    }
}
