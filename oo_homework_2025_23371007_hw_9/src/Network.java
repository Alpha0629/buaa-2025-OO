import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.Map;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons;
    private DisjointSet disjointSet;
    private int tripleSum;
    private boolean needRebuild;

    public Network() {
        persons = new HashMap<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
        needRebuild = false;
    }

    @Override
    public boolean containsPerson(int id) {
        int key = id;
        return persons.containsKey(key);
    }

    @Override
    public PersonInterface getPerson(int id) {
        int key = id;
        if (persons.containsKey(key)) {
            return persons.get(key);
        }
        return null;
    }

    @Override //O(1)
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        int key = person.getId();
        if (persons.containsKey(key)) {
            throw new EqualPersonIdException(key);
        }
        persons.put(key, person);
        disjointSet.add(key);
    }

    @Override //O(1)
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        if (!containsPerson(id1)) { //优先判断是否包含id1
            throw new PersonIdNotFoundException(id1); //立即返回，不会执行剩余代码
        }
        if (!containsPerson(id2)) { //在包含id1的情况下，不包含id2
            throw new PersonIdNotFoundException(id2);
        }

        //normal_behavior
        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);

        if (person1.isLinked(getPerson(id2))) { //已经添加过了关系，或者添加的是自己
            throw new EqualRelationException(id1, id2);
        }

        tripleSum += countNewTriples(person1, person2);

        person1.addRelation(person2, value);
        person2.addRelation(person1, value); //互相添加关系

        if (!this.needRebuild) {
            disjointSet.merge(id1, id2); //把二者联通，建立关系
        }

    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!containsPerson(id1)) { //没有id1
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) { //没有id1，但有id2
            throw new PersonIdNotFoundException(id2);
        }
        if (id1 == id2) { //id1和id2都存在，但两个id指向同一人，无效
            throw new EqualPersonIdException(id1);
        }

        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);

        if (!person1.isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }

        boolean needRemove = person1.queryValue(person2) + value <= 0;
        //如果大于0则不需要去除，反之需要

        if (!needRemove) { //第一类型的修改，不删除关系，只改变value
            person1.modifyValue(person2, value);
            person2.modifyValue(person1, value);
        } else { //第二类型的修改，删除关系，删除Tag
            tripleSum -= countNewTriples(person1, person2);

            person1.delRelation(person2);
            person2.delRelation(person1);

            //时间复杂度O(n)
            person1.delPersonFromEachTag(person2);
            person2.delPersonFromEachTag(person1);

            this.needRebuild = true;
        }
    }

    @Override //O(1)
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }

        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);

        if (!person1.isLinked(person2)) {
            throw new RelationNotFoundException(id1, id2);
        }

        //和 return person2.queryValue(person1) 等价
        return person1.queryValue(person2);
    }

    @Override //O(α(n))
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        //该方法表示id1和id2是联通的
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }

        //保证图的persons中至少有两个人

        if (!needRebuild) {
            //normal_behavior
            //联通 <==> 两者的根节点相同
            return disjointSet.isCircle(id1, id2);
        } else {
            //需要重建
            this.disjointSet = rebuildDisjointSet(); //更新disjointSet
            this.needRebuild = false;
            return disjointSet.isCircle(id1, id2);
        }
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    @Override //O(1)
    public void addTag(int personId, TagInterface tag)
            throws PersonIdNotFoundException, EqualTagIdException {
        if (!containsPerson(personId)) { //没有目标对象
            throw new PersonIdNotFoundException(personId);
        }

        Person person = (Person) getPerson(personId);

        if (person.containsTag(tag.getId())) { //目标对象已经有此Tag
            throw new EqualTagIdException(tag.getId());
        }

        person.addTag(tag);
    }

    @Override //O(1)
    public void addPersonToTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) { //没有id1
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2)) { //没有id1，但有id2
            throw new PersonIdNotFoundException(personId2);
        }
        if (personId1 == personId2) { //id1和id2都存在，但两个id指向同一人，无效
            throw new EqualPersonIdException(personId1);
        }

        Person person1 = (Person) getPerson(personId1);
        Person person2 = (Person) getPerson(personId2);

        if (!person2.isLinked(getPerson(personId1))) { //id1和id2不认识，有误
            //O(1)查找
            throw new RelationNotFoundException(personId1, personId2);
        }
        if (!person2.containsTag(tagId)) { //目标tag不在person2的tags数组中
            throw new TagIdNotFoundException(tagId);
        }

        Tag tag = (Tag) person2.getTag(tagId);

        if (tag.hasPerson(person1)) {
            //如果person2拥有的tag的persons中已经有了person1
            throw new EqualPersonIdException(personId1);
        }

        int length = tag.getSize(); //tag中persons的长度

        if (length > 999) { //如果这个tag的persons的长度过长，不添加
            return;
        }

        tag.addPerson(person1);
        //该函数功能；
        //把person1添加到person2拥有的tag的persons中
    }

    @Override //O(1)
    public int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        Person person = (Person) getPerson(personId);

        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        return person.getTag(tagId).getAgeVar();
    }

    @Override //O(1)
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId1)) { //没有id1
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2)) { //没有id1，但有id2
            throw new PersonIdNotFoundException(personId2);
        }

        Person person1 = (Person) getPerson(personId1);
        Person person2 = (Person) getPerson(personId2);

        if (!person2.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        Tag tag = (Tag) person2.getTag(tagId);

        if (!tag.hasPerson(person1)) {
            throw new PersonIdNotFoundException(personId1);
        }

        tag.delPerson(person1);
        //函数功能：
        //把person1从person2的tag中的persons中删去
    }

    @Override //O(1)
    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        Person person = (Person) getPerson(personId);

        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        person.delTag(tagId);
    }

    @Override //时间复杂度O(n)
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }

        Person person = (Person) getPerson(id);

        if (person.getAcquaintance().size() == 0) { //没有认识的人，独立节点
            throw new AcquaintanceNotFoundException(id);
        }

        return person.getBestAcquaintanceId();
    }

    //通过选择可以做到O(min(m,n)) n为id1朋友的数目,m为id2朋友的数目
    public int countNewTriples(Person person1, Person person2) {
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

    public DisjointSet rebuildDisjointSet() {
        if (persons.size() == 0) { //已经删空了
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

    //
    public PersonInterface[] getPersons() {
        //把哈希转化成数组
        PersonInterface[] persons = new Person[this.persons.size()];

        int index = 0;
        for (Map.Entry<Integer, PersonInterface> entry : this.persons.entrySet()) {
            PersonInterface person = entry.getValue();
            persons[index] = person;
            index++;
        }

        return persons;
    }
}