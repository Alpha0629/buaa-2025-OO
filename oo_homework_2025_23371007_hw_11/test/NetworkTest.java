import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class NetworkTest {
    //测试deleteColdEmoji
    //需要添加人,添加关系,修改关系,添加Tag,加入Tag,存储表情,添加消息,发送消息
    private Network network;
    private Network patternNetwork;
    private ArrayList<PersonInterface> personList;
    private ArrayList<Integer> emojiIdList;
    private ArrayList<Integer> emojiHeatList;
    private ArrayList<Integer> oldEmojiIdList;
    private ArrayList<Integer> oldEmojiHeatList;
    private ArrayList<MessageInterface> messageList;
    private ArrayList<MessageInterface> oldMessageList;
    private HashMap<Integer, Integer> messageId2emojiId;
    private HashMap<Integer, Integer> emoji2Heat;

    @Test
    public void testDeleteColdEmoji() {
        Random random = new Random();
        for (int i = 0; i < 150; i++) {
            construct();
            prepareData(i);
            lookData(i);
            judge(random.nextInt(20));
        }

        judgeSpecialCase1();
        judgeSpecialCase2();
        judgeSpecialCase3();
    }

    public void construct() {
        this.network = new Network();
        this.patternNetwork = new Network();
        this.personList = new ArrayList<>();
        this.emojiIdList = new ArrayList<>();
        this.emojiHeatList = new ArrayList<>();
        this.oldEmojiIdList = new ArrayList<>();
        this.oldEmojiHeatList = new ArrayList<>();
        this.messageList = new ArrayList<>();
        this.oldMessageList = new ArrayList<>();
        this.emoji2Heat = new HashMap<>();
        this.messageId2emojiId = new HashMap<>();
    }

    @SuppressWarnings({"SimplifiableAssertion"})
    public void judge(int limit) {
        int sizeOfList = network.deleteColdEmoji(limit);
        covertDataType();
        verifyRetainedEmojis(limit);
        verifyEmojiSources();
        verifyEmojiCount(limit, sizeOfList);
        assertTrue(emojiIdList.size() == emojiHeatList.size()); //条件4
        verifyMessageConsistency();
        assertTrue(sizeOfList == emojiIdList.size()); //条件8
        //System.out.println(limit + " " + sizeOfList);
    }

    public void verifyRetainedEmojis(int limit) {
        //条件1: 新的数组中一定包含大于等于阈值的旧元素
        for (int i = 0; i < oldEmojiIdList.size(); i++) {
            if (oldEmojiHeatList.get(i) >= limit) {
                assertTrue(emojiIdList.contains(oldEmojiIdList.get(i)));
            }
        }
    }

    public void verifyEmojiSources() {
        //条件2: 新的数组中的元素一定仅来自旧的数组
        for (int i = 0; i < emojiIdList.size(); i++) {
            assertTrue(oldEmojiIdList.contains(emojiIdList.get(i)));
            assertTrue(oldEmojiHeatList.contains(emojiHeatList.get(i)));
        }
    }

    @SuppressWarnings("SimplifiableAssertion")
    public void verifyEmojiCount(int limit, int sizeOfList) {
        //条件3
        int length = 0;
        for (int i = 0; i < oldEmojiIdList.size(); i++) {
            if (oldEmojiHeatList.get(i) >= limit) {
                length++;
            }
        }
        assertTrue(emojiIdList.size() == length);
        assertTrue(sizeOfList == length);
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach", "SimplifiableAssertion"})
    public void verifyMessageConsistency() {
        //条件5,6,7
        int length = 0;
        boolean find = false;
        for (int i = 0; i < oldMessageList.size(); i++) {
            if (oldMessageList.get(i) instanceof EmojiMessageInterface) {
                EmojiMessageInterface emojiMessage = (EmojiMessageInterface) oldMessageList.get(i);
                if (network.containsEmojiId(emojiMessage.getEmojiId())) {
                    length++;
                    for (int j = 0; j < messageList.size(); j++) {
                        if (oldMessageList.get(i).equals(messageList.get(j))) {
                            find = true;
                            break;
                        }
                    }
                    assertTrue(find);
                }
            } else {
                length++;
                for (int j = 0; j < messageList.size(); j++) {
                    if (oldMessageList.get(i).equals(messageList.get(j))) {
                        find = true;
                        break;
                    }
                }
                assertTrue(find);
            }
            find = false;
        }
        assertTrue(messageList.size() == length);
    }

    public void judgeSpecialCase1() {
        for (int i = 0; i < 5; i++) {
            construct();
            prepareData(3);
            lookData(i);
            judge(-1);
        }
    }

    public void judgeSpecialCase2() {
        for (int i = 0; i < 5; i++) {
            construct();
            prepareData(3);
            lookData(i);
            judge(Integer.MAX_VALUE);
        }
    }

    public void judgeSpecialCase3() {
        for (int i = 0; i < 5; i++) {
            construct();
            generateErdosRenyiGraph(0.9);
            Random rand = new Random();
            boolean type = rand.nextBoolean();
            generateEmoji(0); //不生成emoji
            if (type) { //两个人之间发消息
                generateEmojiMessage(0);
            } else { //一个人向一个群里发消息
                generateTag();
                generateEmojiMessage(1);
            }
            sendEmojiMessage();
            judge(rand.nextInt(10));
        }
    }

    public void covertDataType() {
        for (int emojiId : network.getEmojiIdList()) {
            this.emojiIdList.add(emojiId);
        }
        for (int emojiHeat : network.getEmojiHeatList()) {
            this.emojiHeatList.add(emojiHeat);
        }
        for (int emojiId : patternNetwork.getEmojiIdList()) {
            this.oldEmojiIdList.add(emojiId);
        }
        for (int emojiHeat : patternNetwork.getEmojiHeatList()) {
            this.oldEmojiHeatList.add(emojiHeat);
        }
        this.messageList.addAll(Arrays.asList(network.getMessages()));
        this.oldMessageList.addAll(Arrays.asList(patternNetwork.getMessages()));
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public void lookData(int i) {
        /*
        for (HashMap.Entry<Integer, Integer> entry : emoji2Heat.entrySet()) {
            int emojiId = entry.getKey();
            int heat = entry.getValue();
            //System.out.println("第" + i + "组数据: " + "emojiId: " + emojiId + " heat: " + heat);
        }

         */
    }

    public void generatePerson(int i) {
        Random random = new Random();
        Person person = new Person(i, "" + i, 20 + random.nextInt(40));
        try {
            network.addPerson(person);
            patternNetwork.addPerson(new Person(person.getId(), person.getName(), person.getAge()));
            personList.add(person);
        } catch (EqualPersonIdException e) {
            //
        }
    }

    public void generateRelation(int i, int j) {
        Random random = new Random();
        int value = random.nextInt(15) + 1;
        try {
            network.addRelation(i, j, value);
            patternNetwork.addRelation(i, j, value);
        } catch (PersonIdNotFoundException | EqualRelationException e) {
            //
        }
    }

    public void generateIsolationGraph() {
        for (int i = 0; i < 100; i++) {
            generatePerson(i);
        }
    }

    public void generateErdosRenyiGraph(double probability) {
        // 创建所有节点
        for (int i = 0; i < 100; i++) {
            generatePerson(i);
        }

        // 以给定概率随机添加边
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                if (random.nextDouble() <= probability) {
                    generateRelation(i, j);
                }
            }
        }
    }

    public void generateEmoji(int range) { //创造40个emoji
        for (int i = 0; i < range; i++) {
            try {
                network.storeEmojiId(i);
                patternNetwork.storeEmojiId(i);
                emoji2Heat.put(i, 0);
            } catch (EqualEmojiIdException e) {
                //
            }
        }

    }

    public void generateTag() {
        for (int i = 0; i < 20; i++) { //创造20个tag
            try {
                TagInterface tag = new Tag(i);
                network.addTag(i, tag);
                patternNetwork.addTag(i, new Tag(i));
            } catch (EqualTagIdException | PersonIdNotFoundException e) {
                //
            }

        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100; j++) { //100个人，每个人可以加入10个群聊,每个群大约有50人
                try {
                    Random rand = new Random();
                    int id = rand.nextInt(20);
                    network.addPersonToTag(j, id, id);
                    patternNetwork.addPersonToTag(j, id, id);
                } catch (PersonIdNotFoundException | RelationNotFoundException | TagIdNotFoundException |
                         EqualPersonIdException e) {
                    //
                }
            }
        }
    }

    public void generateEmojiMessage(int type) {
        Random random = new Random();
        if (type == 0) {
            for (int i = 0; i < 1000; i++) {
                try {
                    int emojiId = random.nextInt(40);
                    int id1 = random.nextInt(100);
                    int id2 = random.nextInt(100);
                    Message message = new EmojiMessage(i, emojiId, network.getPerson(id1), network.getPerson(id2));
                    Message messageCopy = new EmojiMessage(i, emojiId, patternNetwork.getPerson(id1), patternNetwork.getPerson(id2));
                    network.addMessage(message);
                    patternNetwork.addMessage(messageCopy);
                    messageId2emojiId.put(i, emojiId);
                } catch (EqualMessageIdException | EmojiIdNotFoundException | EqualPersonIdException |
                         ArticleIdNotFoundException e) {
                    //
                }
            }
        } else if (type == 1) {
            for (int i = 0; i < 1000; i++) {
                try {
                    int emojiId = random.nextInt(20);
                    int id = random.nextInt(100);
                    int tagId = random.nextInt(20);
                    Message message = new EmojiMessage(i, emojiId, network.getPerson(id), network.getPerson(tagId).getTag(tagId));
                    Message messageCopy = new EmojiMessage(i, emojiId, patternNetwork.getPerson(id), patternNetwork.getPerson(tagId).getTag(tagId));
                    network.addMessage(message);
                    patternNetwork.addMessage(messageCopy);
                    messageId2emojiId.put(i, emojiId);
                } catch (EqualMessageIdException | EmojiIdNotFoundException | EqualPersonIdException |
                         ArticleIdNotFoundException e) {
                    //
                }
            }
        }

    }

    public void sendEmojiMessage() {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            boolean flag = random.nextBoolean();
            if (flag) {
                try {
                    network.sendMessage(i);
                    patternNetwork.sendMessage(i);
                    emoji2Heat.put(messageId2emojiId.get(i), emoji2Heat.get(messageId2emojiId.get(i)) + 1);
                } catch (RelationNotFoundException | MessageIdNotFoundException | TagIdNotFoundException e) {
                    //
                }
            }
        }
    }

    public void prepareData(int i) {
        if (i == 0) {
            generateIsolationGraph();
            return;
        }
        switch (i % 3) {
            case 0:
                generateErdosRenyiGraph(0.8);
                break;
            case 1:
                generateErdosRenyiGraph(0.9);
                break;
            case 2:
                generateErdosRenyiGraph(1.0); //完全图
                break;
            default:
                break;
        }

        Random rand = new Random();
        boolean type = rand.nextBoolean();

        generateEmoji(40);
        if (type) { //两个人之间发消息
            generateEmojiMessage(0);
        } else { //一个人向一个群里发消息
            generateTag();
            generateEmojiMessage(1);
        }
        sendEmojiMessage();
    }
}