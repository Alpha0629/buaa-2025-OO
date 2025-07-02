import java.util.HashMap;

public class DisjointSet {
    private final HashMap<Integer, Integer> plink;
    private final HashMap<Integer, Integer> rank;

    public DisjointSet() {
        plink = new HashMap<>();
        rank = new HashMap<>();
    }

    public void add(int id) {
        plink.put(id, id);
        rank.put(id, 1); //单一节点高度为1
    }

    public int find(int id) { //找到id节点的根节点
        if (plink.get(id) == id) {
            //id节点本身就是根节点
            return plink.get(id);//返回根节点序号
        }

        int current = id; //
        while (plink.get(current) != current) {
            //如果当前节点的父节点不是自己
            //也就是说该节点还有上级
            current = plink.get(current); //更新current为该节点的上级
            //继续遍历
            //直到找到根节点
        }

        //找到根节点了
        int root = current;

        //把这一系列节点全部进行路径压缩，即直接作为根节点的子节点，rank为2
        int child = id;
        while (child != root) {
            int father = plink.get(child);
            plink.put(child, root); //直接相连
            child = father;
        }
        //经过压缩之后，实际高度小于rank中记录的高度

        return root; //返回根节点序号
    }

    public void merge(int id1, int id2) { //把id1和id2两个节点所指向的根节点按秩合并
        int root1 = find(id1);
        int root2 = find(id2);

        if (root1 == root2) {
            //已经合并过了，直接返回
            return;
        }

        int rank1 = rank.get(root1);
        int rank2 = rank.get(root2);

        if (rank1 > rank2) { //把2合并到1去
            plink.put(root2, root1);
            return;
        }

        if (rank1 < rank2) { //把1合并到2去
            plink.put(root1, root2);
            return;
        }

        if (rank1 == rank2) { //随意
            rank.put(root2, rank2 + 1); //新增一层
            plink.put(root1, root2);
            return;
        }
    }

    public boolean isCircle(int x, int y) {
        return find(x) == find(y);
    }

    public boolean hasPerson(int id) {
        return plink.containsKey(id) && rank.containsKey(id);
    }
}