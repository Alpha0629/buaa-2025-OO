import com.oocourse.elevator2.ScheRequest;

public class Sche implements Request {
    private final int elevatorId;
    private final double speed;
    private final int toFloor;

    public Sche(ScheRequest scheRequest) {
        this.elevatorId = scheRequest.getElevatorId();
        this.speed = scheRequest.getSpeed();

        if (scheRequest.getToFloor().charAt(0) == 'B') {
            this.toFloor = -1 * Integer.parseInt(scheRequest.getToFloor().substring(1));
        } else { //F
            this.toFloor = Integer.parseInt(scheRequest.getToFloor().substring(1));
        }
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public double getSpeed() {
        return this.speed;
    }

    public int getToFloor() {
        return this.toFloor;
    }
}
