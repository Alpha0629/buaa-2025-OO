import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

public class Poly {
    private ArrayList<Mono> monos = new ArrayList<>();

    public Poly(ArrayList<Mono> monos) {
        this.monos = monos;
    }

    public ArrayList<Mono> getMonos() {
        return this.monos;
    }

    public Poly addPoly(Poly other) {
        ArrayList<Mono> result = new ArrayList<>();
        for (Mono mono : monos) {
            result.add(new Mono(mono.getCoe(), mono.getExp()));
        }

        for (Mono mono : other.getMonos()) {
            boolean found = false;
            for (Mono resultMono : result) {
                if (resultMono.getExp() == mono.getExp()) {
                    // 如果有，累加系数
                    resultMono.setCoe(resultMono.getCoe().add(mono.getCoe()));
                    found = true;
                }
            }

            // 如果没有找到相同指数的项，直接添加到结果中
            if (!found) {
                result.add(new Mono(mono.getCoe(), mono.getExp()));
            }
        }

        Poly poly = new Poly(result);
        return poly;

    }


    public Poly multPoly(Poly other) {
        Poly poly = new Poly(new ArrayList<Mono>());
        for (Mono mono : monos) {
            Poly tempPoly = new Poly(new ArrayList<Mono>());
            for (Mono mono2 : other.getMonos()) {
                BigInteger newCoe = mono.getCoe().multiply(mono2.getCoe());
                int newExp = mono.getExp() + mono2.getExp();
                Mono newMono = new Mono(newCoe, newExp);
                tempPoly.getMonos().add(newMono);
            }
            poly = poly.addPoly(tempPoly);
        }
        return poly;
    }

    public Poly powPoly(int pow) {
        if (pow == 0) {
            Poly poly = new Poly(new ArrayList<Mono>());
            Mono mono = new Mono(BigInteger.valueOf(1), 0);
            poly.getMonos().add(mono);
            return poly;
        } else {
            Poly poly = new Poly(new ArrayList<Mono>());
            Mono mono = new Mono(BigInteger.valueOf(1), 0);
            poly.getMonos().add(mono);
            for (int i = 0; i < pow; i++) {
                poly = poly.multPoly(this);
            }
            return poly;
        }

    }

    public void negate() {
        for (Mono mono : monos) {
            mono.setCoe(mono.getCoe().negate());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Mono mono : monos) {
            sb.append(mono.toString());
        }
        String str = sb.toString();
        str = str.replace("1*", ""); // 删除所有的 "1*"
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
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
}
