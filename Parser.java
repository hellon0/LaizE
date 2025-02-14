import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

// Working on : Commenting / more function implementation
// Details : When checking for identifiers, also check for functions and whether or not they're declared - currently in ForLoops

//Subtask : change 'positionSave' to a Stack to allow multiple position saves at once

public class Parser {
    private ArrayList<Token> tokens;
    private static Map<String, Integer> functions;
    public static ArrayList<Var> varStorage;
    private int position;
    private int positionSave;
    private boolean inFunc = false;
    private TokenType returnedType = TokenType.NA;
    private Stack<TokenType> returnTypes = new Stack<>();

    public void runCode() {
        while (!EOF()) {
            checkToken(tokens.get(position));
        }
    }

    //Checks the token at the current position unless it's the end of the code
    public void checkToken(Token token) {
        int parenBuffer = 0;

        if (token.TokenType != TokenType.EOF) {
            switch (token.TokenType) {
                case TokenType.VAR: storeVar(); break; 
                case TokenType.IDENTIFIER: if (isFunc()){callFunction();} else {updateVar();} break;
                case TokenType.PRINT: position++; printValue(); break;
                case TokenType.IF: runIf(); break; 

                case TokenType.SEMICOLON: break;
                case TokenType.CLOSED_CURLY: break;
                case TokenType.OPEN_CURLY: break;
                case TokenType.OPEN_PAREN: break;
                case TokenType.CLOSED_PAREN: break;

                case TokenType.FOR: forLoop(); break;
                case TokenType.WHILE: whileLoop(); break;
                case TokenType.FUNCTION: function(); break;
                case TokenType.RETURN: 
                    if (inFunc) {

                        Token returned = returnHandler(position);
                        while (tokens.get(positionSave).TokenType != TokenType.CLOSED_PAREN || parenBuffer > 0) {
                            if (tokens.get(positionSave).TokenType == TokenType.OPEN_PAREN) {
                                parenBuffer++;
                            }
                            if (tokens.get(positionSave+1).TokenType == TokenType.CLOSED_PAREN) {
                                parenBuffer -= 1;
                            }
                            tokens.remove(positionSave);
                            
                        }                        
                        
                        tokens.remove(positionSave);
                        tokens.add(positionSave, returned);
                        position = positionSave - 1;
                        inFunc = false;
                        break;
                    } else {
                        LaizE.Error("Unexpected return statement", "Parser.checkToken", token.line);
                    }
                default:
                    LaizE.Error("Unexpected Token: " + token.TokenType,  "Parser.checkToken", token.line);
        
            }
            
            position++;
        } else {
            System.exit(0);
        }
    }


    //Checks if it's the end of the code
    public boolean EOF() {
        return position >= tokens.size();
    } 

    //returns the tokens 'forward' indexes infront of the current token
    public Token peek(int forward) {
        if (position + forward >= tokens.size()) {
            return new Token(TokenType.EOF, null, tokens.get(tokens.size()-1).line);
        }
        return tokens.get(position+forward);
    }

