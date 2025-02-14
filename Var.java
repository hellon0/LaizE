public class Var {
    public TokenType TokenType;
    public String identifier;
    public String data;
    public int line;

    public Var(TokenType TokenType, String identifier, String data, int line) {
        this.TokenType = TokenType;
        this.identifier = identifier;
        this.data = data;
        this.line = line;
    }
    
    public void setData(String data) {
        this.data = data;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
