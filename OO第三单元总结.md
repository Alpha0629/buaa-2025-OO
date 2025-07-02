# BUAA OO UNIT3 总结

## 一. 本单元的测试过程
### 对单元测试、功能测试、集成测试、压力测试、回归测试的理解
#### 单元测试
字面意思上来看,单元测试是指测试一个程序中最小的可测试单元,通常表现为单个方法,隔离外部的其它类,其他方法,仅专注于该方法
从实践角度来看,我们在作业中的单元测试就是JUNIT中针对各个方法的测试: 从queryTripleSum,到queryCoupleSum,再到deleteColdEmoji
单元测试需要我们构造比较充分的数据,理解函数的功能,全面的测试这个函数的各个作用,在本单元学习中,着重强调要验证函数前后"不变量"保持一致.

#### 功能测试
与单元测试相比,功能测试的范围更大,需要验证整个业务功能是否按需求工作,整个业务功能的正确需要各个单元的正确,以及各个单元的合理协作,但在这个过程中,我们未必如同单元测试一样,这么关注具体函数的实现方法,我们只需要关注给定一个输入信息,能否得到预期的输出结果.
因此,我认为功能测试类似于黑盒测试,我们平时利用样例,利用评测机来检测程序的正确性,就属于功能测试.

#### 集成测试
集成是指对多个单元的集成,把它们视为一个整体进行测试
通常情况下,几个单元的正确不代表它们能正常协作,单元测试隔离了外部影响,而几个单元共同合作就往往容易暴露问题,出现bug
通过集成测试,我们能更好掌握一个模块的工作情况.

#### 压力测试
压力测试是指测试程序面对极端数据的处理能力,最简单的压力测试就是使用大量的输入数据来测试程序,例如作业中的强测,几千行的数据,更容易暴露出程序设计时的问题,包括逻辑的正误,程序运行的耗时,本单元的强测,更关注程序的性能问题
此外,作业中的互测环节更关注程序处理边界数据的能力,这样的数据未必很长,但是聚焦于不易察觉的特例,利用这些特例进行测试.

#### 回归测试
回归测试通常是当程序发现bug,经过修改后,再利用已有的测试样例进行重测,在这个环节中,一方面关注之前的bug是否成功改正; 另一方面,关注修改bug的过程中是否引发新的bug,是否破坏了现有功能
我们初次提交后,得到bug信息,进行修改后,再次提交的过程就是回归测试的过程

### 数据构造策略
1. 每个数据点内的指令数目要多,同时应该全面覆盖不同指令
2. 在JUNIT测试环节,要构建不同类型的图,既要有稀疏图,又要有稠密图,同时要考虑到孤立图,完全图,零图这三种边界情况; 
3. 在构建评测机时,不能完全随机的生成数据,这样会导致输出大量的异常,有效数据很少,没有起到测试作用,可以考虑设置数组,先生成一些Person,Tag数据放在数组中,再在ar,qba等方法中依照一定的概率来决定是从已有数组中选择(不抛出异常),还是随机构造新的Person,Tag(抛出异常),这样以来,数据的有效性大大提升
4. 构造边界情况,例如hw10中,全局的TagId是可以重复的,有些人没有关注到这一点,使用``HashMap<Integer, TagInterface>``来表示,这就导致了重复的id会覆盖掉原有TagInterface,造成错误. 因此,可以利用这一点构造数据


## 二. 引导大模型在不同场景下完成复杂任务
1. 算法生成: 本单元有许多涉及到图的算法,例如bfs,但难以做到独立写出这一算法,此时就可以利用大模型生成bfs算法,如果不加以特定场景的引导,单纯描述为"请用java语言生成bfs算法",效果并不好,这样生成的代码与作业的场景没有任何关联. 此时,需要加以引导,可以选择将Person类扔到大模型里面,并告诉它,在查找"子节点"的时候调用getAcquaintance()方法,这样以来,大模型就会生成适合于该场景的代码,可以直接使用
2. 在写完一个较为复杂的方法后,例如hw11中的sendMessage,可以将代码交给大模型,并输入该方法的JML规格,引导大模型根据JML来检测我写的方法是否符合JML规范,同时特别强调是否符合pure,not_assigned等原则

