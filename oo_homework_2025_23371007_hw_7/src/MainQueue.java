import java.util.ArrayList;

public class MainQueue {
    private final ArrayList<Person> persons;
    private final ArrayList<Sche> sches;
    private final ArrayList<Update> updates;
    private boolean isInputEnd;
    private int notFinished;

    public MainQueue() {
        persons = new ArrayList<>();
        sches = new ArrayList<>();
        updates = new ArrayList<>();
        isInputEnd = false;
        notFinished = 0;
    }

    public synchronized void pushPersonRequest(Person person) {
        persons.add(person);
        notifyAll();
    }

    public synchronized void pushScheRequest(Sche sche) {
        sches.add(sche);
        notifyAll();
    }

    public synchronized void pushUpdateRequest(Update update) {
        updates.add(update);
        notifyAll();
    }

    public synchronized Request pullRequest() {
        if (allEmpty() && !isRealEnd()) {
            try {
                //System.out.println("Waiting for real end");
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (isRealEnd()) {
            return null;
        }
        if (allEmpty()) {
            return null;
        }

        if (!updates.isEmpty()) {
            return updates.remove(0);
        } else if (!sches.isEmpty()) {
            return sches.remove(0);
        } else if (!persons.isEmpty()) {
            return persons.remove(0);
        }
        return null;
    }

    public synchronized void addNotFinished() {
        notFinished++;
    }

    public synchronized void subNotFinished() {
        notFinished--;
        if (isFinished()) {
            notifyAll();
        }
    }

    public synchronized boolean isFinished() {
        return notFinished == 0;
    }

    public synchronized boolean isRealEnd() {
        return isInputEnd && allEmpty() && isFinished();
    }

    public synchronized void setInputEnd() {
        isInputEnd = true;
        notifyAll(); //关键
    }

    public synchronized boolean allEmpty() {
        return persons.isEmpty() && sches.isEmpty() && updates.isEmpty();
    }
}
