import java.io.*;
import java.io.IOException;
import java.util.ArrayList;

public class CompileEngine {

    private static String token;
    private static String currentLine;
    private static String stringName;

    private final static String[] op = {"+", "-", "*", "/", "&", "|", "<", ">", "="};
    private final static String[] unaryOp = {"-", "~"};
    private final static String[] keywordConstant = {"true", "false", "null", "this"};
    private static String[] type = {"int", "boolean", "char", "Array"};
    private static String[] methodType = {"void", "int", "boolean", "char"};

    private static boolean isKeyword(String s) {
        for (String str : keywordConstant) {
            if (str.equals(s)) return true;
        }

        return false;
    }

    private static boolean isOp(String s) {
        for (String str : op) {
            if (str.equals(s)) return true;
        }

        return false;
    }

    private static boolean isUnaryOp(String s) {
        for (String str : unaryOp) {
            if (str.equals(s)) return true;
        }

        return false;
    }

    private static String indentationCreater(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(" ");
        return sb.toString();
    }

    private static ArrayList<String> compileClass(int indentationNum) {
        ArrayList<String> class_ = new ArrayList<>();

        //first is class declaration
        class_.add("<class>");
        class_.add(indentationCreater(indentationNum) + currentLine);
        //move forward
        token = Tokenizer.tokenAdvance();
        currentLine = Tokenizer.tokenLineAdvance();
        stringName = Tokenizer.stringAdvance();
        //the className become a new type
        //we can declare a className object
        type = Operation.addToArray(type, stringName);

        while (Tokenizer.tokenHasNext() && token != null) {
            //className
            //if it's a className, it's an identifier
            //we keep its primitive input
            try {
                String line = currentLine;
                eat(token, new String[]{"identifier"});
                class_.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileClass fail!");
            }

            //after the className comes '{'
            try {
                String line = currentLine;
                eat(stringName, new String[]{"{"});
                class_.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileClass fail!");
            }

            //if there are any classVars, deal with them
            if (stringName.equals("static") || stringName.equals("field")) {
                while (stringName.equals("static") || stringName.equals("field")) {
                    ArrayList<String> classVar = compileClassVarDec(indentationNum + 2);
                    for (String s : classVar) class_.add(s);
                }
            }

            //if there are any subroutineDec, deal with it
            if (stringName.equals("constructor") || stringName.equals("function") || stringName.equals("method")) {
                while (stringName.equals("constructor") || stringName.equals("function") || stringName.equals("method")) {
                    ArrayList<String> subroutineVar = compileSubroutineDec(indentationNum + 2);
                    for (String s : subroutineVar) class_.add(s);
                }
            }

            //at last, we deal with '}'
            try {
                String line = currentLine;
                eat(stringName, new String[]{"}"});
                class_.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileClass fail!");
            }

            break;
        }

        class_.add("</class>");
        return class_;
    }

    private static ArrayList<String> compileClassVarDec(int indentationNum) {
        ArrayList<String> varDec = new ArrayList<>();
        varDec.add(indentationCreater(indentationNum - 2) + "<classVarDec>");

        //first is field and static declaration
        varDec.add(indentationCreater(indentationNum) + currentLine);
        token = Tokenizer.tokenAdvance();
        currentLine = Tokenizer.tokenLineAdvance();
        stringName = Tokenizer.stringAdvance();

        while (Tokenizer.tokenHasNext() && token != null) {
            //type
            try {
                String line = currentLine;
                eat(stringName, type);
                varDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileClassVarDec fails!");
            }

            //then comes the varName
            try {
                String line = currentLine;
                eat(token, new String[]{"identifier"});
                varDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileClassVarDec fails!");
            }

            //if there's a ',', it should be ", varName"
            while (stringName.equals(",")) {
                varDec.add(currentLine);
                token = Tokenizer.tokenAdvance();
                currentLine = Tokenizer.tokenLineAdvance();
                stringName = Tokenizer.stringAdvance();

                try {
                    String line = currentLine;
                    eat(token, new String[]{"identifier"});
                    varDec.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileClassVarDec fails!");
                }
            }

            //after the ',' comes the ';'
            try {
                String line = currentLine;
                eat(stringName, new String[]{";"});
                varDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileClassVarDec fails!");
            }
            break;
        }

        varDec.add(indentationCreater(indentationNum - 2) + "</classVarDec>");
        return varDec;
    }

