import java.util.ArrayList;

public class math {

    public static double completeProblem(ArrayList<Token> problem) {
        ArrayList<ArrayList<Token>> equations = new ArrayList<>();
        //ArrayList<Token> eq = new ArrayList<>();
        ArrayList<Token> toReplace = new ArrayList<>();

        
        equations.add(problem);


        for (int i = 0; i < equations.get(0).size(); i++) {
            if (equations.get(0).get(i).TokenType == TokenType.OPEN_PAREN) {
                group(equations, equations.get(0), i);
            }
        }

        
        int equationsLastIndex = equations.size()-1;
        for (int i = equationsLastIndex; i > 0; i -= 1) {
            //Fills "toReplace" ArrayList with the Tokens in the last ArrayList of Equations
            for (int j = 0; j < equations.get(i).size(); j++) {
                toReplace.add(equations.get(i).get(j));
            }
            //solves the equation in the last arraylist in equations
            performOpperation(equations.get(i));

            //removes parenthesis from the solved equation
            
            for (int j = 0; j < equations.get(i).size(); j++) {
                if (equations.get(i).get(j).TokenType == TokenType.CLOSED_PAREN ||
                equations.get(i).get(j).TokenType == TokenType.OPEN_PAREN) {
                    
                    equations.get(i).remove(j);
                    
                }
            } 

            //replaces any ArrayLists containing "toReplace" with the solved equation

            
            for (int j = 0; j < equations.size(); j++) {
                replace(equations.get(j), toReplace, equations.get(i).get(0));
            }
            int toReplaceSize = toReplace.size();
            for (int j = 0; j < toReplaceSize; j++) {toReplace.remove(0);}



        }

        performOpperation(equations.get(0));

        return toDouble(equations.get(0).get(0).data);
    }

    //meow
    public static void clearArray(ArrayList<Token> array) {
        
        for (int i = 0; i < array.size(); i++){
            array.remove(0);
        }
    }

    public static void group(ArrayList<ArrayList<Token>> equations, ArrayList<Token> problem, int start) {
        ArrayList<Token> eq = new ArrayList<>();
        int parenFoundAt = 0;
        int parenBuffer = 0;

        
        while (problem.get(start).TokenType != TokenType.CLOSED_PAREN || parenBuffer > -1) {
            if (start + 1 < problem.size() && problem.get(start+1).TokenType == TokenType.OPEN_PAREN) {
                parenBuffer++;
                parenFoundAt = start+1;
            }
            eq.add(problem.get(start));
            start++;
            if (problem.get(start).TokenType == TokenType.CLOSED_PAREN) {
                parenBuffer--;
            }
        }

        if (problem.get(start).TokenType == TokenType.CLOSED_PAREN) {
            eq.add(problem.get(start));
        }

        equations.add(eq);

        if(parenFoundAt > 0) {
            group(equations, problem, parenFoundAt);
        }
    }

    public static void performOpperation(ArrayList<Token> problem) {
        double result = 0;

        
        if (problem.size() == 1) {
            return;
        }
        for (int i = 0; i < problem.size(); i++) {
            Token c = problem.get(i);
            
            switch (c.TokenType) {
                case TokenType.STAR:
                    result = toDouble(problem.get(i-1).data) * toDouble(problem.get(i+1).data);
                    for (int j = 0; j < 3; j++) {problem.remove(i-1);}
                    problem.add(i-1, new Token(TokenType.DEC, String.valueOf(result), c.line));
                    
                    break;
                case TokenType.SLASH:
                    result = toDouble(problem.get(i-1).data) / toDouble(problem.get(i+1).data);
                    for (int j = 0; j < 3; j++) {problem.remove(i-1);}
                    problem.add(i-1, new Token(TokenType.DEC, String.valueOf(result), c.line));
                    break;
                case TokenType.PLUS: break;
                case TokenType.MINUS: break;
                case TokenType.DEC: break;
                case TokenType.INT: break;
                case TokenType.OPEN_PAREN: break;
                case TokenType.CLOSED_PAREN: break;
                case TokenType.STRING: break;
                

                default:
                    LaizE.Error("Unrecognized Opperation " + c.TokenType, "Math", c.line);
            }
        }
        for (int i = 0; i < problem.size(); i++) {
            Token c = problem.get(i);


            switch (c.TokenType) {
                case TokenType.PLUS:
                    result = toDouble(problem.get(i-1).data) + toDouble(problem.get(i+1).data);
                    for (int j = 0; j < 3; j++) {problem.remove(i-1);}
                    problem.add(i-1, new Token(TokenType.DEC, String.valueOf(result), c.line));
                    i -= 1;
                    break;
                case TokenType.MINUS:
                    result = toDouble(problem.get(i-1).data) - toDouble(problem.get(i+1).data);
                    for (int j = 0; j < 3; j++) {problem.remove(i-1);}
                    problem.add(i-1, new Token(TokenType.DEC, String.valueOf(result), c.line));
                    break;
                case TokenType.STAR: break;
                case TokenType.SLASH: break;
                case TokenType.DEC: break;
                case TokenType.INT: break;
                case TokenType.OPEN_PAREN: break;
                case TokenType.CLOSED_PAREN: break;
                case TokenType.STRING: break;

                default:
                    LaizE.Error("Unrecognized Opperation", "Math", c.line);
            }
        }

    }

    public static double toDouble(String num) {
        return Double.parseDouble(num);
    }


    public static void replace(ArrayList<Token> list, ArrayList<Token> toReplace, Token replacement) {
        int matches = 0;
        int toReplacePosition = 0;
        boolean contains = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).data == null && toReplace.get(matches).data == null && list.get(i).TokenType == toReplace.get(matches).TokenType || 
            list.get(i).data != null && toReplace.get(matches).data != null && toDouble(list.get(i).data) == toDouble(toReplace.get(matches).data)
            && toReplace.get(matches).TokenType == list.get(i).TokenType) {

                toReplacePosition = i;
                for (int j = i; matches < toReplace.size(); j++) {


                    if (j >= list.size()) {
                        matches = 0;
                        contains = false;
                        break;
                    }

                    if (list.get(j).data == null && toReplace.get(matches).data == null && list.get(j).TokenType == toReplace.get(matches).TokenType || 
                    list.get(j).data != null && toReplace.get(matches).data != null && list.get(j).data.equals(toReplace.get(matches).data)
                    && toReplace.get(matches).TokenType == list.get(j).TokenType ) {
                        matches++;
                        contains = true;
                        continue;
                    }
                    
                    matches = 0;
                    contains = false;
                    break;
                    
                }
            }
            if (contains) {
                for (int j = 0; j < toReplace.size(); j++) {list.remove(toReplacePosition);}
                list.add(toReplacePosition, replacement);
                break;
            }
        }
    }
}
 