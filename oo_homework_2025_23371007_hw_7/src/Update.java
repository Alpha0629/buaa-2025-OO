import com.oocourse.elevator3.UpdateRequest;

public class Update implements Request {
    private final int elevatorId1;
    private final int elevatorId2;
    private final int transferFloor;

    public Update(UpdateRequest updateRequest) {
        this.elevatorId1 = updateRequest.getElevatorAId();
        this.elevatorId2 = updateRequest.getElevatorBId();

        if (updateRequest.getTransferFloor().charAt(0) == 'B') {
            this.transferFloor =
                    -1 * Integer.parseInt(updateRequest.getTransferFloor().substring(1));
        } else { //F
            this.transferFloor = Integer.parseInt(updateRequest.getTransferFloor().substring(1));
        }
    }

    public int getElevatorAId() {
        return elevatorId1;
    }

    public int getElevatorBId() {
        return elevatorId2;
    }

    public int getTransferFloor() {
        return transferFloor;
    }
}
