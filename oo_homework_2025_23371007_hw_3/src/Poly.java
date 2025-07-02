import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Poly {
    private ArrayList<Unit> units;

    public Poly(ArrayList<Unit> units) {
        this.units = units;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public void negate() {
        for (Unit u : units) {
            u.setCoe(u.getCoe().negate());
        }
    }

    public Poly addPoly(Poly other) {
        ArrayList<Unit> result = new ArrayList<>();
        //System.out.println("进行加法运算");
        for (Unit unit : this.units) {
            result.add(new Unit(unit.getCoe(), unit.getVarExp(),
                    unit.getSinMap(), unit.getCosMap()));
            //System.out.println("加数存在元素");
        }

        for (Unit otherUnit : other.getUnits()) {
            //System.out.println("正在计算第二个加数");
            //System.out.println("正在计算第一个加数" + otherUnit.toString());
            boolean found = false;
            for (Unit resultUnit : result) {
                //System.out.println("正在计算第二个加数" + resultUnit.toString());
                if (resultUnit.equals(otherUnit)) { //如果两个unit可以合并，则系数累加
                    //System.out.println("Found same unit");
                    resultUnit.setCoe(resultUnit.getCoe().add(otherUnit.getCoe()));
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(new Unit(otherUnit.getCoe(), otherUnit.getVarExp(),
                        otherUnit.getSinMap(), otherUnit.getCosMap()));
            }
        }
        Poly poly = new Poly(result);
        return poly;
    }

    public Poly multPoly(Poly other) {
        Poly poly = new Poly(new ArrayList<>());
        for (Unit unit : this.units) {
            Poly tempPoly = new Poly(new ArrayList<>());
            for (Unit otherUnit : other.getUnits()) {
                //BigInteger newCoe = unit.getCoe().multiply(otherUnit.getCoe());
                //int newVarExp = unit.getVarExp() + otherUnit.getVarExp();
                HashMap<Poly, Integer> newSinMap = new HashMap<>();
                HashMap<Poly, Integer> newCosMap = new HashMap<>();
                //sin
                for (Poly p2 : otherUnit.getSinMap().keySet()) {
                    newSinMap.put(p2, otherUnit.getSinMap().get(p2));
                }
                for (Poly p1 : unit.getSinMap().keySet()) {
                    boolean found = false;
                    for (Poly p2 : otherUnit.getSinMap().keySet()) {
                        if (p1.equals(p2)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        newSinMap.put(p1, newSinMap.get(p1) + unit.getSinMap().get(p1));  //p1=p2
                    } else {
                        newSinMap.put(p1, unit.getSinMap().get(p1));//
                    }
                }
                //cos
                for (Poly p2 : otherUnit.getCosMap().keySet()) {
                    newCosMap.put(p2, otherUnit.getCosMap().get(p2));
                }
                for (Poly p1 : unit.getCosMap().keySet()) {
                    boolean found = false;
                    for (Poly p2 : otherUnit.getCosMap().keySet()) {
                        if (p1.equals(p2)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        newCosMap.put(p1, newCosMap.get(p1) + unit.getCosMap().get(p1));  //p1=p2
                    } else {
                        newCosMap.put(p1, unit.getCosMap().get(p1));
                    }
                }
                //end
                BigInteger newCoe = unit.getCoe().multiply(otherUnit.getCoe());
                int newVarExp = unit.getVarExp() + otherUnit.getVarExp();
                Unit newUnit = new Unit(newCoe, newVarExp, newSinMap, newCosMap);
                tempPoly.getUnits().add(newUnit);
            }
            poly = poly.addPoly(tempPoly);
        }
        return poly;
    }

    public Poly powPoly(int pow) {
        if (pow == 0) {
            Poly poly = new Poly(new ArrayList<Unit>());
            Unit unit = new Unit(BigInteger.valueOf(1), 0, new HashMap<>(), new HashMap<>());
            poly.getUnits().add(unit);
            return poly;
        } else {
            Poly poly = new Poly(new ArrayList<Unit>());
            Unit unit = new Unit(BigInteger.valueOf(1), 0, new HashMap<>(), new HashMap<>());
            poly.getUnits().add(unit);
            for (int i = 0; i < pow; i++) {
                poly = poly.multPoly(this);
                //System.out.println(poly.toString());
            }
            return poly;
        }
    }

    public Poly toDx() {
        ArrayList<Unit> result = new ArrayList<>();
        for (Unit u : units) {
            //Unit newUnit = new Unit(BigInteger.valueOf(0), 0, new HashMap<>(), new HashMap<>());
            if (u.getVarExp() == 0 && u.getSinMap().isEmpty() && u.getCosMap().isEmpty()) { //常数求导
                Unit newUnit = new Unit(BigInteger.valueOf(0), 0, new HashMap<>(), new HashMap<>());
                result.add(newUnit);
            } else {
                // 处理x的幂
                if (u.getVarExp() != 0) { //a*x^b求导＝a*b*x^b-1
                    BigInteger newCoe = u.getCoe().multiply(BigInteger.valueOf(u.getVarExp()));
                    int newVarExp = u.getVarExp() - 1;
                    Unit newUnit = new Unit(newCoe, newVarExp,
                            new HashMap<>(u.getSinMap()), new HashMap<>(u.getCosMap()));
                    result.add(newUnit);
                }
                //sin
                for (Poly p : u.getSinMap().keySet()) {
                    int k = u.getSinMap().get(p); //k代表sin的指数
                    if (k != 0) {
                        HashMap<Poly, Integer> newSinMap = new HashMap<>(u.getSinMap());
                        newSinMap.put(p, k - 1);
                        BigInteger newCoe = u.getCoe().multiply(BigInteger.valueOf(k));
                        //当前 sin 项的幂次减 1（因为求导后 sin(f(x))^k 变为 sin(f(x))^{k-1}）
                        if (newSinMap.get(p) == 0) { //如果幂次减到 0，则从 newSinMap 中移除该项
                            newSinMap.remove(p);
                        }
                        HashMap<Poly, Integer> newCosMap = new HashMap<>(u.getCosMap());
                        //sin(f(x)) 的导数是 cos(f(x))
                        newCosMap.put(p, newCosMap.getOrDefault(p, 0) + 1);
                        //如果 cosMap 中已经存在 cos(f(x))，则将其幂次加 1；否则，添加 cos(f(x)) 并设置幂次为 1
                        Unit newUnit = new Unit(newCoe, u.getVarExp(), newSinMap, newCosMap);
                        ArrayList newUnits = new ArrayList();
                        newUnits.add(newUnit);
                        Poly tempPoly1 = new Poly(newUnits);
                        Poly newPoly = tempPoly1.multPoly(p.toDx());
                        result.addAll(newPoly.getUnits());
                    }
                }
                // 处理cos项
                for (Poly p : u.getCosMap().keySet()) {
                    int m = u.getCosMap().get(p);
                    if (m != 0) {
                        HashMap<Poly, Integer> newCosMap = new HashMap<>(u.getCosMap());
                        newCosMap.put(p, m - 1);
                        BigInteger newCoe = u.getCoe().multiply(BigInteger.valueOf(-m));
                        if (newCosMap.get(p) == 0) {
                            newCosMap.remove(p);
                        }
                        HashMap<Poly, Integer> newSinMap = new HashMap<>(u.getSinMap());
                        newSinMap.put(p, newSinMap.getOrDefault(p, 0) + 1);
                        Unit newUnit = new Unit(newCoe, u.getVarExp(), newSinMap, newCosMap);
                        ArrayList newUnits = new ArrayList();
                        newUnits.add(newUnit);
                        Poly tempPoly1 = new Poly(newUnits);
                        Poly newPoly = tempPoly1.multPoly(p.toDx());
                        result.addAll(newPoly.getUnits());
                    }
                }
            }
        }
        return new Poly(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Unit unit : units) {
            sb.append(unit.toString());
        }
        String str = sb.toString();
        //str = str.replace("1*", ""); // 删除所有的 "1*"
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) { //消去第一个加号
            if (!(i == 0 && str.charAt(i) == '+')) {
                result.append(str.charAt(i));
            }
        }

        if (result.toString().equals("")) {
            return "0";
        } else {
            return result.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Poly poly = (Poly) o;
        return Objects.equals(this.toString(), poly.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }
}