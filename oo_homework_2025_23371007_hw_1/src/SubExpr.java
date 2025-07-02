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
        return super.toString();
    }

    @Override
    public void print() {
        System.out.println("SubExpr " + this);
    }
}
