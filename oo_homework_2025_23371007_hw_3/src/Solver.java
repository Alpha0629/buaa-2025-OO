import java.util.HashMap;

public class Solver {
    private String target;  //f{2}((x+1),(x-1))*x
    private String funcN;   //f{n}(x,y)=1*f{n-1}(x^2,y)+2*f{n-2}(x,y^2)+1
    private HashMap<Integer, String> funcI = new HashMap<>();
    private int type;
    private String first;
    private String first1;
    private String first2;
    private String second;
    private String second1;
    private String second2;
    private String funcG = new String();
    private String funcH = new String();
    private int typeG = -1;
    private int typeH = -1;
    private String firstG;
    private String first1G;
    private String secondG;
    private String second1G;
    private String firstH;
    private String first1H;
    private String secondH;
    private String second1H;

    public void distinguish(String[] input) {
        for (int i = 0; i < input.length; i++) {
            if (input[i] != null) {
                char ch = input[i].charAt(0);
                if (ch == 'g') {
                    funcG = input[i];
                } else if (ch == 'h') {
                    funcH = input[i];
                }
            }
        }
    }

    public void identifyType() {
        if (!funcG.isEmpty()) { //定义了g
            int index = funcG.indexOf('=');
            String tempG = funcG.substring(0, index);
            if (tempG.contains(",")) { //双变量
                if (funcG.charAt(2) == 'x') { //(x,y)
                    typeG = 3;
                } else { //(y,x)
                    typeG = 4;
                }
            } else {
                if (funcG.charAt(2) == 'x') {
                    typeG = 1;
                } else {
                    typeG = 2;
                }
            }
        }
        if (!funcH.isEmpty()) {
            int index = funcH.indexOf('=');
            String tempH = funcH.substring(0, index);
            if (tempH.contains(",")) {
                if (funcH.charAt(2) == 'x') {
                    typeH = 3;
                } else {
                    typeH = 4;
                }
            } else {
                if (funcH.charAt(2) == 'x') {
                    typeH = 1;
                } else {
                    typeH = 2;
                }
            }
        }
    }

    public void preReplace() {
        if (funcG.contains("h")) { // 先定义了h，再定义了g
            while (funcG.contains("h")) {
                first1H = Recognize(funcG, typeH, 'h')[0];
                second1H = Recognize(funcG, typeH, 'h')[1];
                String replacement = Replace('h', -1, first1H, second1H);
                if (typeH == 1 || typeH == 2) {
                    funcG = funcG.replace("h(" + first1H + ")", replacement);
                } else {
                    funcG = funcG.replace("h(" + first1H + "," + second1H + ")", replacement);
                }
            }
        }
        if (funcH.contains("g")) {
            while (funcH.contains("g")) {
                first1G = Recognize(funcH, typeG, 'g')[0];
                second1G = Recognize(funcH, typeG, 'g')[1];
                String replacement = Replace('g', -1, first1G, second1G);
                if (typeG == 1 || typeG == 2) {
                    funcH = funcH.replace("g(" + first1G + ")", replacement);
                } else {
                    funcH = funcH.replace("g(" + first1G + "," + second1G + ")", replacement);
                }
            }
        }
    }

    public String[] Recognize(String input, int type, char ch) {
        String[] output = new String[2];
        if (type == 1 || type == 2) {
            int targetIndex = input.indexOf(ch);
            int start = input.indexOf('(', targetIndex);
            int end = Tool.matchParentheses(input, start);
            output[0] = input.substring(start + 1, end);
            return output;
        } else {
            int targetIndex = input.indexOf(ch);
            int start = input.indexOf('(', targetIndex);
            int end = Tool.matchParentheses(input, start);
            String temp = input.substring(start + 1, end);
            output = Tool.findParameters(temp);
            return output;
        }
    }

    public String recursion(String[] input, String[] inputF, String target) {
        this.target = target;
        if (inputF[0] == null && inputF[1] == null && inputF[2] == null) { //没有递归调用
            if (input[0] != null && input[1] != null) { //存在自定义普通函数
                distinguish(input);
                identifyType();
                preReplace(); //至此得到了g和h的最简表达式
            }
        } else { //有递归调用
            if (input[0] != null && input[1] != null) { //存在自定义普通函数
                distinguish(input);
                identifyType();
                preReplace(); //至此得到了g和h的最简表达式
            }
            distinguishF(inputF);
            identifyTypeF();
            processInitialFunc();
            identifyFuncN(); //识别出f{n-1}的形参和f{n-2}的形参
            ReplaceOneByOne(); //得到了f{0}--f{4}的所有结果
        }
        return produce();
    }

