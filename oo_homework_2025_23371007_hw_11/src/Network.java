import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.OfficialAccountInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons;
    private final HashMap<Integer, OfficialAccountInterface> accounts;
    private final HashSet<Integer> articles;
    private final HashSet<Integer> articleContributors;
    private final HashMap<Integer, MessageInterface> messages;
    private DisjointSet disjointSet; //自定义部分
    private int tripleSum;
    private boolean needRebuild;
    private int coupleSum;
    private boolean needRecount;
    private final HashSet<TagInterface> tags;
    private final HashMap<Integer, Integer> emoji2Heat;

    public Network() {
        this.persons = new HashMap<>();
        this.accounts = new HashMap<>();
        this.articles = new HashSet<>();
        this.articleContributors = new HashSet<>();
        this.messages = new HashMap<>();
        this.disjointSet = new DisjointSet(); //自定义部分
        this.tripleSum = 0;
        this.needRebuild = false;
        this.coupleSum = 0;
        this.needRecount = false;
        this.tags = new HashSet<>();
        this.emoji2Heat = new HashMap<>();
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public PersonInterface getPerson(int id) {
        return containsPerson(id) ? persons.get(id) : null;
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
        } else if (!containsPerson(id2)) { //在包含id1的情况下，不包含id2
            throw new PersonIdNotFoundException(id2);
        }
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
                ((Tag) tag).addRelation(value); //找到共有的群聊
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
        } else if (!containsPerson(id2)) { //没有id1，但有id2
            throw new PersonIdNotFoundException(id2);
        } else if (id1 == id2) { //id1和id2都存在，但两个id指向同一人，无效
            throw new EqualPersonIdException(id1);
        }
        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        if (!person1.isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
        boolean needRemove = person1.queryValue(person2) + value <= 0;
        needRecount = true; //如果大于0则不需要去除，反之需要
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
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        if (!person1.isLinked(person2)) {
            throw new RelationNotFoundException(id1, id2);
        }
        return person1.queryValue(person2);
    }

    @Override //O(α(n)) //该方法表示id1和id2是联通的
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (needRebuild) { //需要重建
            this.disjointSet = rebuildDisjointSet(); //更新disjointSet
            this.needRebuild = false;
        }
        return disjointSet.isCircle(id1, id2);
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

    @Override //O(1) //把person1添加到person2拥有的tag的persons中
    public void addPersonToTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) { //没有id1
            throw new PersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) { //没有id1，但有id2
            throw new PersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) { //id1和id2都存在，但两个id指向同一人，无效
            throw new EqualPersonIdException(personId1);
        }
        Person person1 = (Person) getPerson(personId1);
        Person person2 = (Person) getPerson(personId2);
        if (!person2.isLinked(getPerson(personId1))) { //id1和id2不认识，有误
            throw new RelationNotFoundException(personId1, personId2);
        } else if (!person2.containsTag(tagId)) { //目标tag不在person2的tags数组中
            throw new TagIdNotFoundException(tagId);
        }
        Tag tag = (Tag) person2.getTag(tagId);
        if (tag.hasPerson(person1)) { //如果person2拥有的tag的persons中已经有了person1
            throw new EqualPersonIdException(personId1);
        }
        if (tag.getSize() > 999) { //如果这个tag的persons的长度过长，不添加
            return;
        }
        tag.addPerson(person1);
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

    @Override //O(1) //把person1从person2的tag中的persons中删去
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId1)) { //没有id1
            throw new PersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) { //没有id1，但有id2
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

    @Override
    public boolean containsMessage(int id) {
        return this.messages.containsKey(id);
    }

    @Override //类比为草稿,需要检测消息本身的合法性
    public void addMessage(MessageInterface message) throws EqualMessageIdException,
            EmojiIdNotFoundException, EqualPersonIdException, ArticleIdNotFoundException {
        int messageId = message.getId();
        if (containsMessage(messageId)) { //不能重复添加消息
            throw new EqualMessageIdException(messageId);
        }
        if (message instanceof EmojiMessageInterface) { //如果是表情类型的消息,需要保证表情被创建过
            if (!containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) {
                throw new EmojiIdNotFoundException(((EmojiMessageInterface) message).getEmojiId());
            }
        }
        Person person1 = (Person) message.getPerson1();
        Person person2 = (Person) message.getPerson2();
        if (message instanceof ForwardMessageInterface) { //如果是转发类型的消息,需要保证转发的消息被创建过(文章)
            ForwardMessageInterface forwardMessageInterface = (ForwardMessageInterface) message;
            if (!containsArticle(forwardMessageInterface.getArticleId())) {
                throw new ArticleIdNotFoundException(forwardMessageInterface.getArticleId());
            }
            List<Integer> receivedArticles = person1.getReceivedArticles();
            if (!receivedArticles.contains(forwardMessageInterface.getArticleId())) {
                throw new ArticleIdNotFoundException(forwardMessageInterface.getArticleId());
            }
        }
        if (message.getType() == 0 && person1.equals(person2)) {
            throw new EqualPersonIdException(person1.getId());
        }
        messages.put(messageId, message);
    }

    @Override
    public MessageInterface getMessage(int id) {
        return this.messages.get(id);
    }

    @Override
    public void sendMessage(int id)
            throws RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        }
        MessageInterface message = getMessage(id);
        if (message.getType() == 0) {
            Person person1 = (Person) message.getPerson1();
            Person person2 = (Person) message.getPerson2();
            if (!person1.isLinked(person2)) {
                throw new RelationNotFoundException(person1.getId(), person2.getId());
            }
            person1.addSocialValue(message.getSocialValue());
            person2.addSocialValue(message.getSocialValue());
            if (message instanceof RedEnvelopeMessageInterface) {
                person1.addMoney(-((RedEnvelopeMessage) message).getMoney());
                person2.addMoney(+((RedEnvelopeMessage) message).getMoney());
            } else if (message instanceof ForwardMessageInterface) {
                person2.addReceivedArticles(((ForwardMessage) message).getArticleId());
            } else if (message instanceof EmojiMessageInterface) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emoji2Heat.put(emojiId, emoji2Heat.get(emojiId) + 1);
            }
            person2.addMessage(message);
        } else {
            Person person = (Person) message.getPerson1();
            Tag tag = (Tag) message.getTag();
            if (!person.containsTag(tag.getId())) {
                throw new TagIdNotFoundException(tag.getId());
            }
            person.addSocialValue(message.getSocialValue());
            for (Map.Entry<Integer, PersonInterface> entry : tag.getPersons().entrySet()) {
                PersonInterface personInterface = entry.getValue();
                personInterface.addSocialValue(message.getSocialValue());
            }
            if (message instanceof RedEnvelopeMessageInterface && tag.getSize() > 0) {
                int i = ((RedEnvelopeMessage) message).getMoney() / tag.getSize();
                person.addMoney(-i * tag.getSize()); //红包金额100,发给12人,每人8元,实际扣除96元而非100元
                for (Map.Entry<Integer, PersonInterface> entry : tag.getPersons().entrySet()) {
                    PersonInterface personInterface = entry.getValue();
                    personInterface.addMoney(i);
                }
            } else if (message instanceof ForwardMessageInterface && tag.getSize() > 0) {
                for (Map.Entry<Integer, PersonInterface> entry : tag.getPersons().entrySet()) {
                    Person p = (Person) entry.getValue();
                    p.addReceivedArticles(((ForwardMessage) message).getArticleId());
                }
            } else if (message instanceof EmojiMessageInterface) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emoji2Heat.put(emojiId, emoji2Heat.get(emojiId) + 1);
            }
            for (Map.Entry<Integer, PersonInterface> entry : tag.getPersons().entrySet()) {
                Person p = (Person) entry.getValue();
                p.addMessage(message);
            }
        }
        messages.remove(id);
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        Person person = (Person) getPerson(id);
        return person.getSocialValue();
    }

    @Override
    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        Person person = (Person) getPerson(id);
        return person.getReceivedMessages();
    }

    @Override //O(1)查找
    public boolean containsEmojiId(int id) {
        return this.emoji2Heat.containsKey(id);
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new EqualEmojiIdException(id);
        }
        this.emoji2Heat.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        Person person = (Person) getPerson(id);
        return person.getMoney();
    }

    @Override //查询一个表情的使用热度
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new EmojiIdNotFoundException(id);
        }
        return this.emoji2Heat.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        HashSet<Integer> coldEmojis = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : emoji2Heat.entrySet()) {
            int heat = entry.getValue();
            if (heat < limit) {
                coldEmojis.add(entry.getKey());
            }
        }
        for (int emojiId : coldEmojis) {
            emoji2Heat.remove(emojiId);
        }
        Iterator<Map.Entry<Integer, MessageInterface>> iterator = messages.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, MessageInterface> entry = iterator.next();
            MessageInterface messageInterface = entry.getValue();
            if (messageInterface instanceof EmojiMessageInterface) {
                int emojiId = ((EmojiMessageInterface) messageInterface).getEmojiId();
                if (coldEmojis.contains(emojiId)) {
                    iterator.remove();
                }
            }
        }
        return emoji2Heat.size();
        //
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
        coupleSum = 0;
        for (Map.Entry<Integer, PersonInterface> entry : persons.entrySet()) {
            Person personA = (Person) entry.getValue();
            int bestIdForA = personA.getBestAcquaintanceId(); //A最熟悉的人
            Person personB = (Person) persons.get(bestIdForA);
            if (personB == null) {
                continue;
            }
            int bestIdForB = personB.getBestAcquaintanceId();
            if (bestIdForB == personA.getId()) {
                //B最熟悉的人恰好是A，而且B是A最熟悉的人
                coupleSum++;
            }
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
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (!isCircle(id1, id2)) {
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
        } else if (containsAccount(accountId)) {
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
        } else if (!containsAccount(accountId)) {
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
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        }
        OfficialAccount account = (OfficialAccount) this.accounts.get(accountId);
        PersonInterface person = this.persons.get(personId);
        if (!account.containsFollower(person)) { //如果这个人不是当前公众号的订阅者，则没有添加文章的权利
            throw new ContributePermissionDeniedException(personId, articleId);
        }
        this.articles.add(articleId); //添加文章
        this.articleContributors.add(personId); //添加文章的作者
        account.addArticle(person, articleId);
        for (Map.Entry<Integer, PersonInterface> entry : account.getFollowers().entrySet()) {
            Person p = (Person) entry.getValue();
            p.addReceivedArticles(articleId); //让该公众号的所有订阅者都收到了文章，且新的文章排在所有文章的最前面
        }

    }

    @Override //删除这个人在这个公众号的这篇文章
    public void deleteArticle(int personId, int accountId, int articleId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount account = (OfficialAccount) this.accounts.get(accountId);
        if (!account.containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        } else if (account.getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }
        PersonInterface writer = account.getArticles().get(articleId);
        account.removeArticle(articleId); //从公众号的订阅者找到articleId的作者
        for (Map.Entry<Integer, PersonInterface> entry : account.getFollowers().entrySet()) {
            Person p = (Person) entry.getValue();
            p.deleteReceivedArticle(articleId); //把这篇文章从所有订阅了该公众号的订阅者的文库中删除
        }
        account.decreaseContribution(writer);
    }

    @Override
    public void followOfficialAccount(int personId, int accountId) throws
            PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccountInterface account = this.accounts.get(accountId);
        PersonInterface person = getPerson(personId);
        if (account.containsFollower(person)) { //重复订阅
            throw new EqualPersonIdException(personId);
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

    public int countNewTriples(Person person1, Person person2) {
        return Assist.myCountNewTriples(person1, person2);
    }

    public DisjointSet rebuildDisjointSet() {
        return Assist.myRebuildDisjointSet(this.persons);
    }

    public PersonInterface[] getPersons() {
        return Assist.myGetPersons(this.persons);
    }

    public MessageInterface[] getMessages() {
        return Assist.myGetMessages(this.messages);
    }

    public int[] getEmojiIdList() {
        return Assist.myGetEmojiIdList(this.emoji2Heat);
    }

    public int[] getEmojiHeatList() {
        return Assist.myGetEmojiHeatList(this.emoji2Heat);
    }
}