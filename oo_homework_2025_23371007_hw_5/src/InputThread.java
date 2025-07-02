import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

public class InputThread extends Thread {
    private final RequestTable requestTable;

    public InputThread(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest personRequest = (PersonRequest) elevatorInput.nextRequest();
            if (personRequest == null) {
                requestTable.setEnd();
                //System.out.println("Input ended");
                break;
            }
            Person person = new Person(personRequest);
            requestTable.pushRequest(person);
        }
    }
}