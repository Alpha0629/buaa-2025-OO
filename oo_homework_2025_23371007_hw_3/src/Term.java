import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Term {
    private final ArrayList<Factor> factors = new ArrayList<>();
    private int sign = 0;

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
        if (sign == -1) {
            sb.append("-");
        }
        for (Factor factor : factors) {
            /*if (factor instanceof SubExpr) {
                sb.append("(" + factor.toString() + ")");
            } else {*/
            sb.append(factor.toString());
            //}
            sb.append("*");

        }
        return sb.substring(0, sb.length() - 1);
    }

    public Poly toPoly() {
        Poly poly = new Poly(new ArrayList<Unit>());
        Unit unit = new Unit(BigInteger.valueOf(1), 0, new HashMap<>(), new HashMap<>());
        poly.getUnits().add(unit);
        for (Factor it : factors) {
            // System.out.println("cir");
            Poly temp = poly.multPoly(it.toPoly());
            //factor.toPoly是正常的
            //temp出了问题
            //System.out.println(temp.toString());
            poly = temp;
            //System.out.println(it.toString().toString());
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
