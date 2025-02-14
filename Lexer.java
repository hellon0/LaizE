import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Scanner;
import java.util.Map;

public class Lexer {

    private String code;
    private ArrayList<Token> tokenList;

    private int position = 0;
    private int positionSave;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    //Scans text and converts it into an arraylist of tokens
    public ArrayList<Token> convertToTokens() {
        
        while (!EOF()) {

            //Scan tokens
            char c = code.charAt(position);
            switch (c) {

                case('{'): addToken(TokenType.OPEN_CURLY); break;
                case('}'): addToken(TokenType.CLOSED_CURLY); break;
                case('('): addToken(TokenType.OPEN_PAREN); break;
                case(')'): addToken(TokenType.CLOSED_PAREN); break;
                case('['): addToken(TokenType.OPEN_SQUARE); break;
                case(']'): addToken(TokenType.CLOSED_SQUARE); break;
                case(':'): addToken(TokenType.COLON); break;
                case(';'): addToken(TokenType.SEMICOLON); break;
                case('.'): addToken(TokenType.DOT); break;
                case('"'): addToken(TokenType.QUOTE); position++; consumeString(); break;
                case('+'): addToken(TokenType.PLUS); break;
                case('-'): addToken(TokenType.MINUS); break;
                case('*'): addToken(TokenType.STAR); break;
                case(','): addToken(TokenType.COMMA); break;
            
                case('='):
                    if (peek() == '=') {
                        addToken(TokenType.EQUALS_EQUALS);
                        position++;
                        break;
                    }
                    addToken(TokenType.EQUALS);
                    break;
                case('>'): 
                    if (peek() == '=') {
                        addToken(TokenType.GREATER_EQUALS);
                        position++;
                        break;
                    }
                    addToken(TokenType.GREATER);
                    break;
                case('<'):
                    if (peek() == '=') {
                        addToken(TokenType.LESS_EQUALS);
                        position++;
                        break;
                    }
                    addToken(TokenType.LESS);
                    break;
                case('!'):
                    if (peek() == '=') {
                        addToken(TokenType.BANG_EQUALS);
                        position++;
                        break;
                    }
                    addToken(TokenType.BANG);
                    break;
                case('/'): 
                    //skips the remainder of a line if '//' is found, allowing comments
                    if (peek() == '/') {
                        while (code.charAt(position) != '\n') {
                            position++;
                        }
                        break;
                    } 
                    addToken(TokenType.SLASH);
                    break;

                //Increases line count at every new line
                case('\n'): line++; break;

                //Skips over empty space
                case(' '): break;
                case('\t'): break;
                case('\r'): break;

                default:

                    //If part of the string isnt a single character token, checks to see if it's a variable/function name, number, or multicharacter string
                    positionSave = position;
                    String text = consume();
                    TokenType type = keywords.get(text);
                    if (type == null) {
                        //
                        if (scanForNonDigit(text)) {
                            addToken(TokenType.IDENTIFIER, text);
                        } else {
                            position = positionSave;
                            consumeNum();
                        }
                    } else {
                        addToken(type, text);
                    }
                    
                    if (code.charAt(position) == '\n') {line++;}
                    
            }
                
            advance();
        }

        addToken(TokenType.EOF);
        
        return tokenList;
    }

    //Checks if 'position' has read the entire file
    public boolean EOF() {
        return position >= code.length();
    }

    //Moves the pointer
    public void advance() {
        position++;
    }

    //checks the next character in the code
    public char peek() {
        if(position+1 < code.length()) {
            return code.charAt(position+1);
        }
        
        return ' ';
    }

    //Adds a token containing specific data
    public void addToken(TokenType TokenType, String data) {
        tokenList.add(new Token(TokenType, data, line));
    }
    
    //Adds a token containing no data
    public void addToken(TokenType TokenType) {
        tokenList.add(new Token(TokenType, null, line));
    }


    //Scans a String to see if the string has a non-digit
    public boolean scanForNonDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isDigit(s.charAt(i)) && s.charAt(i) != '.') {return true;}
        }
        return false;
    }

    //Checks if a character is a number
    public boolean isDigit(char c) {
        return c <= '9' && c >= '0' && c != ' ';
    }

    //Checks if a character is a letter, space, or underscore
    public boolean isAlpha(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_')) && c != ' ';
    }

    //checks if a character is alphanumeric
    public boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }


    //returns a string from the starting position to the next non-alphanumeric character
    public String consume() {
        String result = "";

        while (!EOF() && isAlphaNumeric(code.charAt(position)) && code.charAt(position) != '\n') {
            result += code.charAt(position);
            position++;
        }
        position--;
        return result;
    }

    //adds a token containing the next portion of numeric characters in the code
    public void consumeNum(){
        String digit = "";
        while (!EOF() && isDigit(code.charAt(position))) {
            digit += code.charAt(position);
            advance();
        }

        //Checks if number is a decimal
        if (code.charAt(position) == '.') {
            digit += code.charAt(position);
            advance();
            while (!EOF() && isDigit(code.charAt(position))) {
                digit += code.charAt(position);
                advance();
            }
            
            addToken(TokenType.DEC, digit);
            return;
        }
        position--;
        addToken(TokenType.INT, digit);

    }

    //Adds a String token containing the next set of alphanumeric characters as data
    public void consumeString() {
        String data = "";
        while (!EOF() && code.charAt(position) != '"') {
            if (code.charAt(position) == '\n'){line++;}
            data += code.charAt(position);
            position++;
        }
        if (EOF()) {
            LaizE.Error("End of String Not Found", "Lexer", line);
        }

        addToken(TokenType.STRING, data);
        addToken(TokenType.QUOTE);
    }


    //Constructor
    public Lexer(String code) {
        this.code = code;
        tokenList = new ArrayList<Token>();
    }

    //Defines the hashmap of keywords for multicharacter Tokens
    static {
        keywords = new HashMap<>();
        keywords.put("var", TokenType.VAR);
        
        keywords.put("str", TokenType.RETURN_STR);
        keywords.put("int", TokenType.RETURN_INT);
        keywords.put("dec", TokenType.RETURN_DEC);
        keywords.put("bool", TokenType.RETURN_BOOL);
         
        keywords.put("true", TokenType.BOOLEAN);
        keywords.put("false", TokenType.BOOLEAN);
        keywords.put("prt", TokenType.PRINT);
        keywords.put("input", TokenType.INPUT);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("func", TokenType.FUNCTION);
        keywords.put("return", TokenType.RETURN);
        keywords.put("na", TokenType.NA);
        keywords.put("for", TokenType.FOR);
        keywords.put("OR", TokenType.OR);
        keywords.put("AND", TokenType.AND);
        keywords.put("elsif", TokenType.ELSE_IF);
    }
    
}