    public void distinguishF(String[] inputF) {
        for (int i = 0; i < 3; i++) {
            if (inputF[i].contains("=")) { //
                if (inputF[i].charAt(2) == '0') { //f{0}  funcI<0,x>
                    funcI.put(0, inputF[i].substring(inputF[i].indexOf("=") + 1));
                } else if (inputF[i].charAt(2) == '1') { //f{1}   <1,y>
                    funcI.put(1, inputF[i].substring(inputF[i].indexOf("=") + 1));
                } else if (inputF[i].charAt(2) == 'n') { //f{n}
                    funcN = inputF[i];
                }
            }
        }
    }

    public void identifyTypeF() {
        int index = funcN.indexOf('=');
        String temp = funcN.substring(0, index);
        if (temp.charAt(5) == 'x') {  //x   //x,y
            if (temp.contains(",")) {
                type = 3;
            } else {
                type = 1;
            }
        } else if (temp.charAt(5) == 'y') {
            if (temp.contains(",")) {
                type = 4;
            } else {
                type = 2;
            }
        }
    }

    public void processInitialFunc() {
        for (int i = 0; i <= 1; i++) {
            String func = funcI.get(i); //把f{0}和f{1}中存在的g，h相关的变量进行替换
            while (func.contains("g") || func.contains("h")) {
                int indexG = func.indexOf("g");
                int indexH = func.indexOf("h");
                if (indexG != -1 && indexH != -1) {
                    if (indexG < indexH) {
                        firstG = Recognize(func, typeG, 'g')[0];
                        secondG = Recognize(func, typeG, 'g')[1];
                        String replacement = Replace('g', -1, firstG, secondG);
                        if (typeG == 1 || typeG == 2) {
                            func = func.replace("g(" + firstG + ")", replacement);
                        } else {
                            func = func.replace("g(" + firstG + "," + secondG + ")", replacement);
                        }
                    } else {
                        firstH = Recognize(func, typeH, 'h')[0];
                        secondH = Recognize(func, typeH, 'h')[1];
                        String replacement = Replace('h', -1, firstH, secondH);
                        if (typeH == 1 || typeH == 2) {
                            func = func.replace("h(" + firstH + ")", replacement);
                        } else {
                            func = func.replace("h(" + firstH + "," + secondH + ")", replacement);
                        }
                    }
                } else if (indexG != -1 && indexH == -1) { //target中有g但无h
                    firstG = Recognize(func, typeG, 'g')[0];
                    secondG = Recognize(func, typeG, 'g')[1];
                    String replacement = Replace('g', -1, firstG, secondG);
                    if (typeG == 1 || typeG == 2) {
                        func = func.replace("g(" + firstG + ")", replacement);
                    } else {
                        func = func.replace("g(" + firstG + "," + secondG + ")", replacement);
                    }
                } else if (indexG == -1 && indexH != -1) { //target中有h但无g
                    firstH = Recognize(func, typeH, 'h')[0];
                    secondH = Recognize(func, typeH, 'h')[1];
                    String replacement = Replace('h', -1, firstH, secondH);
                    if (typeH == 1 || typeH == 2) {
                        func = func.replace("h(" + firstH + ")", replacement);
                    } else {
                        func = func.replace("h(" + firstH + "," + secondH + ")", replacement);
                    }
                } else { //即没有g也没有h，说明func已经得出最终结果
                    break;
                }
            }
            funcI.put(i, func);
        }
    }

    public void identifyFuncN() { //f{n}(x,y)=1*f{n-1}(x^2,y)+2*f{n-2}(x,y^2)+1
        if (type == 1 || type == 2) { //单一参数
            int targetIndex = funcN.indexOf("f{n-1}");
            int start = funcN.indexOf('(', targetIndex);
            int end = Tool.matchParentheses(funcN, start);
            first1 = funcN.substring(start + 1, end);
            //
            targetIndex = funcN.indexOf("f{n-2}");
            start = funcN.indexOf('(', targetIndex);
            end = Tool.matchParentheses(funcN, start);
            first2 = funcN.substring(start + 1, end);
        } else {
            int targetIndex = funcN.indexOf("f{n-1}");
            int start = funcN.indexOf('(', targetIndex);
            int end = Tool.matchParentheses(funcN, start);
            String temp = funcN.substring(start + 1, end);
            first1 = Tool.findParameters(temp)[0];
            second1 = Tool.findParameters(temp)[1];

            targetIndex = funcN.indexOf("f{n-2}");
            start = funcN.indexOf('(', targetIndex);
            end = Tool.matchParentheses(funcN, start);
            temp = funcN.substring(start + 1, end);
            first2 = Tool.findParameters(temp)[0];
            second2 = Tool.findParameters(temp)[1];
        }
    }

