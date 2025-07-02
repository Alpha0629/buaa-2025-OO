import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;

import java.util.HashMap;
import java.util.Map;

public class Assist {
    public static int myCountNewTriples(Person person1, Person person2) {
        //通过选择可以做到O(min(m,n)) n为id1朋友的数目,m为id2朋友的数目
        Person smallerPerson = person1;
        Person biggerPerson = person2;

        HashMap<Integer, PersonInterface> smallerMap = person1.getAcquaintance();
        HashMap<Integer, PersonInterface> biggerMap = person2.getAcquaintance();

        if (smallerMap.size() > biggerMap.size()) {
            HashMap<Integer, PersonInterface> tempMap = smallerMap;
            smallerMap = biggerMap;
            biggerMap = tempMap;

            Person tempPerson = smallerPerson;
            smallerPerson = biggerPerson;
            biggerPerson = tempPerson;
        }

        int count = 0;
        for (Map.Entry<Integer, PersonInterface> entry : smallerMap.entrySet()) {
            int friendId = entry.getKey();
            if (friendId != person1.getId() && friendId != biggerPerson.getId()
                    && biggerPerson.isLinked(entry.getValue())) {
                count++;
            }
        }

        return count;
    }

    public static DisjointSet myRebuildDisjointSet(HashMap<Integer, PersonInterface> persons) {
        if (persons.isEmpty()) { //已经删空了
            return null; //不会发生
        }

        DisjointSet newDisjointSet = new DisjointSet();

        for (Map.Entry<Integer, PersonInterface> e : persons.entrySet()) {
            Person person = (Person) e.getValue();
            int personId = person.getId();

            if (!newDisjointSet.hasPerson(personId)) {
                newDisjointSet.add(personId);
            }

            HashMap<Integer, PersonInterface> acquaintance = person.getAcquaintance();

            for (Map.Entry<Integer, PersonInterface> entry : acquaintance.entrySet()) {
                int friendId = entry.getKey();

                if (!newDisjointSet.hasPerson(friendId)) {
                    newDisjointSet.add(friendId);
                }

                newDisjointSet.merge(personId, friendId);
            }
        }
        return newDisjointSet;
    }

    public static PersonInterface[] myGetPersons(HashMap<Integer, PersonInterface> persons) {
        PersonInterface[] myPersons = new Person[persons.size()];
        int index = 0;
        for (Map.Entry<Integer, PersonInterface> entry : persons.entrySet()) {
            PersonInterface person = entry.getValue();
            myPersons[index] = person;
            index++;
        }
        return myPersons;
    }

    public static MessageInterface[] myGetMessages(HashMap<Integer, MessageInterface> messages) {
        MessageInterface[] myMessages = new MessageInterface[messages.size()];
        int index = 0;
        for (Map.Entry<Integer, MessageInterface> entry : messages.entrySet()) {
            MessageInterface message = entry.getValue();
            myMessages[index] = message;
            index++;
        }
        return myMessages;
    }

    public static int[] myGetEmojiIdList(HashMap<Integer, Integer> emoji2Heat) {
        int[] myEmojiIdList = new int[emoji2Heat.size()];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : emoji2Heat.entrySet()) {
            int emojiId = entry.getKey();
            myEmojiIdList[index] = emojiId;
            index++;
        }
        return myEmojiIdList;
    }

    public static int[] myGetEmojiHeatList(HashMap<Integer, Integer> emoji2Heat) {
        int[] myEmojiHeatList = new int[emoji2Heat.size()];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : emoji2Heat.entrySet()) {
            int heat = entry.getValue();
            myEmojiHeatList[index] = heat;
            index++;
        }
        return myEmojiHeatList;
    }
}
