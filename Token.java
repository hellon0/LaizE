public class Token {

    public TokenType TokenType;
    public String data;
    public int line;

    public Token(TokenType TokenType, String data, int line) {
        this.TokenType = TokenType;
        this.data = data;
        this.line = line;
    }
}
