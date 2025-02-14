import java.util.ArrayList;

public class LaizE {

    //Runs code
    public static void execute(String code) {
        Lexer lexer = new Lexer(code);
        ArrayList<Token> tokens = lexer.convertToTokens();

        Parser parser = new Parser(tokens);
        parser.runCode();
    }

    //Error handling
    public static void Error(String message, String Class, int line) {
        System.out.println("Error: " + message + ": " + Class + "(" + line + ")");
        System.out.println();
        System.out.println(".");
        System.exit(1);
    }
}