    private static ArrayList<String> compileSubroutineDec(int indentationNum) {
        ArrayList<String> subroutineDec = new ArrayList<>();
        subroutineDec.add(indentationCreater(indentationNum - 2) + "<subroutineDec>");

        //deal with the declaration first
        try {
            String line = currentLine;
            eat(stringName, new String[]{"constructor", "function", "method"});
            subroutineDec.add(indentationCreater(indentationNum) + line);
        } catch (IllegalArgumentException e) {
            System.out.println(currentLine);
            System.out.println("The grammar is illegal!");
            System.out.println("compileSubroutineDec fails!");
        }

        while (Tokenizer.tokenHasNext() && token != null) {
            // void/ int/ char/ boolean
            try {
                String line = currentLine;
                eat(stringName, methodType);
                subroutineDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineDec fails!");
            }

            //the subroutineName, it should be an identifier
            try {
                String line = currentLine;
                eat(token, new String[]{"identifier"});
                subroutineDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineDec fails!");
            }

            //then comes the '('
            try {
                String line = currentLine;
                eat(stringName, new String[]{"("});
                subroutineDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineDec fails!");
            }

            //judge if there are any parameter
            subroutineDec.add(indentationCreater(indentationNum) + "<parameterList>");
            while (token.equals("identifier")) {
                //the first should be a type
                try {
                    String line = currentLine;
                    eat(stringName, type);
                    subroutineDec.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineDec fails!");
                }

                //the second is the name of the parameter
                try {
                    String line = currentLine;
                    eat(token, new String[]{"identifier"});
                    subroutineDec.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineDec fails!");
                }

                //the third is a ','
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{","});
                    subroutineDec.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineDec fails!");
                }
            }
            subroutineDec.add(indentationCreater(indentationNum) + "</parameterList>");

            //then comes ')'
            try {
                String line = currentLine;
                eat(stringName, new String[]{")"});
                subroutineDec.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineDec fails!");
            }

            //finally we deal with subroutine body
            subroutineDec.add(indentationCreater(indentationNum) + "<subroutineBody>");
            ArrayList<String> subroutineBody = compileSubroutineBody(indentationNum + 2);
            for (String s : subroutineBody) subroutineDec.add(s);
            subroutineDec.add(indentationCreater(indentationNum) + "</subroutineBody>");

