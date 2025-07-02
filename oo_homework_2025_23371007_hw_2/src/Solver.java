import java.util.HashMap;
import java.util.Stack;

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

    public String recursion(String[] input) {
        distinguish(input);
        identifyType();
        identifyFuncN();
        ReplaceOneByOne();
        //System.out.println(funcN);
        return produce();
    }

    public void distinguish(String[] input) {
        for (int i = 0; i < 4; i++) {
            if (input[i].contains("=")) { //
                if (input[i].charAt(2) == '0') { //f{0}  funcI<0,x>
                    funcI.put(0, input[i].substring(input[i].indexOf("=") + 1));
                } else if (input[i].charAt(2) == '1') { //f{1}   <1,y>
                    funcI.put(1, input[i].substring(input[i].indexOf("=") + 1));
                } else if (input[i].charAt(2) == 'n') { //f{n}
                    funcN = input[i];
                }
            } else { //所求表达式
                target = input[i];
            }
        }
    }

    public void identifyType() {
        if (funcN.charAt(5) == 'x') {  //x   //x,y
            if (funcN.contains(",")) {
                type = 3;
            } else {
                type = 1;
            }
        } else if (funcN.charAt(5) == 'y') {
            if (funcN.contains(",")) {
                type = 4;
            } else {
                type = 2;
            }
        }
    }

    //f{n}(x)=2*f{n-1}(x)-1*f{n-2}(x)
    public void identifyFuncN() { //f{n}(x,y)=1*f{n-1}(x^2,y)+2*f{n-2}(x,y^2)+1
        if (type == 1 || type == 2) { //单一参数
            int targetIndex = funcN.indexOf("f{n-1}");
            int start = funcN.indexOf('(', targetIndex);
            int end = matchParentheses(funcN, start);
            first1 = funcN.substring(start + 1, end);
            //
            targetIndex = funcN.indexOf("f{n-2}");
            start = funcN.indexOf('(', targetIndex);
            end = matchParentheses(funcN, start);
            first2 = funcN.substring(start + 1, end);
        } else {
            int targetIndex = funcN.indexOf("f{n-1}");
            int start = funcN.indexOf('(', targetIndex);
            int end = matchParentheses(funcN, start);
            String temp = funcN.substring(start + 1, end);
            String[] outcome = findParameters(temp); //success
            first1 = outcome[0];
            second1 = outcome[1];
            //System.out.println(first1+" "+second1);

            targetIndex = funcN.indexOf("f{n-2}");
            start = funcN.indexOf('(', targetIndex);
            end = matchParentheses(funcN, start);
            temp = funcN.substring(start + 1, end);
            outcome = findParameters(temp); //success
            first2 = outcome[0];
            second2 = outcome[1];
            //System.out.println(first2+" "+second2);
        }
    }

    public static int matchParentheses(String input, int start) {
        Stack<Integer> stack = new Stack<>();
        stack.push(start); // 将第一个左括号的位置入栈
        int end = start + 1;

        while (end < input.length() && !stack.isEmpty()) {
            char ch = input.charAt(end);
            if (ch == '(') {
                stack.push(end); // 遇到左括号，入栈
            } else if (ch == ')') {
                stack.pop(); // 遇到右括号，出栈
                if (stack.isEmpty()) {
                    // 栈为空，说明匹配到了最外层的右括号
                    return end;
                }
            }
            end++;
        }

        // 如果栈不为空，说明括号不匹配
        return -1;
    }

    public void ReplaceOneByOne() {
        for (int i = 0; i < 4; i++) {
            //System.out.println(1);
            String n1 = Replace(i, first2, second2); //f(n-2)
            //System.out.println(n1);
            String n2 = Replace(i + 1, first1, second1);
            //System.out.println(n2);
            funcN = funcN.substring(funcN.indexOf("=") + 1);
            String str;
            if (type == 1 || type == 2) {
                str = funcN.replace("f{n-1}(" + first1 + ")", n2);
                str = str.replace("f{n-2}(" + first2 + ")", n1);
            } else {
                str = funcN.replace("f{n-1}(" + first1 + "," + second1 + ")", n2);
                str = str.replace("f{n-2}(" + first2 + "," + second2 + ")", n1);
            }
            //System.out.println(str);
            funcI.put(i + 2, str);
        }
    }

    public String Replace(int i, String first, String second) {
        StringBuilder sb = new StringBuilder();
        String pattern = funcI.get(i);
        sb.append("(");
        //System.out.println(funcI.get(0));
        for (int j = 0; j < pattern.length(); j++) {
            char ch = pattern.charAt(j);
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
        sb.append(")");
        //System.out.println(sb.toString());
        return sb.toString();
    }

    public int recognize() {
        int number = -1;
        if (type == 1 || type == 2) { //success
            int targetIndex = target.indexOf("f{");  //对应f的下标
            number = Integer.parseInt(String.valueOf(target.charAt(targetIndex + 2)));
            int start = target.indexOf('(', targetIndex);
            int end = matchParentheses(target, start);
            first = target.substring(start + 1, end);
            //System.out.println(first);
        } else { //f{2}((x+1),x)*x
            int targetIndex = target.indexOf("f{");  //对应f的下标
            number = Integer.parseInt(String.valueOf(target.charAt(targetIndex + 2)));
            int start = target.indexOf('(', targetIndex);
            int end = matchParentheses(target, start);
            String temp = target.substring(start + 1, end);
            //System.out.println(temp);

            String[] outcome = findParameters(temp); //success
            first = outcome[0];
            second = outcome[1];

            //System.out.println(first + " " + second);
        }

        return number;
    }

    public String produce() {
        //System.out.println(1);
        while (target.contains("f")) {
            //System.out.println(1);
            int number = recognize();
            String replacement = Replace(number, first, second);
            if (type == 1 || type == 2) {
                target = target.replace("f{" + number + "}(" + first + ")", replacement);
            } else {
                target = target.replace("f{" + number + "}(" + first + "," + second + ")",
                        replacement);
            }
        }

        return target;
    }

    public String[] findParameters(String temp) {
        String[] outcome = new String[2];
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < temp.length(); i++) {
            char ch = temp.charAt(i);
            if (ch == '(') {
                stack.push(i);
            } else if (ch == ')') {
                stack.pop();
            }

            if (ch == ',' && stack.isEmpty()) {
                outcome[0] = temp.substring(0, i);
                outcome[1] = temp.substring(i + 1);
                break;
            }
        }

        return outcome;
    }
}