    public void ReplaceOneByOne() {
        for (int i = 0; i < 4; i++) {
            String n1 = Replace('f', i, first2, second2); //f(n-2)
            String n2 = Replace('f', i + 1, first1, second1);
            funcN = funcN.substring(funcN.indexOf("=") + 1);
            String str;
            if (type == 1 || type == 2) {
                str = funcN.replace("f{n-1}(" + first1 + ")", n2);
                str = str.replace("f{n-2}(" + first2 + ")", n1);
            } else {
                str = funcN.replace("f{n-1}(" + first1 + "," + second1 + ")", n2);
                str = str.replace("f{n-2}(" + first2 + "," + second2 + ")", n1);
            }
            while (str.contains("g") || str.contains("h")) {
                int indexG = str.indexOf("g");
                int indexH = str.indexOf("h");
                if (indexG != -1 && indexH != -1) {
                    if (indexG < indexH) {
                        firstG = Recognize(str, typeG, 'g')[0];
                        secondG = Recognize(str, typeG, 'g')[1];
                        String replacement = Replace('g', -1, firstG, secondG);
                        if (typeG == 1 || typeG == 2) {
                            str = str.replace("g(" + firstG + ")", replacement);
                        } else {
                            str = str.replace("g(" + firstG + "," + secondG + ")", replacement);
                        }
                    } else {
                        firstH = Recognize(str, typeH, 'h')[0];
                        secondH = Recognize(str, typeH, 'h')[1];
                        String replacement = Replace('h', -1, firstH, secondH);
                        if (typeH == 1 || typeH == 2) {
                            str = str.replace("h(" + firstH + ")", replacement);
                        } else {
                            str = str.replace("h(" + firstH + "," + secondH + ")", replacement);
                        }
                    }
                } else if (indexG != -1 && indexH == -1) { //target中有g但无h
                    firstG = Recognize(str, typeG, 'g')[0];
                    secondG = Recognize(str, typeG, 'g')[1];
                    String replacement = Replace('g', -1, firstG, secondG);
                    if (typeG == 1 || typeG == 2) {
                        str = str.replace("g(" + firstG + ")", replacement);
                    } else {
                        str = str.replace("g(" + firstG + "," + secondG + ")", replacement);
                    }
                } else if (indexG == -1 && indexH != -1) { //target中有h但无g
                    firstH = Recognize(str, typeH, 'h')[0];
                    secondH = Recognize(str, typeH, 'h')[1];
                    String replacement = Replace('h', -1, firstH, secondH);
                    if (typeH == 1 || typeH == 2) {
                        str = str.replace("h(" + firstH + ")", replacement);
                    } else {
                        str = str.replace("h(" + firstH + "," + secondH + ")", replacement);
                    }
                }
            }
            funcI.put(i + 2, str);
        }
    }

