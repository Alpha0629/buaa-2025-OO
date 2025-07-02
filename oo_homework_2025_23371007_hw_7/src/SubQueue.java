import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class SubQueue {
    private boolean isEnd;
    private final MainQueue mainQueue;
    private final ArrayList<Person> persons;
    private final ArrayList<Person> cache; //缓存
    private final ArrayList<Sche> sches;
    private final ArrayList<Update> updates;
    private boolean moving;

    public SubQueue(MainQueue mainQueue) {
        this.isEnd = false;
        this.mainQueue = mainQueue;
        this.persons = new ArrayList<>();
        this.cache = new ArrayList<>();
        this.sches = new ArrayList<>();
        this.updates = new ArrayList<>();
        this.moving = false;
    }

    public synchronized void pushPersonRequest(Person person) {
        if (sches.isEmpty() && updates.isEmpty() && !moving) {
            TimableOutput.println("RECEIVE-" + person.getPersonId() + "-" + person.getElevatorId());
            persons.add(person); //先输出RECEIVE再添加到数组中，否则时间顺序可能会出错
            notifyAll();
        } else {
            cache.add(person);  //向cache添加的时候不要唤醒
            System.out.println(person.getPersonId() + "添加到了" + person.getElevatorId() + "号电梯的cache中");
        }
        //notifyAll();
    }

    public synchronized void pushScheRequest(Sche sche) {
        sches.add(sche);
        notifyAll();
    }

    public synchronized void pushUpdateRequest(Update update) {
        updates.add(update);
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
        notifyAll();//这里再唤醒，等价于把人从main转移到sub的persons中
    }

    public synchronized void transferFromCacheToMain() {
        Iterator<Person> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            mainQueue.pushPersonRequest(person);
            iterator.remove();
        }
        //notifyAll();
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
            //System.out.println("Sche queue is already empty, Error!");\
            //do nothing
        }
        sches.remove(0);
    }

    public synchronized void deleteFirstUpdate() {
        if (updates.isEmpty()) {
            //System.out.println("Update queue is already empty, Error!");
            //do nothing
        }
        updates.remove(0);
    }

    public synchronized ArrayList<Person> getPersons() {
        return persons;
    }

    public synchronized ArrayList<Person> getCache() {
        return cache;
    }

    public synchronized ArrayList<Sche> getSches() {
        return sches;
    }

    public synchronized ArrayList<Update> getUpdates() {
        return updates;
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean bothEmpty() {
        return persons.isEmpty() && sches.isEmpty();
    }

    public synchronized boolean personsIsEmpty() {
        return persons.isEmpty();
    }

    public synchronized boolean schesIsEmpty() {
        return sches.isEmpty();
    }

    public synchronized boolean updatesIsEmpty() {
        return updates.isEmpty();
    }

    public synchronized void setMoving(boolean bool) {
        moving = bool;
        notifyAll();
    }

    public synchronized int getAllPersonsCount() {
        return persons.size() + cache.size();
    }
}
