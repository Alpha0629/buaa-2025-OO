import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.PersonInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private Network network;

    public NetworkTest(Network network) {
        this.network = network;
    }

    @Parameters
    public static Collection prepareData() {

        int testNumber = 100;
        Random random = new Random();

        Object[][] data = new Object[testNumber][];

        for (int i = 0; i < testNumber; i++) {
            Network network = new Network();
            for (int j = 0; j < 5 * testNumber; j++) {
                int type = random.nextInt(3);
                switch (type) {
                    case 0:
                        network = addPerson(network);
                        break;
                    case 1:
                        network = addRelation(network);
                        break;
                    case 2:
                        network = modifyRelation(network);
                        break;
                    default:
                        break;
                }
            }
            data[i] = new Object[]{network};
        }

        return Arrays.asList(data);
    }

    @Test
    public void queryTripleSum() {
        PersonInterface[] interfaces = network.getPersons();
        assertNotNull(interfaces);

        Person[] persons = new Person[interfaces.length];

        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i] instanceof Person) {
                persons[i] = (Person) interfaces[i];
            } else {
                throw new ClassCastException();
            }
        }

        int count = getCount(persons);

        int expectedTripleSum = network.queryTripleSum();
        PersonInterface[] expectedInterfaces = network.getPersons();
        assertNotNull(expectedInterfaces);

        Person[] expectedPersons = new Person[expectedInterfaces.length];

        for (int i = 0; i < expectedInterfaces.length; i++) {
            if (expectedInterfaces[i] instanceof Person) {
                expectedPersons[i] = (Person) expectedInterfaces[i];
            } else {
                throw new ClassCastException();
            }
        }

        assertEquals(expectedTripleSum, count);

        assertEquals(expectedPersons.length, persons.length);

        for (int i = 0; i < expectedPersons.length - 1; i++) {
            for (int j = i + 1; j < expectedPersons.length; j++) {
                assertNotEquals(expectedPersons[i], expectedPersons[j]);
            }
        }

        for (int i = 0; i < persons.length; i++) {
            Person person = persons[i];
            Person expectedPerson = expectedPersons[i];
            assertTrue(person.strictEquals(expectedPerson));
        }

        //System.out.println("三元组数目为: " + count);
    }

    public int getCount(PersonInterface[] persons) {
        int count = 0;
        for (int i = 0; i < persons.length - 2; i++) {
            PersonInterface first = persons[i];
            for (int j = i + 1; j < persons.length - 1; j++) {
                PersonInterface second = persons[j];
                if (!first.isLinked(second)) {
                    continue;
                }
                for (int k = j + 1; k < persons.length; k++) {
                    //到此保证了1和2互相认识
                    PersonInterface third = persons[k];
                    if (first.isLinked(third) && second.isLinked(third)) {
                        //1和3认识且2和3认识
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Test
    public void queryBestAcquaintance() {
        Person[] persons = (Person[]) network.getPersons();
        assertNotNull(persons);

        for (int i = 0; i < persons.length; i++) {
            int bestId = Integer.MAX_VALUE;
            int maxValue = Integer.MIN_VALUE;

            Person person = persons[i];
            int expectedBestId = person.getBestAcquaintanceId();
            HashMap<Integer, Integer> value = person.getValue();

            for (Integer id : value.keySet()) {
                if (value.get(id) > maxValue || value.get(id) == maxValue && id < bestId) {
                    maxValue = value.get(id);
                    bestId = id;
                }
            }

            assertEquals(expectedBestId, bestId);

        }

        Person[] expectedPersons = (Person[]) network.getPersons();
        assertNotNull(expectedPersons);
        assertEquals(expectedPersons.length, persons.length);
    }

    @Test
    public void isCircleTest() {
        Person[] persons = (Person[]) network.getPersons();
        assertNotNull(persons);

        boolean expected = false;
        boolean actual;
        for (int i = 0; i < persons.length - 1; i++) {
            Person personI = (Person) persons[i];
            for (int j = i + 1; j < persons.length; j++) {
                Person personJ = (Person) persons[j];
                try {
                    expected = network.isCircle(personI.getId(), personJ.getId());
                } catch (PersonIdNotFoundException e) {
                    e.print();
                }
                actual = bfs(personI, personJ);
                assertEquals(actual, expected);
            }
        }

        Person[] expectedPersons = (Person[]) network.getPersons();
        assertNotNull(expectedPersons);
        assertEquals(expectedPersons.length, persons.length);
    }



    public boolean bfs(Person personA, Person personB) {
        int id1 = personA.getId();
        int id2 = personB.getId();

        if (id1 == id2) {
            return true;
        }

        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        queue.add(id1);
        visited.add(id1);

        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            Person currentPerson = (Person) network.getPerson(currentId);

            for (PersonInterface friendPerson : currentPerson.getAcquaintance().values()) {
                Person friend = (Person) friendPerson;
                int friendId = friend.getId();

                if (friendId == id2) {
                    return true;
                }

                if (!visited.contains(friendId)) {
                    queue.add(friendId);
                    visited.add(friendId);
                }
            }
        }

        return false;
    }


    public static Network addPerson(Network network) {
        Random random = new Random();
        int id = random.nextInt(20);
        String name = String.valueOf(random.nextInt(100));
        int age = random.nextInt(100);
        Person person = new Person(id, name, age);

        try {
            network.addPerson(person);
        } catch (EqualPersonIdException e) {
            e.print();
        }
        return network;
    }

    public static Network addRelation(Network network) {
        Random random = new Random();
        int id1 = random.nextInt(20);
        int id2 = random.nextInt(20);
        int value = random.nextInt(100);

        try {
            network.addRelation(id1, id2, value);
        } catch (PersonIdNotFoundException e) {
            e.print();
            return network;
        } catch (EqualRelationException e) {
            e.print();
            return network;
        }
        return network;
    }

    public static Network modifyRelation(Network network) {
        Random random = new Random();
        int id1 = random.nextInt(20);
        int id2 = random.nextInt(20);
        int value = random.nextInt(100) - 200;

        try {
            network.modifyRelation(id1, id2, value);
            //System.out.println(1);
        } catch (PersonIdNotFoundException e) {
            e.print();
            return network;
        } catch (EqualPersonIdException e) {
            e.print();
            return network;
        } catch (RelationNotFoundException e) {
            e.print();
            return network;
        }
        return network;
    }
}