public class SubExpr extends Expr implements Factor {
    private int exp = 1;

    public void setExp(int exp) {
        this.exp = exp;
    }

    @Override
    public Poly toPoly() {
        Poly poly = super.toPoly().powPoly(this.exp);
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exp > 1) {
            sb.append("(" + super.toString() + ")" + "^" + exp);
        } else {
            sb.append("(" + super.toString() + ")");
        }
        return sb.toString();
    }

    @Override
    public void print() {
        System.out.println("SubExpr " + this);
    }
}
