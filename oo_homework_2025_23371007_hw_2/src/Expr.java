import java.util.ArrayList;

public class Expr {
    private final ArrayList<Term> terms = new ArrayList<>();

    public void addTerm(Term term) {
        terms.add(term);
    }

    public Poly toPoly() {
        Poly poly = new Poly(new ArrayList<>());
        for (Term it : terms) {
            Poly temp = poly.addPoly(it.toPoly());
            poly = temp;
        }
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term term : terms) {
            sb.append(term.toString());
            sb.append("+");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public void print() {
        System.out.println("Expr " + this);
    }
}
