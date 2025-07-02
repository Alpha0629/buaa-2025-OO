# 面向对象第六次实验指导书

## 实验仓库

>公共信息发布区：[exp_6](http://gitlab.oo.buaa.edu.cn/2025_oo_public/practice/practice_6)。

>个人仓库：oo_homework_2025 / homework_2025_你的学号_exp_6。

>请同学们作答完成后，将json文件与代码**提交到个人仓库**，并将json文件内容**填入内容提交区**,再在本页面下方选择对应的 `commit`，最后**点击最下方的按钮进行提交**。详细的提交过程请参见*提交事项*章节。

## 实验目标

1. 了解 Java 的垃圾回收机制
2. 训练根据 JML 补充代码及撰写 JML 的能力
3. 在逻辑复杂度较高的场景中，合理使用大模型完成任务的能力

## 背景引入

与 C++ 程序设计语言相比，Java 程序设计语言拥有一个独特的语言特性——自动垃圾回收机制 (Garbage Collection)。在 Java 和 C++ 中，新创建一个对象都需要使用 `new` 运算符。然而，在 C++ 中，程序员需要人工管理内存，对于不再使用的对象使用 `delete` 运算符显式地回收内存；在 Java 中，程序员无需人工管理内存，JVM 会自动触发垃圾回收，将没有被引用的对象占据的内存空间释放。

### 垃圾回收机制

基本的 Java 垃圾回收机制如下：

首先，垃圾回收器会找出当前哪些对象是正在使用中的，并将其标记为存活对象；以及哪些对象是没有被引用的，并将其标记为未引用对象，这一步称为**标记** 。下图显示了一个标记前后的内存图的样式：

![图片描述](http://api.oo.buaa.edu.cn/image/2024_03_20_12_46_33_ce1fdeaa04379ff4f6848f40ff95053b6b321de1)  
其次，垃圾回收器会将当前所有未引用对象删除，也就是上图中橙色的部分。

![图片描述](http://api.oo.buaa.edu.cn/image/2024_03_20_12_46_50_feffd35b828c4488f4030b14971eda9fee965a40)

最后，为了提升性能，在删除完未引用对象后，通常还会采取**压缩**操作，将内存中的存活对象放置在一起，以便后续能够更加高效快捷地分配新的对象。

![图片描述](http://api.oo.buaa.edu.cn/image/2024_03_20_12_47_10_1c96573d592834bbf4389e0906833d9534244b7e)

## 实验任务

本次实验我们将会采用标记-压缩方法，实现一个简单的 JVM 垃圾回收机制，模拟垃圾回收的工作流程。

仓库中会给出 src 文件夹，有如下几个类：

- `MyObject`
    - 模拟创建的对象，其中每个对象的id都是唯一的。
- `MyHeap`
    - 普通小顶堆
- `JvmHeap`
    - JVM 中的堆，继承自`MyHeap`。
- `MyJvm`
    - 模拟的 JVM，负责管理堆、创建对象、删除对象引用和垃圾回收
- `Main`
    - 模拟程序的输入输出，输入方式为先输入指令名称，换行后再输入参数。
    - 输入有以下几条指令：
        - `CreateObject` ：创建新的对象，换行后输入创建对象的个数
        - `SetUnreferenced` ：将对象设置为未引用，换行后输入删除引用的对象id，用空格分隔
        - `SnapShot` ：查看当前 JVM 中堆的快照

注意：JVM 中的“堆”实际上是一段内存。在本次实验中，为了方便数据操作，我们使用了小顶堆来实现这一概念。

**任务清单:**

任务分为`JML补全`和`代码补全`,其中要求为 MyJvm 类中的 createObject 方法较为完整地编写规格

1. `JvmHeap` 类：根据规格实现[1]处的代码
2. `MyHeap` 类：按要求补全 [2]处的规格
3. `MyJvm` 类：利用大模型，按要求补全 [3] 中规格，并提交对话记录，具体见大模型使用提示。

**提交事项：**

1. 需要填空的地方在程序中已用 [1]，[2] 等序号标注。

2. 对于 JML 中的临时变量的使用，按照 `i`，`j`，`k` 的次序依次使用。

3. `JML补全`任务的答案放在 answer.json 文件中提交，**并同时**需要将作答内容的规格补全部分，即answer.json 文件中的`"2": "","3": "",`部分内容写在内容提交区。提交示例如下：

   ```json
   {
     "2": "",
     "3": "",
     "session log": {
         "input1":"",
         "output1":"",
         "input2":"",
         "output2":"",
         "input3":"",
         "output3":""
     }
    }
   ```

4. 对于 [1]  ，直接在官方文件夹 src 中作更改。提交目录应包括子目录 src 及 answer.json 文件，即 answer.json 文件不放在 src 子目录中。因此，你的仓库布局应当如下所示：

```
homework_2025_你的学号_exp_6 // 仓库目录
| - answer.json             // [2]-[3]的答案及大模型对话记录
| - src           // [1]同步实现代码
| - | - JvmHeap.java
| - | - Main.java
| - | - MyHeap.java
| - | - MyJvm.java
| - | - MyObject.java
```


**注意：本次实验要求编译成功**

**无需关注的部分：**

以下部分均由课程组封装好，实验过程中无需关注：

1. Main.java 文件
2. `MyJvm` 类的 `getSnapShot` 方法

**输入输出样例:**

输入

```
CreateObject
5
SetUnreferenced
1 4
SnapShot
CreateObject
10
SetUnreferenced
2 6 10 15
CreateObject
5
SnapShot
```

输出

```
Create 5 Objects.
Set id: 1 Unreferenced Object.
Set id: 4 Unreferenced Object.
Heap: 5
0 1 2 3 4 
the youngest one's id is 0

---------------------------------
Create 10 Objects.
Set id: 2 Unreferenced Object.
Set id: 6 Unreferenced Object.
Set id: 10 Unreferenced Object.
Set id: 15 Unreferenced Object.
Heap reaches its capacity,triggered Garbage Collection.
Create 5 Objects.

Heap: 15
0 3 5 7 8 9 11 12 13 14 15 16 17 18 19 
the youngest one's id is 0

---------------------------------
```

关于实验测试数据：

1. 不需要同学们考虑异常的情况
2. 不需要同学们考虑数据很大的情况
3. 不需要同学们考虑性能相关实现

## 提示

本题需要同学们在短时间内完成Java垃圾回收机制的阅读理解、JML规格与Java代码的阅读以及对应题目的填写。

Java垃圾回收机制是整个代码的关键；请确保充分理解该机制之后再进行填空。

关于JML与代码，如果感觉时间吃紧，可以试着从方法名上猜测该方法的具体行为，再与JML对照来加快代码阅读速度。此外，在填写代码时可以考虑如何使用代码中已经提供的方法。

此外，如果有一定的空余时间，建议测试一下你所补完的程序，在熟悉Java垃圾回收机制的同时检测代码中的错误。

## 大模型使用提示

在本次作业中，我们要求使用大模型生成JML，同学们需要根据以下提示，层层递进引导大模型给出正确的JML，并提交对话记录，填写在anwser.json文件中并同时提交于仓库和内容提交区。

通常情况下，由于JML的严谨性，大模型并不能很好的给出复杂方法的JML，可能出现丢失约束、关键词错用等错误，此时就需要人工引导大模型逐步给出合理的答案。具体而言，同学们需要提交的<font color="red">**引导过程（对话记录）要包括3种prompt**</font>，且层层递进，在[3]中提交<font color="red">**最后一次回答的JML**</font>，session log中提交<font color="red">**完整三次对话记录**</font>（无需给出使用了哪些文件）。

课程组推荐使用deepseek-r1模型但不强制要求，同时也鼓励同学们适当改进prompt以获得更好的回答。

三次递进的提问可以按照以下步骤：

#### 一、直接给出源码

最为简单的方式，直接打包各个文件的源码提交给大模型，让其补充完整[3]处的JML，参考prompt如下

“请阅读以下代码文件，补充[3]处的JML”

#### 二、辅以中文注释

在给出源码的基础上，给出JML应当满足的要求，同时给出一些JML规范

方法的参考注释如下

```java
    /**
     * 该方法用于模拟新建对象
     * 仅可以对 heap 的属性和 MyObject 的静态属性 totalId 进行修改
     * 需要考虑以下两种正常情况：
     * 1. 如果新建并加入 count 个对象之后，堆的大小没有达到 DEFAULT_CAPACITY，则堆正常增大，不会触发 GC，调用方法后，只需要保证新增对象正常加入堆，而且堆中原有的所有元素仍在堆中；
     * 2. 否则，需要触发 GC，将新元素加入堆中之后要释放堆中所有目前不被引用的对象，调用方法后，堆 heap 需要满足：若原elements数组中元素仍被引用，则该元素应包含于elements数组，若原elements数组中元素未被引用，则该元素应不包含于elements数组
     * 无论那种情况下，heap.capacity 保持不减，且 MyObject.totalId 需要增加 count
     * @param count 要新建的对象个数
     */
```

参考prompt如下

> 假如你是JML专家，请阅读文件中的java代码，理解代码含义，**按照方法上的注释**，编写MyJvm类中createObject方法的JML规格，要求包括requires前置条件、ensures后置条件和副作用限定，可以使用\result约束结果、\forall和\exists约束表达式等

#### 三、给出规格模板

在给出中文注释的基础上，进一步通过JML模板规范大模型的回答，参考模板如下

```java
    /*@ public normal_behavior
      @ assignable [3-1];
      @ requires \old(heap.size) < [3-2];
      @ ensures heap.size == [3-3];
      @ ensures (\forall int i;
                    1 <= i && i <= \old(heap.size);
                    [3-4]
                );
      @ also
      @ requires \old(heap.size) >= [3-5];
      @ ensures heap.size == [3-6];
      @ ensures (\forall int i;
                    1 <= i && i <= \old(heap.size);
                    \old(heap.elements[i].isReferenced()) ==>
                        [3-7]
                );
      @ ensures (\forall int i;
                    1 <= i && i <= \old(heap.size);
                    !(\old(heap.elements[i].isReferenced())) ==>
                        [3-8]
                );
      @*/
```



### 大模型部分提交示例

此处以训练栏目为例，展示了实验中提交的具体格式。注意：

- input1无论输入为代码/文件，均只需要注明文件名即可
- 对于大模型给出的回答，只需提交JML部分即可
- 请同学们完整提交<font color="red">**三轮对话记录**</font>，体现递进的过程
- 示例中第三次提问的模板并非本次实验的模板，请以上一节的规格模板为准

```json
{
    "session log": {
      "input1":"请阅读以下代码，补全TODO处方法的JML。使用文件为IntSet.java",
      "output1":"
        /*@ public normal_behavior
          @ assignable ia, a.ia;
          @ ensures ia.length == \old(a.ia.length);
          @ ensures (\forall int i; 0 <= i && i < ia.length; ia[i] == \old(a.ia[i]));
          @ ensures a.ia.length == \old(ia.length);
          @ ensures (\forall int i; 0 <= i && i < a.ia.length; a.ia[i] == \old(ia[i]));
          @ also
          @ public exceptional_behavior
          @ requires a == null;
          @ assignable \nothing;
          @ signals (NullPointerException e) a == null;
          @*/
        public void elementSwap(IntSet a) throws NullPointerException;",
      "input2":
        "假如你是JML专家，请阅读文件中的java代码，理解代码含义，**按照方法上的注释**，编写IntSet接口中elementSwap方法的JML规格，要求包括requires前置条件、ensures后置条件和副作用限定，可以使用\result约束结果、\forall和\exists约束表达式等。方法注释如下
    	/* 该方法完成两个IntSet对象所包含元素的交换, 例如：
           IntSet对象a中的元素为{1，2，3}，IntSet对象b中的元素为{4，5，6}
           经过交换操作后，a中的元素应为{4, 5, 6}, b中的元素为{1, 2, 3}
           两个IntSet对象中元素的数量可以不相同，例如：
           IntSet对象a中的元素为{1，2，3，4}，IntSet对象b中的元素为{5，6}
           经过交换操作后，a中的元素应为{5, 6}, b中的元素为{1, 2, 3，4}
           该方法无返回值
          */",
      "output2":"
        /*@ public normal_behavior
          @ requires a != null;
          @ assignable ia, a.ia;
          @ ensures ia.length == \old(a.ia.length);
          @ ensures (\forall int i; 0 <= i && i < ia.length; ia[i] == \old(a.ia[i]));
          @ ensures a.ia.length == \old(ia.length);
          @ ensures (\forall int i; 0 <= i && i < a.ia.length; a.ia[i] == \old(ia[i]));
          @ also
          @ public exceptional_behavior
          @ requires a == null;
          @ assignable \nothing;
          @ signals (NullPointerException e) true;
          @*/
        public void elementSwap(IntSet a) throws NullPointerException;",
      "input3":"请在以上提示和注释的基础上，参照以下模板，补全elementSwap方法的JML
    	/*@ assignable \nothing;
          @ public normal_behavior
          @ requires a != null;
          @ ensures (\forall int i; 0 <= i < [TODO];
                     ((\exists int j; 0 <= j < ia.length; \result.ia[i] == ia[j]) &&
                       (\forall int k; [TODO]; \result.ia[i] != a.ia[k])) ||
                     ((\exists int k; 0 <= k < a.ia.length; \result.ia[i] == a.ia[k]) &&
                       (\forall int j; [TODO]; \result.ia[i] != ia[j])));
          @ ensures (\forall int j; 0 <= j < ia.length;
                     (\forall int k; 0 <= k < a.ia.length; ia[j] != a.ia[k]) ==>
                      [TODO]);
          @ ensures (\forall int k; 0 <= k < a.ia.length;
                     (\forall int j; 0 <= j < ia.length; a.ia[k] != ia[j]) ==>
                      [TODO]);
          @ also
          @ public exceptional_behavior
          @ requires [TODO];
          @ signals_only NullPointerException;
          @ */",
      "output3":"
        /*@ assignable ia, a.ia;
          @ public normal_behavior
          @ requires a != null;
          @ ensures (\forall int i; 0 <= i < \old(ia.length + a.ia.length);
                     ((\exists int j; 0 <= j < \old(ia.length); ia[i] == \old(ia[j])) &&
                       (\forall int k; 0 <= k < \old(a.ia.length); ia[i] != \old(a.ia[k]))) ||
                     ((\exists int k; 0 <= k < \old(a.ia.length); ia[i] == \old(a.ia[k])) &&
                       (\forall int j; 0 <= j < \old(ia.length); ia[i] != \old(ia[j]))));
          @ ensures (\forall int j; 0 <= j < ia.length;
                     (\forall int k; 0 <= k < a.ia.length; ia[j] != a.ia[k]) ==>
                      (\exists int m; 0 <= m < \old(ia.length); ia[j] == \old(ia[m])));
          @ ensures (\forall int k; 0 <= k < a.ia.length;
                     (\forall int j; 0 <= j < ia.length; a.ia[k] != ia[j]) ==>
                      (\exists int n; 0 <= n < \old(a.ia.length); a.ia[k] == \old(a.ia[n])));
          @ also
          @ public exceptional_behavior
          @ requires a == null;
          @ signals_only NullPointerException;
          @ */
        public void elementSwap(IntSet a) throws NullPointerException;"
  }
}
```

