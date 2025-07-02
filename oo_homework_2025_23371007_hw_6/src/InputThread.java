import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;

import java.util.HashMap;

public class InputThread extends Thread {
    private final MainQueue mainQueue;
    private final HashMap<Integer, SubQueue> subQueues;

    public InputThread(MainQueue mainQueue, HashMap<Integer, SubQueue> subQueues) {
        this.mainQueue = mainQueue;
        this.subQueues = subQueues;
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