## 三. 架构设计
![alt text](image-1.png)
### 图模型构建和维护策略
本单元作业我是依照规格设计的要求,在Person类里面设置acquaintance,表示和当前person节点相连的所有节点
#### 并查集
单独设置了DisjointSet类用于构建并查集,从而实现isCircle方法
1. 每添加一个person(节点),就添加到并查集当中,并将指针指向自己,表示自己就是根节点
``` java
    public void add(int id) {
        plink.put(id, id);
        rank.put(id, 1); //单一节点高度为1
    }
```
2. 每次添加两个人的关系(连接两个节点),就调用merge方法,在merge方法中调用find方法找到两个人的根节点,然后将两个根节点合并
3. 这里需要注意,find的时候可以进行路径压缩,查找到根节点后,直接将当前节点的指针指向根节点,这样在后续再次find当前节点时,可以降低时间复杂度
``` java
    //find方法
    int child = id;
        while (child != root) {
            int father = plink.get(child);
            plink.put(child, root); //直接相连
            child = father;
        }
```
4. 在合并的时候,考虑按秩合并,把秩小的添加到秩大的树上,可以尽可能避免层数增多
``` java
    //merge方法
    if (rank1 > rank2) { //把2合并到1去
            plink.put(root2, root1);
    } else if (rank1 < rank2) { //把1合并到2去
        plink.put(root1, root2);
    } else (rank1 == rank2) { //随意
        rank.put(root2, rank2 + 1); //新增一层
        plink.put(root1, root2);
    }
```
#### bfs
在queryShortestPath方法内利用广度优先搜索,找到两个节点直接最短的路径(经历最少节点)
1. 设置队列``Queue<PersonInterface> queue = new LinkedList<>();``,在遍历一个节点的子节点(acquaintance)的时候,就将子节点加入到队列中,后续访问的时候从队列中获取节点
2. 设置``HashMap<Integer, Integer> steps = new HashMap<>();``,用于记录从id1到当前节点的步长,当访问到一个节点的所有子节点时,子节点的步长是该节点的步长+1
3. 设置一个HashSet用于记录哪些id是被访问过的``HashSet<Integer> visited = new HashSet<>();``,在遍历的时候确保当前子节点是未被访问过的新节点
4. 当所遍历的子节点的id恰为id2的时候,就代表找到了最短路径,返回步长
``` java
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
```

## 四. 性能问题
### 动态维护
本单元涉及到许多查询某个值的指令,如果我们在每次查询的时候,进行遍历从而获得结果,就会大大提升时间复杂度,我们需要采用动态维护的策略,当结果的值发生改变时,就进行维护; 当查询时,直接返回结果,从原来的O(n),O(n²)降低为O(1)
1. queryTagAgeVar: 查询Tag中所有人年龄的方差,如果不进行动态维护,那么时间复杂度就是O(n),这里采取动态维护,每次向tag内添加person和从tag内删除person的时候,修改ageSum和agePowSum,等到查询的时候,进行一次简单的方差计算即可,时间复杂度为O(1)
``` java
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
```
2. queryTripleSum: 查询社交网络中的所有三元组,同理,在添加关系的时候计算因新增关系而增加的三元组数目,在删除关系的时候计算因删除关系而减少的三元组数目,查询时直接返回三元组数目结果
``` java
    public int countNewTriples(Person person1, Person person2) {
        //略
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }
```
3. queryTagValueSum: 查询同一tag中互相认识的人的权重之和,在每次把人添加到tag,从tag中删除,增加或修改关系的时候进行动态维护,查询时直接返回结果

对于动态维护,有这样一种思想,那就是一个样例中往往是增删指令为少数,查询指令为多数,这也符合实际一个社交网络的客观情况. 因此,增删指令可以稍慢,但查询指令一定要快.
在大多情况下,我们只能把一个O(n²)的查询方法分解到每次增删的方法中,且使增删指令复杂度增加到O(n). 一个是从O(n²)降到O(1),另一方此消彼长,从O(1)变为O(n),这是不可避免的,但正如前文所述,查询占多数,增删占少数,这样的优化已经可以极大提升性能了.

### 改时更新
1. 有一些数据类型,不便于执行动态维护策略,例如并查集,如果每次删除关系的时候维护并查集,是很不现实的. 因此,对于这类数据,采用改时更新的策略,对于需要重排的数据,设置一个标记位.
2. 在查询的时候,如果标记位为false,说明不需要重建,也就是数据没有被"污染",因此直接返回结果,反之,说明此时的数据已经失效了,不是正确的结果,所以要重建
3. 这种方法的好处在于,通过标记,牺牲查询时的一点性能,避免了每次修改时复杂的维护过程
4. 除了并查集,本单元中还有很多地方体现了改时更新的重要思想,例如queryBestAcquaintance,queryCoupleSum,queryBestContributor,以queryBestAcquaintance为例,如果依然选择动态维护,则在modifyRelation的时候需要通过遍历的方式更新bestAcquaintance,得不偿失如果接连多次修改,则需要多次遍历,而改时更新提供了一个将多次修改暂存,合并为一次修改的途径,而后续不再modify的时候,查询依然是O(1)
             
