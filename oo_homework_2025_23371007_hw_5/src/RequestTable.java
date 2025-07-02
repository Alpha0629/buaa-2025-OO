import java.util.ArrayList;

public class RequestTable {
    private boolean isEnd;
    private int requestCount;
    private final ArrayList<Person> persons;

    public RequestTable() {
        this.isEnd = false;
        this.requestCount = 0;
        this.persons = new ArrayList<>();
    }

    public synchronized void pushRequest(Person person) {
        persons.add(person);
        requestCount++;
        notifyAll();
    }

    public synchronized Person pullRequest() {
        if (requestCount == 0 && !isEnd) {
            waitRequest();
        }
        if (requestCount == 0) {
            return null;
        }
        requestCount--;
        notifyAll();
        return persons.remove(0);
    }

    public synchronized Person pullGivenRequest(int currentFloor, boolean direction) {
        /*if (requestCount == 0 && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }*/
        if (requestCount == 0) {
            notifyAll();
            return null;
        } else {
            ArrayList<Person> tempPersons = new ArrayList<>(persons);
            tempPersons.sort((p1, p2) -> Integer.compare(p2.getPriority(), p1.getPriority()));
            for (Person person : tempPersons) {
                boolean sameDirection = person.getDirection() == direction;
                if (person.getFromFloor() == currentFloor && sameDirection) {
                    persons.remove(person); //从原列表中删除
                    requestCount--;
                    notifyAll();
                    return person;
                }
            }
        }
        notifyAll();
        return null;
    }

    public synchronized ArrayList<Person> getPersons() {
        notifyAll();
        return persons;
    }

    public synchronized void setEnd() {
        this.isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return this.isEnd;
    }

    public synchronized int getRequestCount() {
        notifyAll();
        return this.requestCount;
    }

    public synchronized void waitRequest() {
        try {
            this.wait();
            //System.out.println("Waiting for request");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
