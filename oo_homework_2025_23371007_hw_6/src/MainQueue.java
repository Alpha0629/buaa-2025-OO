import java.util.ArrayList;

public class MainQueue {
    private final ArrayList<Person> persons;
    private final ArrayList<Sche> sches;
    private boolean isInputEnd;
    private int notFinished;

    public MainQueue() {
        persons = new ArrayList<>();
        sches = new ArrayList<>();
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

    public synchronized Request pullRequest() {
        if (bothEmpty() && !isRealEnd()) {
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
        if (bothEmpty()) {
            return null;
        }

        if (!sches.isEmpty()) {
            return sches.remove(0);
        } else if (!persons.isEmpty()) {
            //System.out.println("pulling persons");
            return persons.remove(0);
        }
        return null;
    }

    public synchronized void addNotFinished() {
        notFinished++;
    }

    public synchronized void subNotFinished() {
        notFinished--;
        //System.out.println(notFinished);
        if (isFinished()) {
            notifyAll();
        }
        //notifyAll(); //调用该方法代表有一个乘客真正的出去了，
    }

    public synchronized boolean isFinished() {
        return notFinished == 0;
    }

    public synchronized boolean isRealEnd() {
        return isInputEnd && bothEmpty() && isFinished();
    }

    public synchronized void setInputEnd() {
        isInputEnd = true;
        notifyAll(); //关键
    }

    public synchronized boolean bothEmpty() {
        //notifyAll();
        return persons.isEmpty() && sches.isEmpty();
    }
}
