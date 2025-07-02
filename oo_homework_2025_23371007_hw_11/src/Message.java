import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private final int id;
    private final int socialValue;
    private final int type;
    private final PersonInterface person1;
    private final PersonInterface person2;
    private final TagInterface tag;

    public Message(int messageId, int messageSocialValue,
                   PersonInterface messagePerson1, PersonInterface messagePerson2) {
        //私发消息
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.type = 0;
        this.person1 = messagePerson1;
        this.person2 = messagePerson2;
        this.tag = null;
    }

    public Message(int messageId, int messageSocialValue,
                   PersonInterface messagePerson1, TagInterface messageTag) {
        //群发消息
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.type = 1;
        this.person1 = messagePerson1;
        this.person2 = null;
        this.tag = messageTag;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getSocialValue() {
        return this.socialValue;
    }

    @Override
    public PersonInterface getPerson1() {
        return this.person1;
    }

    @Override
    public PersonInterface getPerson2() {
        return this.person2;
    }

    @Override
    public TagInterface getTag() {
        return this.tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MessageInterface)) {
            return false;
        }
        MessageInterface message = (MessageInterface) obj;
        return this.id == message.getId();
    }
}
