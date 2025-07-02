import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Num implements Factor {
    private final String value;
    private int exp = 1;

    public Num(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public void print() {
        System.out.println("Num " + this);
    }

    @Override
    public Poly toPoly() {
        BigInteger coe = new BigInteger(value);
        Unit unit = new Unit(coe, 0, new HashMap<>(), new HashMap<>());
        ArrayList<Unit> units = new ArrayList<>();
        units.add(unit);
        Poly poly = new Poly(units);
        return poly;
    }
}
