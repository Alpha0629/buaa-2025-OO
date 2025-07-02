import java.util.ArrayList;

public class Dx implements Factor {
    private Expr expr;

    public Dx(Expr expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("dx(");
        sb.append(expr.toString());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void print() {
        System.out.println("dx " + this);
    }

    @Override
    public Poly toPoly() {
        Poly poly = new Poly(new ArrayList<>());
        poly = poly.addPoly(expr.toPoly());
        poly = poly.toDx();
        return poly;
    }
}