    public String Replace(char form, int i, String first, String second) {
        StringBuilder sb = new StringBuilder();
        if (form == 'f') {
            String pattern = funcI.get(i);
            sb.append("(");
            for (int j = 0; j < pattern.length(); j++) {
                char ch = pattern.charAt(j);
                Append(ch, type, sb, first, second);
            }
            sb.append(")");
        } else {
            sb.append("(");
            if (form == 'h') { //代表要用first，second作为实参去替换h的形参
                int index = funcH.indexOf("=");
                String temp = funcH.substring(index + 1);
                for (int j = 0; j < temp.length(); j++) {
                    char ch = temp.charAt(j);
                    Append(ch, typeH, sb, first, second);
                }
            } else if (form == 'g') {
                int index = funcG.indexOf("=");
                String temp = funcG.substring(index + 1);
                for (int j = 0; j < temp.length(); j++) {
                    char ch = temp.charAt(j);
                    Append(ch, typeG, sb, first, second);
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public void Append(char ch, int type, StringBuilder sb, String first, String second) {
        if (type == 1) {
            if (ch == 'x') {
                sb.append("(").append(first).append(")");
            } else {
                sb.append(ch);
            }
        } else if (type == 2) {
            if (ch == 'y') {
                sb.append("(").append(first).append(")");
            } else {
                sb.append(ch);
            }
        } else if (type == 3) {
            if (ch == 'x') {
                sb.append("(").append(first).append(")");
            } else if (ch == 'y') {
                sb.append("(").append(second).append(")");
            } else {
                sb.append(ch);
            }
        } else if (type == 4) {
            if (ch == 'y') {
                sb.append("(").append(first).append(")");
            } else if (ch == 'x') {
                sb.append("(").append(second).append(")");
            } else {
                sb.append(ch);
            }
        }
    }

    public int recognize() {
        int number = -1;
        if (type == 1 || type == 2) { //success
            int targetIndex = target.indexOf("f{");  //对应f的下标
            number = Integer.parseInt(String.valueOf(target.charAt(targetIndex + 2)));
            int start = target.indexOf('(', targetIndex);
            int end = Tool.matchParentheses(target, start);
            first = target.substring(start + 1, end);
        } else { //f{2}((x+1),x)*x
            int targetIndex = target.indexOf("f{");  //对应f的下标
            number = Integer.parseInt(String.valueOf(target.charAt(targetIndex + 2)));
            int start = target.indexOf('(', targetIndex);
            int end = Tool.matchParentheses(target, start);
            String temp = target.substring(start + 1, end);
            first = Tool.findParameters(temp)[0];
            second = Tool.findParameters(temp)[1];
        }
        return number;
    }

    public String produce() {
        while (target.contains("f") || target.contains("g") || target.contains("h")) {
            int indexF = target.indexOf("f");
            int indexG = target.indexOf("g");
            int indexH = target.indexOf("h");
            if (indexF != -1 && indexG != -1 && indexH != -1) {
                if (indexF < indexG && indexG < indexH) {
                    target = processF(target);
                } else if (indexF < indexH && indexH < indexG) {
                    target = processF(target);
                } else if (indexG < indexF && indexF < indexH) {
                    target = processG(target);
                } else if (indexG < indexH && indexH < indexF) {
                    target = processG(target);
                } else if (indexH < indexF && indexF < indexG) {
                    target = processH(target);
                } else { //H,G,F
                    target = processH(target);
                }
            } else if (indexF != -1 && indexG != -1 && indexH == -1) {
                if (indexF < indexG) {
                    target = processF(target);
                } else {
                    target = processG(target);
                }
            } else if (indexF != -1 && indexG == -1 && indexH != -1) {
                if (indexF < indexH) {
                    target = processF(target);
                } else {
                    target = processH(target);
                }
            } else if (indexF == -1 && indexG != -1 && indexH != -1) {
                if (indexG < indexH) {
                    target = processG(target);
                } else {
                    target = processH(target);
                }
            } else if (indexF != -1 && indexG == -1 && indexH == -1) {
                target = processF(target);
            } else if (indexF == -1 && indexG != -1 && indexH == -1) {
                target = processG(target);
            } else if (indexF == -1 && indexG == -1 && indexH != -1) {
                target = processH(target);
            }
        }
        return target;
    }

    public String processF(String target) {
        String temp = target;
        int number = recognize();
        String replacement = Replace('f', number, first, second);
        if (type == 1 || type == 2) {
            temp = temp.replace("f{" + number + "}(" + first + ")", replacement);
        } else {
            temp = temp.replace("f{" + number + "}(" + first + "," + second + ")",
                    replacement);
        }
        return temp;
    }

    public String processG(String target) {
        String temp = target;
        firstG = Recognize(temp, typeG, 'g')[0];
        secondG = Recognize(temp, typeG, 'g')[1];
        String replacement = Replace('g', -1, firstG, secondG);
        if (typeG == 1 || typeG == 2) {
            temp = temp.replace("g(" + firstG + ")", replacement);
        } else {
            temp = temp.replace("g(" + firstG + "," + secondG + ")", replacement);
        }
        return temp;
    }

    public String processH(String target) {
        String temp = target;
        firstH = Recognize(temp, typeH, 'h')[0];
        secondH = Recognize(temp, typeH, 'h')[1];
        String replacement = Replace('h', -1, firstH, secondH);
        if (typeH == 1 || typeH == 2) {
            temp = temp.replace("h(" + firstH + ")", replacement);
        } else {
            temp = temp.replace("h(" + firstH + "," + secondH + ")", replacement);
        }
        return temp;
    }
}