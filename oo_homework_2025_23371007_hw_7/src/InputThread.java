import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

public class InputThread extends Thread {
    private final MainQueue mainQueue;

    public InputThread(MainQueue mainQueue) {
        this.mainQueue = mainQueue;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                mainQueue.setInputEnd();
                //System.out.println("End of Input");
                break;
            }
            if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest);
                mainQueue.addNotFinished(); //每个人的请求，对应着一个未完成的标记
                mainQueue.pushPersonRequest(person);
            } else if (request instanceof ScheRequest) {
                ScheRequest scheRequest = (ScheRequest) request;
                Sche sche = new Sche(scheRequest);
                mainQueue.pushScheRequest(sche);
            } else if (request instanceof UpdateRequest) {
                UpdateRequest updateRequest = (UpdateRequest) request;
                Update update = new Update(updateRequest);
                mainQueue.pushUpdateRequest(update);
            } else {
                System.out.println("Invalid request type");
            }
        }
        try {
            elevatorInput.close();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}