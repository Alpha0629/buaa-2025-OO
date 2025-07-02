import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final Pattern numberPattern = Pattern.compile("[a-z0-9]+"); // TODO
    private final HashMap<String, Integer> parameters;
    
    public Parser(HashMap<String, Integer> parameters) {
        this.parameters = parameters;
    }
    
    public Operator parse(String expression) {
        int position = findAddOrSub(expression);
        if (position != -1) {
            if (expression.charAt(position) == '+') {
                return new Add(parse(expression.substring(0, position)),
                        parse(expression.substring(position + 1)));
            } else {
                return new Sub(parse(expression.substring(0, position)),
                        parse(expression.substring(position + 1)));
            }
        } else {
            position = findMul(expression);
            if (position != -1) {
                return new Mul(parse(expression.substring(0, position))
                        , parse(expression.substring(position + 1)));
            } else {
                if (!expression.equals("")) { //最小单元例如 x
                    Matcher matcher = numberPattern.matcher(expression);
                    
                    // TODO
                    int num = 0;
                    if(matcher.find()) {
                        String matchedText = matcher.group();
                        if(parameters.get(matchedText) != null) {
                            num =  parameters.get(matchedText);
                        }
                        else{
                            num = Integer.parseInt(matchedText);
                        }
                    }
                    return new Num(num);


                } else {
                    return new Num(0);
                }
            }
        }
    }

    private int findAddOrSub(String expression) {
        int position = -1;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '+' || expression.charAt(i) == '-') {
                position = i;
            }
        }
        return position;
    }

    private int findMul(String expression) {
        int position = -1;
        
        // TODO
        for(int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '*') {
                position = i;
            }
        }

        return position;
    }
}
