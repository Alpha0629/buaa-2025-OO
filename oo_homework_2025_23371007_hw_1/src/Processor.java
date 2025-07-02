public class Processor {
    public String Process(String input) {
        String processed = input.replaceAll("[ \t]", "");//去除空白项
        return mergeSigns(processed);
    }

    private String mergeSigns(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '+' || c == '-') {
                int count = 0;
                while (i < input.length() && (input.charAt(i) == '+' || input.charAt(i) == '-')) {
                    if (input.charAt(i) == '-') {
                        count++;
                    }
                    i++;
                }

                if (count % 2 == 1) {
                    sb.append('-');
                } else {
                    if (count == 0) {
                        sb.append('+');
                    }
                }
            } else {
                sb.append(c);
                i++;
            }

        }   //处理连续正负号


        return sb.toString();
    }
}
