import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

public class Unit {
    private BigInteger coe;
    private int varExp;
    private HashMap<Poly, Integer> sinMap;
    private HashMap<Poly, Integer> cosMap;

    public Unit(BigInteger coe, int varExp, HashMap<Poly, Integer> sinMap,
                HashMap<Poly, Integer> cosMap) {
        this.coe = coe;
        this.varExp = varExp;
        this.sinMap = sinMap;
        this.cosMap = cosMap;
    }

    public int getVarExp() {
        return varExp;
    }

    public void setVarExp(int varExp) {
        this.varExp = varExp;
    }

    public BigInteger getCoe() {
        return coe;
    }

    public void setCoe(BigInteger coe) {
        this.coe = coe;
    }

    /*public HashMap<Poly, Integer> getSinMap() {
        return sinMap;
    }*/
    public HashMap<Poly, Integer> getSinMap() {
        if (sinMap == null) {
            sinMap = new HashMap<>();
        }
        return sinMap;
    }

    public HashMap<Poly, Integer> getCosMap() {
        if (cosMap == null) {
            cosMap = new HashMap<>();
        }
        return cosMap;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Unit) {
            Unit u = (Unit) o;
            if (u.varExp == this.varExp &&
                    u.sinMap.equals(this.sinMap) && u.cosMap.equals(this.cosMap)) {
                //x的幂次相同，sin，cos内部相同
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (coe.compareTo(BigInteger.ZERO) == 0) { //coe == 0
            return "";
        } else if (coe.compareTo(BigInteger.ZERO) > 0) {
            BiggerThanZero(sb);
        } else { //系数为负数
            LesserThanZero(sb);
        }
        return sb.toString();
    }

    public void BiggerThanZero(StringBuilder sb) {
        if (varExp == 0) {
            if (sinMap.isEmpty() && cosMap.isEmpty()) {
                if (coe.compareTo(BigInteger.ONE) == 0) {
                    sb.append("+1");
                } else {
                    sb.append("+");
                    sb.append(coe);
                }
            } else {
                if (coe.compareTo(BigInteger.ONE) == 0) { //wrong!
                    sb.append("+");
                    //sb.append(coe);
                } else {
                    sb.append("+");
                    sb.append(coe);
                    sb.append("*");
                }

                if (!sinMap.isEmpty()) {
                    sinMapToString(sb);
                    if (!cosMap.isEmpty()) {
                        sb.append("*");
                        cosMapToString(sb);
                    }
                } else {
                    if (!cosMap.isEmpty()) {
                        cosMapToString(sb);
                    }
                }
            }
        } else { //x的指数不为0
            sb.append("+");
            if (varExp == 1) {
                if (coe.compareTo(BigInteger.ONE) != 0) {
                    sb.append(coe.toString());
                    sb.append("*");
                }
                sb.append("x");
            } else {
                if (coe.compareTo(BigInteger.ONE) != 0) {
                    sb.append(coe.toString());
                    sb.append("*");
                }
                sb.append("x^" + varExp);
            }
            if (!sinMap.isEmpty()) {
                sb.append("*");
                sinMapToString(sb);
                if (!cosMap.isEmpty()) {
                    sb.append("*");
                    cosMapToString(sb);
                }
            } else { //sin空
                if (!cosMap.isEmpty()) {
                    sb.append("*");
                    cosMapToString(sb);
                }
            }
        }
    }

    public void LesserThanZero(StringBuilder sb) {
        if (varExp == 0) {
            //sb.append("+");
            if (sinMap.isEmpty() && cosMap.isEmpty()) {
                sb.append(coe);
            } else {
                if (coe.compareTo(BigInteger.valueOf(-1)) == 0) { //如果系数为-1且后面跟着sin，则直接输出负号
                    sb.append("-");
                } else {
                    sb.append(coe);
                    sb.append("*");
                }
                if (!sinMap.isEmpty()) {
                    sinMapToString(sb);
                    if (!cosMap.isEmpty()) {
                        sb.append("*");
                        cosMapToString(sb);
                    }
                } else { //sin空
                    if (!cosMap.isEmpty()) {
                        //sb.append("*");
                        cosMapToString(sb);
                    }
                }

            }

        } else { //指数不为0
            if (varExp == 1) {
                if (coe.compareTo(BigInteger.valueOf(-1)) == 0) {
                    sb.append("-");
                    sb.append("x");
                } else {
                    sb.append(coe.toString() + "*x");
                }
            } else { //指数大于1
                if (coe.compareTo(BigInteger.valueOf(-1)) == 0) {
                    sb.append("-");
                    sb.append("x^" + varExp);
                } else {
                    sb.append(coe.toString() + "*x^" + varExp);
                }
            }
            if (!sinMap.isEmpty()) {
                sb.append("*");
                sinMapToString(sb);
                if (!cosMap.isEmpty()) {
                    sb.append("*");
                    cosMapToString(sb);
                }
            } else { //sin空
                if (!cosMap.isEmpty()) {
                    sb.append("*");
                    cosMapToString(sb);
                }
            }
        }
    }

    public void sinMapToString(StringBuilder sb) {
        Iterator<Poly> iterator = this.sinMap.keySet().iterator();
        while (iterator.hasNext()) {
            Poly p = iterator.next();
            // 判断是否是最后一个元素
            if (iterator.hasNext()) {
                if (this.sinMap.get(p) == 0) {
                    // 处理 sinMap.get(p) == 0 的情况
                } else if (this.sinMap.get(p) == 1) {
                    sb.append("sin((" + p.toString() + "))");
                    sb.append("*");
                } else {
                    sb.append("sin((" + p.toString() + "))^" + this.sinMap.get(p));
                    sb.append("*");
                }
            } else { //最后一个元素
                if (this.sinMap.get(p) == 0) {
                    // 处理 cosMap.get(p) == 0 的情况
                } else if (this.sinMap.get(p) == 1) {
                    sb.append("sin((" + p.toString() + "))");
                } else {
                    sb.append("sin((" + p.toString() + "))^" + this.sinMap.get(p));
                }
            }
        }
    }

    public void cosMapToString(StringBuilder sb) {
        Iterator<Poly> iterator = this.cosMap.keySet().iterator();
        while (iterator.hasNext()) {
            Poly p = iterator.next();
            // 判断是否是最后一个元素
            if (iterator.hasNext()) {
                if (this.cosMap.get(p) == 0) {
                    // 处理 cosMap.get(p) == 0 的情况
                } else if (this.cosMap.get(p) == 1) {
                    sb.append("cos((" + p.toString() + "))");
                    sb.append("*");
                } else {
                    sb.append("cos((" + p.toString() + "))^" + this.cosMap.get(p));
                    sb.append("*");
                }
            } else { //最后一个元素
                if (this.cosMap.get(p) == 0) {
                    // 处理 cosMap.get(p) == 0 的情况
                } else if (this.cosMap.get(p) == 1) {
                    sb.append("cos((" + p.toString() + "))");
                } else {
                    sb.append("cos((" + p.toString() + "))^" + this.cosMap.get(p));
                }
            }
        }
    }
}

