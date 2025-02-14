import java.util.ArrayList;
import java.util.Arrays;


//Missing advanced string comparison (Connected strings using '"string" + "string"')
public class Condition {

    public static int position = 0;
     

    public static boolean conditional(ArrayList<Token> statement) {
        boolean result = true;
        int symbolPosition = 0;
        ArrayList<ArrayList<Token>> brokenUpStatement = new ArrayList<>();
        ArrayList<Token> condition = new ArrayList<>();
        ArrayList<Token> finalStatement = new ArrayList<>();
        TokenType[] symbols = {TokenType.EQUALS, TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS, TokenType.GREATER, TokenType.GREATER_EQUALS, TokenType.LESS, TokenType.LESS_EQUALS};
        TokenType[] determiners = {TokenType.OR, TokenType.AND};


        while (position <= statement.size()) {
            condition = new ArrayList<Token>();
            if (position < statement.size() && Arrays.asList(determiners).contains(statement.get(position).TokenType)) {
                symbolPosition = position;
                for (int i = 0; i < symbolPosition; i++) {
                    condition.add(statement.get(i));
                }
                brokenUpStatement.add(condition);
                
                condition = new ArrayList<Token>();
                condition.add(statement.get(symbolPosition));

                brokenUpStatement.add(condition);
                symbolPosition++;
            } else if (position == statement.size()) {
                for (int i = symbolPosition; i < statement.size(); i++) {
                    condition.add(statement.get(i));
                }
                brokenUpStatement.add(condition);

            }
            position++;
        }

        ArrayList<Token> value1 = new ArrayList<>();
        ArrayList<Token> value2 = new ArrayList<>();
        Token opperand;


        for (int i = 0; i < brokenUpStatement.size(); i++) {
            value1 = new ArrayList<Token>();
            value2 = new ArrayList<Token>();
            if (brokenUpStatement.get(i).get(0).TokenType != TokenType.BOOLEAN) {
                position = 0;
                while (!Arrays.asList(symbols).contains(brokenUpStatement.get(i).get(position).TokenType)) {
                    value1.add(brokenUpStatement.get(i).get(position));
                    position++;
                }
                opperand = brokenUpStatement.get(i).get(position);
                position++;
                
                for (int j = position; j < brokenUpStatement.get(i).size(); j++) {
                    value2.add(brokenUpStatement.get(i).get(j));
                    position++;
                }

                finalStatement.add(compareValues(value1, value2, opperand));
                if (i + 1 < brokenUpStatement.size()) {
                    i++;
                    finalStatement.add(brokenUpStatement.get(i).get(0));
                }
            } else {
                finalStatement.add(brokenUpStatement.get(i).get(0));
                if (i + 1 < brokenUpStatement.size()) {
                    i++;
                    finalStatement.add(brokenUpStatement.get(i).get(0));
                }
            }
        }

        position = 0;
        String dataHolder;

        while (true) {
            if (finalStatement.size() == 1) {
                result = Boolean.parseBoolean(finalStatement.get(0).data);
                break;
            }

            if (finalStatement.get(position+1).TokenType == TokenType.OR) {
                dataHolder = String.valueOf(Boolean.parseBoolean(finalStatement.get(position).data) || Boolean.parseBoolean(finalStatement.get(position+2).data));
                for (int i = 0; i < 3; i++) {finalStatement.remove(0);}
                finalStatement.add(0, new Token(TokenType.BOOLEAN, dataHolder, finalStatement.get(0).line));
            } else if (finalStatement.get(position+1).TokenType == TokenType.AND) {
                dataHolder = String.valueOf(Boolean.parseBoolean(finalStatement.get(position).data) && Boolean.parseBoolean(finalStatement.get(position+2).data));
                for (int i = 0; i < 3; i++) {finalStatement.remove(0);}

                finalStatement.add(0, new Token(TokenType.BOOLEAN, dataHolder, statement.get(0).line));
            }
        }

        return result;
    }


