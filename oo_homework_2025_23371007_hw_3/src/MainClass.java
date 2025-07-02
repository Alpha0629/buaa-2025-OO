import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt(); //自定义普通函数定义的个数
        scanner.nextLine(); // 消耗掉换行符
        String[] input = new String[2];
        String[] inputF = new String[3]; //递归函数f的部分
        String target = new String();
        Processor processor = new Processor();
        Solver solver = new Solver();
        String outCome = new String();

        if (n == 0) {
            int m0 = scanner.nextInt();
            scanner.nextLine();
            if (m0 == 0) {
                String input0 = scanner.nextLine();
                outCome = processor.Process(input0);
            } else if (m0 == 1) {
                for (int i = 0; i < 3; i++) {
                    inputF[i] = processor.Process(scanner.nextLine());
                }
                target = processor.Process(scanner.nextLine());
                outCome = solver.recursion(input, inputF, target);
                //System.out.println(outCome);
            }
        } else if (n == 1) {
            input[0] = processor.Process(scanner.nextLine());
            input[1] = "null";
            int m1 = scanner.nextInt();
            scanner.nextLine();
            if (m1 == 0) { //无递归函数
                target = processor.Process(scanner.nextLine());//target
                outCome = solver.recursion(input, inputF, target);
                //System.out.println(outCome);
            } else if (m1 == 1) { //有递归函数
                for (int i = 0; i < 3; i++) {
                    inputF[i] = processor.Process(scanner.nextLine());
                }
                target = processor.Process(scanner.nextLine());//target
                outCome = solver.recursion(input, inputF, target);
                //System.out.println(outCome);
            }
        } else {
            input[0] = processor.Process(scanner.nextLine());
            input[1] = processor.Process(scanner.nextLine());
            int m2 = scanner.nextInt();
            scanner.nextLine();
            if (m2 == 0) { //无递归函数
                target = processor.Process(scanner.nextLine());//target
                outCome = solver.recursion(input, inputF, target);
                //System.out.println(outCome);
            } else if (m2 == 1) { //有递归函数
                for (int i = 0; i < 3; i++) {
                    inputF[i] = processor.Process(scanner.nextLine());
                }
                target = processor.Process(scanner.nextLine());//target
                outCome = solver.recursion(input, inputF, target);
                //System.out.println(outCome);
            }
        }
        output(outCome);
    }

    public static void output(String outCome) {
        Lexer lexer = new Lexer(outCome);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        System.out.println(poly.toString());
    }
}
