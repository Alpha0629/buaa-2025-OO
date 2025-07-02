import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Cos implements Factor {
    //cosFactor.java
    private Factor factor; //三角函数括号内的因子
    private int exp; //三角函数的指数部分

    public Cos(Factor factor, int exp) {
        this.factor = factor;
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cos(");
        sb.append(factor.toString());
        sb.append(")^" + exp);
        return sb.toString();
    }

    @Override
    public void print() {
        System.out.println("Cos " + this);
    }

    @Override
    public Poly toPoly() {
        HashMap<Poly, Integer> cosMap = new HashMap<>();
        //1
        Unit unit;
        cosMap.put(factor.toPoly(), exp);
        if (exp != 0) {
            unit = new Unit(BigInteger.valueOf(1), 0, new HashMap<>(), cosMap);
        } else {
            unit = new Unit(BigInteger.valueOf(1), 0, new HashMap<>(), new HashMap<>());
        }
        ArrayList<Unit> units = new ArrayList<>();
        units.add(unit);
        Poly poly = new Poly(units);
        return poly;
    }
}
