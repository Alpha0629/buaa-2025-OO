import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factors = new ArrayList<>();
    int sign = 0;

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getSign() {
        return this.sign;
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(sign == -1){
            sb.append("-");
        }
        for (Factor factor : factors) {
            if (factor instanceof SubExpr) {
                sb.append("(" + factor.toString() + ")");
            } else {
                sb.append(factor.toString());
            }
            sb.append("*");

        }
        return sb.substring(0, sb.length() - 1); // remove the last "*"
    }

    public Poly toPoly() {
        Poly poly = new Poly(new ArrayList<Mono>());
        poly.getMonos().add(new Mono(BigInteger.valueOf(1), 0));
        for (Factor it : factors) {
            Poly temp = poly.multPoly(it.toPoly());
            poly = temp;
            System.out.println("cir");
        }
        if (sign == -1) { //如果项前面是负数则需要把所有该项所有因子全部反转
            poly.negate();
        }
        return poly;
    }
    public void print() {
        System.out.println("Term " + this);
    }
}
