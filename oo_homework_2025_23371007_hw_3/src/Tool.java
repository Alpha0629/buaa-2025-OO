import java.util.Stack;

public class Tool {

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
                    return end;
                }
            }
            end++;
        }
        return -1;
    }

    public static String[] findParameters(String temp) {
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