            break;
        }

        subroutineDec.add(indentationCreater(indentationNum - 2) + "</subroutineDec>");
        return subroutineDec;
    }

    private static ArrayList<String> compileSubroutineBody(int indentationNum) {
        ArrayList<String> subroutineBody = new ArrayList<>();

        while (Tokenizer.tokenHasNext() && token != null) {
            //first we deal with '{'
            try {
                String line = currentLine;
                eat(stringName, new String[]{"{"});
                subroutineBody.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineBody fails!");
            }

            //then are the varDec
            while (stringName.equals("var")) {
                subroutineBody.add(indentationCreater(indentationNum) + "<varDec>");

                //the first is var
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"var"});
                    subroutineBody.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineBody fails!");
                }

                //the second is the type
                try {
                    String line = currentLine;
                    eat(stringName, type);
                    subroutineBody.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineBody fails!");
                }

                //then comes the varName, which is an identifier
                //attention! There may consist ",", like var int a, b, c, d
                //there must be at least one varName, so we deal with it first
                try {
                    String line = currentLine;
                    eat(token, new String[]{"identifier"});
                    subroutineBody.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineBody fails!");
                }

                //then we deal with ","
                while (stringName.equals(",")) {
                    try {
                        String line = currentLine;
                        eat(stringName, new String[]{","});
                        subroutineBody.add(indentationCreater(indentationNum + 2) + line);
                    } catch (IllegalArgumentException e) {
                        System.out.println(currentLine);
                        System.out.println("The grammar is illegal!");
                        System.out.println("compileSubroutineBody fails!");
                    }

                    try {
                        String line = currentLine;
                        eat(token, new String[]{"identifier"});
                        subroutineBody.add(indentationCreater(indentationNum + 2) + line);
                    } catch (IllegalArgumentException e) {
                        System.out.println(currentLine);
                        System.out.println("The grammar is illegal!");
                        System.out.println("compileSubroutineBody fails!");
                    }
                }

                //at last is the ';'
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{";"});
                    subroutineBody.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineBody fails!");
                }

                subroutineBody.add(indentationCreater(indentationNum) + "</varDec>");
            }

            //until we meet '}', the rest are all statements
            while (!stringName.equals("}")) {
                ArrayList<String> statementsName = compileStatements(indentationNum + 2);
                for (String s : statementsName) subroutineBody.add(s);
            }

            //finally we meet '}'
            try {
                String line = currentLine;
                eat(stringName, new String[]{"}"});
                subroutineBody.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineBody fails!");
            }

            break;
        }

        return subroutineBody;
    }

    private static ArrayList<String> compileStatements(int indentationNum) {
        ArrayList<String> statements = new ArrayList<>();
        statements.add(indentationCreater(indentationNum - 2) + "<statements>");

        while (Tokenizer.tokenHasNext() && token != null) {
            while (stringName.equals("let") || stringName.equals("do")
                    || stringName.equals("while") || stringName.equals("return")
                    || stringName.equals("if")) {
                if (stringName.equals("let")) {
                    ArrayList<String> letStatement = compileLetStatement(indentationNum);
                    for (String s : letStatement) statements.add(s);
                } else if (stringName.equals("do")) {
                    ArrayList<String> doStatement = compileDoStatement(indentationNum);
                    for (String s : doStatement) statements.add(s);
                } else if (stringName.equals("while")) {
                    ArrayList<String> whileStatement = compileWhileStatement(indentationNum);
                    for (String s : whileStatement) statements.add(s);
                } else if (stringName.equals("return")) {
                    ArrayList<String> returnStatement = compileReturnStatement(indentationNum);
                    for (String s : returnStatement) statements.add(s);
                } else if (stringName.equals("if")) {
                    ArrayList<String> ifStatement = compileIfStatement(indentationNum);
                    for (String s : ifStatement) statements.add(s);
                }
            }

            break;
        }

        statements.add(indentationCreater(indentationNum - 2) + "</statements>");
        return statements;
    }

    private static ArrayList<String> compileLetStatement(int indentationNum) {
        ArrayList<String> letStatement = new ArrayList<>();
        letStatement.add(indentationCreater(indentationNum) + "<letStatement>");
        while (Tokenizer.tokenHasNext() && token != null) {
            //the first is let
            try {
                String line = currentLine;
                eat(stringName, new String[]{"let"});
                letStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileLetStatement fails!");
            }

            //the second is var
            try {
                String line = currentLine;
                eat(token, new String[]{"identifier"});
                letStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileLetStatement fails!");
            }

            //if there's a '[', we need an expression behind it
            if (stringName.equals("[")) {
                //we deal with "["
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"["});
                    letStatement.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileLetStatement fails!");
                }

                //then we deal with expression
                ArrayList<String> expression = compileExpression(indentationNum + 2);
                for (String s : expression) letStatement.add(s);

                //after then, we deal with ']'
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"]"});
                    letStatement.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileLetStatement fails!");
                }
            }

            //then we deal with "="
            try {
                String line = currentLine;
                eat(stringName, new String[]{"="});
                letStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileLetStatement fails!");
            }

            //then we deal with another expression
            ArrayList<String> expression = compileExpression(indentationNum + 2);
            for (String s : expression) letStatement.add(s);

            //finally we have a ";"
            try {
                String line = currentLine;
                eat(stringName, new String[]{";"});
                letStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileLetStatement fails!");
            }

            break;
        }

        letStatement.add(indentationCreater(indentationNum) + "</letStatement>");
        return letStatement;
    }

    private static ArrayList<String> compileDoStatement(int indentationNum) {
        ArrayList<String> doStatement = new ArrayList<>();
        doStatement.add(indentationCreater(indentationNum) + "<doStatement>");

        while (Tokenizer.tokenHasNext() && token != null) {
            //the first is do
            try {
                String line = currentLine;
                eat(stringName, new String[]{"do"});
                doStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileDoStatement fails!");
            }

            //then we deal with subroutineCall
            ArrayList<String> subroutineCall = compileSubroutineCall(indentationNum + 2);
            for (String s : subroutineCall) doStatement.add(s);

            //finally we have a ";"
            try {
                String line = currentLine;
                eat(stringName, new String[]{";"});
                doStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileLetStatement fails!");
            }

            break;
        }

        doStatement.add(indentationCreater(indentationNum) + "</doStatement>");
        return doStatement;
    }

    private static ArrayList<String> compileWhileStatement(int indentationNum) {
        ArrayList<String> whileStatement = new ArrayList<>();
        whileStatement.add(indentationCreater(indentationNum) + "<whileStatement>");

        while(Tokenizer.tokenHasNext() && token != null) {
            //the first is while
            try {
                String line = currentLine;
                eat(stringName, new String[]{"while"});
                whileStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileWhileStatement fails!");
            }

            //then comes "("
            try {
                String line = currentLine;
                eat(stringName, new String[]{"("});
                whileStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileWhileStatement fails!");
            }

            //then is the expression
            ArrayList<String> expression = compileExpression(indentationNum + 2);
            for (String s : expression) whileStatement.add(s);

            //then is the ")"
            try {
                String line = currentLine;
                eat(stringName, new String[]{")"});
                whileStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileWhileStatement fails!");
            }

            //then we have "{"
            try {
                String line = currentLine;
                eat(stringName, new String[]{"{"});
                whileStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileWhileStatement fails!");
            }

            //then is the statements
            ArrayList<String> statements = compileStatements(indentationNum + 4);
            for (String s : statements) whileStatement.add(s);

            //then comes "}"
            try {
                String line = currentLine;
                eat(stringName, new String[]{"}"});
                whileStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileWhileStatement fails!");
            }

            break;
        }

        whileStatement.add(indentationCreater(indentationNum) + "</whileStatement>");
        return whileStatement;
    }

    private static ArrayList<String> compileReturnStatement(int indentationNum) {
        ArrayList<String> returnStatement = new ArrayList<>();
        returnStatement.add(indentationCreater(indentationNum) + "<returnStatement>");

        while (Tokenizer.tokenHasNext() && token != null) {
            //first we deal with return
            try {
                String line = currentLine;
                eat(stringName, new String[]{"return"});
                returnStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileReturnStatement fails!");
            }

            //then is the expression
            ArrayList<String> expression = compileExpression(indentationNum + 2);
            for (String s : expression) returnStatement.add(s);

            //finally we have ";"
            try {
                String line = currentLine;
                eat(stringName, new String[]{";"});
                returnStatement.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
            }

            break;
        }

        returnStatement.add(indentationCreater(indentationNum) + "</returnStatement>");
        return returnStatement;
    }

    private static ArrayList<String> compileIfStatement(int indentationNum) {
        ArrayList<String> ifStatement = new ArrayList<>();
        ifStatement.add(indentationCreater(indentationNum) + "<ifStatement>");

        while (Tokenizer.tokenHasNext() && token != null) {

            //the first is if keyword
            try {
                String line = currentLine;
                eat(stringName, new String[]{"if"});
                ifStatement.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileIfStatement fails!");
            }

            //the second is "("
            try {
                String line = currentLine;
                eat(stringName, new String[]{"("});
                ifStatement.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileIfStatement fails!");
            }

            //then comes expression
            ArrayList<String> expression = compileExpression(indentationNum);
            for (String s : expression) ifStatement.add(s);

            //then is ")"
            try {
                String line = currentLine;
                eat(stringName, new String[]{")"});
                ifStatement.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileIfStatement fails!");
            }

            try {
                String line = currentLine;
                eat(stringName, new String[]{"{"});
                ifStatement.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileIfStatement fails!");
            }

            ArrayList<String> statements = compileStatements(indentationNum + 2);
            for (String s : statements) ifStatement.add(s);

            try {
                String line = currentLine;
                eat(stringName, new String[]{"}"});
                ifStatement.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileIfStatement fails!");
            }

            if (stringName.equals("else")) {
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"else"});
                    ifStatement.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileIfStatement fails!");
                }

                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"{"});
                    ifStatement.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileIfStatement fails!");
                }

                statements = compileStatements(indentationNum + 2);
                for (String s : statements) ifStatement.add(s);

                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"}"});
                    ifStatement.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileIfStatement fails!");
                }
            }

            break;
        }

        ifStatement.add(indentationCreater(indentationNum) + "</ifStatement>");
        return ifStatement;
    }

    private static ArrayList<String> compileSubroutineCall(int indentationNum) {
        ArrayList<String> subroutineCall = new ArrayList<>();

        while (Tokenizer.tokenHasNext() && token != null) {
            //first, we should judge if the current case is varName + '.'
            //we store the first line and second line of the program
            //if the second stringName equals ".", then we have a varName
            //else, do as usual

            //notice that our static variables store exactly the current state
            String firstToken = token;
            String firstLine = stringName;
            String secondLine = currentLine;
            String secondStringName = Tokenizer.getNextString();

            //we first deal with varName/className
            try {
                String line = currentLine;
                eat(token, new String[]{"identifier"});
                subroutineCall.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineCall fails!");
            }

            //if there's a ".", we deal with it and the name behind it
            if (secondStringName.equals(".") && firstToken.equals("identifier")) {
                //we deal with the subroutineName
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"."});
                    subroutineCall.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineCall fails!");
                }

                try {
                    String line = currentLine;
                    eat(token, new String[]{"identifier"});
                    subroutineCall.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileSubroutineCall fails!");
                }

            }

            //we deal with the '('
            try {
                String line = currentLine;
                eat(stringName, new String[]{"("});
                subroutineCall.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineCall fails!");
            }

            //then we deal with expressionList
            ArrayList<String> expressionList = compileExpressionList(indentationNum + 2);
            for (String s : expressionList) subroutineCall.add(s);

            //finally we deal with ")"
            try {
                String line = currentLine;
                eat(stringName, new String[]{")"});
                subroutineCall.add(indentationCreater(indentationNum) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileSubroutineCall fails!");
            }

            break;
        }
        return subroutineCall;
    }

    private static ArrayList<String> compileExpressionList(int indentationNum) {
        ArrayList<String> expressionList = new ArrayList<>();
        expressionList.add(indentationCreater(indentationNum - 2) + "<expressionList>");

        while (Tokenizer.tokenHasNext() && token != null) {
            ArrayList<String> expression = compileExpression(indentationNum);
            for (String s : expression) expressionList.add(s);

            while (stringName.equals(",")) {
                try {
                    String line = currentLine;
                    eat(stringName, new String[]{","});
                    expressionList.add(indentationCreater(indentationNum) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileExpressionList fails!");
                }

                expression = compileExpression(indentationNum + 2);
                for (String s : expression) expressionList.add(s);
            }
            break;
        }

        expressionList.add(indentationCreater(indentationNum - 2) + "</expressionList>");
        return expressionList;
    }

    private static ArrayList<String> compileExpression(int indentationNum) {
        ArrayList<String> expression = new ArrayList<>();

        while (Tokenizer.tokenHasNext() && token != null) {
            ArrayList<String> term = compileTerm(indentationNum + 2);

            if (!term.isEmpty()) {
                expression.add(indentationCreater(indentationNum) + "<expression>");
                for (String s : term) expression.add(s);

                //if there's an op, we continue the loop
                while (isOp(stringName)) {
                    try {
                        String line = currentLine;
                        eat(stringName, op);
                        expression.add(indentationCreater(indentationNum + 2) + line);
                    } catch (IllegalArgumentException e) {
                        System.out.println(currentLine);
                        System.out.println("The grammar is illegal!");
                        System.out.println("compileExpression fails!");
                    }

                    term = compileTerm(indentationNum + 2);
                    for (String s : term) expression.add(s);
                }

                expression.add(indentationCreater(indentationNum) + "</expression>");
            }

            break;
        }
        return expression;
    }

    private static ArrayList<String> compileTerm(int indentationNum) {
        ArrayList<String> term = new ArrayList<>();

        while (Tokenizer.tokenHasNext() && token != null) {
            //like what we have done in subroutineCall
            //we read the first and second line in advance
            String firstToken = token;
            String firstLine = currentLine;
            String firstStringName = stringName;
            String secondStringName = Tokenizer.getNextString();

            //case 1: integer/string/keyword
            //integer and string is implied by the token
            //keyword can be judged by isKeyword method
            if (firstToken.equals("integerConstant") || firstToken.equals("stringConstant")
                    || isKeyword(firstStringName)) {
                term.add(indentationCreater(indentationNum + 2) + firstLine);

                //we advance it annually
                //because we have no idea what the expected string array is
                token = Tokenizer.tokenAdvance();
                currentLine = Tokenizer.tokenLineAdvance();
                stringName = Tokenizer.stringAdvance();

                break;
            }

            //case 2: unaryOp term
            //we simply use isUnaryOp method
            if (isUnaryOp(firstStringName)) {
                term.add(indentationCreater(indentationNum + 2) + firstLine);

                //the same as above
                token = Tokenizer.tokenAdvance();
                currentLine = Tokenizer.tokenLineAdvance();
                stringName = Tokenizer.stringAdvance();

                ArrayList<String> termOp = compileTerm(indentationNum + 2);
                for (String s : termOp) term.add(s);

                break;
            }

            //case 3: parenthesis plus expression
            if (firstStringName.equals("(")) {
                try {
                    eat(firstStringName, new String[]{"("});
                    term.add(indentationCreater(indentationNum + 2) + firstLine);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileTerm fails!");
                }

                ArrayList<String> expression = compileExpression(indentationNum + 2);
                for (String s : expression) term.add(s);

                try {
                    String line = currentLine;
                    eat(stringName, new String[]{")"});
                    term.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileTerm fails!");
                }

                break;
            }

            //case 4: subroutineCall
            //both subroutineCall's first element and varName are identifier
            //so we should differ them by the second element

            //subroutine case 1: Name + ()
            if (firstToken.equals("identifier") && secondStringName.equals("(")) {
                ArrayList<String> subroutineCall = compileSubroutineCall(indentationNum + 2);
                for (String s : subroutineCall) term.add(s);
                break;
            }

            //subroutine case 2: className + "." + subroutineName
            if (firstToken.equals("identifier") && secondStringName.equals(".")) {
                ArrayList<String> subroutineCall = compileSubroutineCall(indentationNum + 2);
                for (String s : subroutineCall) term.add(s);
                break;
            }

            //case 5: varName
            //we should pay attention to the '['
            if (firstToken.equals("identifier") &&secondStringName.equals("[")) {
                //deal with varName first
                try {
                    String line = currentLine;
                    eat(token, new String[]{"identifier"});
                    term.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileTerm fails!");
                }

                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"["});
                    term.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileTerm fails!");
                }

                ArrayList<String> expression = compileExpression(indentationNum + 2);
                for (String s : expression) term.add(s);

                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"]"});
                    term.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileTerm fails!");
                }

                try {
                    String line = currentLine;
                    eat(stringName, new String[]{"}"});
                    term.add(indentationCreater(indentationNum + 2) + line);
                } catch (IllegalArgumentException e) {
                    System.out.println(currentLine);
                    System.out.println("The grammar is illegal!");
                    System.out.println("compileTerm fails!");
                }

                break;
            }

            try {
                String line = currentLine;
                eat(token, new String[]{"identifier"});
                term.add(indentationCreater(indentationNum + 2) + line);
            } catch (IllegalArgumentException e) {
                System.out.println(currentLine);
                System.out.println("The grammar is illegal!");
                System.out.println("compileTerm fails!");
            }

            break;
        }

        if (!term.isEmpty()) {
            term.add(0, indentationCreater(indentationNum) + "<term>");
            term.add(indentationCreater(indentationNum) + "</term>");
        }
        return term;
    }

    private static void eat(String input, String[] expect) {
        for (String s : expect) {
            if (input.equals(s)) {
                if (Tokenizer.tokenHasNext()) token = Tokenizer.tokenAdvance();
                if (Tokenizer.tokenLineHasNext()) currentLine = Tokenizer.tokenLineAdvance();
                if (Tokenizer.stringHasNext()) stringName = Tokenizer.stringAdvance();
                return;
            }
        }
        throw new IllegalArgumentException("Strings are not equal");
    }

    public static void constructor(String[] filenames) {
        //first, we put the className into type
        //so that we can
        for (String filename : filenames) {
            String name = filename.substring(0, filename.indexOf(".jack"));
            Tokenizer.className = Operation.addToArray(Tokenizer.className, name);
            Operation.addToArray(type, name);
            Operation.addToArray(methodType, name);
        }

        for (String filename : filenames) {
            Tokenizer.constructor(filename);

            token = Tokenizer.tokenAdvance();
            currentLine = Tokenizer.tokenLineAdvance();
            stringName = Tokenizer.stringAdvance();

            try {
                String outputFile = filename.substring(0, filename.indexOf(".jack")) + ".xml";
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                ArrayList<String> classOutput = compileClass(2);
                for (String s : classOutput) {
                    if (s == null) break;
                    writer.write(s);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}