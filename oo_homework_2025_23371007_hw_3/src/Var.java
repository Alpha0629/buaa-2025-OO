import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Var implements Factor {
    private final String name;
    private int exp = 1;

    public Var(String name) {
        this.name = name;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (exp > 1) {
            sb.append("^").append(exp);
        }
        return sb.toString();
    }

    public void print() {
        System.out.println("Var " + this);
    }

    @Override
    public Poly toPoly() {
        Unit unit = new Unit(BigInteger.valueOf(1), exp, new HashMap<>(), new HashMap<>());
        ArrayList<Unit> units = new ArrayList<>();
        units.add(unit);
        Poly poly = new Poly(units);
        return poly;
    }
}
