import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * AdvancedCalculator - CLI calculator with many features.
 * Save as AdvancedCalculator.java
 *
 * Features:
 * - Basic arithmetic: +, -, *, /
 * - Modulus, power, percentage
 * - Square root, factorial
 * - Log base 10, natural log
 * - Trig functions (sin, cos, tan) in degrees or radians
 * - Conversions: decimal <-> binary, decimal <-> hex
 * - History tracking and saving history to a file
 *
 * Java version: works with Java 8+ (uses core libs only)
 */
public class AdvancedCalculator {

    private static final int SCALE = 12; // decimal precision for BigDecimal operations
    private final List<String> history = new ArrayList<>();
    private final Scanner sc = new Scanner(System.in);
    private boolean trigInDegrees = true;

    public static void main(String[] args) {
        AdvancedCalculator calc = new AdvancedCalculator();
        calc.run();
    }

    private void run() {
        printWelcome();
        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": binaryOperation(); break;
                case "2": unaryOperation(); break;
                case "3": conversionsMenu(); break;
                case "4": toggleTrigMode(); break;
                case "5": showHistory(); break;
                case "6": saveHistoryToFile(); break;
                case "0": exitProgram(); return;
                default: System.out.println("Invalid choice — try again.");
            }
            System.out.println();
        }
    }

    private void printWelcome() {
        System.out.println("=== Advanced Calculator ===");
        System.out.println("Built-in operations: + - * / % pow sqrt factorial log ln sin cos tan");
        System.out.println("Conversions: decimal <-> binary, decimal <-> hex");
        System.out.println("History available and can be saved to a file.");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("Menu:");
        System.out.println("1) Binary operations (two operands)  e.g. add, sub, mul, div, pow, mod, percent");
        System.out.println("2) Unary operations (single operand) e.g. sqrt, factorial, log, ln, sin, cos, tan");
        System.out.println("3) Conversions (decimal <-> binary/hex)");
        System.out.println("4) Toggle trig mode (currently " + (trigInDegrees ? "Degrees" : "Radians") + ")");
        System.out.println("5) Show history");
        System.out.println("6) Save history to file");
        System.out.println("0) Exit");
        System.out.print("Choose option: ");
    }

    // Binary ops: take two numbers
    private void binaryOperation() {
        System.out.println("Binary operations: add, sub, mul, div, mod, pow, percent");
        System.out.print("Enter operation: ");
        String op = sc.nextLine().trim().toLowerCase();

        BigDecimal a = readBigDecimal("Enter first number: ");
        BigDecimal b = readBigDecimal("Enter second number: ");

        try {
            BigDecimal result;
            switch (op) {
                case "add":
                case "+":
                    result = a.add(b);
                    recordAndPrint(op, a, b, result);
                    break;
                case "sub":
                case "-":
                    result = a.subtract(b);
                    recordAndPrint(op, a, b, result);
                    break;
                case "mul":
                case "*":
                case "x":
                    result = a.multiply(b);
                    recordAndPrint(op, a, b, result);
                    break;
                case "div":
                case "/":
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        System.out.println("Error: Division by zero.");
                        return;
                    }
                    result = a.divide(b, SCALE, RoundingMode.HALF_UP);
                    recordAndPrint(op, a, b, result.stripTrailingZeros());
                    break;
                case "mod":
                case "%":
                    result = a.remainder(b);
                    recordAndPrint(op, a, b, result);
                    break;
                case "pow":
                case "^":
                    // power using double for exponent (supports fractional exponents)
                    double powResult = Math.pow(a.doubleValue(), b.doubleValue());
                    result = BigDecimal.valueOf(powResult);
                    recordAndPrint(op, a, b, result.stripTrailingZeros());
                    break;
                case "percent":
                    // interpret b as percentage of a (a * b / 100)
                    result = a.multiply(b).divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);
                    recordAndPrint(op, a, b, result.stripTrailingZeros());
                    break;
                default:
                    System.out.println("Unknown binary operation.");
            }
        } catch (ArithmeticException ex) {
            System.out.println("Math error: " + ex.getMessage());
        }
    }

    // Unary ops: take one number
    private void unaryOperation() {
        System.out.println("Unary operations: sqrt, factorial, log, ln, sin, cos, tan, abs");
        System.out.print("Enter operation: ");
        String op = sc.nextLine().trim().toLowerCase();

        switch (op) {
            case "sqrt":
                BigDecimal n = readBigDecimal("Enter number: ");
                if (n.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Error: square root of negative number.");
                    return;
                }
                BigDecimal sqrt = BigDecimal.valueOf(Math.sqrt(n.doubleValue()));
                recordAndPrint(op, n, sqrt.stripTrailingZeros());
                break;
            case "factorial":
                int val = readInt("Enter non-negative integer: ");
                if (val < 0) {
                    System.out.println("Error: factorial of negative number.");
                    return;
                }
                BigDecimal fact = BigDecimal.valueOf(factorial(val));
                recordAndPrint(op, BigDecimal.valueOf(val), fact);
                break;
            case "log":
                BigDecimal xLog = readBigDecimal("Enter number (>0): ");
                if (xLog.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Error: log undefined for <= 0.");
                    return;
                }
                BigDecimal log10 = BigDecimal.valueOf(Math.log10(xLog.doubleValue()));
                recordAndPrint(op, xLog, log10.stripTrailingZeros());
                break;
            case "ln":
                BigDecimal xLn = readBigDecimal("Enter number (>0): ");
                if (xLn.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Error: ln undefined for <= 0.");
                    return;
                }
                BigDecimal ln = BigDecimal.valueOf(Math.log(xLn.doubleValue()));
                recordAndPrint(op, xLn, ln.stripTrailingZeros());
                break;
            case "sin":
            case "cos":
            case "tan":
                BigDecimal angle = readBigDecimal("Enter angle: ");
                double angleRad = trigInDegrees ? Math.toRadians(angle.doubleValue()) : angle.doubleValue();
                double trigResult;
                if (op.equals("sin")) trigResult = Math.sin(angleRad);
                else if (op.equals("cos")) trigResult = Math.cos(angleRad);
                else trigResult = Math.tan(angleRad);
                BigDecimal trigBD = BigDecimal.valueOf(trigResult);
                recordAndPrint(op, angle, trigBD.stripTrailingZeros());
                break;
            case "abs":
                BigDecimal a = readBigDecimal("Enter number: ");
                BigDecimal abs = a.abs();
                recordAndPrint(op, a, abs);
                break;
            default:
                System.out.println("Unknown unary operation.");
        }
    }

    private void conversionsMenu() {
        System.out.println("Conversions:");
        System.out.println("1) Decimal to Binary");
        System.out.println("2) Binary to Decimal");
        System.out.println("3) Decimal to Hex");
        System.out.println("4) Hex to Decimal");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        try {
            switch (c) {
                case "1":
                    long dec = readLong("Enter integer (decimal): ");
                    String bin = Long.toBinaryString(dec);
                    recordAndPrintConversion("dec->bin", String.valueOf(dec), bin);
                    break;
                case "2":
                    System.out.print("Enter binary string: ");
                    String bStr = sc.nextLine().trim();
                    long decFromBin = Long.parseLong(bStr, 2);
                    recordAndPrintConversion("bin->dec", bStr, String.valueOf(decFromBin));
                    break;
                case "3":
                    long dec2 = readLong("Enter integer (decimal): ");
                    String hex = Long.toHexString(dec2).toUpperCase();
                    recordAndPrintConversion("dec->hex", String.valueOf(dec2), hex);
                    break;
                case "4":
                    System.out.print("Enter hex string: ");
                    String hx = sc.nextLine().trim();
                    long decFromHex = Long.parseLong(hx, 16);
                    recordAndPrintConversion("hex->dec", hx, String.valueOf(decFromHex));
                    break;
                default:
                    System.out.println("Invalid conversion choice.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number format: " + ex.getMessage());
        }
    }

    private void toggleTrigMode() {
        trigInDegrees = !trigInDegrees;
        System.out.println("Trig mode is now: " + (trigInDegrees ? "Degrees" : "Radians"));
    }

    private void showHistory() {
        if (history.isEmpty()) {
            System.out.println("History is empty.");
            return;
        }
        System.out.println("History:");
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%d: %s%n", i + 1, history.get(i));
        }
    }

    private void saveHistoryToFile() {
        if (history.isEmpty()) {
            System.out.println("Nothing to save — history is empty.");
            return;
        }
        System.out.print("Enter filename to save (e.g. history.txt): ");
        String filename = sc.nextLine().trim();
        try (FileWriter fw = new FileWriter(filename)) {
            for (String line : history) {
                fw.write(line + System.lineSeparator());
            }
            System.out.println("History saved to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private void exitProgram() {
        System.out.println("Exiting. Bye!");
        sc.close();
    }

    // UTIL: record and print for binary ops
    private void recordAndPrint(String op, BigDecimal a, BigDecimal b, BigDecimal result) {
        String entry = String.format("%s %s %s = %s", a.stripTrailingZeros().toPlainString(), op, b.stripTrailingZeros().toPlainString(), result.stripTrailingZeros().toPlainString());
        System.out.println(entry);
        history.add(entry);
    }

    // Overload for unary (one operand)
    private void recordAndPrint(String op, BigDecimal operand, BigDecimal result) {
        String entry = String.format("%s(%s) = %s", op, operand.stripTrailingZeros().toPlainString(), result.stripTrailingZeros().toPlainString());
        System.out.println(entry);
        history.add(entry);
    }

    private void recordAndPrintConversion(String op, String input, String output) {
        String entry = String.format("%s: %s -> %s", op, input, output);
        System.out.println(entry);
        history.add(entry);
    }

    // Helper readers
    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid decimal. Try again.");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    // Factorial (iterative, returns long - for n>20 will overflow long)
    private long factorial(int n) {
        if (n < 2) return 1L;
        long res = 1L;
        for (int i = 2; i <= n; i++) {
            res *= i;
            // if overflowed, keep going but caller should be aware
        }
        return res;
    }
}
