import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Sin implements Factor {
    //sinFactor.java
    private Factor factor; //三角函数括号内的因子
    private int exp; //三角函数的指数部分

    public Sin(Factor factor, int exp) {
        this.factor = factor;
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("sin(");
        sb.append(factor.toString());
        if (exp > 1) {
            sb.append(")^" + exp);
        } else {
            sb.append(")");
        }

        return sb.toString();
    }

    @Override
    public void print() {
        System.out.println("Sin " + this);
    }

    @Override
    public Poly toPoly() {
        HashMap<Poly, Integer> sinMap = new HashMap<>();
        Unit unit;
        sinMap.put(factor.toPoly(), exp);
        if (exp != 0) {
            unit = new Unit(BigInteger.valueOf(1), 0, sinMap, new HashMap<>());
        } else {
            unit = new Unit(BigInteger.valueOf(1), 0, new HashMap<>(), new HashMap<>());
        }
        ArrayList<Unit> units = new ArrayList<>();
        units.add(unit);
        Poly poly = new Poly(units);
        return poly;
    }
}
