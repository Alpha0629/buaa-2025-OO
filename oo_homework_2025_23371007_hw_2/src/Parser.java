public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        int sign;
        if (lexer.getCurToken().getType() == Token.Type.SUB) { //开头是+
            sign = -1;
            lexer.nextToken();//index++
        } else if (lexer.getCurToken().getType() == Token.Type.ADD) { //开头是-
            sign = 1;
            lexer.nextToken();//index++
        } else { //开头无+\-,故默认为+,index不必往下走
            sign = 1;
        }
        expr.addTerm(parseTerm(sign));//+、-、无符号
        while (!lexer.isEnd() && (lexer.getCurToken().getType() == Token.Type.ADD ||
                lexer.getCurToken().getType() == Token.Type.SUB)) {
            if (lexer.getCurToken().getType() == Token.Type.ADD) {
                lexer.nextToken();
                expr.addTerm(parseTerm(1));
            } else if (lexer.getCurToken().getType() == Token.Type.SUB) {
                lexer.nextToken();
                expr.addTerm(parseTerm(-1));
            }

        }
        //expr.print();
        return expr;
    }

    public Term parseTerm(int sign) {
        Term term = new Term();
        term.setSign(sign);
        term.addFactor(parseFactor());
        while (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.MUL) {
            lexer.nextToken();
            term.addFactor(parseFactor());
        }
        //term.print();
        return term;
    }

    public Factor parseFactor() {
        Token token = lexer.getCurToken();
        if (token.getType() == Token.Type.ADD || token.getType() == Token.Type.SUB) {
            int sign = 0;
            if (token.getType() == Token.Type.ADD) {
                lexer.nextToken();
                sign = 1;
            } else if (token.getType() == Token.Type.SUB) {
                lexer.nextToken();
                sign = -1;
            }
            return parseNum(sign);
        } else if (token.getType() == Token.Type.NUM) {
            return parseNum(1);
        } else if (token.getType() == Token.Type.VAR) {
            return parseVar();
        } else if (token.getType() == Token.Type.SIN) {
            return parseSinCosFactor("sin");
        } else if (token.getType() == Token.Type.COS) {
            return parseSinCosFactor("cos");
        } else { //当前token.type是括号
            lexer.nextToken();  //进入括号内
            SubExpr subExpr = parseSubExpr();
            lexer.nextToken(); //跳过右括号
            if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.POW) { //遇到指数
                lexer.nextToken();
                if (lexer.getCurToken().getType() == Token.Type.ADD) { //遇到无用的加号
                    lexer.nextToken(); //解析数字
                    Num pow = parseNum(1);
                    subExpr.setExp(Integer.valueOf(pow.getValue()));
                } else { //直接解析
                    Num pow = parseNum(1);
                    subExpr.setExp(Integer.valueOf(pow.getValue()));
                }
            }
            //subExpr.print();
            return subExpr;
        }
    }

    public Num parseNum(int sign) {
        Token token = lexer.getCurToken();
        lexer.nextToken();
        Num num;
        if (sign == -1) {
            num = new Num('-' + token.getContent());
        } else {
            num = new Num(token.getContent());
        }
        //num.print();
        return num;
    }

    public Var parseVar() {
        Token token = lexer.getCurToken();
        lexer.nextToken();
        Var var = new Var(token.getContent());
        if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.POW) {
            lexer.nextToken();//跳过指数符号
            if (lexer.getCurToken().getType() == Token.Type.ADD) {
                lexer.nextToken(); //跳过无用的加号
                Num pow = parseNum(1);
                var.setExp(Integer.valueOf(pow.getValue()));
            } else {
                Num pow = parseNum(1);
                var.setExp(Integer.valueOf(pow.getValue()));
            }
        }
        //var.print();
        return var;
    }

    public Factor parseSinCosFactor(String type) {
        //Token token = lexer.getCurToken();
        lexer.nextToken();//此时指向左括号
        lexer.nextToken();//此时指向括号内的因子
        Factor inside = parseFactor(); //解析三角函数内部的因子
        lexer.nextToken();//跳过右括号，指向后面内容
        int exp = 1;//指数
        if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.POW) {
            lexer.nextToken();
            if (lexer.getCurToken().getType() == Token.Type.ADD) {
                lexer.nextToken();//跳过加号
                Num pow = parseNum(1);
                exp = Integer.valueOf(pow.getValue());
            } else {
                Num pow = parseNum(1);
                exp = Integer.valueOf(pow.getValue());
            }
        }
        if (type.equals("sin")) {
            Sin sin = new Sin(inside, exp);
            //sin.print();
            return sin;
        } else { //type.equals("cos")
            Cos cos = new Cos(inside, exp);
            //cos.print();
            return cos;
        }
    }

    public SubExpr parseSubExpr() {
        SubExpr subExpr = new SubExpr();
        int sign;
        if (lexer.getCurToken().getType() == Token.Type.SUB) { //开头是+
            sign = -1;
            lexer.nextToken();//index++
        } else if (lexer.getCurToken().getType() == Token.Type.ADD) { //开头是-
            sign = 1;
            lexer.nextToken();//index++
        } else { //开头无+\-,故默认为+,index不必往下走
            sign = 1;
        }
        subExpr.addTerm(parseTerm(sign));//+、-、无符号
        while (!lexer.isEnd() && (lexer.getCurToken().getType() == Token.Type.ADD ||
                lexer.getCurToken().getType() == Token.Type.SUB)) {
            if (lexer.getCurToken().getType() == Token.Type.ADD) {
                lexer.nextToken();
                subExpr.addTerm(parseTerm(1));
            } else if (lexer.getCurToken().getType() == Token.Type.SUB) {
                lexer.nextToken();
                subExpr.addTerm(parseTerm(-1));
            }

        }
        //subExpr.print();
        return subExpr;
    }
}