    //Prints a value
    public void printValue() {
        String valueToPrint = "";
        boolean inQuotes = false;
        boolean varFound = false;

        //Checks for the open parenthesis
        if (tokens.get(position).TokenType == TokenType.OPEN_PAREN) {

            //Adds strings to 'valueToPrint' until it detects a closed paranthesis and a semicolon
            while(!endOfPrint(inQuotes)) {

                //Determines if the next part of the string is in quotes or not
                if (tokens.get(position).TokenType == TokenType.QUOTE && !inQuotes) {
                    inQuotes = true;
                    position++;
                    continue;
                } else if (tokens.get(position).TokenType == TokenType.QUOTE && inQuotes) {
                    inQuotes = false;
                    position++;
                    continue;
                }

                //Adds the string data to 'valueToPrint' if it's in quotes
                //If not in quotes 
                if (inQuotes) {
                    valueToPrint += tokens.get(position).data;
                } else if (tokens.get(position).TokenType == TokenType.PLUS){
                    position++;
                    if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                        for (int i = 0; i < varStorage.size(); i++) {
                            if (tokens.get(position).data.equals(varStorage.get(i).identifier)) {
                                varFound = true;
                                valueToPrint += varStorage.get(i).data;
                            }
                        }
                        
                        //Checks for function & whether or not the function is declared. if not a function, checks whether it's an undeclared function or variable
                        if (!varFound && isFunc()) {
                            if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                                LaizE.Error("Missing token '('", "Parser.opperationInt", tokens.get(position).line);
                            }
                            callFunction();
                        } else if (!varFound && !isFunc()) {
                            if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                                LaizE.Error("Undeclared function " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                            } else {
                                LaizE.Error("Undeclared variable " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                            }
                        }

                        varFound = false;

                    //Checks if theres a start of a string
                    } else if (tokens.get(position).TokenType == TokenType.QUOTE){
                        inQuotes = true;
                    
                    //Checks if there is a string
                    } else if (tokens.get(position).TokenType != TokenType.STRING) {
                        valueToPrint += tokens.get(position).data;
                    }
                
                //In case the first character is a variable or function
                } else if (tokens.get(position).TokenType == TokenType.IDENTIFIER && tokens.get(position-1).TokenType == TokenType.OPEN_PAREN) {
                    if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                        for (int i = 0; i < varStorage.size(); i++) {
                            if (tokens.get(position).data.equals(varStorage.get(i).identifier)) {
                                varFound = true;
                                valueToPrint += varStorage.get(i).data;
                            }
                        }

                        //Checks for function & whether or not the function is declared. if not a function, checks whether it's an undeclared function or variable
                        if (!varFound && isFunc()) {
                            if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                                LaizE.Error("Missing token '('", "Parser.opperationInt", tokens.get(position).line);
                            }
                            callFunction();
                        } else if (!varFound && !isFunc()) {
                            if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                                LaizE.Error("Undeclared function " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                            } else {
                                LaizE.Error("Undeclared variable " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                            }
                        }

                        varFound = false;

                    }
                } else if (tokens.get(position).TokenType != TokenType.QUOTE && tokens.get(position-1).TokenType != TokenType.QUOTE && tokens.get(position).data != null){
                    valueToPrint += tokens.get(position).data;
                } 
                position++;
            }
        } else {
            LaizE.Error("Missing Token '('", "Parser.printValue", tokens.get(position).line);
        }
        
        if (peek(1).TokenType != TokenType.SEMICOLON) {
            LaizE.Error("Missing Semicolon", "Parser", tokens.get(position-1).line);
        }
        System.out.println(valueToPrint);
    }

    //Checks if it's the end of the current print statement
    public boolean endOfPrint(boolean inQuotes) {
        if (EOF() || (!inQuotes && tokens.get(position).TokenType == TokenType.CLOSED_PAREN && peek(1).TokenType == TokenType.SEMICOLON)) {
            return true;
        } else if (!inQuotes && tokens.get(position).TokenType == TokenType.CLOSED_PAREN) {
            LaizE.Error("Semicolon not found", "Parser.endOfPrint", tokens.get(position).line);
        }
        return false;
    }

    //Checks if it's the end of the current String variable
    public boolean endOfString(boolean inQuotes) {
        return !inQuotes && tokens.get(position).TokenType == TokenType.SEMICOLON;
    }

    //Runs if statements
    public void runIf() {
        int bracketBuffer = 0;
        boolean executed = false;
        
        //Runs until no longer in an if or elsif statement
        while (tokens.get(position).TokenType == TokenType.ELSE_IF || tokens.get(position).TokenType == TokenType.IF) {
            while (position == 0 || tokens.get(position-1).TokenType != TokenType.OPEN_PAREN) {
                position++;
            }
            
             
            // meow
            //Checks if an if has already been executed and if its condition is true
            if (!executed && conditional(true)) {
                executed = true;
                while(tokens.get(position-1).TokenType != TokenType.CLOSED_PAREN) {
                    position++;
                }

                //Checks tokens until end of statement
                while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || bracketBuffer > 0) {
                    if (peek(1).TokenType == TokenType.OPEN_CURLY) {
                        bracketBuffer++;
                    } else if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                        bracketBuffer--;
                    }

                    if (tokens.get(position).TokenType == TokenType.CLOSED_CURLY && bracketBuffer <= 0) {
                        break;
                    }
                    checkToken(tokens.get(position));
                }

            //Skips current statement if condition is false or if a statement has already been executed
            } else {
                while(tokens.get(position).TokenType != TokenType.OPEN_CURLY) {
                    position++;
                }

                while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || bracketBuffer > 0) {
                    if (peek(1).TokenType == TokenType.OPEN_CURLY) {
                        bracketBuffer++;
                    } else if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                        bracketBuffer--;
                    }
                    position++;
                }
                position++;

                
            }
            //Moves to the next token if stuck on the last curly bracket
            if (tokens.get(position).TokenType == TokenType.CLOSED_CURLY) {position++;}

        }

        //If none of the conditions are true and there's an else statement, run the else
        if (!executed && tokens.get(position).TokenType == TokenType.ELSE) {
            while(tokens.get(position-1).TokenType != TokenType.OPEN_CURLY) {
                position++;
            }

            //Runs the tokens in the if statement
            while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || bracketBuffer > 0) {
                if (peek(1).TokenType == TokenType.OPEN_CURLY) {
                    bracketBuffer++;
                } else if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                    bracketBuffer--;
                }
                checkToken(tokens.get(position));
                position++;
            }
        
        //Otherwise, skip the else statement
        } else {
            while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || bracketBuffer > 0) {
                if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                    bracketBuffer--;
                }
                if (tokens.get(position).TokenType == TokenType.OPEN_CURLY) {
                    bracketBuffer++;
                }

                position++;
            }
        }
        

    }

    

    //Returns the result of an equation as a decimal 
    public double opperationDec() {
        ArrayList<Token> problem = new ArrayList<>();
        boolean varFound = false;

        while (tokens.get(position).TokenType != TokenType.SEMICOLON && (peek(1).TokenType != TokenType.QUOTE || tokens.get(position).TokenType != TokenType.PLUS) && tokens.get(position).TokenType != TokenType.CLOSED_PAREN) {
            if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                for (int i = 0; i < varStorage.size(); i++) {
                    if (varStorage.get(i).identifier.equals(tokens.get(position).data)) {
                        varFound = true;
                        problem.add(new Token(varStorage.get(i).TokenType, varStorage.get(i).data, varStorage.get(i).line));
                    }
                }

                //Checks for function & whether or not the function is declared. if not a function, checks whether it's an undeclared function or variable
                if (!varFound && isFunc()) {
                    if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                        LaizE.Error("Missing token '('", "Parser.opperationInt", tokens.get(position).line);
                    }
                    callFunction();
                } else if (!varFound && !isFunc()) {
                    if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                        LaizE.Error("Undeclared function " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                    } else {
                        LaizE.Error("Undeclared variable " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                    }
                }
                varFound = false;
            } else {
                problem.add(tokens.get(position));
            }
            position++;
        }
        
        return math.completeProblem(problem);

    }

    //Returns the result of an equation as an integer
    public int opperationInt() {
        ArrayList<Token> problem = new ArrayList<>();
        boolean varFound = false;

        while (tokens.get(position).TokenType != TokenType.SEMICOLON && (peek(1).TokenType != TokenType.QUOTE || tokens.get(position).TokenType != TokenType.PLUS) && tokens.get(position).TokenType != TokenType.CLOSED_PAREN) {
            
            //Scans for variable or function
            if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                for (int i = 0; i < varStorage.size(); i++) {
                    if (varStorage.get(i).identifier.equals(tokens.get(position).data)) {
                        varFound = true;
                        problem.add(new Token(varStorage.get(i).TokenType, varStorage.get(i).data, varStorage.get(i).line));
                    }
                }

                //Checks for function & whether or not the function is declared. if not a function, checks whether it's an undeclared function or variable
                if (!varFound && isFunc()) {
                    if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                        LaizE.Error("Missing token '('", "Parser.opperationInt", tokens.get(position).line);
                    }
                    callFunction();
                } else if (!varFound && !isFunc()) {
                    if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                        LaizE.Error("Undeclared function " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                    } else {
                        LaizE.Error("Undeclared variable " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                    }
                }

                varFound = false;

            } else {
                problem.add(tokens.get(position));
            }
            
            position++;
        }
        

        return (int) math.completeProblem(problem);
    }

    //Stores a function
    public void function() {
        String functionName = "";
        int curlyBuffer = 0;
        
        //Saves function position
        positionSave = position;
        position+=2;

        //Saves function name
        if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
            functionName = tokens.get(position).data;
        } else {
            LaizE.Error("Missing Identifier; Found " + tokens.get(position).TokenType, "Parser.function", tokens.get(position).line);
        }

        //Saves the functions name and position to a HashMap
        functions.put(functionName, positionSave);

        //Skips to the beginning of the parameters
        while (tokens.get(position).TokenType != TokenType.OPEN_CURLY) {
            position++;
        }

        while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || curlyBuffer > 0) {
            if (tokens.get(position).TokenType == TokenType.OPEN_CURLY) {
                curlyBuffer++;
            }
            if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                curlyBuffer--;
            }
            position++;
        }
    }

    //Checks if is a function
    public boolean isFunc() {
        return functions.get(tokens.get(position).data) != null;
    }

    //Calls a function
    public void callFunction() {
        inFunc = true;
        TokenType returnType = TokenType.NA;
        String functionName = "";
        ArrayList<Token> parameters = new ArrayList<>();
        ArrayList<Var> tempVar = new ArrayList<>();
        int parenBuffer = 0;
        int paramCount = 0;
        

        //Saves position to return to
        positionSave = position;

        //grabs the called function's name
        if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
            functionName = tokens.get(position).data;
        } else {
            LaizE.Error("Processing Error", "Parser.callFunction", tokens.get(position).line);
        }

        //moves pointer to the beginning of the parameters
        while (tokens.get(position-1).TokenType != TokenType.OPEN_PAREN) {
            position++;
        }

        //Adds the values given to an ArrayList called 'parameters'
        while (tokens.get(position).TokenType != TokenType.CLOSED_PAREN || parenBuffer > 0) {
            if (tokens.get(position).TokenType == TokenType.OPEN_PAREN) {
                parenBuffer++;
            }
            if (peek(1).TokenType == TokenType.CLOSED_PAREN) {
                parenBuffer -= 1;
            }
            
            if (tokens.get(position).TokenType != TokenType.COMMA && tokens.get(position).TokenType != TokenType.OPEN_PAREN && tokens.get(position).TokenType != TokenType.CLOSED_PAREN) {
                parameters.add(tokens.get(position));
            }
            position++;
        } 

        //Sets position to the corresponding function's position
        if (isFunc()) {
            position = functions.get(functionName);
        } else {
            LaizE.Error("Function " + functionName + " undeclared", "Parser.callFunction", tokens.get(position).line);
        }

        //Sets return type
        position++;
        returnType = tokens.get(position).TokenType;
        returnTypes.push(returnType);

        //Skips to the beginning of the parameters
        while (tokens.get(position-1).TokenType != TokenType.OPEN_PAREN) {
            position++;
        }
        
        //Adds parameters to a list of temporary variables
        while (tokens.get(position).TokenType != TokenType.CLOSED_PAREN) {
            if (paramCount >= parameters.size()) {
                LaizE.Error("Woah", "Parser.callFunction", tokens.get(position).line);
            }
            if (tokens.get(position).TokenType != TokenType.COMMA) {
                tempVar.add(new Var(parameters.get(paramCount).TokenType, tokens.get(position).data, parameters.get(paramCount).data, parameters.get(paramCount).line));
                paramCount++;
            }
            position++;
        }

        //Runs function
        while (tokens.get(position).TokenType != TokenType.OPEN_CURLY) {
            position++;
        }
        while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY && inFunc) {
            checkToken(tokens.get(position));
        }

        
        //Return error handling
        if (returnType == TokenType.NA && returnedType != TokenType.NA) {
            LaizE.Error("Unexpected Return statement", "Parser.callFunction", tokens.get(position).line);
        } else if (returnedType != returnType){
            LaizE.Error("Invalid return type " + returnedType, "Parser.callFunction", tokens.get(position).line);
        } else if (returnedType == returnType) {
            returnedType = TokenType.NA;
        }


        //clears tempVar 
        if (tempVar.size() > 0){
            for (int i = 0; i < parameters.size(); i++) {
                tempVar.remove(tempVar.size()-1);
            }
        }

        //Returns position to where it was before
        position = positionSave;
        if (tokens.get(position).TokenType == TokenType.IDENTIFIER && peek(1).TokenType == TokenType.OPEN_PAREN) {
            parenBuffer = 0;
            while (tokens.get(position).TokenType != TokenType.CLOSED_PAREN || parenBuffer > 0) {
                if (tokens.get(position).TokenType == TokenType.OPEN_PAREN) {parenBuffer++;}
                if (peek(1).TokenType == TokenType.CLOSED_PAREN) {parenBuffer -= 1;}
                position++;
            }
            position++;
        }
        inFunc = false;
    }

    //Processes return statements
    public Token returnHandler(int returnPosition) {
        int positionSave = returnPosition;
        TokenType[] symbols = {TokenType.EQUALS, TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS, TokenType.GREATER, TokenType.GREATER_EQUALS, TokenType.LESS, TokenType.LESS_EQUALS};
        Var tempVarStorage = null;
        String toReturn = null;
        TokenType returnType = null;
        TokenType returnTokenType = TokenType.NA;

        //Checks if the return is a boolean
        while (tokens.get(position).TokenType != TokenType.SEMICOLON) {
            if (Arrays.asList(symbols).contains(tokens.get(position).TokenType)) {
                position = positionSave + 1;
                
                return new Token(TokenType.BOOLEAN, String.valueOf(conditional(false)), -1);
            }
            position++;
        }
        position = positionSave + 1;

        //Runs same process as updateVar to determine what to return
        

        boolean checking = true;
        boolean varFound = false;
        while (checking) {
            checking = false;
            Token type = tokens.get(position);

            switch(type.TokenType) {
                case TokenType.IDENTIFIER:
                    for (int i = 0; i < varStorage.size(); i++) {
                        if (type.data.equals(varStorage.get(i).identifier)) {
                            tempVarStorage = varStorage.get(i);
                        }
                    }
                    //Checks for function & whether or not the function is declared. if not a function, checks whether it's an undeclared function or variable
                    if (!varFound && isFunc()) {
                        if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                            LaizE.Error("Missing token '('", "Parser.opperationInt", tokens.get(position).line);
                        }
                        callFunction();
                        checking = true;
                        break;
                    } else if (!varFound && !isFunc()) {
                        if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                            LaizE.Error("Undeclared function " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                        } else {
                            LaizE.Error("Undeclared variable " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                        }
                    }

                    varFound = false;
                    

                    switch (tempVarStorage.TokenType) {
                        case TokenType.STRING: toReturn = addString(false); returnType = tempVarStorage.TokenType; break;
                        case TokenType.INT:
                            returnType = tempVarStorage.TokenType;
                            if (isInt(position)) {
                                toReturn = String.valueOf(opperationInt());
                                break;
                            }
                            toReturn = String.valueOf(opperationDec());
                            break;
                        case TokenType.DEC:
                            returnType = tempVarStorage.TokenType;
                            if (isInt(position)) {
                                toReturn = String.valueOf(opperationInt());
                                break;
                            }
                            toReturn = String.valueOf(opperationDec());
                            break;
                        default:
                            LaizE.Error("Unregistered token type for variable " + tempVarStorage.identifier, "Parser.returnHandler", type.line);
                    }
                    break;
                case TokenType.STRING:
                    toReturn = addString(false);
                    returnType = TokenType.STRING;
                    break;
                case TokenType.INT:

                    
                    if (isInt(position)) {
                        toReturn = String.valueOf(opperationInt());
                        returnType = TokenType.INT;
                        break;
                    }
                    toReturn = String.valueOf(opperationDec());
                    returnType = TokenType.DEC;
                    break;
                case TokenType.DEC:

                    if (isInt(position)) {
                        toReturn = String.valueOf(opperationInt());
                        returnType = TokenType.INT;
                        break;
                    }
                    toReturn = String.valueOf(opperationDec());
                    returnType = TokenType.DEC;
                    break;
                case TokenType.BOOLEAN:
                    toReturn = type.data;
                    returnType = TokenType.BOOLEAN;
                    break;
                case TokenType.OPEN_PAREN: checking = true; position++; break;
                case TokenType.MINUS: checking = true; position++; break;
                case TokenType.QUOTE: 
                    returnType = TokenType.STRING;
                    toReturn = addString(false); break;
                case TokenType.EQUALS: checking = true; position++; break;
                default:
                    LaizE.Error("Unrecognized Variable Type " + type.TokenType, "Parser.returnHandler", type.line);
                    
                    
            }

        }

        TokenType validReturnType = returnTypes.pop();
        returnTokenType = returnType;
        
        //Convert "return Type" to a token containing return (ex. RETURN_INT)

        switch (returnType) {
            case TokenType.STRING: returnType = TokenType.RETURN_STR; break;
            case TokenType.INT: returnType = TokenType.RETURN_INT; break;
            case TokenType.DEC: returnType = TokenType.RETURN_DEC; break;
            case TokenType.BOOLEAN: returnType = TokenType.RETURN_BOOL; break;
            

            default: 
                LaizE.Error("Invalid return type " + returnType, "Parser.returnHandler", tokens.get(position).line);
        }

        returnedType = returnType;
        
        //Runs if the function has the correct returnType
        if (validReturnType == returnType) {
            return new Token(returnTokenType, toReturn, tokens.get(position).line);
        }


        LaizE.Error("Invalid return type " + returnType + "; expecting " + validReturnType, "Parser.returnHandler", tokens.get(position).line);
        return null;

    }



    //Handles while loops
    public void whileLoop() {
        int curlyBuffer = 0;

        //Skips to the condition
        while (tokens.get(position-1).TokenType != TokenType.OPEN_PAREN) {
            position++;
        }
        
        positionSave = position;

        //Runs if the condition is true
        if (conditional(true)) {

            //Runs until the condition isn't true
            while (true) {
                while (tokens.get(position-1).TokenType != TokenType.OPEN_CURLY) {
                    position++;
                }
                while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY) {
                    checkToken(tokens.get(position));
                }

                position = positionSave;
                if (!conditional(true)) {
                    break;
                }
            }

        //Skips the while loop if the conditino isnt true
        } else {
            while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || curlyBuffer > 0) {
                if (tokens.get(position).TokenType == TokenType.OPEN_CURLY) {
                    curlyBuffer++;
                }
                if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                    curlyBuffer--;
                }
                
                position++;
            }
        }

        //Skips over the while loop, as the prior while loop returns the position to the previous
        while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || curlyBuffer > 0) {
            if (tokens.get(position).TokenType == TokenType.OPEN_CURLY) {
                curlyBuffer++;
            }
            if (peek(1).TokenType == TokenType.CLOSED_CURLY) {
                curlyBuffer--;
            }
            
            position++;
        }
        
        
        
    }

    //Handles for loops
    public void forLoop() {
        Var loopCount = null;
        int comparisonValue = -1;
        boolean condition = false;
        boolean predeterminedVar = false;
        boolean varFound = false;
        Token opperation;
        Token changeValue;
        
        int curlyBuffer = 0;

        //Skips to the condition
        while (tokens.get(position-1).TokenType != TokenType.OPEN_PAREN) {
            position++;
        }

        if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
            for (int i = 0; i < varStorage.size(); i++) {
                if (varStorage.get(i).identifier.equals(tokens.get(position).data)) {
                    loopCount = varStorage.get(i);
                    predeterminedVar = true;
                    varFound = true;
                }
            }
            //Checks for function & whether or not the function is declared. if not a function, checks whether it's an undeclared function or variable
           if (!varFound && isFunc()) {
                if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                    LaizE.Error("Missing token '('", "Parser.opperationInt", tokens.get(position).line);
                }
                callFunction();
            } else if (!varFound && !isFunc()) {
                if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                    LaizE.Error("Undeclared function " + tokens.get(position).data, "Parser.opperationInt", tokens.get(position).line);
                }
            } 

            varFound = false;
 
            //If the variable isn't an already declared one, 
            if (!predeterminedVar) {
                varStorage.add(new Var(TokenType.INT, tokens.get(position).data, "0", tokens.get(position).line));
                loopCount = varStorage.get(varStorage.size()-1);
            }

        } else {
            LaizE.Error("Identifier not found", "Parser.forLoop", tokens.get(position).line);
        }

        //Checks what the loop condition is
        position++;
        if (peek(1).TokenType == TokenType.IDENTIFIER) {
            for (int i = 0; i < varStorage.size(); i++) {
                if (varStorage.get(i).identifier.equals(peek(1).data)) {
                    comparisonValue = Integer.parseInt(varStorage.get(i).data);
                    varFound = true;
                }
            }
            position++;
            if (!varFound && isFunc()) {
                if (peek(1).TokenType != TokenType.OPEN_PAREN) {
                    LaizE.Error("Missing token '('", "Parser.forLoop", tokens.get(position).line);
                }
                callFunction();
            } else if (!varFound && !isFunc()) {
                if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                    LaizE.Error("Function " + tokens.get(position).data + " undefined", "Parser.forLoop", tokens.get(position).line);
                } else {
                    LaizE.Error("Variable " + tokens.get(position).data + " undefined", "Parser.forLoop", tokens.get(position).line);
                }
            }
            position--;
            varFound = false;
        } else {
            comparisonValue = Integer.parseInt(peek(1).data);
        }
        
        

        //sets the condition
        
        TokenType comparator = tokens.get(position).TokenType;
        switch(comparator) {
            case TokenType.GREATER: condition = Integer.parseInt(loopCount.data) > comparisonValue; break;
            case TokenType.GREATER_EQUALS: condition = Integer.parseInt(loopCount.data) >= comparisonValue; break;
            case TokenType.LESS: condition = Integer.parseInt(loopCount.data) < comparisonValue; break;
            case TokenType.LESS_EQUALS: condition = Integer.parseInt(loopCount.data) <= comparisonValue; break;
            case TokenType.EQUALS_EQUALS: condition = Integer.parseInt(loopCount.data) == comparisonValue; break;
            case TokenType.BANG_EQUALS: condition = Integer.parseInt(loopCount.data) != comparisonValue; break;
            default:
                LaizE.Error("Invalid opperand " + tokens.get(position).TokenType, "Parser.forLoop", tokens.get(position).line);
        }

        //Skips to the next statement
        while (tokens.get(position-1).TokenType != TokenType.SEMICOLON) {
            position++;
        }

        //sets the opperation and value to change by
        opperation = tokens.get(position);
        position++;
        changeValue = tokens.get(position);

        if (changeValue.TokenType == TokenType.IDENTIFIER) {
            for (int i = 0; i < varStorage.size(); i++) {
                if (varStorage.get(i).identifier.equals(changeValue.data)) {
                    changeValue = new Token(varStorage.get(i).TokenType, varStorage.get(i).data, changeValue.line);
                    varFound = true;
                }
            }

            if (!varFound && isFunc()) {
                callFunction();
            } else if (!varFound && !isFunc()) {
                if (peek(1).TokenType == TokenType.OPEN_PAREN) {
                    LaizE.Error("Undeclared function " + changeValue.data, "Parser.forLoop", changeValue.line);
                } else {
                    LaizE.Error("Undeclared variable " + changeValue.data, "Parser.forLoop", changeValue.line);
                }
            
            }
        }

        //Skips to beginning of the loop
        while (tokens.get(position-1).TokenType != TokenType.OPEN_CURLY) {
            position++;
        }

        //Skips for loop if the condition isnt true
        positionSave = position;
        if (!condition) {
            while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY || curlyBuffer > 0) {
                if (tokens.get(position).TokenType == TokenType.OPEN_CURLY) {curlyBuffer++;}
                if (peek(1).TokenType == TokenType.CLOSED_CURLY) {curlyBuffer -= 1;}
                
                position++;
            }
        }

        //Runs while the condition is true
        while(condition) {
            position = positionSave;

            while (tokens.get(position).TokenType != TokenType.CLOSED_CURLY) {
                checkToken(tokens.get(position));
            }


            loopCount.data = String.valueOf(changeValue(loopCount, opperation, changeValue));
            switch(comparator) {
                case TokenType.GREATER: condition = Integer.parseInt(loopCount.data) > comparisonValue; break;
                case TokenType.GREATER_EQUALS: condition = Integer.parseInt(loopCount.data) >= comparisonValue; break;
                case TokenType.LESS: condition = Integer.parseInt(loopCount.data) < comparisonValue; break;
                case TokenType.LESS_EQUALS: condition = Integer.parseInt(loopCount.data) <= comparisonValue; break;
                case TokenType.EQUALS_EQUALS: condition = Integer.parseInt(loopCount.data) == comparisonValue; break;
                case TokenType.BANG_EQUALS: condition = Integer.parseInt(loopCount.data) != comparisonValue; break;
                default:
                    LaizE.Error("Invalid opperand " + tokens.get(position).TokenType, "Parser.forLoop", tokens.get(position).line);
            }
            
        }
    }

    //Changes the value of a variable based on the opperation
    public int changeValue(Var i, Token opperation, Token change) {
        int changeValue = Integer.parseInt(change.data);

        switch (opperation.TokenType) {
            case TokenType.PLUS: return Integer.parseInt(i.data) + changeValue; 
            case TokenType.MINUS: return Integer.parseInt(i.data) - changeValue; 
            case TokenType.STAR: return Integer.parseInt(i.data) * changeValue; 
            case TokenType.SLASH: return Integer.parseInt(i.data) / changeValue;

            default:
                LaizE.Error("Invalid opperand " + opperation.TokenType, "Parser.changeValue", opperation.line);
        }

        return Integer.parseInt(i.data);
    }

    //Checks if a set of tokens contains a decimal
    public boolean isInt(int start) {
        int i = start;

        while (tokens.get(i).TokenType != TokenType.SEMICOLON) {
            if (tokens.get(i).TokenType == TokenType.DEC) {
                return false;
            }
            i++;
        }

        return true;
    }
    

    //Processes a string
    //Finish commenting
    public String addString(boolean isPrintStatement) {
        String value = "";
        boolean inQuotes = false;

        //
        if (tokens.get(position).TokenType == TokenType.OPEN_PAREN || tokens.get(position).TokenType == TokenType.QUOTE && !isPrintStatement) {
            while(!endOfPrint(inQuotes) && isPrintStatement || !endOfString(inQuotes) && !isPrintStatement) {
                if (tokens.get(position).TokenType == TokenType.QUOTE && !inQuotes) {
                    inQuotes = true;
                    position++;
                    continue;
                } else if (tokens.get(position).TokenType == TokenType.QUOTE && inQuotes) {
                    inQuotes = false;
                    position++;
                    continue;
                }

                if (inQuotes) {
                    value += tokens.get(position).data;
                } else if (tokens.get(position).TokenType == TokenType.PLUS && peek(2).TokenType == TokenType.PLUS
                || tokens.get(position).TokenType == TokenType.PLUS && peek(2).TokenType == TokenType.SEMICOLON && !inQuotes
                || tokens.get(position).TokenType == TokenType.PLUS && peek(2).TokenType == TokenType.CLOSED_PAREN && !inQuotes){
                    position++;
                    if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                        for (int i = 0; i < varStorage.size(); i++) {
                            if (tokens.get(position).data.equals(varStorage.get(i).identifier)) {
                                value += varStorage.get(i).data;
                            }
                        }
                    } else if (tokens.get(position).data != null) {
                        value += tokens.get(position).data;
                    }
                } else if (tokens.get(position).TokenType == TokenType.IDENTIFIER){
                    for (int i = 0; i < varStorage.size(); i++) {
                        if (tokens.get(position).data.equals(varStorage.get(i).identifier)) {
                            value += varStorage.get(i).data;
                        }
                    }
                }
                
                position++;
            }
        }
        return value;
    }


    //Can't do conditions with compound strings
    //Loads a condition and runs it through the condition class
    public boolean conditional(boolean isIf) {
        ArrayList<Token> condition = new ArrayList<>();
        boolean result = false;
        if (isIf) {
            while (tokens.get(position).TokenType != TokenType.CLOSED_PAREN && peek(1).TokenType != TokenType.OPEN_CURLY) {
                    if (tokens.get(position).TokenType != TokenType.QUOTE) {
                        if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                            for (int i = 0; i < varStorage.size(); i++) {
                                if (varStorage.get(i).identifier.equals(tokens.get(position).data)) {
                                    condition.add(new Token(varStorage.get(i).TokenType, varStorage.get(i).data, tokens.get(position).line));
                                }
                            }
                        } else {
                            condition.add(tokens.get(position));
                        }
                    }
                    position++;

                if (EOF()) {
                    LaizE.Error("Missing Bracket", "Parser", tokens.get(position-1).line);
                }
            }
        } else {
            while (tokens.get(position).TokenType != TokenType.SEMICOLON) {
                if (tokens.get(position).TokenType != TokenType.QUOTE) {
                    if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
                        for (int i = 0; i < varStorage.size(); i++) {
                            if (varStorage.get(i).identifier.equals(tokens.get(position).data)) {
                                condition.add(new Token(varStorage.get(i).TokenType, varStorage.get(i).data, tokens.get(position).line));
                            }
                        }
                    } else {
                        condition.add(tokens.get(position));
                    }
                }
                position++;

                if (EOF()) {
                    LaizE.Error("Missing Bracket", "Parser", tokens.get(position-1).line);
                }
            }
        }

        result = Condition.conditional(condition);
        return result;
    }

    //Stores a variable
    public void storeVar() {
        position++;
        Token id = tokens.get(position);
        Var tempVarStorage = null;
        boolean checking = true;
        int tempPosition;
        TokenType[] symbols = {TokenType.EQUALS, TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS, TokenType.GREATER, TokenType.GREATER_EQUALS, TokenType.LESS, TokenType.LESS_EQUALS};


        if (id.TokenType == TokenType.IDENTIFIER
        && peek(1).TokenType == TokenType.EQUALS) {
            position += 2;
            tempPosition = position;
            while (tokens.get(tempPosition).TokenType != TokenType.SEMICOLON) {
                if (Arrays.asList(symbols).contains(tokens.get(tempPosition).TokenType)) {
                    addVar(TokenType.BOOLEAN, id.data, String.valueOf(conditional(false)), id.line);
                    return;
                }
                tempPosition++;
            }
            tempPosition = position;
            while (checking) {
                checking = false;
                Token type = tokens.get(tempPosition);
                for (int i = 0; i < varStorage.size(); i++) {
                    if (id.data.equals(varStorage.get(i).identifier)) {
                        varStorage.remove(i);
                    }
                }
                switch(type.TokenType) {
                    case TokenType.IDENTIFIER:
                        for (int i = 0; i < varStorage.size(); i++) {
                            if (type.data.equals(varStorage.get(i).identifier)) {
                                tempVarStorage = varStorage.get(i);
                            }
                        }
                        if (functions.get(type.data) != null) {
                            
                            
                            callFunction();
                            checking = true;

                            break;
                        }
                        if (tempVarStorage == null) {
                            LaizE.Error("Variable " + type.data + " not declared", "Parser.storeVar", type.line);
                        }

                        
                        switch (tempVarStorage.TokenType) {
                            case TokenType.STRING: addVar(TokenType.STRING, id.data, addString(false), type.line); break;
                            case TokenType.INT:
                                if (isInt(position)) {
                                    addVar(TokenType.INT, id.data, String.valueOf(opperationInt()), id.line);
                                    break;
                                }
                                addVar(TokenType.DEC, id.data, String.valueOf(opperationDec()), id.line);
                                break;
                            case TokenType.DEC:
                            if (isInt(position)) {
                                addVar(TokenType.INT, id.data, String.valueOf(opperationInt()), id.line);
                                break;
                            }
                            addVar(TokenType.DEC, id.data, String.valueOf(opperationDec()), id.line);
                            break;
                            default:
                                LaizE.Error("Unregistered token type for variable " + tempVarStorage.identifier, "Parser.storeVar", type.line);
                        }
                        break;
                    case TokenType.STRING:
                        addVar(TokenType.STRING, id.data, type.data, id.line);
                        break;
                    case TokenType.INT:
                        if (isInt(position)) {
                            addVar(TokenType.INT, id.data, String.valueOf(opperationInt()), id.line);
                            break;
                        }
                        addVar(TokenType.DEC, id.data, String.valueOf(opperationDec()), id.line);
                        break;
                    case TokenType.DEC:
                    if (isInt(position)) {
                        addVar(TokenType.INT, id.data, String.valueOf(opperationInt()), id.line);
                        break;
                    }
                    addVar(TokenType.DEC, id.data, String.valueOf(opperationDec()), id.line);
                    break;
                    case TokenType.BOOLEAN:
                        addVar(TokenType.BOOLEAN, id.data, type.data, id.line);
                        break;
                    case TokenType.INPUT: 
                        TokenType thisType;
                        if (peek(1).TokenType == TokenType.OPEN_PAREN && peek(2).TokenType == TokenType.CLOSED_PAREN) {
                            String input = takeInput();
                            if (input.equals("true") || input.equals("false")) {
                                thisType = TokenType.BOOLEAN;
                            } else if (isInt(input)){
                                thisType = TokenType.INT;
                            } else if (isDec(input)) {
                                thisType = TokenType.DEC;
                            } else {
                                thisType = TokenType.STRING;
                            }
                            addVar(thisType, id.data, input, id.line);
                            position++;
                            break;
                        }
                    case TokenType.OPEN_PAREN: checking = true; tempPosition++; break;
                    case TokenType.MINUS: checking = true; tempPosition++; break;
                    case TokenType.QUOTE: addVar(TokenType.STRING, id.data, addString(false), id.line); break;
                    default:
                        LaizE.Error("Unrecognized Variable Type " + type.TokenType, "Parser.storeVar", type.line);
                        
                        
                }
            }

            while (tokens.get(position).TokenType != TokenType.SEMICOLON) {
                position++;
            }
        }

    }
    
    //Updates an already declared
    public void updateVar() {
        Var toUpdate = null;
        Var tempVarStorage = null;
        int tempPosition = 0;
        String update = null;
        boolean checking = true;
        TokenType[] symbols = {TokenType.EQUALS, TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS, TokenType.GREATER, TokenType.GREATER_EQUALS, TokenType.LESS, TokenType.LESS_EQUALS};

    
        if (tokens.get(position).TokenType == TokenType.IDENTIFIER) {
            for (int i = 0; i < varStorage.size(); i++) {
                if (varStorage.get(i).identifier.equals(tokens.get(position).data)) {
                    toUpdate = varStorage.get(i);
                    break;
                }
            }

            if (toUpdate == null) {
                LaizE.Error("Undeclared variable " + tokens.get(position).data, "Parser.updateVar", tokens.get(position).line);
            }
        } else {
            LaizE.Error("Identifier Not Found", "Parser.updateVar", tokens.get(position).line);
        }

        while (tokens.get(position-1).TokenType != TokenType.EQUALS) {
            position++;
        }
        tempPosition = position;
        while (tokens.get(tempPosition).TokenType != TokenType.SEMICOLON) {
            if (Arrays.asList(symbols).contains(tokens.get(tempPosition).TokenType)) {
                if (!match(new Token(TokenType.BOOLEAN, "true", -1), toUpdate)) {
                    
                    LaizE.Error("Cannot convert variable " + toUpdate.TokenType + " to BOOLEAN", "Parser.updateVar", toUpdate.line);
                    
                }
                update = String.valueOf(conditional(false));
                return;
            }
            tempPosition++;
        }

        while (checking) {
            checking = false;
            Token type = tokens.get(position);

            switch(type.TokenType) {
                case TokenType.IDENTIFIER:
                    for (int i = 0; i < varStorage.size(); i++) {
                        if (type.data.equals(varStorage.get(i).identifier)) {
                            tempVarStorage = varStorage.get(i);
                        }
                    }
                    if (functions.get(type.data) != null) {
                        callFunction();
                        checking = true;
                        
                        break;
                    }
                    if (tempVarStorage == null) {
                        LaizE.Error("Variable " + type.data + " not declared", "Parser.updateVar", type.line);
                    }
                    

                    
                    switch (tempVarStorage.TokenType) {
                        case TokenType.STRING: update = addString(false); break;
                        case TokenType.INT:
                            if (isInt(position)) {
                                update = String.valueOf(opperationInt());
                                break;
                            }
                            update = String.valueOf(opperationDec());
                            break;
                        case TokenType.DEC:
                            if (isInt(position)) {
                                update = String.valueOf(opperationInt());
                                break;
                            }
                            update = String.valueOf(opperationDec());
                            break;
                        default:
                            LaizE.Error("Unregistered token type for variable " + tempVarStorage.identifier, "Parser.updateVar", type.line);
                    }
                    break;
                case TokenType.STRING:
                    if (!match(type, toUpdate)) {
                        LaizE.Error("Cannot convert variable type " + type.TokenType + " to " + toUpdate.TokenType, "Parser.updateVar", type.line);
                    }
                    update = addString(false);
                    break;
                case TokenType.INT:
                    if (!match(type, toUpdate)) {
                        LaizE.Error("Cannot convert variable type " + type.TokenType + " to " + toUpdate.TokenType, "Parser.updateVar", type.line);
                    }
                    if (isInt(position)) {
                        update = String.valueOf(opperationInt());
                        break;
                    }
                    update = String.valueOf(opperationDec());
                    break;
                case TokenType.DEC:
                    if (!match(type, toUpdate)) {
                        LaizE.Error("Cannot convert variable type " + type.TokenType + " to " + toUpdate.TokenType, "Parser.updateVar", type.line);
                    }
                    if (isInt(position)) {
                        update = String.valueOf(opperationInt());
                        break;
                    }
                    update = String.valueOf(opperationDec());
                    break;
                case TokenType.BOOLEAN:
                    if (!match(type, toUpdate)) {
                        LaizE.Error("Cannot convert variable type " + type.TokenType + " to " + toUpdate.TokenType, "Parser.updateVar", type.line);
                    }
                    update = type.data;
                    break;
                case TokenType.OPEN_PAREN: checking = true; position++; break;
                case TokenType.MINUS: checking = true; position++; break;
                case TokenType.QUOTE: 
                    if (!match(new Token(TokenType.STRING, null, -1), toUpdate)) {
                        LaizE.Error("Cannot convert variable type STRING to " + toUpdate.TokenType, "Parser.updateVar", type.line);
                    }
                    update = addString(false); break;
                case TokenType.EQUALS: checking = true; position++; break;
                default:
                    LaizE.Error("Unrecognized Variable Type " + type.TokenType, "Parser.updateVar", type.line);
                    
                    
            }

            toUpdate.data = update;
        }


    }

    //Checks if an string is an integer
    public boolean isInt(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    //Checks if a string is a decimal
    public boolean isDec(String str) {
        for (int i = 0; i < str.length(); i++) {
            if ((str.charAt(i) < '0' || str.charAt(i) > '9') && str.charAt(i) != '.') {
                return false;
            }
        }
        return true;
    }

    //Takes a user input
    public String takeInput() {
        Scanner scanner = new Scanner(System.in);

        String input = String.valueOf(scanner.nextLine());
        scanner.close();
        return input;
    }

    //Adds a variable to varStorage
    public void addVar(TokenType type, String id, String text, int line) {
        varStorage.add(new Var(type, id, text, line));
    }

    //Checks if two token types are the same
    public boolean match(Token a, Var b) {
        return a.TokenType == b.TokenType;
    }

    
    //Constructor
    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        Parser.varStorage = new ArrayList<Var>();
        Parser.functions = new HashMap<>();
    }

}
