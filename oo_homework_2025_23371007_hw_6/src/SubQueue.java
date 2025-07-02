import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class SubQueue {
    private boolean isEnd;
    private final MainQueue mainQueue;
    private final ArrayList<Person> persons;
    private final ArrayList<Person> cache; //缓存
    private final ArrayList<Sche> sches;
    private boolean block;

    public SubQueue(MainQueue mainQueue) {
        this.isEnd = false;
        this.mainQueue = mainQueue;
        this.persons = new ArrayList<>();
        this.cache = new ArrayList<>();
        this.sches = new ArrayList<>();
    }

    public synchronized void pushPersonRequest(Person person) {
        if (sches.isEmpty()) {
            TimableOutput.println("RECEIVE-" + person.getPersonId() + "-" + person.getElevatorId());
            persons.add(person);
        } else {
            cache.add(person);
        }
        notifyAll();
    }

    public synchronized void pushScheRequest(Sche sche) {
        sches.add(sche);
        notifyAll();
    }

    public synchronized void transferFromSubToMain() {
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            mainQueue.pushPersonRequest(person);
            iterator.remove();
        }
    }

    public synchronized void transferFromCacheToSub() {
        Iterator<Person> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            TimableOutput.println("RECEIVE-" + person.getPersonId() + "-" + person.getElevatorId());
            persons.add(person);
            iterator.remove();
        }
        notifyAll();
    }

    public synchronized Person pullGivenRequest(int currentFloor, boolean direction) {
        if (persons.isEmpty()) {
            //();
            return null;
        } else {
            ArrayList<Person> tempPersons = new ArrayList<>(persons);
            tempPersons.sort((p1, p2) -> Integer.compare(p2.getPriority(), p1.getPriority()));
            for (Person person : tempPersons) {
                boolean sameDirection = person.getDirection() == direction;
                if (person.getFromFloor() == currentFloor && sameDirection) {
                    persons.remove(person); //从原列表中删除
                    //notifyAll();
                    return person;
                }
            }
        }
        //notifyAll();
        return null;
    }

    public synchronized void deleteFirstSche() {
        if (sches.isEmpty()) {
            System.out.println("Sche queue is already empty, Error!");
        }
        sches.remove(0);
        //notifyAll();
    }

    public synchronized ArrayList<Person> getPersons() {
        //notifyAll();
        return persons;
    }

    public synchronized ArrayList<Sche> getSches() {
        //notifyAll();
        return sches;
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean bothEmpty() {
        //notifyAll();
        return persons.isEmpty() && sches.isEmpty();
    }

    public synchronized boolean personsIsEmpty() {
        return persons.isEmpty();
    }

    public synchronized boolean schesIsEmpty() {
        return sches.isEmpty();
    }

    public synchronized boolean getBlocke() {
        return block;
    }

    public synchronized void setBlock(boolean block) {
        this.block = block;
    }
}