### 规格与实现分离
我认为最典型的规格与实现分离的例子就是数据类型的选取,所有通过查找id进行匹配的数据类型都可以从高规格的普通数组改为HashMap(前提是id不能重复);
此外,对于需要插入到头部,后面元素后移一位的数组,都可以使用LinkedList而非ArrayList,前者可以实现快速的头插;对于一些id可能重复但其他不重复的数据,例如全局中的tagInterface,可以采用HashSet来存储,而一旦选用了自定义的数据类型,相应方法的具体实现也会随之更改.

除了数据类型的选取,上述动态维护,改时更新的两大策略也体现出规格与实现的分离,我们不按照规格所示的,时间复杂度较高的方法,而是自主选择合适的实现方法

综上所述,JML所限制的规格可以视为对功能的一种描述,且仅仅是采用了某一种实现方式用作描述,我们需要明确的是JML的目的只是告诉我们需要"做什么",具体到实现代码的场景中,也就是"如何做",则需要根据实际情况选择最适合的实现方式,不能照搬JML的规格



## 五. JUNIT测试
***本单元JUNIT测试的核心目的是来测试编写的程序/方法是否符合规格信息***
1. 首先,需要我们认真阅读规格信息的每行信息(结合离散数学所学知识),针对每一条规格信息,我们都需要在JUNIT中实现对它的验证
2. 在阅读了规格信息后,我们就知道了要实现验证,需要构造哪些数据,hw9,hw10中,我们只需要构造Person相关的数据,而在hw11中,我们不仅需要构造Person相关的数据,还需要构造Tag,Message的数据
3. 构造数据: 根据目的,选择合适的数据强度,例如hw11的deleteColdEmoji,为了验证其效果,就需要构造一个较为稠密的图,同时,emoji数量要控制在中等,过少,每个emoji的热度会非常高,过多,每个emoji的热度都接近0,Tag的数目要较少一些,保证每个Tag的成员较多,更利于在随即中成功发送消息
4. JML中往往涉及到old数组和新数组之间关系的判定,old数组其实就是调用目标方法前的数组,新数组就是调用方法后的数组,这样以来,我们就需要同时 拥有旧数组和新数组,实现方式是在构造数据的时候,对每一项数据进行深克隆,并添加到另一个"network"中,我这里称为patternNetwork,与之相对的是将调用deleteColdEmoji的network. 之后调用network.deleteColdEmoji,获得新的数组,在patternNetwork中get旧数组
5. 构造完数据后,进入judge函数,在这个函数中,实现对JML每一条信息的判断,对每一条"ensures",就用一个子函数进行判定,如下所示:
``` java
@ ensures (\forall int i; 0 <= i && i < \old(emojiIdList.length);
@          (\old(emojiHeatList[i] >= limit) ==>
@          (\exists int j; 0 <= j && j < emojiIdList.length; emojiIdList[j] == \old(emojiIdList[i]))));

public void verifyRetainedEmojis(int limit) {
        //条件1: 新的数组中一定包含大于等于阈值的旧元素
        for (int i = 0; i < oldEmojiIdList.size(); i++) {
            if (oldEmojiHeatList.get(i) >= limit) {
                assertTrue(emojiIdList.contains(oldEmojiIdList.get(i)));
            }
        }
}
```
6. 总结而言,只要我们依照规格构造了合理的数据,进行了深克隆,再按照规格进行验证,就能很好的实现检验代码实现与规格是否一致

## 六. 学习体会
1. 本单元的难度不大,重点在于让我们体会规格设计,契约式编程,同时,在规格的约束下,我们也需要考虑规格与实现相分离,尽可能地选择合理的数据结构,从而提升运行速度,这也说明了,契约式编程不是完全照搬,也需要有自己的思考
2. 对我而言,本单元最大的收获在于了解了许多不同类型容器的底层实现,例如HashMap的添加,查找,处理hash冲突,动态数组的扩容等问题,让我对java的理解更上一层楼
3. 此外,我还巩固了一些有关图的算法(并查集,bfs,dfs); 学习到了动态维护和改时更新这两个重要的思想,为今后降低程序的时间复杂度,提升程序运行效率提供了很好的策略