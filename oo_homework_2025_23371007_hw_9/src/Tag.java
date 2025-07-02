import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.math.BigInteger;
import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private final HashMap<Integer, PersonInterface> persons;
    private BigInteger ageSum;
    private BigInteger agePowSum;

    public Tag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
        this.ageSum = BigInteger.ZERO;
        this.agePowSum = BigInteger.ZERO;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) obj;
        return this.id == tag.getId();
    }

    @Override
    public void addPerson(PersonInterface person) {
        int key = person.getId();
        persons.put(key, person);
        BigInteger age = BigInteger.valueOf(person.getAge());
        ageSum = ageSum.add(age);
        //agePowSum += person.getAge() * person.getAge();
        agePowSum = agePowSum.add(age.multiply(age));
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        int key = person.getId();
        return persons.containsKey(key);
    }

    @Override
    public int getAgeMean() { //计算所有人的平均年龄
        if (persons.size() == 0) {
            return 0;
        }
        BigInteger size = BigInteger.valueOf(persons.size());
        int mean = ageSum.divide(size).intValue();
        return mean;
    }

    @Override
    public int getAgeVar() { //计算年龄方差
        if (persons.size() == 0) {
            return 0;
        }

        BigInteger ageMean = BigInteger.valueOf(getAgeMean());
        BigInteger size = BigInteger.valueOf(persons.size());
        BigInteger numerator = agePowSum.subtract(ageMean.multiply(ageSum).
                multiply(BigInteger.valueOf(2))).add(ageMean.multiply(ageMean).multiply(size));

        int result = numerator.divide(size).intValue();
        /*int result = (agePowSum - 2 * getAgeMean() * ageSum +
                persons.size() * getAgeMean() * getAgeMean()) / persons.size();
         */
        return result;
    }

    @Override
    public void delPerson(PersonInterface person) {
        int key = person.getId();
        persons.remove(key);
        //ageSum -= person.getAge();
        //agePowSum -= person.getAge() * person.getAge();
        BigInteger age = BigInteger.valueOf(person.getAge());
        ageSum = ageSum.subtract(age);
        agePowSum = agePowSum.subtract(age.multiply(age));
    }

    @Override
    public int getSize() { //统计人数
        return this.persons.size();
    }
}
