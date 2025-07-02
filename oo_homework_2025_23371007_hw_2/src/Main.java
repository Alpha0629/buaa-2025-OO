import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        scanner.nextLine(); // 消耗掉换行符
        if (n == 0) {
            String input = scanner.nextLine();
            Processor processor = new Processor();
            String processed = processor.Process(input);
            Lexer lexer = new Lexer(processed);
            Parser parser = new Parser(lexer);
            Expr expr = parser.parseExpr();
            Poly poly = expr.toPoly();
            System.out.println(poly.toString());
        } else if (n == 1) {
            String[] input = new String[4];
            for (int i = 0; i < 4; i++) {
                Processor processor = new Processor();
                input[i] = processor.Process(scanner.nextLine());
            }
            Solver solver = new Solver();
            String outCome = solver.recursion(input);
            Lexer lexer = new Lexer(outCome);
            Parser parser = new Parser(lexer);
            Expr expr = parser.parseExpr();
            Poly poly = expr.toPoly();
            System.out.println(poly.toString());
        }
    }
}
