import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, PersonInterface> acquaintance;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, TagInterface> tags;
    private final LinkedList<Integer> receivedArticles;
    private int bestAcquaintanceId;
    private int maxValue;
    private boolean needRearrange;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.value = new HashMap<>();
        this.tags = new HashMap<>();
        this.receivedArticles = new LinkedList<>();
        this.bestAcquaintanceId = Integer.MAX_VALUE; //越小越好
        this.maxValue = Integer.MIN_VALUE; //越大越好
        this.needRearrange = false;
    }

    //全部方法均为O(1)

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public boolean containsTag(int id) {
        int key = id;
        if (this.tags.containsKey(key)) {
            return true;
        }
        return false;
    }

    @Override
    public TagInterface getTag(int id) {
        int key = id;
        if (this.tags.containsKey(key)) {
            return this.tags.get(key);
        }
        return null;
    }

    @Override
    public void addTag(TagInterface tag) {
        int key = tag.getId();
        this.tags.put(key, tag);
    }

    @Override
    public void delTag(int id) {
        int key = id;
        this.tags.remove(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PersonInterface)) {
            return false;
        }
        PersonInterface personInterface = (PersonInterface) obj;
        return this.id == personInterface.getId();
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        int key = person.getId();
        if (this.acquaintance.containsKey(key) || key == this.id) {
            return true;
        }
        return false;
    }

    @Override
    public int queryValue(PersonInterface person) {
        int key = person.getId();
        if (this.acquaintance.containsKey(key)) {
            return this.value.get(key);
        }
        return 0;
    }

    @Override //返回所有文章
    public List<Integer> getReceivedArticles() {
        return this.receivedArticles;
    }

    @Override //返回前五篇文章
    public List<Integer> queryReceivedArticles() {
        List<Integer> result = new LinkedList<>();
        int length = Math.min(5, this.receivedArticles.size());
        for (int i = 0; i < length; i++) {
            result.add(this.receivedArticles.get(i));
        }
        return result;
    }

    //以下是新定义的方法
    public HashMap<Integer, PersonInterface> getAcquaintance() {
        return this.acquaintance;
    }

    public HashMap<Integer, Integer> getValue() {
        return this.value;
    }

    public void addRelation(Person person, int value) {
        int id = person.getId();
        this.acquaintance.put(id, person); //添加person到this认识的人
        this.value.put(id, value); //添加好友的权重

        if (!this.needRearrange) { //如果不需要重排，再该，否则可能被跳过
            if (value > this.maxValue || value == this.maxValue && id < this.bestAcquaintanceId) {
                this.bestAcquaintanceId = id;
                this.maxValue = value;
            }
        }

    }

    public void delRelation(Person person) {
        int id = person.getId();
        this.acquaintance.remove(id);
        this.value.remove(id);

        if (acquaintance.isEmpty()) {
            this.bestAcquaintanceId = Integer.MAX_VALUE;
            this.maxValue = Integer.MIN_VALUE;
        }

        if (this.bestAcquaintanceId == id) { //触发概率可能比较小
            //最适合的人被删除了
            //需要重新寻找
            this.needRearrange = true;
        }
    }

    public void modifyValue(Person person, int value) {
        int id = person.getId();
        int newValue = this.value.get(id) + value;
        this.value.put(id, newValue);

        if (!this.needRearrange) { //重新判断
            if (this.bestAcquaintanceId == id) {
                if (newValue >= this.maxValue) {
                    this.maxValue = newValue;
                    return;
                }
                if (newValue < this.maxValue) {
                    //当前id就是最大值，但最大值被减少了
                    //可能需要重排
                    this.needRearrange = true;
                    return;
                }
            }
            if (newValue > this.maxValue ||
                    newValue == this.maxValue && id < this.bestAcquaintanceId) {
                this.bestAcquaintanceId = id;
                this.maxValue = newValue;
            }
        }
    }

    public int getBestAcquaintanceId() {
        if (!this.needRearrange) {
            //如果不需要重排，直接返回即可
            return this.bestAcquaintanceId;
        }

        //需要重排
        //时间复杂度O(n)
        this.bestAcquaintanceId = Integer.MAX_VALUE;
        this.maxValue = Integer.MIN_VALUE;

        for (Map.Entry<Integer, Integer> entry : this.value.entrySet()) {
            int id = entry.getKey();
            int value = entry.getValue();

            if (value > this.maxValue || value == this.maxValue && id < this.bestAcquaintanceId) {
                this.bestAcquaintanceId = id;
                this.maxValue = value;
            }
        }

        this.needRearrange = false; //重排结束

        return this.bestAcquaintanceId;
    }

    //时间复杂度O(n)
    public void delPersonFromEachTag(PersonInterface person) {
        for (Map.Entry<Integer, TagInterface> entry : this.tags.entrySet()) {
            Tag tag = (Tag) entry.getValue();
            if (tag.hasPerson(person)) {
                tag.delPerson(person);
            }
        }
    }

    public void addReceivedArticles(int articleId) {
        this.receivedArticles.addFirst(articleId);
    }

    public void deleteReceivedArticle(int articleId) {
        this.receivedArticles.remove(Integer.valueOf(articleId));
    }

    public boolean strictEquals(PersonInterface person) {
        if (this.id != person.getId() || !this.name.equals(person.getName())
                || this.age != person.getAge()) {
            return false;
        }

        HashMap<Integer, PersonInterface> acquaintance = ((Person) person).getAcquaintance();
        if (this.acquaintance.size() != acquaintance.size()) {
            return false;
        }

        for (Map.Entry<Integer, PersonInterface> entry : this.acquaintance.entrySet()) {
            int key = entry.getKey();
            PersonInterface value = entry.getValue();

            if (!acquaintance.containsKey(key)) {
                return false;
            }

            if (!value.equals(acquaintance.get(key))) {
                return false;
            }
        }

        HashMap<Integer, Integer> values = ((Person) person).getValue();
        if (this.value.size() != values.size()) {
            return false;
        }
        for (Map.Entry<Integer, Integer> entry : this.value.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (!values.containsKey(key)) {
                return false;
            }
            if (values.get(key) != (value)) {
                return false;
            }
        }

        if (this.getReceivedArticles().size() != person.getReceivedArticles().size()) {
            return false;
        }
        for (int i = 0; i < this.receivedArticles.size(); i++) {
            if (!this.getReceivedArticles().get(i).equals(person.getReceivedArticles().get(i))) {
                return false;
            }
        }

        return true;

    }
}