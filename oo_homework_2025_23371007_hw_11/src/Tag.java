import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Tag implements TagInterface {
    private final int id;
    private final HashMap<Integer, PersonInterface> persons;
    private BigInteger ageSum;
    private BigInteger agePowSum;
    private int valueSum;

    public Tag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
        this.ageSum = BigInteger.ZERO;
        this.agePowSum = BigInteger.ZERO;
        this.valueSum = 0;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TagInterface)) {
            return false;
        }
        TagInterface tag = (TagInterface) obj;
        return this.id == tag.getId();
    }

    @Override
    public void addPerson(PersonInterface person) {
        int key = person.getId();
        persons.put(key, person);
        BigInteger age = BigInteger.valueOf(person.getAge());
        ageSum = ageSum.add(age);
        agePowSum = agePowSum.add(age.multiply(age));


        for (Map.Entry<Integer, PersonInterface> entry : persons.entrySet()) {
            if (entry.getValue().isLinked(person)) {
                valueSum += 2 * entry.getValue().queryValue(person);
            }
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        int key = person.getId();
        return persons.containsKey(key);
    }

    @Override //查询同一群聊中互相认识的人的权重之和
    public int getValueSum() {
        /*
        this.valueSum = 0;
        for (PersonInterface person : persons.values()) {
            for (PersonInterface otherPerson : persons.values()) {
                if (person.isLinked(otherPerson)) {
                    valueSum += person.queryValue(otherPerson);
                }
            }
        }

         */
        return valueSum;
    }

    @Override
    public int getAgeMean() { //计算所有人的平均年龄
        if (persons.isEmpty()) {
            return 0;
        }
        BigInteger size = BigInteger.valueOf(persons.size());
        int mean = ageSum.divide(size).intValue();
        return mean;
    }

    @Override
    public int getAgeVar() { //计算年龄方差
        if (persons.isEmpty()) {
            return 0;
        }

        BigInteger ageMean = BigInteger.valueOf(getAgeMean());
        BigInteger size = BigInteger.valueOf(persons.size());
        BigInteger numerator = agePowSum.subtract(ageMean.multiply(ageSum).
                multiply(BigInteger.valueOf(2))).add(ageMean.multiply(ageMean).multiply(size));

        int result = numerator.divide(size).intValue();
        return result;
    }

    @Override
    public void delPerson(PersonInterface person) {
        int key = person.getId();


        for (Map.Entry<Integer, PersonInterface> entry : persons.entrySet()) {
            if (entry.getValue().isLinked(person)) {
                valueSum -= 2 * entry.getValue().queryValue(person);
            }
        }

        persons.remove(key);
        BigInteger age = BigInteger.valueOf(person.getAge());
        ageSum = ageSum.subtract(age);
        agePowSum = agePowSum.subtract(age.multiply(age));
    }

    @Override
    public int getSize() { //统计人数
        return this.persons.size();
    }

    public void addRelation(int value) {
        valueSum += 2 * value;
    }

    public void modifyRelation(int value) {
        valueSum += 2 * value;
    }

    public void delRelation(int value) {
        valueSum -= 2 * value;
    }

    public HashMap<Integer, PersonInterface> getPersons() {
        return persons;
    }
}
