import java.math.BigInteger;

public class Mono {
    private BigInteger coe;
    private int exp;

    public Mono(BigInteger coe, int exp) {
        this.coe = coe;
        this.exp = exp;
    }

    public BigInteger getCoe() {
        return coe;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public String toString() {
        if (exp == 0) { //指数为0
            if (coe.compareTo(BigInteger.ZERO) == 0) { //0
                return "";
            } else { // 直接返回系数
                if (coe.compareTo(BigInteger.ZERO) > 0) {
                    return "+" + coe.toString(); //额外添加符号
                } else  /*(coe.compareTo(BigInteger.ZERO) < 0)*/ {
                    return coe.toString();
                }
            }
        } else if (exp == 1) {
            // 如果指数为1，返回 coe*x
            if (coe.compareTo(BigInteger.ZERO) > 0) {
                return "+" + coe.toString() + "*x";
            } else if (coe.compareTo(BigInteger.ZERO) < 0) {
                return coe.toString() + "*x";
            } else {
                return "";
            }
        } else {
            // 如果指数大于1，返回 coe*x^exp
            if (coe.compareTo(BigInteger.ZERO) > 0) {
                return "+" + coe.toString() + "*x^" + exp;
            } else if (coe.compareTo(BigInteger.ZERO) < 0) {
                return coe.toString() + "*x^" + exp;
            } else {
                return "";
            }
        }
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setCoe(BigInteger coe) {
        this.coe = coe;
    }

}
