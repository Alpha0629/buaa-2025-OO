import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons;
    private final HashMap<Integer, OfficialAccountInterface> accounts;
    private final HashSet<Integer> articles;
    private final HashSet<Integer> articleContributors;
    private DisjointSet disjointSet;
    private int tripleSum;
    private boolean needRebuild;
    private int coupleSum;
    private boolean needRecount;
    private final HashSet<TagInterface> tags;

    public Network() {
        persons = new HashMap<>();
        accounts = new HashMap<>();
        articles = new HashSet<>();
        articleContributors = new HashSet<>();
        disjointSet = new DisjointSet();
        tripleSum = 0;
        needRebuild = false;
        coupleSum = 0;
        needRecount = false;
        tags = new HashSet<>();
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

        for (TagInterface tag : tags) {
            if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                //找到共有的群聊
                ((Tag) tag).addRelation(value);
            }
        }

        needRecount = true;

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
        needRecount = true;
        //如果大于0则不需要去除，反之需要

        if (!needRemove) { //第一类型的修改，不删除关系，只改变value
            person1.modifyValue(person2, value);
            person2.modifyValue(person1, value);

            for (TagInterface tag : tags) {
                if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                    ((Tag) tag).modifyRelation(value);
                }
            }


        } else { //第二类型的修改，删除关系，删除Tag
            tripleSum -= countNewTriples(person1, person2);

            int valueToSub = person1.queryValue(person2);
            for (TagInterface tag : tags) {
                if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                    ((Tag) tag).delRelation(valueToSub);
                }
            }

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
        this.tags.add(tag);
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

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        PersonInterface person = getPerson(personId);

        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        TagInterface tag = person.getTag(tagId);

        return tag.getValueSum();
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

        TagInterface tag = person.getTag(tagId);
        person.delTag(tagId);
        this.tags.remove(tag);
    }

    @Override //时间复杂度O(n)
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }

        Person person = (Person) getPerson(id);

        if (person.getAcquaintance().isEmpty()) { //没有认识的人，独立节点
            throw new AcquaintanceNotFoundException(id);
        }

        return person.getBestAcquaintanceId();
    }

    @Override
    public int queryCoupleSum() {
        if (!needRecount) {
            return coupleSum;
        }

        //needRecount
        coupleSum = 0;

        for (Map.Entry<Integer, PersonInterface> entry : persons.entrySet()) {
            Person personA = (Person) entry.getValue();
            int bestIdForA = personA.getBestAcquaintanceId(); //A最熟悉的人
            if (bestIdForA == Integer.MAX_VALUE) {
                continue;
            }
            Person personB = (Person) persons.get(bestIdForA);
            int bestIdForB = personB.getBestAcquaintanceId();
            if (bestIdForB == Integer.MAX_VALUE) {
                continue;
            }
            if (bestIdForB == personA.getId()) {
                //B最熟悉的人恰好是A，而且B是A最熟悉的人
                coupleSum++;
            }
        }
        if (coupleSum % 2 != 0) {
            System.out.println("ERROR");
        }

        coupleSum = coupleSum / 2;
        this.needRecount = false;

        return coupleSum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (!isCircle(id1, id2)) {
            throw new PathNotFoundException(id1, id2);
        }

        if (id1 == id2) {
            return 0; //路径长为0
        }

        Queue<PersonInterface> queue = new LinkedList<>();
        HashMap<Integer, Integer> steps = new HashMap<>();
        HashSet<Integer> visited = new HashSet<>();

        queue.add(persons.get(id1));
        steps.put(id1, 0);
        visited.add(id1);

        while (!queue.isEmpty()) {
            Person current = (Person) queue.poll();
            int currentId = current.getId();
            int currentStep = steps.get(currentId);

            for (PersonInterface neighbor : current.getAcquaintance().values()) {
                int neighborId = neighbor.getId();

                if (neighborId == id2) {
                    return currentStep + 1;
                }

                if (!visited.contains(neighborId)) {
                    queue.add(neighbor);
                    steps.put(neighborId, currentStep + 1);
                    visited.add(neighborId);
                }
            }
        }

        throw new PathNotFoundException(id1, id2); //不可能发生
    }

    @Override
    public boolean containsAccount(int id) {
        return this.accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name)
            throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        if (containsAccount(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        }

        OfficialAccountInterface account = new OfficialAccount(personId, accountId, name);
        account.addFollower(persons.get(personId));
        this.accounts.put(accountId, account);
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }

        OfficialAccountInterface account = this.accounts.get(accountId);

        if (account.getOwnerId() != personId) {
            throw new
                    DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }

        this.accounts.remove(accountId);
    }

    @Override
    public boolean containsArticle(int id) {
        return this.articles.contains(id);
    }

    @Override //一个人向一个公众号贡献一篇文章
    public void contributeArticle(int personId, int accountId, int articleId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }

        if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        }

        OfficialAccount account = (OfficialAccount) this.accounts.get(accountId);
        PersonInterface person = this.persons.get(personId);

        if (!account.containsFollower(person)) { //如果这个人不是当前公众号的订阅者，则没有添加文章的权利
            //id1 尝试贡献文章的人员 ID
            //id2 本次尝试贡献的文章 ID
            throw new ContributePermissionDeniedException(personId, articleId);
        }

        this.articles.add(articleId); //添加文章
        this.articleContributors.add(personId); //添加文章的作者
        account.addArticle(person, articleId);

        //让该公众号的所有订阅者都收到了文章，且新的文章排在所有文章的最前面
        for (Map.Entry<Integer, PersonInterface> entry : account.getFollowers().entrySet()) {
            Person p = (Person) entry.getValue();
            p.addReceivedArticles(articleId);
        }

    }

    @Override //删除这个人在这个公众号的这篇文章
    public void deleteArticle(int personId, int accountId, int articleId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }

        OfficialAccount account = (OfficialAccount) this.accounts.get(accountId);

        if (!account.containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }

        if (account.getOwnerId() != personId) {
            //只有号主才有权限删除文章
            //id1 尝试删除文章的人员 ID
            //id2 本次尝试删除的文章 ID
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }

        //从公众号的订阅者找到articleId的作者
        PersonInterface writer = account.getArticles().get(articleId);
        account.removeArticle(articleId);

        for (Map.Entry<Integer, PersonInterface> entry : account.getFollowers().entrySet()) {
            Person p = (Person) entry.getValue();
            p.deleteReceivedArticle(articleId);
            //把这篇文章从所有订阅了该公众号的订阅者的文库中删除
        }

        account.decreaseContribution(writer);
    }

    @Override
    public void followOfficialAccount(int personId, int accountId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }

        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }

        OfficialAccountInterface account = this.accounts.get(accountId);
        PersonInterface person = getPerson(personId);

        if (account.containsFollower(person)) {
            throw new EqualPersonIdException(personId);
            //重复订阅
        }

        account.addFollower(person);
    }

    @Override
    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        }
        return accounts.get(id).getBestContributor();
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).queryReceivedArticles();
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