    public static Token compareValues(ArrayList<Token> value1, ArrayList<Token> value2, Token opperand) {
        boolean result = false;
        boolean isInt = true;
        double x;
        double y;


        if ((value1.get(0).TokenType == TokenType.INT || value1.get(0).TokenType == TokenType.DEC) 
        && value2.get(0).TokenType == TokenType.INT || value2.get(0).TokenType == TokenType.DEC) {
            for (int i = 0; i < value1.size(); i++) {
                if (value1.get(i).TokenType == TokenType.DEC) {
                    isInt = false;
                    break;
                }
            }
            if (isInt) {
                x = Double.parseDouble(opperationInt(value1).data);
            } else {
                x = Double.parseDouble(opperationDec(value1).data);
            }
        
            for (int i = 0; i < value2.size(); i++) {
                if (value2.get(i).TokenType == TokenType.DEC) {
                    isInt = false;
                    break;
                }
            }
            if (isInt) {
                y = Double.parseDouble(opperationDec(value2).data);
            } else {
                y = Double.parseDouble(opperationDec(value1).data);
            }

            switch (opperand.TokenType) {
                case TokenType.EQUALS_EQUALS: result = x == y; break;
                case TokenType.BANG_EQUALS: result = x != y; break;
                case TokenType.GREATER: result = x > y; break;
                case TokenType.GREATER_EQUALS: result = x >= y; break;
                case TokenType.LESS: result = x < y; break;
                case TokenType.LESS_EQUALS: result = x <= y; break;

                default:
                    LaizE.Error("Invalid Opperand " + opperand.TokenType, "Condition", opperand.line);
                    break;
            }
        } else if (value1.get(0).TokenType == value2.get(0).TokenType && value1.get(0).TokenType == TokenType.STRING) {

            
            if (opperand.TokenType == TokenType.EQUALS_EQUALS) {
                result = value1.get(0).data.equals(value2.get(0).data);
            } else if (opperand.TokenType == TokenType.BANG_EQUALS) {
                result = !value1.get(0).data.equals(value2.get(0).data);
            } else {
                LaizE.Error("Cannot compare token types " + value1.get(0).TokenType + " and " + value2.get(0).TokenType + " using opperand " + opperand.TokenType, "Condition", opperand.line);
            }

        }
        


        return new Token(TokenType.BOOLEAN, String.valueOf(result), value1.get(0).line);
    }

    public static Token opperationDec(ArrayList<Token> arr) {
        ArrayList<Token> problem = new ArrayList<>();
        int arrPosition = 0;
        while (arrPosition < arr.size()) {
            if (arr.get(arrPosition).TokenType == TokenType.IDENTIFIER) {
                for (int i = 0; i < Parser.varStorage.size(); i++) {
                    if (Parser.varStorage.get(i).identifier.equals(arr.get(arrPosition).data)) {
                        problem.add(new Token(Parser.varStorage.get(i).TokenType, Parser.varStorage.get(i).data, Parser.varStorage.get(i).line));
                    }
                }
            } else {
                problem.add(arr.get(arrPosition));
            }
            
            arrPosition++;
        }
        
        return new Token(TokenType.DEC, String.valueOf(math.completeProblem(problem)), arr.get(0).line);

    }

    public static Token opperationInt(ArrayList<Token> arr) {
        ArrayList<Token> problem = new ArrayList<>();
        int arrPosition = 0;
        while (arrPosition < arr.size()) {
            if (arr.get(arrPosition).TokenType == TokenType.IDENTIFIER) {
                for (int i = 0; i < Parser.varStorage.size(); i++) {
                    if (Parser.varStorage.get(i).identifier.equals(arr.get(arrPosition).data)) {
                        problem.add(new Token(Parser.varStorage.get(i).TokenType, Parser.varStorage.get(i).data, Parser.varStorage.get(i).line));
                    }
                }
            } else {
                problem.add(arr.get(arrPosition));
            }
            
            arrPosition++;
        }

        return new Token(TokenType.DEC, String.valueOf((int) math.completeProblem(problem)), arr.get(0).line);

    }
}
