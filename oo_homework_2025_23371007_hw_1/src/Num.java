import java.math.BigInteger;
import java.util.ArrayList;

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
        BigInteger num = new BigInteger(value);
        Mono mono = new Mono(num,0);
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        Poly poly = new Poly(monos);
        return poly;
    }
}
