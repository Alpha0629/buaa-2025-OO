import java.math.BigInteger;
import java.util.ArrayList;

public class Var implements Factor{
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
        return name;
    }

    public void print() {
        System.out.println("Var " + this);
    }

    @Override
    public Poly toPoly() {
        Mono mono = new Mono(BigInteger.valueOf(1),this.exp);
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(mono);
        Poly poly = new Poly(monos);
        return poly;
    }
}
