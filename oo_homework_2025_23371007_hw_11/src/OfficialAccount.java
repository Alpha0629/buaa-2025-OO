import com.oocourse.spec3.main.OfficialAccountInterface;
import com.oocourse.spec3.main.PersonInterface;

import java.util.HashMap;
import java.util.Map;

public class OfficialAccount implements OfficialAccountInterface {
    private final int ownerId;
    private final int id;
    private final String name;
    private final HashMap<Integer, PersonInterface> followers;
    private final HashMap<Integer, PersonInterface> articles;
    //原本是HashSet<Integer> 仅以文章的id为键 现在改为HashMap 文章id为键 文章作者为值
    //一篇文章对应唯一作者 一个作者可能有多篇文章
    private final HashMap<Integer, Integer> contributions; //作者id为key,贡献值为value
    private int bestContributorId;
    private int maxContributionValue;
    private boolean needRearrange;

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        this.followers = new HashMap<>();
        this.articles = new HashMap<>();
        this.contributions = new HashMap<>();
        this.bestContributorId = Integer.MAX_VALUE;
        this.maxContributionValue = Integer.MIN_VALUE;
        this.needRearrange = false;
    }

    @Override
    public int getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        int id = person.getId();
        this.followers.put(id, person);
        this.contributions.put(id, 0); //初始贡献值为0

        if (!this.needRearrange) {
            if (0 > maxContributionValue ||
                    (0 == maxContributionValue //把原本的大于号改成等于号
                            && id < bestContributorId)) {
                bestContributorId = id;
                maxContributionValue = contributions.get(id); //0
            }
        }

    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        int key = person.getId();
        return this.followers.containsKey(key);
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        int articleId = id;
        int personId = person.getId();
        this.contributions.put(personId, contributions.get(personId) + 1);

        if (!this.needRearrange) {
            if (contributions.get(personId) > maxContributionValue ||
                    (contributions.get(personId) == maxContributionValue //把原本的大于号改成等于号
                            && personId < bestContributorId)) {
                bestContributorId = personId;
                maxContributionValue = contributions.get(personId);
            }
        }

        this.articles.put(articleId, person);
    }

    @Override
    public boolean containsArticle(int id) {
        int articleId = id;
        return this.articles.containsKey(articleId); //文章的HashMap中有以文章号为键的元素，也就代表有这篇文章
    }

    @Override
    public void removeArticle(int id) {
        this.articles.remove(id);
    }

    @Override
    public int getBestContributor() {
        if (!this.needRearrange) {
            return this.bestContributorId;
        }

        //重排
        this.bestContributorId = Integer.MAX_VALUE;
        this.maxContributionValue = Integer.MIN_VALUE;

        for (Map.Entry<Integer, Integer> entry : contributions.entrySet()) {
            int id = entry.getKey();
            int contribution = entry.getValue();

            if (contribution > maxContributionValue ||
                    contribution == maxContributionValue && id < bestContributorId) {
                this.bestContributorId = id;
                this.maxContributionValue = contribution;
            }
        }

        this.needRearrange = false;

        return this.bestContributorId;
    }

    //
    public void decreaseContribution(PersonInterface person) {
        int personId = person.getId();
        this.contributions.put(personId, contributions.get(personId) - 1);

        if (!this.needRearrange) {
            if (personId == this.bestContributorId) {
                this.needRearrange = true;
                return;
            }
        }
    }

    public HashMap<Integer, PersonInterface> getFollowers() {
        return this.followers;
    }

    public HashMap<Integer, PersonInterface> getArticles() {
        return this.articles;
    }
}
