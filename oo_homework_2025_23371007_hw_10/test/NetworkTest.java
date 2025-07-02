import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.main.PersonInterface;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NetworkTest {
    private Network network;
    private Network patternNetwork;
    private ArrayList<PersonInterface> personList;

    @org.junit.Test
    public void queryCoupleSum() {
        for (int i = 0; i < 50; i++) {

            this.network = new Network();
            this.patternNetwork = new Network();
            this.personList = new ArrayList<>();

            if (i == 0) {
                generateEmptyNetWork();
            } else if (i == 1) {
                generateSinglePerson();
            } else if (i == 2) {
                generateTwoPersonsNoRelation();
            } else if (i == 3) {
                generateTwoPersonsWithRelation();
            } else if (i == 4) {
                generateTriangleRelation();
            } else if (i % 10 == 0) {
                generateCompleteGraph();
            } else if (i % 10 == 1) {
                generateRandomGraph(); // 随机图
            } else if (i % 10 == 2) {
                generateErdosRenyiGraph(0.4);
            } else if (i % 10 == 3) {
                generateErdosRenyiGraph(0.1);
            } else if (i % 10 == 4) {
                generateFlowerGraph(); // 花
            } else if (i % 10 == 5) {
                generateIsolationGraph();
            } else if (i % 10 == 6) {
                generateCircleGraph(); // 环
            } else if (i % 10 == 7) {
                generateEmptyNetWork();
            } else if (i % 10 == 8) {
                generateErdosRenyiGraph(0.9);
            } else {
                generateSpecialCases(); // 特殊案例 //链式
            }

            assertEquals(network.queryCoupleSum(), getCoupleCount());
            assertEquals(network.getPersons().length, patternNetwork.getPersons().length);

            for (int j = 0; j < network.getPersons().length; j++) {
                for (int k = j + 1; k < network.getPersons().length; k++) {
                    assertNotEquals(network.getPersons()[j], network.getPersons()[k]);
                }
            }

            for (int j = 0; j < network.getPersons().length; j++) {
                Person person = (Person) network.getPersons()[j];
                Person patterPerson = (Person) patternNetwork.getPersons()[j];
                assertTrue(person.strictEquals(patterPerson));
            }
        }
    }

    public void generateEmptyNetWork() {
        //
    }

    public void generateSinglePerson() {
        try {
            Person person = new Person(0, "person0", 20);
            Person patterPerson = new Person(0, "person0", 20);
            network.addPerson(person);
            patternNetwork.addPerson(patterPerson);
            personList.add(person);
        } catch (EqualPersonIdException e) {
            //e.print();
        }
    }

    public void generateTwoPersonsNoRelation() {
        try {
            Person p1 = new Person(1, "person1", 21);
            Person p2 = new Person(2, "person2", 22);
            network.addPerson(p1);
            network.addPerson(p2);
            //深克隆
            patternNetwork.addPerson(new Person(p1.getId(), p1.getName(), p1.getAge()));
            patternNetwork.addPerson(new Person(p2.getId(), p2.getName(), p2.getAge()));
            personList.add(p1);
            personList.add(p2);
        } catch (EqualPersonIdException e) {
            //e.print();
        }
    }

    public void generateTwoPersonsWithRelation() {
        try {
            Person p1 = new Person(1, "person1", 21);
            Person p2 = new Person(2, "person2", 22);
            network.addPerson(p1);
            network.addPerson(p2);
            network.addRelation(1, 2, 10);
            patternNetwork.addPerson(new Person(p1.getId(), p1.getName(), p1.getAge()));
            patternNetwork.addPerson(new Person(p2.getId(), p2.getName(), p2.getAge()));
            patternNetwork.addRelation(1, 2, 10);
            personList.add(p1);
            personList.add(p2);
        } catch (EqualPersonIdException | PersonIdNotFoundException | EqualRelationException e) {
            //
        }
    }

    public void generateTriangleRelation() {
        try {
            Person p1 = new Person(1, "person1", 21);
            Person p2 = new Person(2, "person2", 22);
            Person p3 = new Person(3, "person3", 23);

            network.addPerson(p1);
            network.addPerson(p2);
            network.addPerson(p3);

            network.addRelation(1, 2, 5);
            network.addRelation(2, 3, 10);
            network.addRelation(1, 3, 7);

            patternNetwork.addPerson(new Person(p1.getId(), p1.getName(), p1.getAge()));
            patternNetwork.addPerson(new Person(p2.getId(), p2.getName(), p2.getAge()));
            patternNetwork.addPerson(new Person(p3.getId(), p3.getName(), p3.getAge()));
            patternNetwork.addRelation(1, 2, 5);
            patternNetwork.addRelation(2, 3, 10);
            patternNetwork.addRelation(1, 3, 7);

            personList.add(p1);
            personList.add(p2);
            personList.add(p3);
        } catch (EqualPersonIdException | PersonIdNotFoundException | EqualRelationException e) {
            //
        }
    }

    public void generateCompleteGraph() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Person person = new Person(i, "" + i, 1 + random.nextInt(70));
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //e.print();
            }
        }

        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                int value = random.nextInt(20) + 1;
                try {
                    network.addRelation(i, j, value);
                    patternNetwork.addRelation(i, j, value);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                    //
                }
            }
        }
    }

    public void generateIsolationGraph() {
        Random random = new Random();
        for (int i = 0; i < 200; i++) {
            Person person = new Person(i, "" + i, 1 + random.nextInt(70));
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //
            }
        }
    }

    public void generateRandomGraph() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Person person = new Person(i, "" + i, 1 + random.nextInt(30));
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //
            }
        }

        for (int i = 0; i < 100; i++) {
            int value = random.nextInt(10) + 1;
            try {
                int id1 = random.nextInt(100);
                int id2 = random.nextInt(100);
                network.addRelation(id1, id2, value);
                patternNetwork.addRelation(id1, id2, value);
            } catch (PersonIdNotFoundException | EqualRelationException e) {
                //
            }

        }
    }

    public void generateErdosRenyiGraph(double probability) {
        // 创建所有节点
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Person person = new Person(i, "" + i, 20 + random.nextInt(40));
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //e.print();
            }
        }

        // 以给定概率随机添加边
        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                if (random.nextDouble() < probability) {
                    int value = random.nextInt(15) + 1;
                    try {
                        network.addRelation(i, j, value);
                        patternNetwork.addRelation(i, j, value);
                    } catch (PersonIdNotFoundException | EqualRelationException e) {
                        //
                    }
                }
            }
        }
    }

    public void generateFlowerGraph() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            Person person = new Person(i, "" + i, 1 + random.nextInt(30));
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //e.print();
            }
        }

        int core = random.nextInt(99);
        //随机选择一个中心
        for (int i = 0; i < 100; i++) {
            if (i == core) {
                continue;
            }
            try {
                int value = random.nextInt(5) + 1;
                network.addRelation(core, i, value);
                patternNetwork.addRelation(core, i, value);
            } catch (PersonIdNotFoundException | EqualRelationException e) {
                //
            }
        }
    }

    public void generateCircleGraph() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) { //0-99
            Person person = new Person(i, "" + i, 1 + random.nextInt(30));
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //e.print();
            }
        }
        for (int i = 0; i < 99; i++) {
            int value = random.nextInt(10) + 1;
            try {
                network.addRelation(i, i + 1, value);
                patternNetwork.addRelation(i, i + 1, value);
            } catch (PersonIdNotFoundException | EqualRelationException e) {
                //
            }
        }
        try {
            int value = random.nextInt(5) + 1;
            network.addRelation(99, 0, value);
            patternNetwork.addRelation(99, 0, value);
        } catch (PersonIdNotFoundException | EqualRelationException e) {
            //
        }
    }

    public void generateSpecialCases() {
        Random random = new Random();
        // 特殊情况1: 所有关系权重相同
        int numNodes = 20;
        for (int i = 0; i < numNodes; i++) {
            Person person = new Person(i, "" + i, 20 + i);
            try {
                network.addPerson(person);
                patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
                personList.add(person);
            } catch (EqualPersonIdException e) {
                //e.print();
            }
        }

        // 完全图，所有边权重相同
        if (random.nextBoolean()) {
            for (int i = 0; i < numNodes; i++) {
                for (int j = i + 1; j < numNodes; j++) {
                    try {
                        network.addRelation(i, j, 10);
                        patternNetwork.addRelation(i, j, 10);
                    } catch (PersonIdNotFoundException | EqualRelationException e) {
                        //
                    }
                }
            }
        } else {
            //链式图
            int chainLength = 15;
            for (int i = 0; i < chainLength; i++) {
                try {
                    int value = random.nextInt(10) + 1;
                    network.addRelation(i, (i + 1) % chainLength, value);
                    patternNetwork.addRelation(i, (i + 1) % chainLength, value);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                    //
                }
            }
        }
    }

    public int getCoupleCount() {
        int count = 0;
        for (int i = 0; i < personList.size(); i++) {
            for (int j = i + 1; j < personList.size(); j++) {
                Person personI = (Person) personList.get(i);
                Person personJ = (Person) personList.get(j);
                int id1 = personI.getId();
                int id2 = personJ.getId();
                try {
                    int id1Goal = network.queryBestAcquaintance(id1);
                    int id2Goal = network.queryBestAcquaintance(id2);
                    if (id1Goal == id2 && id2Goal == id1) {
                        count++;
                    }
                } catch (PersonIdNotFoundException | AcquaintanceNotFoundException e) {
                    //e.print();
                }
            }
        }
        return count;
    }
}