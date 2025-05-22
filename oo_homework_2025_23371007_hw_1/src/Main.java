import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Processor processor = new Processor();
        String processed = processor.Process(input);
        Lexer lexer = new Lexer(processed);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        System.out.println(poly.toString());
    }
}
